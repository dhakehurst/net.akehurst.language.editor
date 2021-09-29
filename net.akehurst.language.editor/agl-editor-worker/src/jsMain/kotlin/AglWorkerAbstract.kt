package net.akehurst.language.editor.worker

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.sppt.SPPTBranch
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.api.syntaxAnalyser.AsmElementSimple
import net.akehurst.language.editor.common.*

abstract class AglWorkerAbstract {

    private var _languageDefinition: MutableMap<String, LanguageDefinition> = mutableMapOf()
    private var _styleHandler: MutableMap<String, AglStyleHandler> = mutableMapOf()

    protected fun sendMessage(port: dynamic, msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        port.postMessage(msg.toObjectJS(), transferables)
    }

    protected fun createProcessor(port: dynamic, message:MessageProcessorCreate) {
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

    protected fun interrupt(port: dynamic, message:MessageParserInterruptRequest) {
        _languageDefinition[message.languageId]?.processor?.interrupt(message.reason)
    }

    protected fun setStyle(port: dynamic, message: MessageSetStyle) {
        try {
            val style = AglStyleHandler(message.languageId)
            this._styleHandler[message.languageId] = style
            val rules: List<AglStyleRule> = Agl.registry.agl.style.processor!!.process<List<AglStyleRule>,Any>(message.css).first
            rules.forEach { rule ->
                style.mapClass(rule.selector)
            }
            sendMessage(port, MessageSetStyleResult(message.languageId, message.editorId, message.sessionId, true, "OK"))
        } catch (t: Throwable) {
            sendMessage(port, MessageSetStyleResult(message.languageId, message.editorId, message.sessionId, false, t.message!!))
        }
    }

    protected fun parse(port: dynamic, message:MessageProcessRequest) {
        try {
            sendMessage(port, MessageParseStart(message.languageId, message.editorId, message.sessionId))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val goal = message.goalRuleName
            val sppt = if (null == goal) proc.parse(message.text) else proc.parse(message.text,goal)
            val tree = createParseTree(sppt.root)
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, true, "OK", tree, null, null))
            this.sendParseLineTokens(port, message.languageId, message.editorId, message.sessionId, sppt)
            this.syntaxAnalysis(port, message, sppt)
        } catch (e: ParseFailedException) {
            val sppt = e.longestMatch
            val tree = createParseTree(sppt!!.root)
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, false, e.message!!, tree, e.location, e.expected.toTypedArray()))
        } catch (t: Throwable) {
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, false, t.message!!, null, null, emptyArray()))
        }
    }

    private fun syntaxAnalysis(port: dynamic, message:MessageProcessRequest, sppt: SharedPackedParseTree) {
        try {
            sendMessage(port, MessageSyntaxAnalysisStart(message.languageId, message.editorId, message.sessionId))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val asm = proc.syntaxAnalysis<Any,Any>(sppt)
            val asmTree = createAsmTree(asm) ?: "No Asm"
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, true, "OK", asmTree))
            this.semanticAnalysis(port, message, asm)
        } catch (t: Throwable) {
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, false, t.message!!, null))
        }
    }

    private fun semanticAnalysis(port: dynamic, message:MessageProcessRequest, asm: Any?) {
        sendMessage(port, MessageSyntaxAnalysisStart(message.languageId, message.editorId, message.sessionId))
        val context = message.context
        if (null!=asm && null!=context) {
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val locationMap = ld.syntaxAnalyser?.locationMap ?: error("SyntaxAnalyser is null")
            val items = proc.semanticAnalysis(asm, locationMap,context)
        } else {
            //no analysis possible
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