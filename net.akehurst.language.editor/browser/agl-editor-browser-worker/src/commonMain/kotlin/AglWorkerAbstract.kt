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
import net.akehurst.language.agl.agl.parser.SentenceDefault
import net.akehurst.language.agl.default.TypeModelFromGrammar
import net.akehurst.language.agl.language.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.language.grammar.ContextFromGrammarRegistry
import net.akehurst.language.agl.processor.*
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.*
import net.akehurst.language.api.sppt.Sentence
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.api.EditorOptions
import net.akehurst.language.editor.common.AglStyleHandler
import net.akehurst.language.editor.common.messages.*

abstract class AglWorkerAbstract<AsmType : Any, ContextType : Any> {

    // languageId -> def
    private var _languageDefinition: MutableMap<String, LanguageDefinition<AsmType, ContextType>> = mutableMapOf()

    // languageId -> sh
    private var _styleHandler: MutableMap<String, AglStyleHandler> = mutableMapOf()

    // editorId -> options
    private var _editorOptions: MutableMap<String, EditorOptions> = mutableMapOf()

    protected abstract fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any> = emptyArray())
    protected abstract fun serialiseParseTreeToStringJson(sentence: String, sppt: SharedPackedParseTree?): String?

    protected open fun configureLanguageDefinition(ld: LanguageDefinition<AsmType, ContextType>, grammarStr: String?, crossReferenceModelStr: String?) {
        // TODO: could be an argument
        ld.configuration = Agl.configurationDefault() as LanguageProcessorConfiguration<AsmType, ContextType>
        ld.grammarStr = grammarStr
        ld.crossReferenceModelStr = crossReferenceModelStr
    }

    protected open fun createLanguageDefinition(languageId: String, grammarStr: String?, crossReferenceModelStr: String?): LanguageDefinition<AsmType, ContextType> {
        val ld = Agl.registry.findOrPlaceholder<AsmType, ContextType>(
            identity = languageId,
            aglOptions = Agl.options {
                semanticAnalysis {
                    context(ContextFromGrammarRegistry(Agl.registry))
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            //TODO: how to use configurationDefault ? - needed once completion-provider moved to worker
            configuration = Agl.configurationBase() //use if placeholder created, not found
        )
        if (ld.isModifiable) {
            configureLanguageDefinition(ld, grammarStr, crossReferenceModelStr)
        }
        return ld
    }

    protected fun receiveAglWorkerMessage(port: Any, msg: AglWorkerMessage) {
        when (msg) {
            is MessageProcessorCreate -> this.createProcessor(port, msg)
            is MessageParserInterruptRequest -> this.interrupt(port, msg)
            is MessageProcessRequest<*, *> -> this.process(port, msg as MessageProcessRequest<AsmType, ContextType>)
            is MessageSetStyle -> this.setStyle(port, msg)
            is MessageCodeCompleteRequest -> this.getCodeCompletions(port, msg)
            else -> error("Unknown Message type")
        }
    }

    protected fun createProcessor(port: Any, message: MessageProcessorCreate) {
        if (message.grammarStr.isBlank()) {
            MessageProcessorCreateResponse(message.endPoint, MessageStatus.FAILURE, "Cannot createProcessor if there is no grammar", emptyList(), emptyList())
        } else {
            try {
                val ld = createLanguageDefinition(message.endPoint.languageId, message.grammarStr, message.crossReferenceModelStr)
                _languageDefinition[message.endPoint.languageId] = ld
                _editorOptions[message.endPoint.editorId] = message.editorOptions
                //if there is a grammar check that grammar is well-defined and a processor can be created from it

                val proc = ld.processor // should throw exception if there are problems
                if (null == proc) {
                    sendMessage(port, MessageProcessorCreateResponse(message.endPoint, MessageStatus.FAILURE, "Error", emptyList(), ld.issues.all.toList()))
                } else {
                    sendMessage(port, MessageProcessorCreateResponse(message.endPoint, MessageStatus.SUCCESS, "OK", proc.scanner!!.matchables, ld.issues.all.toList()))
                }
            } catch (t: Throwable) {
                sendMessage(port, MessageProcessorCreateResponse(message.endPoint, MessageStatus.FAILURE, t.message!!, emptyList(), emptyList()))
            }
        }
    }

    protected fun deleteProcessor(port: Any, message: MessageProcessorDelete) {
        this._languageDefinition.remove(message.endPoint.languageId)
        Agl.registry.unregister(message.endPoint.languageId)
        sendMessage(port, MessageProcessorDeleteResponse(message.endPoint, MessageStatus.SUCCESS, "OK"))
    }

    protected fun interrupt(port: Any, message: MessageParserInterruptRequest) {
        _languageDefinition[message.endPoint.languageId]?.processor?.interrupt(message.reason)
    }

    protected fun setStyle(port: Any, message: MessageSetStyle) {
        try {
            val style = AglStyleHandler(message.endPoint.languageId)
            this._styleHandler[message.endPoint.languageId] = style
            val result = Agl.registry.agl.style.processor!!.process(message.css)
            val styleMdl = result.asm
            if (null != styleMdl) {
                styleMdl.rules.forEach { rule ->
                    rule.selector.forEach { sel -> style.mapClass(sel.value) }
                }
                sendMessage(port, MessageSetStyleResult(message.endPoint, MessageStatus.SUCCESS, "OK"))
            } else {
                //TODO: handle issues!
            }
        } catch (t: Throwable) {
            sendMessage(port, MessageSetStyleResult(message.endPoint, MessageStatus.FAILURE, t.message!!))
        }
    }

    protected fun process(port: Any, message: MessageProcessRequest<AsmType, ContextType>) {
        //val editorOptions = _editorOptions[message.endPoint.editorId]
        val ld = this._languageDefinition[message.endPoint.languageId] ?: error("LanguageDefinition '${message.endPoint.languageId}' not found, was it created correctly?")
        val proc = ld.processor ?: error("Processor for '${message.endPoint.languageId}' not found, is the grammar correctly set ?")

        //val scan = scan(port, message.endPoint, proc, message.text)
        val parse = parse(port, message.endPoint, proc, message.options, message.text)
        val syntaxAnalysis = parse.sppt?.let { this.syntaxAnalysis(port, message.endPoint, proc, message.options, it) }
        val semanticAnalysis = syntaxAnalysis?.let { r -> r.asm?.let { this.semanticAnalysis(port, message.endPoint, proc, message.options, it, r.locationMap) } }
    }

    /*
        protected fun scan(port: Any, endPoint: EndPointIdentity, proc: LanguageProcessor<AsmType, ContextType>, sentence: String): ScanResult {
            return try {
                sendMessage(port, MessageScanResult(endPoint, MessageStatus.START, "Start", emptyList(), emptyList()))
                val result = proc.scan(sentence)
                sendMessage(port, MessageScanResult(endPoint, MessageStatus.SUCCESS, "Success", emptyList(), emptyList()))
                this.sendLineTokens(port, endPoint, result.tokens)
                result
            } catch (t: Throwable) {
                val st = t.stackTraceToString().substring(0, 100)
                val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message!!}\n$st"
                sendMessage(port, MessageScanResult(endPoint, MessageStatus.FAILURE, msg, emptyList(), emptyList()))
                ScanResultDefault(emptyList(), IssueHolder(LanguageProcessorPhase.SCAN))
            }
        }
    */

    protected fun parse(
        port: Any,
        endPoint: EndPointIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        processOptions: ProcessOptions<AsmType, ContextType>,
        sentence: String
    ): ParseResult {
        return try {
            sendMessage(port, MessageParseResult(endPoint, MessageStatus.START, "Start", emptyList(), null))
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.parse) {
                val result = proc.parse(sentence, processOptions.parse)
                val sppt = result.sppt
                if (null == sppt) {
                    sendMessage(port, MessageParseResult(endPoint, MessageStatus.FAILURE, "Parse Failed", result.issues.all.toList(), null))
                } else {
                    this.sendLineTokens(port, endPoint, SentenceDefault(sentence), sppt, editorOptions.lineTokensChunkSize)
                    if (editorOptions.parseTree) {
                        //TODO: send TreeData rather than encode parse tree...it should be faster
                        val treeStr = serialiseParseTreeToStringJson(sentence, sppt)
                        val treeStrEncoded = treeStr?.let { JsonString.encode(it) } //double encode treeStr as it itself is json
                        sendMessage(port, MessageParseResult(endPoint, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), treeStrEncoded))
                    } else {
                        sendMessage(port, MessageParseResult(endPoint, MessageStatus.SUCCESS, "ParseTree Interest not registered during Processor Creation", result.issues.all.toList(), null))
                    }
                }
                result
            } else {
                sendMessage(port, MessageParseResult(endPoint, MessageStatus.FAILURE, "Parse Interest not registered during Processor Creation", emptyList(), null))
                ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message!!}\n$st"
            sendMessage(port, MessageParseResult(endPoint, MessageStatus.FAILURE, msg, emptyList(), null))
            ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
        }
    }

    private fun syntaxAnalysis(
        port: Any,
        endPoint: EndPointIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        options: ProcessOptions<AsmType, ContextType>,
        sppt: SharedPackedParseTree
    ): SyntaxAnalysisResult<AsmType> {
        return try {
            sendMessage(port, MessageSyntaxAnalysisResult(endPoint, MessageStatus.START, "Start", emptyList(), null))
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.syntaxAnalysis) {
                val result = proc.syntaxAnalysis(sppt, options)
                val asm = result.asm
                if (null == asm) {
                    sendMessage(port, MessageSyntaxAnalysisResult(endPoint, MessageStatus.FAILURE, "SyntaxAnalysis Failed", result.issues.all.toList(), null))
                } else {
                    if (editorOptions.syntaxAnalysisAsm) {
                        sendMessage(port, MessageSyntaxAnalysisResult(endPoint, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm))
                    } else {
                        sendMessage(
                            port,
                            MessageSyntaxAnalysisResult(endPoint, MessageStatus.SUCCESS, "SyntaxAnalysis ASM Interest not registered during Processor Creation", result.issues.all.toList(), null)
                        )
                    }
                }
                result
            } else {
                sendMessage(port, MessageSyntaxAnalysisResult(endPoint, MessageStatus.FAILURE, "SyntaxAnalysis Interest not registered during Processor Creation", emptyList(), null))
                SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), emptyMap())
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during syntaxAnalysis - ${t::class.simpleName} - ${t.message!!}\n$st"
            sendMessage(port, MessageSyntaxAnalysisResult(endPoint, MessageStatus.FAILURE, msg, emptyList(), null))
            SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), emptyMap())
        }
    }

    private fun semanticAnalysis(
        port: Any,
        endPoint: EndPointIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        options: ProcessOptions<AsmType, ContextType>,
        asm: AsmType,
        locationMap: Map<Any, InputLocation>
    ) {
        try {
            sendMessage(port, MessageSemanticAnalysisResult(endPoint, MessageStatus.START, "Start", emptyList(), null))
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.semanticAnalysis) {
                // to save time serialisating/deserialising contexts that are based on information already in the worker
                // when (language) {
                //  is Agl Grammar -> create ContextFromGrammarRegistry
                //  is Agl CrossReferences -> context should be a reference to a diff LanguageDefinition, get its typemodel and create ContextFromTypeModel
                // }
                val ctx = when (endPoint.languageId) {
                    Agl.registry.agl.grammar.identity -> options.semanticAnalysis.context ?: ContextFromGrammarRegistry(Agl.registry)
                    Agl.registry.agl.crossReference.identity -> when (options.semanticAnalysis.context) {
                        is ContextFromTypeModelReference -> {
                            val langId = (options.semanticAnalysis.context as ContextFromTypeModelReference).languageDefinitionId
                            val ld = _languageDefinition[langId] ?: error("Language '$langId' not defined in worker")
                            val tm = TypeModelFromGrammar.createFromGrammarList(ld.grammarList)
                            ContextFromTypeModel(tm)
                        }

                        else -> options.semanticAnalysis.context
                    }

                    else -> options.semanticAnalysis.context
                }
                val opts = Agl.options(options) {
                    semanticAnalysis {
                        locationMap(locationMap)
                        context(ctx as ContextType)
                        option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                    }
                }
                val result = proc.semanticAnalysis(asm, opts)
                if (editorOptions.semanticAnalysisAsm) {
                    sendMessage(port, MessageSemanticAnalysisResult(endPoint, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm))
                } else {
                    sendMessage(
                        port,
                        MessageSemanticAnalysisResult(endPoint, MessageStatus.SUCCESS, "SemanticAnalysis ASM Interest not registered during Processor Creation", result.issues.all.toList(), null)
                    )
                }
            } else {
                sendMessage(port, MessageSemanticAnalysisResult(endPoint, MessageStatus.FAILURE, "SemanticAnalysis Interest not registered during Processor Creation", emptyList(), null))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during semanticAnalysis - ${t::class.simpleName} - ${t.message!!}\n$st"
            sendMessage(port, MessageSemanticAnalysisResult(endPoint, MessageStatus.FAILURE, msg, emptyList(), null))
        }
    }

    private fun sendLineTokens(port: Any, endPoint: EndPointIdentity, sentence: Sentence, sppt: SharedPackedParseTree, lineTokensChunkSize: Int) {
        try {
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.parseLineTokens) {
                val tokens = sppt.tokensByLineAll()
                val style = this._styleHandler[endPoint.languageId] ?: error("StyleHandler for ${endPoint.languageId} not found") //TODO: send Error msg not exception
                if (0 < lineTokensChunkSize) {
                    val lineTokensChunked = tokens.chunked(lineTokensChunkSize)
                    var chunkstart = 0
                    for (chunk in lineTokensChunked) {
                        val lineTokens = chunk.mapIndexed { lineNum, leaves ->
                            style.transformToTokens(leaves)
                        }
                        sendMessage(port, MessageLineTokens(endPoint, MessageStatus.SUCCESS, "Success", chunkstart, lineTokens))
                        chunkstart += chunk.size
                    }
                } else {
                    val lineTokens = tokens.mapIndexed { lineNum, leaves ->
                        style.transformToTokens(leaves)
                    }
                    sendMessage(port, MessageLineTokens(endPoint, MessageStatus.SUCCESS, "Success", 0, lineTokens))
                }
            } else {
                sendMessage(port, MessageLineTokens(endPoint, MessageStatus.FAILURE, "ParseLineTokens Interest not registered during Processor Creation", -1, emptyList()))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "${t.message}\n$st"
            sendMessage(port, MessageLineTokens(endPoint, MessageStatus.FAILURE, msg, -1, emptyList()))
        }
    }

    private fun grammarAmbiguityAnalysis(port: Any, message: MessageGrammarAmbiguityAnalysisRequest) {
        try {
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.endPoint, MessageStatus.START, null, emptyList()))
            val ld = this._languageDefinition[message.endPoint.languageId] ?: error("LanguageDefinition '${message.endPoint.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.endPoint.languageId}' not found, is the grammar correctly set ?")
            val result = Agl.registry.agl.grammar.processor!!.semanticAnalysis(listOf(proc.grammar!!), Agl.options {
                semanticAnalysis {
                    context(ContextFromGrammarRegistry(Agl.registry))
                    locationMap(proc.syntaxAnalyser!!.locationMap)
                    //context(message.context)
                }
            })
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.endPoint, MessageStatus.SUCCESS, null, result.issues.all.toList()))
        } catch (t: Throwable) {
            val msg = "Exception during ambiguityAnalysis - ${t::class.simpleName} - ${t.message!!}"
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.endPoint, MessageStatus.FAILURE, msg, emptyList()))
        }
    }

    private fun getCodeCompletions(port: Any, message: MessageCodeCompleteRequest) {
        try {
            sendMessage(port, MessageCodeCompleteResult(message.endPoint, MessageStatus.START, "Start", emptyList(), null))
            val ld = this._languageDefinition[message.endPoint.languageId] ?: error("LanguageDefinition '${message.endPoint.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.endPoint.languageId}' not found, is the grammar correctly set ?")
            val result = proc.expectedItemsAt(
                message.text,
                message.position,
                1,
                Agl.options { parse { goalRuleName(message.goalRuleName) } }
            )
            sendMessage(port, MessageCodeCompleteResult(message.endPoint, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), result.items))
            result.items
        } catch (t: Throwable) {
            val msg = "Exception during 'getCodeCompletions' - ${t::class.simpleName} - ${t.message!!}"
            sendMessage(port, MessageCodeCompleteResult(message.endPoint, MessageStatus.FAILURE, msg, emptyList(), null))
        }
    }

}