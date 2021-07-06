/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.language.editor.worker

import net.akehurst.language.api.syntaxAnalyser.AsmElementSimple
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.sppt.SPPTBranch
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.editor.common.*
import net.akehurst.language.agl.processor.Agl
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.MessagePort
import org.w3c.dom.SharedWorkerGlobalScope

external val self: DedicatedWorkerGlobalScope

class AglWorker {

    private var processor: LanguageProcessor? = null
    private var styleHandler: AglStyleHandler? = null

    init {
        start()
    }

    fun start() {
        self.onmessage = {
            val msg: dynamic = it.data
            when (msg.action) {
                "MessageProcessorCreate" -> this.createProcessor(self, msg.languageId, msg.editorId, msg.grammarStr)
                "MessageParserInterruptRequest" -> this.interrupt(self, msg.languageId, msg.editorId, msg.reason)
                "MessageParseRequest" -> this.parse(self, msg.languageId, msg.editorId, msg.text)
                "MessageSetStyle" -> this.setStyle(self, msg.languageId, msg.editorId, msg.css)
            }
        }
    }
/*
    fun startShared() {
        (self as SharedWorkerGlobalScope).onconnect = { e ->
            val port = e.asDynamic().ports[0] as MessagePort
            port.onmessage = {
                val msg: dynamic = it.data
                when (msg.action) {
                    "MessageProcessorCreate" -> this.createProcessor(port, msg.languageId, msg.editorId, msg.grammarStr)
                    "MessageParserInterruptRequest" -> this.interrupt(port, msg.languageId, msg.editorId, msg.reason)
                    "MessageParseRequest" -> this.parse(port, msg.languageId, msg.editorId, msg.text)
                    "MessageSetStyle" -> this.setStyle(port, msg.languageId, msg.editorId, msg.css)
                }
            }
            true //onconnect insists on having a return value!
        }
    }
*/
    private fun sendMessage(port: dynamic, msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        port.postMessage(msg.toObjectJS(), transferables)
    }

    private fun createProcessor(port: dynamic, languageId: String, editorId: String, grammarStr: String?) {
        if (null == grammarStr) {
            this.processor = null
            sendMessage(port, MessageProcessorCreateSuccess(languageId, editorId, "reset"))
        } else {
            try {
                //cheet because I don't want to serialise grammars
                when (grammarStr) {
                    "@Agl.grammarProcessor@" -> createAgl(languageId, Agl.grammarProcessor)
                    "@Agl.styleProcessor@" -> createAgl(languageId, Agl.styleProcessor)
                    "@Agl.formatProcessor@" -> createAgl(languageId, Agl.formatProcessor)
                    else -> createAgl(languageId, Agl.processorFromString(grammarStr))
                }
                sendMessage(port, MessageProcessorCreateSuccess(languageId, editorId, "OK"))
            } catch (t: Throwable) {
                sendMessage(port, MessageProcessorCreateFailure(languageId, editorId, t.message!!))
            }
        }
    }

    private fun createAgl(langId: String, proc: LanguageProcessor) {
        this.processor = proc
    }

    private fun interrupt(port: dynamic, languageId: String, editorId: String, reason: String) {
        val proc = this.processor
        if (proc != null) {
            proc.interrupt(reason)
        }
    }

    private fun setStyle(port: dynamic, languageId: String, editorId: String, css: String) {
        try {
            val style = AglStyleHandler(languageId)
            this.styleHandler = style
            val rules: List<AglStyleRule> = Agl.styleProcessor.process(List::class,css)
            rules.forEach { rule ->
                style.mapClass(rule.selector)
            }
            sendMessage(port,MessageSetStyleResult(languageId, editorId, true, "OK"))
        } catch (t: Throwable) {
            sendMessage(port,MessageSetStyleResult(languageId, editorId, false, t.message!!))
        }
    }

    private fun parse(port: dynamic, languageId: String, editorId: String, sentence: String) {
        try {
            sendMessage(port,MessageParseStart(languageId, editorId))
            val proc = this.processor ?: throw RuntimeException("Processor for $languageId not found")
            val sppt = proc.parse(sentence)
            val tree = createParseTree(sppt.root)
            sendMessage(port,MessageParseSuccess(languageId, editorId, tree))
            this.sendParseLineTokens(port, languageId, editorId, sppt)
            this.process(port, languageId, editorId, sppt)
        } catch (e: ParseFailedException) {
            val sppt = e.longestMatch
            val tree = createParseTree(sppt!!.root)
            sendMessage(port,MessageParseFailure(languageId, editorId, e.message!!, e.location, e.expected.toTypedArray(), tree))
        } catch (t: Throwable) {
            sendMessage(port,MessageParseFailure(languageId, editorId, t.message!!, null, emptyArray(), null))
        }
    }

    private fun process(port: dynamic, languageId: String, editorId: String, sppt: SharedPackedParseTree) {
        try {
            sendMessage(port,MessageProcessStart(languageId, editorId))
            val proc = this.processor ?: throw RuntimeException("Processor for $languageId not found")
            val asm = proc.processFromSPPT<Any>(Any::class,sppt)
            val asmTree = createAsmTree(asm) ?: "No Asm"
            sendMessage(port,MessageProcessSuccess(languageId, editorId, asmTree))
        } catch (t: Throwable) {
            sendMessage(port,MessageProcessFailure(languageId, editorId, t.message!!))
        }
    }

    private fun sendParseLineTokens(port: dynamic, languageId: String, editorId: String, sppt: SharedPackedParseTree) {
        if (null == sppt) {
            //nothing
        } else {
            val style = this.styleHandler ?: throw RuntimeException("StyleHandler for $languageId not found")
            val lineTokens = sppt.tokensByLineAll().mapIndexed { lineNum, leaves ->
                style.transformToTokens(leaves)
            }
            val lt = lineTokens.map {
                it.toTypedArray()
            }.toTypedArray()
            sendMessage(port,MessageLineTokens(languageId, editorId, lt))
        }
    }

    private fun createParseTree(spptNode: SPPTNode): dynamic {
        return when (spptNode) {
            is SPPTLeaf -> objectJS {
                 isBranch = false
                 name = spptNode.name
                 nonSkipMatchedText = spptNode.nonSkipMatchedText
            }
            is SPPTBranch -> objectJS {
                 isBranch = true
                 name = spptNode.name
                 children = spptNode.children.map {
                    createParseTree(it)
                }.toTypedArray()
            }
            else -> error("Not supported")
        }
    }

    private fun createAsmTree(asm: Any?): Any? {
        return if (null == asm) {
            null
        } else {
            when (asm) {
                is AsmElementSimple -> {
                    objectJS {
                         isAsmElementSimple = true
                         typeName = asm.typeName
                         properties = asm.properties.map {
                             objectJS {
                                 isAsmElementProperty = true
                                 name = it.name
                                 value = createAsmTree(it.value)
                            }
                        }.toTypedArray()
                    }
                }
                is List<*> -> asm.map {
                    createAsmTree(it)
                }.toTypedArray()
                else -> asm.toString()
            }
        }
    }
}