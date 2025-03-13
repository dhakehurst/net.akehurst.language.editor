/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.language.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.akehurst.language.agl.*
import net.akehurst.language.agl.processor.SyntaxAnalysisResultDefault
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.agl.simple.ContextFromGrammarAndTypeModel
import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglStyleHandlerCssClass
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.grammar.processor.ContextFromGrammarRegistry
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.parser.api.ParseResult
import net.akehurst.language.parser.leftcorner.ParseResultDefault
import net.akehurst.language.reference.asm.CrossReferenceModelDefault
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.scanner.api.ScanResult
import net.akehurst.language.scanner.common.ScanResultDefault
import net.akehurst.language.sentence.api.InputLocation
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.sppt.api.LeafData
import net.akehurst.language.sppt.api.SharedPackedParseTree
import net.akehurst.language.style.api.AglStyleModel
import net.akehurst.language.transform.asm.TransformDomainDefault
import net.akehurst.language.typemodel.asm.TypeModelSimple

class LanguageServiceDirectExecution(
    logFunction: LogFunction?
) : LanguageService {
    val logger = AglEditorLogger("LanguageServiceDirectExecution", logFunction)
    val response = LanguageServiceResponseDirectExecution(logFunction)
    override val request: LanguageServiceRequest = LanguageServiceRequestDirectExecution(response, logFunction)

    override fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse) {
        this.response.responseObjects[endPointIdentity] = response
    }
}

class LanguageServiceResponseDirectExecution(
    logFunction: LogFunction?
) : LanguageServiceResponse {
    val logger = AglEditorLogger("LanguageServiceResponseDirectExecution", logFunction)
    val responseObjects = mutableMapOf<EndPointIdentity, LanguageServiceResponse>()

    override fun processorCreateResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageStatus,
        message: String,
        issues: List<LanguageIssue>,
        scannerMatchables: List<Matchable>
    ) {
        logger.logTrace { "processorCreateResponse $endPointIdentity, $requestId, $status, $message, $issues, $scannerMatchables" }
        responseObjects[endPointIdentity]?.processorCreateResponse(endPointIdentity, requestId, status, message, issues, scannerMatchables)
    }

    override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String) {
        logger.logTrace { "processorDeleteResponse  $endPointIdentity, $requestId, $status, $message" }
        responseObjects[endPointIdentity]?.processorDeleteResponse(endPointIdentity, requestId, status, message)
    }

    override fun processorSetStyleResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageStatus,
        message: String,
        issues: List<LanguageIssue>,
        styleModel: AglStyleModel?
    ) {
        logger.logTrace { "processorSetStyleResponse $endPointIdentity, $requestId, $status, $message, $issues, ${styleModel?.asString()}" }
        responseObjects[endPointIdentity]?.processorSetStyleResponse(endPointIdentity, requestId, status, message, issues, styleModel)
    }

    override fun sentenceScanResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>) {
        logger.logTrace { "sentenceParseResponse $endPointIdentity, $requestId, $status, $message, $issues, <tree> " }
        responseObjects[endPointIdentity]?.sentenceScanResponse(endPointIdentity, requestId, status, message, issues)
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        logger.logTrace { "sentenceParseResponse $endPointIdentity, $requestId, $status, $message, $issues, <tree> " }
        responseObjects[endPointIdentity]?.sentenceParseResponse(endPointIdentity, requestId, status, message, issues, tree)
    }

    override fun sentenceLineTokensResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageStatus,
        message: String,
        startLine: Int,
        lineTokens: List<List<AglToken>>
    ) {
        logger.logTrace { "sentenceLineTokensResponse $endPointIdentity, $requestId, $status, $message, $startLine, $lineTokens" }
        responseObjects[endPointIdentity]?.sentenceLineTokensResponse(endPointIdentity, requestId, status, message, startLine, lineTokens)
    }

    override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
        logger.logTrace { "sentenceSyntaxAnalysisResponse $endPointIdentity, $requestId, $status, $message, $issues, <asm>" }
        responseObjects[endPointIdentity]?.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, status, message, issues, asm)
    }

    override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
        logger.logTrace { "sentenceSemanticAnalysisResponse $endPointIdentity, $requestId, $status, $message, $issues, <asm>" }
        responseObjects[endPointIdentity]?.sentenceSemanticAnalysisResponse(endPointIdentity, requestId, status, message, issues, asm)
    }

    override fun sentenceCodeCompleteResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageStatus,
        message: String,
        issues: List<LanguageIssue>,
        completionItems: List<CompletionItem>
    ) {
        logger.logTrace { "sentenceCodeCompleteResponse $endPointIdentity, $requestId, $status, $message, $issues, $completionItems" }
        responseObjects[endPointIdentity]?.sentenceCodeCompleteResponse(endPointIdentity, requestId, status, message, issues, completionItems)
    }
}

