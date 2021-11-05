/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import net.akehurst.language.agl.grammar.grammar.ContextFromGrammar
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.editor.common.AglStyleHandler
import net.akehurst.language.editor.common.messages.*

abstract class AglWorkerAbstract {

    private var _languageDefinition: MutableMap<String, LanguageDefinition> = mutableMapOf()
    private var _styleHandler: MutableMap<String, AglStyleHandler> = mutableMapOf()

    protected abstract fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any> = emptyArray())
    protected abstract fun serialiseParseTree(spptNode: SPPTNode?) : String?

    protected fun receiveAglWorkerMessage(port: Any, msg: AglWorkerMessage) {
        when (msg) {
            is MessageProcessorCreate -> this.createProcessor(port, msg)
            is MessageSyntaxAnalyserConfigure -> this.configureSyntaxAnalyser(port, msg)
            is MessageParserInterruptRequest -> this.interrupt(port, msg)
            is MessageProcessRequest -> this.parse(port, msg)
            is MessageSetStyle -> this.setStyle(port, msg)
            else -> error("Unknown Message type")
        }
    }

    protected fun createProcessor(port: Any, message: MessageProcessorCreate) {
        if (message.grammarStr.isNullOrBlank()) {
            this._languageDefinition.remove(message.languageId)
            sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, true, "reset"))
        } else {
            try {
                val ld = Agl.registry.findOrPlaceholder(message.languageId)
                if (ld.grammarIsModifiable) {
                    ld.grammar = message.grammarStr
                }
                _languageDefinition[message.languageId] = ld
                //check that grammar is well-defined and a processor can be created from it
                val proc = ld.processor // should throw exception if there are problems
                sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, true, "OK"))
            } catch (t: Throwable) {
                sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, false, t.message!!))
            }
        }
    }

    protected fun configureSyntaxAnalyser(port: Any, message: MessageSyntaxAnalyserConfigure) {
        val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
        val grmr = ld.processor?.grammar
        if (null != grmr) {
            val issues = ld.syntaxAnalyser?.configure(ContextFromGrammar(grmr), message.configuration as String)
            sendMessage(
                port, MessageSyntaxAnalyserConfigureResponse(
                    message.languageId, message.editorId, message.sessionId,
                    true,
                    "OK",
                    issues ?: emptyList()
                )
            )
        } else {
            sendMessage(
                port, MessageSyntaxAnalyserConfigureResponse(
                    message.languageId, message.editorId, message.sessionId,
                    false,
                    "Failed to configure Syntax Analyser, no grammar for processor",
                    emptyList(),
                )
            )
        }
    }

    protected fun interrupt(port: Any, message: MessageParserInterruptRequest) {
        _languageDefinition[message.languageId]?.processor?.interrupt(message.reason)
    }

    protected fun setStyle(port: Any, message: MessageSetStyle) {
        try {
            val style = AglStyleHandler(message.languageId)
            this._styleHandler[message.languageId] = style
            val rules: List<AglStyleRule>? = Agl.registry.agl.style.processor!!.process<List<AglStyleRule>, Any>(message.css).first
            if (null != rules) {
                rules.forEach { rule ->
                    style.mapClass(rule.selector)
                }
                sendMessage(port, MessageSetStyleResult(message.languageId, message.editorId, message.sessionId, true, "OK"))
            } else {

            }
        } catch (t: Throwable) {
            sendMessage(port, MessageSetStyleResult(message.languageId, message.editorId, message.sessionId, false, t.message!!))
        }
    }

    protected fun parse(port: Any, message: MessageProcessRequest) {
        try {
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, false, "Start", null, emptyList()))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val goal = message.goalRuleName
            val (sppt, issues) = if (null == goal) proc.parse(message.text) else proc.parse(message.text, goal)
            val treeStr = serialiseParseTree(sppt?.root)
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, true, "Success", treeStr, issues))
            sppt?.let {
                this.sendParseLineTokens(port, message.languageId, message.editorId, message.sessionId, sppt)
                this.syntaxAnalysis(port, message, proc, sppt)
            }
        } catch (t: Throwable) {
            sendMessage(
                port,
                MessageParseResult(
                    message.languageId,
                    message.editorId,
                    message.sessionId,
                    false,
                    "Exception during parse - ${t::class.simpleName} - ${t.message!!}",
                    null,
                    emptyList()
                )
            )
        }
    }

    private fun syntaxAnalysis(port: Any, message: MessageProcessRequest, proc: LanguageProcessor, sppt: SharedPackedParseTree) {
        try {
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, false, "Start", null, emptyList()))
            val context = message.context
            val (asm, issues, locationMap) = proc.syntaxAnalysis<Any, Any>(sppt, context)
            val asmTree = asm //createAsmTree(asm) ?: "No Asm"
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, true, "Success", asmTree, issues))
            this.semanticAnalysis(port, message, proc, asm, locationMap)
        } catch (t: Throwable) {
            sendMessage(
                port,
                MessageSyntaxAnalysisResult(
                    message.languageId,
                    message.editorId,
                    message.sessionId,
                    false,
                    "Exception during syntaxAnalysis - ${t::class.simpleName} - ${t.message!!}",
                    null,
                    emptyList()
                )
            )
        }
    }

    private fun semanticAnalysis(port: Any, message: MessageProcessRequest, proc: LanguageProcessor, asm: Any?, locationMap: Map<*, InputLocation>) {
        try {
            sendMessage(port, MessageSemanticAnalysisResult(message.languageId, message.editorId, message.sessionId, false, "Start", emptyList()))
            val context = message.context
            if (null != asm) {
                val issues = proc.semanticAnalysis(asm, locationMap, context)
                sendMessage(port, MessageSemanticAnalysisResult(message.languageId, message.editorId, message.sessionId, false, "Success", issues))
            } else {
                //no analysis possible
            }
        } catch (t: Throwable) {
            sendMessage(
                port,
                MessageSemanticAnalysisResult(
                    message.languageId,
                    message.editorId,
                    message.sessionId,
                    false,
                    "Exception during semanticAnalysis - ${t::class.simpleName} - ${t.message!!}",
                    emptyList()
                )
            )
        }
    }

    private fun sendParseLineTokens(port: Any, languageId: String, editorId: String, sessionId: String, sppt: SharedPackedParseTree) {
        if (null == sppt) {
            //nothing
        } else {
            try {
                val style = this._styleHandler[languageId] ?: error("StyleHandler for $languageId not found") //TODO: send Error msg not exception
                val lineTokens = sppt.tokensByLineAll().mapIndexed { lineNum, leaves ->
                    style.transformToTokens(leaves)
                }
                val lt = lineTokens.map {
                    it.toTypedArray()
                }.toTypedArray()
                sendMessage(port, MessageLineTokens(languageId, editorId, sessionId, true, "Success", lt))
            } catch (t: Throwable) {
                sendMessage(port, MessageLineTokens(languageId, editorId, sessionId, false, t.message!!, emptyArray()))
            }
        }
    }

}