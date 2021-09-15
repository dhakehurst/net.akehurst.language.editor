package net.akehurst.language.editor.worker

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.sppt.SPPTBranch
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.api.syntaxAnalyser.AsmElementSimple
import net.akehurst.language.editor.common.*

abstract class AglWorkerAbstract {

    private var _languageDefinition: MutableMap<String,LanguageDefinition> = mutableMapOf()
    private var _styleHandler: MutableMap<String,AglStyleHandler> = mutableMapOf()

    private fun sendMessage(port: dynamic, msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        port.postMessage(msg.toObjectJS(), transferables)
    }

    protected fun createProcessor(port: dynamic, languageId: String, editorId: String, grammarStr: String?) {
        if (null == grammarStr) {
            this._languageDefinition.remove(languageId)
            sendMessage(port, MessageProcessorCreateSuccess(languageId, editorId, "reset"))
        } else {
            try {
                val ld = Agl.registry.findOrPlaceholder(languageId)
                if(ld.grammarIsModifiable) {
                    ld.grammar = grammarStr
                }
                _languageDefinition[languageId] = ld
                sendMessage(port, MessageProcessorCreateSuccess(languageId, editorId, "OK"))
            } catch (t: Throwable) {
                sendMessage(port, MessageProcessorCreateFailure(languageId, editorId, t.message!!))
            }
        }
    }

    protected fun interrupt(port: dynamic, languageId: String, editorId: String, reason: String) {
        val proc = this._languageDefinition[languageId]?.processor
        if (proc != null) {
            proc.interrupt(reason)
        }
    }

    protected fun setStyle(port: dynamic, languageId: String, editorId: String, css: String) {
        try {
            val style = AglStyleHandler(languageId)
            this._styleHandler[languageId] = style
            val rules: List<AglStyleRule> = Agl.registry.agl.style.processor!!.process(List::class, css)
            rules.forEach { rule ->
                style.mapClass(rule.selector)
            }
            sendMessage(port, MessageSetStyleResult(languageId, editorId, true, "OK"))
        } catch (t: Throwable) {
            sendMessage(port, MessageSetStyleResult(languageId, editorId, false, t.message!!))
        }
    }

    protected fun parse(port: dynamic, languageId: String, editorId: String, goalRuleName: String?, sentence: String) {
        try {
            sendMessage(port, MessageParseStart(languageId, editorId))
            val proc = this._languageDefinition[languageId]?.processor ?: throw RuntimeException("Processor for $languageId not found")
            val sppt = if (null == goalRuleName) proc.parse(sentence) else proc.parseForGoal(goalRuleName, sentence)
            val tree = createParseTree(sppt.root)
            sendMessage(port, MessageParseSuccess(languageId, editorId, tree))
            this.sendParseLineTokens(port, languageId, editorId, sppt)
            this.process(port, languageId, editorId, sppt)
        } catch (e: ParseFailedException) {
            val sppt = e.longestMatch
            val tree = createParseTree(sppt!!.root)
            sendMessage(port, MessageParseFailure(languageId, editorId, e.message!!, e.location, e.expected.toTypedArray(), tree))
        } catch (t: Throwable) {
            sendMessage(port, MessageParseFailure(languageId, editorId, t.message!!, null, emptyArray(), null))
        }
    }

    private fun process(port: dynamic, languageId: String, editorId: String, sppt: SharedPackedParseTree) {
        try {
            sendMessage(port, MessageProcessStart(languageId, editorId))
            val proc = this._languageDefinition[languageId]?.processor ?: throw RuntimeException("Processor for $languageId not found")
            val asm = proc.processFromSPPT<Any>(Any::class, sppt)
            val asmTree = createAsmTree(asm) ?: "No Asm"
            sendMessage(port, MessageProcessSuccess(languageId, editorId, asmTree))
        } catch (t: Throwable) {
            sendMessage(port, MessageProcessFailure(languageId, editorId, t.message!!))
        }
    }

    private fun sendParseLineTokens(port: dynamic, languageId: String, editorId: String, sppt: SharedPackedParseTree) {
        if (null == sppt) {
            //nothing
        } else {
            val style = this._styleHandler[languageId] ?: throw RuntimeException("StyleHandler for $languageId not found") //TODO: send Error msg not exception
            val lineTokens = sppt.tokensByLineAll().mapIndexed { lineNum, leaves ->
                style.transformToTokens(leaves)
            }
            val lt = lineTokens.map {
                it.toTypedArray()
            }.toTypedArray()
            sendMessage(port, MessageLineTokens(languageId, editorId, lt))
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