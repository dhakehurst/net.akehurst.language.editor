package net.akehurst.language.editor.worker

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.asm.AsmElementSimple
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.sppt.SPPTBranch
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.editor.common.*
import org.w3c.dom.MessageEvent

abstract class AglWorkerAbstract {

    private var _languageDefinition: MutableMap<String, LanguageDefinition> = mutableMapOf()
    private var _styleHandler: MutableMap<String, AglStyleHandler> = mutableMapOf()

    protected fun sendMessage(port: dynamic, msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        val str = AglWorkerMessage.serialise(msg)
        port.postMessage(str, transferables)
    }

    protected fun receiveMessage(port: dynamic, ev: MessageEvent) {
        try {
            if (ev.data is String) {
                val str = ev.data as String
                val msg: AglWorkerMessage? = AglWorkerMessage.deserialise(str)
                if (null == msg) {
                    port.postMessage("Error: Worker cannot handle message: $str")
                } else {
                    when (msg) {
                        is MessageProcessorCreate -> this.createProcessor(port, msg)
                        is MessageParserInterruptRequest -> this.interrupt(port, msg)
                        is MessageProcessRequest -> this.parse(port, msg)
                        is MessageSetStyle -> this.setStyle(port, msg)
                        else -> error("Unknown Message type")
                    }
                }
            } else {
                port.postMessage("Error: Worker error in receiveMessage: data content should be a String, got - '${ev.data}'")
            }
        } catch (e: Throwable) {
            port.postMessage("Error: Worker error in receiveMessage: ${e.message!!}")
        }
    }

    protected fun createProcessor(port: dynamic, message: MessageProcessorCreate) {
        if (null == message.grammarStr) {
            this._languageDefinition.remove(message.languageId)
            sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, true, "reset"))
        } else {
            try {
                val ld = Agl.registry.findOrPlaceholder(message.languageId)
                if (ld.grammarIsModifiable) {
                    ld.grammar = message.grammarStr
                }
                _languageDefinition[message.languageId] = ld
                sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, true, "OK"))
            } catch (t: Throwable) {
                sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, false, t.message!!))
            }
        }
    }

    protected fun interrupt(port: dynamic, message: MessageParserInterruptRequest) {
        _languageDefinition[message.languageId]?.processor?.interrupt(message.reason)
    }

    protected fun setStyle(port: dynamic, message: MessageSetStyle) {
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

    protected fun parse(port: dynamic, message: MessageProcessRequest) {
        try {
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, false, "Start", null, emptyArray()))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val goal = message.goalRuleName
            val (sppt, issues) = if (null == goal) proc.parse(message.text) else proc.parse(message.text, goal)
            val tree = sppt?.let { createParseTree(sppt.root) }
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, true, "Success", tree, issues.toTypedArray()))
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
                    emptyArray()
                )
            )
        }
    }

    private fun syntaxAnalysis(port: dynamic, message: MessageProcessRequest, proc: LanguageProcessor, sppt: SharedPackedParseTree) {
        try {
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, false, "Start", null, emptyArray()))
            val context = message.context
            val (asm, issues, locationMap) = proc.syntaxAnalysis<Any, Any>(sppt,context)
            val asmTree = createAsmTree(asm) ?: "No Asm"
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, true, "Success", asmTree, issues.toTypedArray()))
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
                    emptyArray()
                )
            )
        }
    }

    private fun semanticAnalysis(port: dynamic, message: MessageProcessRequest, proc: LanguageProcessor, asm: Any?, locationMap: Map<*, InputLocation>) {
        try {
            sendMessage(port, MessageSemanticAnalysisResult(message.languageId, message.editorId, message.sessionId, false, "Start", emptyArray()))
            val context = message.context
            if (null != asm) {
                val issues = proc.semanticAnalysis(asm, locationMap, context)
                sendMessage(port, MessageSemanticAnalysisResult(message.languageId, message.editorId, message.sessionId, false, "Success", issues.toTypedArray()))
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
                    emptyArray()
                )
            )
        }
    }

    private fun sendParseLineTokens(port: dynamic, languageId: String, editorId: String, sessionId: String, sppt: SharedPackedParseTree) {
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