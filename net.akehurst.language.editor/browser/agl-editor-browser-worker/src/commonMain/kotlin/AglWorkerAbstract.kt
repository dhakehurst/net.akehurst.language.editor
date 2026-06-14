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
import net.akehurst.language.agl.*
import net.akehurst.language.agl.processor.SyntaxAnalysisResultDefault
import net.akehurst.language.agl.processor.contextFromGrammarRegistry
import net.akehurst.language.agl.semanticAnalyser.contextFromTypesDomain
import net.akehurst.language.agl.syntaxAnalyser.LocationMapDefault
import net.akehurst.language.api.processor.*
import net.akehurst.language.api.syntaxAnalyser.LocationMap
import net.akehurst.language.editor.api.EditorOptions
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.MessageResponseStatus
import net.akehurst.language.editor.api.RequestIdentity
import net.akehurst.language.editor.common.AglStyleHandlerCssClass
import net.akehurst.language.editor.language.service.messages.*
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.parser.api.ParseResult
import net.akehurst.language.parser.leftcorner.ParseResultDefault
import net.akehurst.language.reference.asm.CrossReferenceDomainDefault
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.sppt.api.SharedPackedParseTree


abstract class AglWorkerAbstract {

    // languageId -> def
    private var _languageDefinition: MutableMap<LanguageIdentity, LanguageDefinition<Any, Any>> = mutableMapOf()

    // languageId -> sh
    private var _styleHandler: MutableMap<LanguageIdentity, AglStyleHandlerCssClass> = mutableMapOf()

    // editorId -> options
    private var _editorOptions: MutableMap<String, EditorOptions> = mutableMapOf()