open class LanguageServiceRequestDirectExecution(
    val response: LanguageServiceResponse,
    logFunction: LogFunction?
) : LanguageServiceRequest {
    val logger = AglEditorLogger("LanguageServiceRequestDirectExecution", logFunction)

    // --- LanguageServiceRequest ---
    override fun processorCreateRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        grammarStr: GrammarString,
        typeModelStr: TypesString?,
        asmTransformStr: TransformString?,
        crossReferenceModelStr: CrossReferenceString?,
        editorOptions: EditorOptions //TODO: make part of a different call
    ) {
        logger.logTrace { "processorCreateRequest $endPointIdentity, $languageId" }
        try {
            if (grammarStr.value.isBlank()) {
                response.processorCreateResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "Cannot createProcessor if there is no grammar", emptyList(), emptyList())
            } else {
                val ld = createLanguageDefinition(languageId, grammarStr, typeModelStr, asmTransformStr, crossReferenceModelStr)
                _languageDefinition[languageId] = ld
                _editorOptions[endPointIdentity] = editorOptions
                //if there is a grammar check that grammar is well-defined and a processor can be created from it

                try {
                    val proc = ld.processor // should throw exception if there are problems
                    if (null == proc) {
                        response.processorCreateResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "Error", ld.issues.all.toList(), emptyList())
                    } else {
                        response.processorCreateResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "OK", ld.issues.all.toList(), proc.scanner!!.matchables)
                    }
                } catch (t: Throwable) {
                    println(t.stackTraceToString())
                    response.processorCreateResponse(endPointIdentity, requestId, MessageStatus.FAILURE, t.message ?: "", ld.issues.all.toList(), emptyList())
                }
            }
        } catch (t: Throwable) {
            println(t.stackTraceToString())
            response.processorCreateResponse(endPointIdentity, requestId, MessageStatus.FAILURE, t.message ?: "", emptyList(), emptyList())
        }
    }

    override fun processorDeleteRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity) {
        logger.logTrace { "processorDeleteRequest $endPointIdentity, $languageId" }
        //TODO("not implemented")
    }

    override fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity, styleStr: StyleString) {
        logger.logTrace { "processorSetStyleRequest $endPointIdentity, $languageId" }
        try {
            val styleHndlr = AglStyleHandlerCssClass(languageId)
            this._styleHandler[endPointIdentity] = styleHndlr
            val result = Agl.registry.agl.style.processor!!.process(styleStr.value)
            val styleMdl = result.asm
            if (null != styleMdl) {
                styleHndlr.updateStyleModel(styleMdl)
                response.processorSetStyleResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "OK", result.issues.all.toList(), styleMdl)
            } else {
                response.processorSetStyleResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "Error in style string", result.issues.all.toList(), null)
            }
        } catch (t: Throwable) {
            response.processorSetStyleResponse(endPointIdentity, requestId, MessageStatus.FAILURE, t.message ?: "Thrown exception: ${t::class.simpleName}", emptyList(), null)
        }
    }

    override fun interruptRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity, reason: String) {
        logger.logTrace { "interruptRequest $endPointIdentity, $languageId" }
        _languageDefinition[languageId]?.processor?.interrupt(reason)
    }

    override fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        sentence: String,
        processOptions: ProcessOptions<AsmType, ContextType>
    ) {
        logger.logTrace { "sentenceProcessRequest $endPointIdentity, $languageId" }
        val ld = this._languageDefinition[languageId] ?: error("LanguageDefinition '${languageId}' not found, was it created correctly?")
        val proc = ld.processor as LanguageProcessor<AsmType, ContextType>? ?: error("Processor for '${languageId}' not found, is the grammar correctly set ?")
        val scan = scan(endPointIdentity, requestId, languageId, proc, processOptions, sentence)
        val parse = parse(endPointIdentity, requestId, languageId, proc, processOptions, sentence)
        val syntaxAnalysis = parse.sppt?.let { this.syntaxAnalysis(endPointIdentity, requestId, languageId, proc, processOptions, it) }
        val semanticAnalysis = syntaxAnalysis?.let { r -> r.asm?.let { this.semanticAnalysis(endPointIdentity, requestId, languageId, proc, processOptions, it, r.locationMap) } }
    }

    override fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        sentence: String,
        position: Int,
        processOptions: ProcessOptions<AsmType, ContextType>
    ) {
        logger.logTrace { "sentenceCodeCompleteRequest $endPointIdentity, $languageId" }
        try {
            val ld = this._languageDefinition[languageId] ?: error("LanguageDefinition '${languageId}' not found, was it created correctly?")
            val proc = ld.processor as LanguageProcessor<AsmType, ContextType>? ?: error("Processor for '${languageId}' not found, is the grammar correctly set ?")
            val result = proc.expectedItemsAt(sentence, position, processOptions)
            response.sentenceCodeCompleteResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "OK", result.issues.all.toList(), result.items)
        } catch (t: Throwable) {
            response.sentenceCodeCompleteResponse(endPointIdentity, requestId, MessageStatus.FAILURE, t.message ?: "Thrown exception: ${t::class.simpleName}", emptyList(), emptyList())
        }
    }

    // --- Implementation ---
    protected open fun configureLanguageDefinition(
        ld: LanguageDefinition<Any, Any>,
        grammarStr: GrammarString?,
        typeModelStr: TypesString?,
        asmTransformStr: TransformString?,
        crossReferenceModelStr: CrossReferenceString?
    ) {
        logger.logTrace { "configureLanguageDefinition ${ld.identity}" }
        // TODO: could be an argument
        ld.configuration = Agl.configuration<Any, Any>(Agl.configurationSimple() as LanguageProcessorConfiguration<Any, Any>) {
            grammarString(grammarStr)
            typesString(typeModelStr)
            transformString(asmTransformStr)
            crossReferenceString(crossReferenceModelStr)
            //if (null != styleStr) {
            //     styleResolver { p:LanguageProcessor<Asm, ContextAsmSimple> -> AglStyleModelDefault.fromString(ContextFromGrammar.createContextFrom(p.grammarModel!!), styleStr) }
            // }
            // if (null != formatterModelStr) {
            //     formatterResolver { p:LanguageProcessor<Asm, ContextAsmSimple> -> AglFormatterModelFromAsm.fromString(ContextFromTypeModel(p.typeModel), formatterModelStr) }
            // }
        }
        //ld.update(grammarStr, typeModelStr, asmTransformStr, crossReferenceModelStr, ld.styleString)
    }

    protected open fun createLanguageDefinition(
        languageId: LanguageIdentity,
        grammarStr: GrammarString?,
        typeModelStr: TypesString?,
        asmTransformStr: TransformString?,
        crossReferenceModelStr: CrossReferenceString?
    ): LanguageDefinition<Any, Any> {
        logger.logTrace { "createLanguageDefinition $languageId" }
        val ld = Agl.languageDefinitionFromString(
            identity = languageId,
            grammarDefinitionStr = grammarStr ?: GrammarString(""),
            typeStr =  typeModelStr,
            transformStr = asmTransformStr,
            referenceStr = crossReferenceModelStr,
            grammarAglOptions = Agl.options {
                semanticAnalysis {
                    context(ContextFromGrammarRegistry(Agl.registry))
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            //TODO: how to use configurationDefault ? - needed once completion-provider moved to worker
            configurationBase = Agl.configurationBase() //use if placeholder created, not found
        )
        if (ld.isModifiable) {
            configureLanguageDefinition(ld, grammarStr, typeModelStr, asmTransformStr, crossReferenceModelStr)
        }
        return ld
    }

    protected fun <AsmType : Any, ContextType : Any> scan(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        processOptions: ProcessOptions<AsmType, ContextType>,
        sentence: String
    ): ScanResult {
        logger.logTrace { "parse $endPointIdentity, $languageId" }
        return try {
            response.sentenceScanResponse(endPointIdentity, requestId, MessageStatus.START, "Start", emptyList())
            val editorOptions = _editorOptions[endPointIdentity]
            if (true == editorOptions?.scan && processOptions.scan.enabled) {
                val result = proc.scan(sentence, processOptions.scan)
                this.sendLineTokens(endPointIdentity, requestId, languageId, result.tokensByLine, editorOptions.lineTokensChunkSize)
                response.sentenceScanResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Success", result.issues.all.toList())
                result
            } else {
                response.sentenceParseResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Scan not enabled, in editor and process options", emptyList(), null)
                ScanResultDefault(emptyList(), IssueHolder(LanguageProcessorPhase.SCAN))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during 'scan' - ${t::class.simpleName} - ${t.message ?: ""}\n$st"
            response.sentenceScanResponse(endPointIdentity, requestId, MessageStatus.FAILURE, msg, emptyList())
            ScanResultDefault(emptyList(), IssueHolder(LanguageProcessorPhase.SCAN))
        }
    }

    protected fun <AsmType : Any, ContextType : Any> parse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        processOptions: ProcessOptions<AsmType, ContextType>,
        sentence: String
    ): ParseResult {
        logger.logTrace { "parse $endPointIdentity, $languageId" }
        return try {
            response.sentenceParseResponse(endPointIdentity, requestId, MessageStatus.START, "Start", emptyList(), null)
            val editorOptions = _editorOptions[endPointIdentity]
            if (true == editorOptions?.parse && processOptions.parse.enabled) {
                val result = proc.parse(sentence, processOptions.parse)
                val sppt = result.sppt
                if (null == sppt) {
                    response.sentenceParseResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "Parse Failed", result.issues.all.toList(), null)
                } else {
                    val tokens = sppt.tokensByLineAll()
                    this.sendLineTokens(endPointIdentity, requestId, languageId, tokens, editorOptions.lineTokensChunkSize)
                    if (editorOptions.parseTree) {
                        response.sentenceParseResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), sppt)
                    } else {
                        response.sentenceParseResponse(
                            endPointIdentity,
                            requestId,
                            MessageStatus.SUCCESS,
                            "ParseTree Interest not registered during Processor Creation",
                            result.issues.all.toList(),
                            null
                        )
                    }
                }
                result
            } else {
                response.sentenceParseResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "Parse Interest not registered during Processor Creation", emptyList(), null)
                ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message ?: ""}\n$st"
            response.sentenceParseResponse(endPointIdentity, requestId, MessageStatus.FAILURE, msg, emptyList(), null)
            ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
        }
    }

    private fun sendLineTokens(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        tokens: List<List<LeafData>>,
        lineTokensChunkSize: Int
    ) {
        logger.logTrace { "sendLineTokens $endPointIdentity, $languageId" }
        try {
            val editorOptions = _editorOptions[endPointIdentity]
            if (true == editorOptions?.parseLineTokens) {
                val style = this._styleHandler[endPointIdentity]
                if (null == style) {
                    val msg = "StyleHandler for ${languageId} not found"
                    response.sentenceLineTokensResponse(endPointIdentity, requestId, MessageStatus.FAILURE, msg, -1, emptyList())
                } else {
                    if (0 < lineTokensChunkSize) {
                        val lineTokensChunked = tokens.chunked(lineTokensChunkSize)
                        var chunkstart = 0
                        for (chunk in lineTokensChunked) {
                            val lineTokens = chunk.mapIndexed { lineNum, leaves ->
                                style.transformToTokens(leaves)
                            }
                            response.sentenceLineTokensResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Success", chunkstart, lineTokens)
                            chunkstart += chunk.size
                        }
                    } else {
                        val lineTokens = tokens.mapIndexed { lineNum, leaves ->
                            style.transformToTokens(leaves)
                        }
                        response.sentenceLineTokensResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Success", 0, lineTokens)
                    }
                }
            } else {
                response.sentenceLineTokensResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "ParseLineTokens Interest not registered during Processor Creation", -1, emptyList())
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "${t.message}\n$st"
            response.sentenceLineTokensResponse(endPointIdentity, requestId, MessageStatus.FAILURE, msg, -1, emptyList())
        }
    }

    private fun sendLineTokens() {

    }

    private fun <AsmType : Any, ContextType : Any> syntaxAnalysis(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        options: ProcessOptions<AsmType, ContextType>,
        sppt: SharedPackedParseTree
    ): SyntaxAnalysisResult<AsmType> {
        logger.logTrace { "syntaxAnalysis $endPointIdentity, $languageId" }
        return try {
            response.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, MessageStatus.START, "Start", emptyList(), null)
            val editorOptions = _editorOptions[endPointIdentity]
            if (true == editorOptions?.syntaxAnalysis && options.syntaxAnalysis.enabled) {
                val result = proc.syntaxAnalysis(sppt, options)
                val asm = result.asm
                if (null == asm) {
                    response.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "SyntaxAnalysis Failed", result.issues.all.toList(), null)
                } else {
                    if (editorOptions.syntaxAnalysisAsm) {
                        response.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm)
                    } else {
                        response.sentenceSyntaxAnalysisResponse(
                            endPointIdentity,
                            requestId, MessageStatus.SUCCESS,
                            "SyntaxAnalysis ASM Interest not registered during Processor Creation",
                            result.issues.all.toList(),
                            null
                        )
                    }
                }
                result
            } else {
                response.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "SyntaxAnalysis Interest not enabled, in editor and process options", emptyList(), null)
                SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), emptyMap())
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during syntaxAnalysis - ${t::class.simpleName} - ${t.message ?: ""}\n$st"
            response.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, MessageStatus.FAILURE, msg, emptyList(), null)
            SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), emptyMap())
        }
    }

    private fun <AsmType : Any, ContextType : Any> semanticAnalysis(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        options: ProcessOptions<AsmType, ContextType>,
        asm: AsmType,
        locationMap: Map<Any, InputLocation>
    ) {
        logger.logTrace { "semanticAnalysis $endPointIdentity, $languageId" }
        try {
            response.sentenceSemanticAnalysisResponse(endPointIdentity, requestId, MessageStatus.START, "Start", emptyList(), null)
            val editorOptions = _editorOptions[endPointIdentity]
            if (true == editorOptions?.semanticAnalysis && options.semanticAnalysis.enabled) {
                // to save time serialisating/deserialising contexts that are based on information already in the worker
                // when (language) {
                //  is Agl Grammar -> create ContextFromGrammarRegistry
                //  is Agl CrossReferences -> context should be a reference to a diff LanguageDefinition, get its typemodel and create ContextFromTypeModel
                // }
                val ctx = when (languageId) {
                    Agl.registry.agl.grammar.identity -> options.semanticAnalysis.context ?: ContextFromGrammarRegistry(Agl.registry)
                    Agl.registry.agl.crossReference.identity -> when (options.semanticAnalysis.context) {
                        is ContextFromTypeModelReference -> {
                            val langId = LanguageIdentity((options.semanticAnalysis.context as ContextFromTypeModelReference).languageDefinitionId.value)
                            val ld = _languageDefinition[langId] ?: error("Language '$langId' not defined in worker")
                            val tm = TransformDomainDefault.fromGrammarModel(ld.grammarModel!!).asm!!.typeModel!!
                            ContextFromTypeModel(tm)
                        }

                        else -> options.semanticAnalysis.context
                    }

                    else -> options.semanticAnalysis.context
                }
                val opts = Agl.options(options) {
                    semanticAnalysis {
                        locationMap(locationMap)
                        ctx?.let { context(it as ContextType) }
                        option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false) //FIXME: not sure we should override this here!
                    }
                }
                val result = proc.semanticAnalysis(asm, opts)
                if (editorOptions.semanticAnalysisAsm) {
                    response.sentenceSemanticAnalysisResponse(endPointIdentity, requestId, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm)
                } else {
                    response.sentenceSemanticAnalysisResponse(
                        endPointIdentity,
                        requestId, MessageStatus.SUCCESS,
                        "SemanticAnalysis ASM Interest not registered during Processor Creation",
                        result.issues.all.toList(),
                        null
                    )
                }
            } else {
                response.sentenceSemanticAnalysisResponse(endPointIdentity, requestId, MessageStatus.FAILURE, "SemanticAnalysis not enabled, in editor and process options", emptyList(), null)
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during semanticAnalysis - ${t::class.simpleName} - ${t.message ?: "null"}\n$st"
            response.sentenceSemanticAnalysisResponse(endPointIdentity, requestId, MessageStatus.FAILURE, msg, emptyList(), null)
        }
    }


    // languageId -> def
    private var _languageDefinition: MutableMap<LanguageIdentity, LanguageDefinition<Any, Any>> = mutableMapOf()

    // languageId -> sh
    private var _styleHandler: MutableMap<EndPointIdentity, AglStyleHandlerCssClass> = mutableMapOf()

    // editorId -> options
    private var _editorOptions: MutableMap<EndPointIdentity, EditorOptions> = mutableMapOf()
}