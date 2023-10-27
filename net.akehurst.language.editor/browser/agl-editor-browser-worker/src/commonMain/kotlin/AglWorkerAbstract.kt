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

import net.akehurst.kotlin.json.JsonString
import net.akehurst.language.agl.language.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.processor.LanguageProcessorConfiguration
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.common.AglStyleHandler
import net.akehurst.language.editor.common.messages.*

abstract class AglWorkerAbstract<AsmType : Any, ContextType : Any> {

    private var _languageDefinition: MutableMap<String, LanguageDefinition<AsmType, ContextType>> = mutableMapOf()
    private var _styleHandler: MutableMap<String, AglStyleHandler> = mutableMapOf()

    protected abstract fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any> = emptyArray())
    protected abstract fun serialiseParseTreeToStringJson(sentence: String, sppt: SharedPackedParseTree?): String?

    protected open fun configureLanguageDefinition(ld: LanguageDefinition<AsmType, ContextType>, grammarStr: String?, scopeModelStr: String?) {
        // TODO: could be an argument
        ld.configuration = Agl.configurationDefault() as LanguageProcessorConfiguration<AsmType, ContextType>
        ld.grammarStr = grammarStr
        ld.scopeModelStr = scopeModelStr
    }

    protected open fun createLanguageDefinition(languageId: String, grammarStr: String?, scopeModelStr: String?): LanguageDefinition<AsmType, ContextType> {
            val ld = Agl.registry.findOrPlaceholder<AsmType, ContextType>(
                identity = languageId,
                aglOptions = Agl.options {
                    semanticAnalysis {
                        option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                    }
                },
                //TODO: how to use configurationDefault ? - needed once completion-provider moved to worker
                configuration = Agl.configurationEmpty() //use if placeholder created, not found
            )
            if (ld.isModifiable) {
                configureLanguageDefinition(ld, grammarStr, scopeModelStr)
            }
            return ld
    }

    protected fun receiveAglWorkerMessage(port: Any, msg: AglWorkerMessage) {
        when (msg) {
            is MessageProcessorCreate -> this.createProcessor(port, msg)
            is MessageSyntaxAnalyserConfigure -> this.configureSyntaxAnalyser(port, msg)
            is MessageParserInterruptRequest -> this.interrupt(port, msg)
            is MessageProcessRequest<*> -> this.parse(port, msg as MessageProcessRequest<ContextType>)
            is MessageSetStyle -> this.setStyle(port, msg)
            is MessageCodeCompleteRequest -> this.getCodeCompletions(port, msg)
            else -> error("Unknown Message type")
        }
    }

    protected fun createProcessor(port: Any, message: MessageProcessorCreate) {
        if (message.grammarStr.isBlank()) {
            MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, MessageStatus.FAILURE, "Cannot createProcessor if there is no grammar", emptyList())
        } else {
            try {
                val ld = createLanguageDefinition(message.languageId, message.grammarStr, message.scopeModelStr)
                _languageDefinition[message.languageId] = ld
                //if there is a grammar check that grammar is well-defined and a processor can be created from it

                val proc = ld.processor // should throw exception if there are problems
                if (null == proc) {
                    ld.issues
                    sendMessage(
                        port,
                        MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, MessageStatus.FAILURE, "Error", ld.issues.all.toList())
                    )
                } else {
                    sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, "OK", emptyList()))
                }
            } catch (t: Throwable) {
                sendMessage(port, MessageProcessorCreateResponse(message.languageId, message.editorId, message.sessionId, MessageStatus.FAILURE, t.message!!, emptyList()))
            }
        }
    }

    protected fun deleteProcessor(port: Any, message: MessageProcessorDelete) {
        this._languageDefinition.remove(message.languageId)
        Agl.registry.unregister(message.languageId)
        sendMessage(port, MessageProcessorDeleteResponse(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, "OK"))
    }

    protected fun configureSyntaxAnalyser(port: Any, message: MessageSyntaxAnalyserConfigure) {
        val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
        val grmr = ld.processor?.grammar
        if (null != grmr) {
            //val issues = ld.syntaxAnalyser?.configure(ContextFromGrammar(grmr), message.configuration)
            //sendMessage(
            //    port, MessageSyntaxAnalyserConfigureResponse(
            //        message.languageId, message.editorId, message.sessionId,
            //        true,
            //        "OK",
            //        issues ?: emptyList()
            //    )
            //)
        } else {
            sendMessage(
                port, MessageSyntaxAnalyserConfigureResponse(
                    message.languageId, message.editorId, message.sessionId,
                    MessageStatus.FAILURE,
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
            val result = Agl.registry.agl.style.processor!!.process(message.css)
            val styleMdl = result.asm
            if (null != styleMdl) {
                styleMdl.rules.forEach { rule ->
                    rule.selector.forEach { sel -> style.mapClass(sel.value) }
                }
                sendMessage(port, MessageSetStyleResult(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, "OK"))
            } else {
                //TODO: handle issues!
            }
        } catch (t: Throwable) {
            sendMessage(port, MessageSetStyleResult(message.languageId, message.editorId, message.sessionId, MessageStatus.FAILURE, t.message!!))
        }
    }

    protected fun parse(port: Any, message: MessageProcessRequest<ContextType>) {
        try {
            sendMessage(port, MessageParseResult(message.languageId, message.editorId, message.sessionId, MessageStatus.START, "Start", emptyList(), null))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val goal = message.goalRuleName
            val result = if (null == goal) proc.parse(message.text) else proc.parse(message.text, Agl.parseOptions { goalRuleName(goal) })
            val sppt = result.sppt
            if (null == sppt) {
                sendMessage(
                    port,
                    MessageParseResult(message.languageId, message.editorId, message.sessionId, MessageStatus.FAILURE, "Parse Failed", result.issues.all.toList(), null)
                )
            } else {
                val treeStr = serialiseParseTreeToStringJson(message.text, sppt)
                val treeStrEncoded = treeStr?.let { JsonString.encode(it) } //double encode treeStr as it itself is json
                sendMessage(
                    port,
                    MessageParseResult(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), treeStrEncoded)
                )
                this.sendParseLineTokens(port, message.languageId, message.editorId, message.sessionId, sppt)
                this.syntaxAnalysis(port, message, proc, sppt)
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message!!}\n$st"
            sendMessage(
                port,
                MessageParseResult(
                    message.languageId,
                    message.editorId,
                    message.sessionId,
                    MessageStatus.FAILURE,
                    msg,
                    emptyList(),
                    null
                )
            )
        }
    }

    private fun syntaxAnalysis(port: Any, message: MessageProcessRequest<ContextType>, proc: LanguageProcessor<AsmType, ContextType>, sppt: SharedPackedParseTree) {
        try {
            sendMessage(port, MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, MessageStatus.START, "Start", emptyList(), null))
            val result = proc.syntaxAnalysis(sppt)
            val asm = result.asm
            if (null == asm) {
                sendMessage(
                    port,
                    MessageSyntaxAnalysisResult(
                        message.languageId,
                        message.editorId,
                        message.sessionId,
                        MessageStatus.FAILURE,
                        "SyntaxAnalysis Failed",
                        result.issues.all.toList(),
                        null
                    )
                )
            } else {
                sendMessage(
                    port,
                    MessageSyntaxAnalysisResult(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm)
                )
                this.semanticAnalysis(port, message, proc, asm, result.locationMap)
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during syntaxAnalysis - ${t::class.simpleName} - ${t.message!!}\n$st"
            sendMessage(
                port,
                MessageSyntaxAnalysisResult(
                    message.languageId,
                    message.editorId,
                    message.sessionId,
                    MessageStatus.FAILURE,
                    msg,
                    emptyList(),
                    null
                )
            )
        }
    }

    private fun semanticAnalysis(
        port: Any,
        message: MessageProcessRequest<ContextType>,
        proc: LanguageProcessor<AsmType, ContextType>,
        asm: AsmType,
        locationMap: Map<Any, InputLocation>
    ) {
        try {
            sendMessage(port, MessageSemanticAnalysisResult(message.languageId, message.editorId, message.sessionId, MessageStatus.START, "Start", emptyList(), null))
            val result = proc.semanticAnalysis(asm, Agl.options {
                semanticAnalysis {
                    locationMap(locationMap)
                    context(message.context)
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            })
            sendMessage(
                port,
                MessageSemanticAnalysisResult(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm)
            )
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during semanticAnalysis - ${t::class.simpleName} - ${t.message!!}\n$st"
            sendMessage(
                port,
                MessageSemanticAnalysisResult(
                    message.languageId, message.editorId, message.sessionId,
                    MessageStatus.FAILURE,
                    msg,
                    emptyList(),
                    null
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
                sendMessage(port, MessageLineTokens(languageId, editorId, sessionId, MessageStatus.SUCCESS, "Success", lineTokens))
            } catch (t: Throwable) {
                val st = t.stackTraceToString().substring(0, 100)
                val msg = "${t.message}\n$st"
                sendMessage(port, MessageLineTokens(languageId, editorId, sessionId, MessageStatus.FAILURE, msg, emptyList()))
            }
        }
    }

    private fun grammarAmbiguityAnalysis(port: Any, message: MessageGrammarAmbiguityAnalysisRequest) {
        try {
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.languageId, message.editorId, message.sessionId, MessageStatus.START, null, emptyList()))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val result = Agl.registry.agl.grammar.processor!!.semanticAnalysis(listOf(proc.grammar!!), Agl.options {
                semanticAnalysis {
                    locationMap(proc.syntaxAnalyser!!.locationMap)
                    //context(message.context)
                }
            })
            sendMessage(
                port,
                MessageGrammarAmbiguityAnalysisResult(message.languageId, message.editorId, message.sessionId, MessageStatus.SUCCESS, null, result.issues.all.toList())
            )
        } catch (t: Throwable) {
            sendMessage(
                port,
                MessageGrammarAmbiguityAnalysisResult(
                    message.languageId,
                    message.editorId,
                    message.sessionId,
                    MessageStatus.FAILURE,
                    "Exception during ambiguityAnalysis - ${t::class.simpleName} - ${t.message!!}",
                    emptyList()
                )
            )
        }
    }

    private fun getCodeCompletions(port: Any, message: MessageCodeCompleteRequest) {
        try {
            sendMessage(port, MessageCodeCompleteResult(message.languageId, message.editorId, message.sessionId, MessageStatus.START, "Start", emptyList(), null))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val result = proc.expectedItemsAt(
                message.text,
                message.position,
                1,
                Agl.options { parse { goalRuleName(message.goalRuleName) } })
            sendMessage(
                port, MessageCodeCompleteResult(
                    message.languageId, message.editorId, message.sessionId,
                    MessageStatus.SUCCESS,
                    "Success",
                    result.issues.all.toList(),
                    result.items
                )
            )
            result.items
        } catch (t: Throwable) {
            sendMessage(
                port, MessageCodeCompleteResult(
                    message.languageId, message.editorId, message.sessionId,
                    MessageStatus.FAILURE,
                    "Exception during 'getCodeCompletions' - ${t::class.simpleName} - ${t.message!!}",
                    emptyList(),
                    null
                )
            )
        }
    }

}