    protected abstract fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any> = emptyArray())
    protected abstract fun serialiseParseTreeToStringJson(sentence: String, sppt: SharedPackedParseTree?): String?

    protected open fun configureLanguageDefinition(
        ld: LanguageDefinition<Any, Any>,
        grammarStr: GrammarString?,
        typeModelStr: TypesString?,
        asmTransformStr: AsmTransformString?,
        crossReferenceModelStr: CrossReferenceString?
    ) {
        //style and format not handled here, handled separately
        // TODO: could be an argument
        ld.configuration = Agl.configuration(base = Agl.configurationSimple() as LanguageProcessorConfiguration<Any, Any>) {
            if (null != crossReferenceModelStr) {
                crossReferenceResolver { p -> CrossReferenceDomainDefault.fromString(contextFromTypesDomain(p.typesDomain), crossReferenceModelStr) }
            }
        }
        ld.update(
            grammarString = grammarStr,
            typesString = typeModelStr,
            asmTransformString = asmTransformStr,
            crossReferenceString = crossReferenceModelStr,
            styleString = null,
            formatString = null,
        )
    }

    protected open fun createLanguageDefinition(
        languageId: LanguageIdentity,
        grammarStr: GrammarString?,
        typeModelStr: TypesString?,
        asmTransformStr: AsmTransformString?,
        crossReferenceModelStr: CrossReferenceString?
    ): LanguageDefinition<Any, Any> {
        val ld = Agl.registry.findOrPlaceholder<Any, Any>(
            identity = languageId,
            aglOptions = Agl.options {
                semanticAnalysis {
                    sentenceContext(contextFromGrammarRegistry(Agl.registry))
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            //TODO: how to use configurationDefault ? - needed once completion-provider moved to worker
            configuration = Agl.configurationBase() //use if placeholder created, not found
        )
        if (ld.isModifiable) {
            configureLanguageDefinition(ld, grammarStr, typeModelStr, asmTransformStr, crossReferenceModelStr)
        }
        return ld
    }

    protected fun receiveAglWorkerMessage(port: Any, msg: AglWorkerMessage) {
        when (msg) {
            is MessageProcessorCreate -> this.createProcessor(port, msg)
            is MessageParserInterruptRequest -> this.interrupt(port, msg)
            is MessageProcessRequest<*, *> -> this.process(port, msg as MessageProcessRequest<Any, Any>)
            is MessageSetStyle -> this.setStyle(port, msg)
            is MessageCodeCompleteRequest<*, *> -> this.getCodeCompletions(port, msg as MessageCodeCompleteRequest<Any, Any>)
            else -> error("Unknown Message type")
        }
    }

    protected fun createProcessor(port: Any, message: MessageProcessorCreate) {
        if (message.grammarStr.isBlank()) {
            MessageProcessorCreateResponse(message.endPoint, message.requestId, MessageResponseStatus.FAILURE, "Cannot createProcessor if there is no grammar", emptyList(), emptyList())
        } else {
            try {
                val ld = createLanguageDefinition(
                    message.languageId,
                    GrammarString(message.grammarStr),
                    message.typesModelStr?.let { TypesString(it) },
                    message.transformStr?.let { AsmTransformString(it) },
                    message.crossReferenceStr?.let { CrossReferenceString(it) }
                )
                _languageDefinition[message.languageId] = ld
                _editorOptions[message.endPoint.editorId] = message.editorOptions
                //if there is a grammar check that grammar is well-defined and a processor can be created from it
                var proc: LanguageProcessor<Any, Any>? = null
                try {
                    proc = ld.processor // should throw exception if there are problems
                } catch (t: Throwable) {

                }
                if (null == proc) {
                    sendMessage(port, MessageProcessorCreateResponse(message.endPoint, message.requestId, MessageResponseStatus.FAILURE, "Error", ld.issues.all.toList(), emptyList()))
                } else {
                    sendMessage(port, MessageProcessorCreateResponse(message.endPoint, message.requestId, MessageResponseStatus.SUCCESS, "OK", ld.issues.all.toList(), proc.scanner!!.matchables))
                }
            } catch (t: Throwable) {
                sendMessage(
                    port,
                    MessageProcessorCreateResponse(
                        message.endPoint,
                        message.requestId,
                        MessageResponseStatus.FAILURE,
                        "${t::class.simpleName}: ${t.message ?: "<no exception message>"}",
                        emptyList(),
                        emptyList()
                    )
                )
            }
        }
    }

    protected fun deleteProcessor(port: Any, message: MessageProcessorDelete) {
        this._languageDefinition.remove(message.languageId)
        Agl.registry.unregister(message.languageId)
        sendMessage(port, MessageProcessorDeleteResponse(message.endPoint, message.requestId, MessageResponseStatus.SUCCESS, "OK"))
    }

    protected fun interrupt(port: Any, message: MessageParserInterruptRequest) {
        _languageDefinition[message.languageId]?.processor?.interrupt(message.reason)
    }

    protected fun setStyle(port: Any, message: MessageSetStyle) {
        try {
            val styleHndlr = AglStyleHandlerCssClass(message.languageId)
            this._styleHandler[message.languageId] = styleHndlr
            val result = Agl.registry.agl.style.processor!!.process(message.styleStr)
            val styleMdl = result.asm
            if (null != styleMdl) {
                styleHndlr.updateStyleModel(styleMdl)
                sendMessage(port, MessageSetStyleResponse(message.endPoint, message.requestId, MessageResponseStatus.SUCCESS, "OK", result.allIssues.all.toList(), styleMdl))
            } else {
                sendMessage(port, MessageSetStyleResponse(message.endPoint, message.requestId, MessageResponseStatus.FAILURE, "Invalid Style", result.allIssues.all.toList(), null))
            }
        } catch (t: Throwable) {
            sendMessage(port, MessageSetStyleResponse(message.endPoint, message.requestId, MessageResponseStatus.FAILURE, t.message ?: "", emptyList(), null))
        }
    }

    protected fun process(port: Any, message: MessageProcessRequest<Any, Any>) {
        //val editorOptions = _editorOptions[message.endPoint.editorId]
        val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
        val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")

        //val scan = scan(port, message.endPoint, proc, message.text)
        val parse = parse(port, message.endPoint, message.requestId, message.languageId, proc, message.options, message.text)
        val syntaxAnalysis = parse.sppt?.let { this.syntaxAnalysis(port, message.endPoint, message.requestId, proc, message.options, it) }
        val semanticAnalysis =
            syntaxAnalysis?.let { r -> r.asm?.let { this.semanticAnalysis(port, message.endPoint, message.requestId, message.languageId, proc, message.options, it, r.locationMap) } }
    }

    /*
        protected fun scan(port: Any, endPoint: EndPointIdentity, proc: LanguageProcessor<AsmType, ContextType>, sentence: String): ScanResult {
            return try {
                sendMessage(port, MessageScanResult(endPoint, MessageResponseStatus.START, "Start", emptyList(), emptyList()))
                val result = proc.scan(sentence)
                sendMessage(port, MessageScanResult(endPoint, MessageResponseStatus.SUCCESS, "Success", emptyList(), emptyList()))
                this.sendLineTokens(port, endPoint, result.tokens)
                result
            } catch (t: Throwable) {
                val st = t.stackTraceToString().substring(0, 100)
                val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message!!}\n$st"
                sendMessage(port, MessageScanResult(endPoint, MessageResponseStatus.FAILURE, msg, emptyList(), emptyList()))
                ScanResultDefault(emptyList(), IssueHolder(LanguageProcessorPhase.SCAN))
            }
        }
    */

    protected fun parse(
        port: Any,
        endPoint: EndPointIdentity,
        requestId: RequestIdentity,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<Any, Any>,
        processOptions: ProcessOptions<Any, Any>,
        sentence: String
    ): ParseResult {
        return try {
            sendMessage(port, MessageParseResult(endPoint, requestId, MessageResponseStatus.RECEIVED, "Start", emptyList(), null))
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.parse) {
                val result = proc.parse(sentence, processOptions.parse)
                val sppt = result.sppt
                if (null == sppt) {
                    sendMessage(port, MessageParseResult(endPoint, requestId, MessageResponseStatus.FAILURE, "Parse Failed", result.issues.all.toList(), null))
                } else {
                    val sentenceObj = SentenceDefault(sentence, processOptions.parse.sentenceIdentity.invoke())
                    this.sendLineTokens(port, endPoint, requestId, languageId, sentenceObj, sppt, editorOptions.lineTokensChunkSize)
                    if (editorOptions.parseTree) {
                        //TODO: send TreeData rather than encode parse tree...it should be faster
                        val treeStr = serialiseParseTreeToStringJson(sentence, sppt)
                        val treeStrEncoded = treeStr?.let { JsonString.encode(it) } //double encode treeStr as it itself is json
                        sendMessage(port, MessageParseResult(endPoint, requestId, MessageResponseStatus.SUCCESS, "Success", result.issues.all.toList(), treeStrEncoded))
                    } else {
                        sendMessage(
                            port,
                            MessageParseResult(endPoint, requestId, MessageResponseStatus.SUCCESS, "ParseTree Interest not registered during Processor Creation", result.issues.all.toList(), null)
                        )
                    }
                }
                result
            } else {
                sendMessage(port, MessageParseResult(endPoint, requestId, MessageResponseStatus.FAILURE, "Parse Interest not registered during Processor Creation", emptyList(), null))
                ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message ?: ""}\n$st"
            sendMessage(port, MessageParseResult(endPoint, requestId, MessageResponseStatus.FAILURE, msg, emptyList(), null))
            ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
        }
    }

    private fun syntaxAnalysis(
        port: Any,
        endPoint: EndPointIdentity,
        requestId: RequestIdentity,
        proc: LanguageProcessor<Any, Any>,
        options: ProcessOptions<Any, Any>,
        sppt: SharedPackedParseTree
    ): SyntaxAnalysisResult<Any> {
        return try {
            sendMessage(port, MessageSyntaxAnalysisResult(endPoint, requestId, MessageResponseStatus.RECEIVED, "Start", emptyList(), null))
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.syntaxAnalysis) {
                val result = proc.syntaxAnalysis(sppt, options)
                val asm = result.asm
                if (null == asm) {
                    sendMessage(port, MessageSyntaxAnalysisResult(endPoint, requestId, MessageResponseStatus.FAILURE, "SyntaxAnalysis Failed", result.issues.all.toList(), null))
                } else {
                    if (editorOptions.syntaxAnalysisAsm) {
                        sendMessage(port, MessageSyntaxAnalysisResult(endPoint, requestId, MessageResponseStatus.SUCCESS, "Success", result.issues.all.toList(), asm))
                    } else {
                        sendMessage(
                            port,
                            MessageSyntaxAnalysisResult(
                                endPoint,
                                requestId,
                                MessageResponseStatus.SUCCESS,
                                "SyntaxAnalysis ASM Interest not registered during Processor Creation",
                                result.issues.all.toList(),
                                null
                            )
                        )
                    }
                }
                result
            } else {
                sendMessage(
                    port,
                    MessageSyntaxAnalysisResult(endPoint, requestId, MessageResponseStatus.FAILURE, "SyntaxAnalysis Interest not registered during Processor Creation", emptyList(), null)
                )
                SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), LocationMapDefault())
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during syntaxAnalysis - ${t::class.simpleName} - ${t.message ?: ""}\n$st"
            sendMessage(port, MessageSyntaxAnalysisResult(endPoint, requestId, MessageResponseStatus.FAILURE, msg, emptyList(), null))
            SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), LocationMapDefault())
        }
    }

    private fun semanticAnalysis(
        port: Any,
        endPoint: EndPointIdentity,
        requestId: RequestIdentity,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<Any, Any>,
        options: ProcessOptions<Any, Any>,
        asm: Any,
        locationMap: LocationMap
    ) {
        try {
            sendMessage(port, MessageSemanticAnalysisResult(endPoint, requestId, MessageResponseStatus.RECEIVED, "Start", emptyList(), null))
            val editorOptions = _editorOptions[endPoint.editorId]
            when {
                true != editorOptions?.semanticAnalysis -> {
                    sendMessage(
                        port,
                        MessageSemanticAnalysisResult(endPoint, requestId, MessageResponseStatus.FAILURE, "SemanticAnalysis Interest not registered during Processor Creation", emptyList(), null)
                    )
                }

                else -> {
                    // to save time serialisating/deserialising contexts that are based on information already in the worker
                    // when (language) {
                    //  is Agl Grammar -> create ContextFromGrammarRegistry
                    //  is Agl CrossReferences -> context should be a reference to a diff LanguageDefinition, get its typemodel and create ContextFromTypeModel
                    // }
                    val ctx = when (languageId) {
                        Agl.registry.agl.grammar.identity -> options.semanticAnalysis.sentenceContext ?: contextFromGrammarRegistry(Agl.registry)
                        Agl.registry.agl.crossReference.identity -> when (options.semanticAnalysis.sentenceContext) {
//                            is ContextFromTypesDomainReference -> {
//                                val langId = (options.semanticAnalysis.sentenceContext as ContextFromTypesDomainReference).languageDefinitionId
//                                val ld = _languageDefinition[langId] ?: error("Language '$langId' not defined in worker")
//                                val res = AsmTransformDomainDefault.fromGrammarDomain(ld.grammarDomain!!)
//                                val trfm = when {
//                                    res.allIssues.errors.isEmpty() -> res.asm ?: error("No error creating TransformModel from GrammarModel, but asm is null!")
//                                    else -> TODO()
//                                }
//                                val tm = trfm.typesDomain ?: error("No TypeModel found in TransformModel")
//                                //val tm = TypeModelFromGrammar.createFromGrammarList(ld.grammarList)
//                                ContextFromTypesDomain(tm)
//                            }

                            else -> options.semanticAnalysis.sentenceContext
                        }

                        else -> options.semanticAnalysis.sentenceContext
                    }
                    val opts = Agl.options(options) {
                        semanticAnalysis {
                            locationMap(locationMap)
                            sentenceContext(ctx as Any?)
                            option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                        }
                    }
                    val result = proc.semanticAnalysis(asm, opts)
                    val issues = result.issues.all
                    if (editorOptions.semanticAnalysisAsm) {
                        sendMessage(port, MessageSemanticAnalysisResult(endPoint, requestId, MessageResponseStatus.SUCCESS, "Success", issues.toList(), asm))
                    } else {
                        sendMessage(
                            port,
                            MessageSemanticAnalysisResult(
                                endPoint,
                                requestId,
                                MessageResponseStatus.SUCCESS,
                                "SemanticAnalysis ASM Interest not registered during Processor Creation",
                                issues.toList(),
                                null
                            )
                        )
                    }
                }
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during semanticAnalysis - ${t::class.simpleName} - ${t.message ?: ""}\n$st"
            sendMessage(port, MessageSemanticAnalysisResult(endPoint, requestId, MessageResponseStatus.FAILURE, msg, emptyList(), null))
        }
    }

    private fun sendLineTokens(
        port: Any,
        endPoint: EndPointIdentity,
        requestId: RequestIdentity,
        languageId: LanguageIdentity,
        sentence: Sentence,
        sppt: SharedPackedParseTree,
        lineTokensChunkSize: Int
    ) {
        try {
            val editorOptions = _editorOptions[endPoint.editorId]
            if (true == editorOptions?.parseLineTokens) {
                val tokens = sppt.tokensByLineAll()
                val style = this._styleHandler[languageId] ?: error("StyleHandler for ${languageId} not found") //TODO: send Error msg not exception
                if (0 < lineTokensChunkSize) {
                    val lineTokensChunked = tokens.chunked(lineTokensChunkSize)
                    var chunkstart = 0
                    for (chunk in lineTokensChunked) {
                        val lineTokens = chunk.mapIndexed { lineNum, leaves ->
                            style.transformToTokens(leaves)
                        }
                        sendMessage(port, MessageLineTokens(endPoint, requestId, MessageResponseStatus.SUCCESS, "Success", chunkstart, lineTokens))
                        chunkstart += chunk.size
                    }
                } else {
                    val lineTokens = tokens.mapIndexed { lineNum, leaves ->
                        style.transformToTokens(leaves)
                    }
                    sendMessage(port, MessageLineTokens(endPoint, requestId, MessageResponseStatus.SUCCESS, "Success", 0, lineTokens))
                }
            } else {
                sendMessage(port, MessageLineTokens(endPoint, requestId, MessageResponseStatus.FAILURE, "ParseLineTokens Interest not registered during Processor Creation", -1, emptyList()))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "${t.message}\n$st"
            sendMessage(port, MessageLineTokens(endPoint, requestId, MessageResponseStatus.FAILURE, msg, -1, emptyList()))
        }
    }

    private fun grammarAmbiguityAnalysis(port: Any, message: MessageGrammarAmbiguityAnalysisRequest) {
        try {
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.endPoint, message.requestId, MessageResponseStatus.RECEIVED, null, emptyList()))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")

            val result = Agl.registry.agl.grammar.processor!!.semanticAnalysis(proc.grammarDomain!!, Agl.options {
                semanticAnalysis {
                    sentenceContext(contextFromGrammarRegistry(Agl.registry))
                    locationMap(proc.syntaxAnalyser!!.locationMap)
                    //context(message.context)
                }
            })
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.endPoint, message.requestId, MessageResponseStatus.SUCCESS, null, result.issues.all.toList()))
        } catch (t: Throwable) {
            val msg = "Exception during ambiguityAnalysis - ${t::class.simpleName} - ${t.message ?: ""}"
            sendMessage(port, MessageGrammarAmbiguityAnalysisResult(message.endPoint, message.requestId, MessageResponseStatus.FAILURE, msg, emptyList()))
        }
    }

    private fun getCodeCompletions(port: Any, message: MessageCodeCompleteRequest<Any, Any>) {
        try {
            sendMessage(port, MessageCodeCompleteResult(message.endPoint, message.requestId, MessageResponseStatus.RECEIVED, "Start", emptyList(), 0, emptyList()))
            val ld = this._languageDefinition[message.languageId] ?: error("LanguageDefinition '${message.languageId}' not found, was it created correctly?")
            val proc = ld.processor ?: error("Processor for '${message.languageId}' not found, is the grammar correctly set ?")
            val result = proc.expectedItemsAt(
                message.text,
                message.position,
                message.options
            )
            sendMessage(port, MessageCodeCompleteResult(message.endPoint, message.requestId, MessageResponseStatus.SUCCESS, "Success", result.issues.all.toList(), result.offset, result.items))
            result.items
        } catch (t: Throwable) {
            val msg = "Exception during 'getCodeCompletions' - ${t::class.simpleName} - ${t.message ?: ""}"
            sendMessage(port, MessageCodeCompleteResult(message.endPoint, message.requestId, MessageResponseStatus.FAILURE, msg, emptyList(), 0, emptyList()))
        }
    }

}