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

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.CrossReferenceString
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.agl.StyleString
import net.akehurst.language.agl.processor.SyntaxAnalysisResultDefault
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglStyleHandler
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.grammar.processor.ContextFromGrammarRegistry
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.parser.api.ParseResult
import net.akehurst.language.parser.leftcorner.ParseResultDefault
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.sentence.api.InputLocation
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.sppt.api.SharedPackedParseTree
import net.akehurst.language.style.api.AglStyleModel
import net.akehurst.language.transform.asm.TransformModelDefault

class LanguageServiceDirectExecution() : LanguageService {
    val response = LanguageServiceResponseDirectExecution()
    override val request: LanguageServiceRequest = LanguageServiceRequestDirectExecution(response)

    override fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse) {
        this.response.responseObjects[endPointIdentity] = response
    }
}

class LanguageServiceResponseDirectExecution() : LanguageServiceResponse {
    val responseObjects = mutableMapOf<EndPointIdentity, LanguageServiceResponse>()

    override fun processorCreateResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>) {
        responseObjects[endPointIdentity]?.processorCreateResponse(endPointIdentity, status, message, issues, scannerMatchables)
    }

    override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String) {
        responseObjects[endPointIdentity]?.processorDeleteResponse(endPointIdentity, status, message)
    }

    override fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleModel?) {
        responseObjects[endPointIdentity]?.processorSetStyleResponse(endPointIdentity, status, message, issues, styleModel)
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        responseObjects[endPointIdentity]?.sentenceParseResponse(endPointIdentity, status, message, issues, tree)
    }

    override fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>) {
        responseObjects[endPointIdentity]?.sentenceLineTokensResponse(endPointIdentity, status, message, startLine, lineTokens)
    }

    override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
        responseObjects[endPointIdentity]?.sentenceSyntaxAnalysisResponse(endPointIdentity, status, message, issues, asm)
    }

    override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
        responseObjects[endPointIdentity]?.sentenceSemanticAnalysisResponse(endPointIdentity, status, message, issues, asm)
    }

    override fun sentenceCodeCompleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems: List<CompletionItem>) {
        responseObjects[endPointIdentity]?.sentenceCodeCompleteResponse(endPointIdentity, status, message, issues, completionItems)
    }
}

open class LanguageServiceRequestDirectExecution(
    val response: LanguageServiceResponse
) : LanguageServiceRequest {

    // --- LanguageServiceRequest ---
    override fun processorCreateRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, grammarStr: GrammarString, crossReferenceModelStr: CrossReferenceString?, editorOptions: EditorOptions) {
        try {
            if (grammarStr.value.isBlank()) {
                response.processorCreateResponse(endPointIdentity, MessageStatus.FAILURE, "Cannot createProcessor if there is no grammar", emptyList(), emptyList())
            } else {
                val ld = createLanguageDefinition(languageId, grammarStr, crossReferenceModelStr)
                _languageDefinition[languageId] = ld
                _editorOptions[endPointIdentity.editorId] = editorOptions
                //if there is a grammar check that grammar is well-defined and a processor can be created from it

                val proc = ld.processor // should throw exception if there are problems
                if (null == proc) {
                    response.processorCreateResponse(endPointIdentity, MessageStatus.FAILURE, "Error", ld.issues.all.toList(), emptyList())
                } else {
                    response.processorCreateResponse(endPointIdentity, MessageStatus.SUCCESS, "OK", ld.issues.all.toList(), proc.scanner!!.matchables)
                }
            }
        } catch (t: Throwable) {
            println(t.stackTraceToString())
            response.processorCreateResponse(endPointIdentity, MessageStatus.FAILURE, t.message?:"", emptyList(), emptyList())
        }
    }

    override fun processorDeleteRequest(endPointIdentity: EndPointIdentity) {
        //TODO("not implemented")
    }

    override fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, styleStr: StyleString) {
        try {
            val style = AglStyleHandler(languageId)
            this._styleHandler[languageId] = style
            val result = Agl.registry.agl.style.processor!!.process(styleStr.value)
            val styleMdl = result.asm
            if (null != styleMdl) {
                styleMdl.allDefinitions.forEach { ss ->
                    ss.rules.forEach { rule ->
                        rule.selector.forEach { sel -> style.mapClass(sel.value) }
                    }
                }
                response.processorSetStyleResponse(endPointIdentity, MessageStatus.SUCCESS, "OK", result.issues.all.toList(), styleMdl)
            } else {
                response.processorSetStyleResponse(endPointIdentity, MessageStatus.FAILURE, "Error in style string", result.issues.all.toList(), null)
            }
        } catch (t: Throwable) {
            response.processorSetStyleResponse(endPointIdentity, MessageStatus.FAILURE, t.message ?: "Thrown exception: ${t::class.simpleName}", emptyList(),null)
        }
    }

    override fun interruptRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, reason: String) {
        _languageDefinition[languageId]?.processor?.interrupt(reason)
    }

    override fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, sentence: String, processOptions: ProcessOptions<AsmType, ContextType>) {
        val ld = this._languageDefinition[languageId] ?: error("LanguageDefinition '${languageId}' not found, was it created correctly?")
        val proc = ld.processor as LanguageProcessor<AsmType, ContextType>? ?: error("Processor for '${languageId}' not found, is the grammar correctly set ?")
        val parse = parse(endPointIdentity, languageId, proc, processOptions, sentence)
        val syntaxAnalysis = parse.sppt?.let { this.syntaxAnalysis(endPointIdentity, proc, processOptions, it) }
        val semanticAnalysis = syntaxAnalysis?.let { r -> r.asm?.let { this.semanticAnalysis(endPointIdentity, languageId, proc, processOptions, it, r.locationMap) } }
    }

    override fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
        endPointIdentity: EndPointIdentity,
        languageId: LanguageIdentity,
        sentence: String,
        position: Int,
        processOptions: ProcessOptions<AsmType, ContextType>
    ) {
        try {
            val ld = this._languageDefinition[languageId] ?: error("LanguageDefinition '${languageId}' not found, was it created correctly?")
            val proc = ld.processor as LanguageProcessor<AsmType, ContextType>? ?: error("Processor for '${languageId}' not found, is the grammar correctly set ?")
            val result = proc.expectedItemsAt(sentence, position, -1, processOptions)
            response.sentenceCodeCompleteResponse(endPointIdentity, MessageStatus.SUCCESS, "OK", result.issues.all.toList(), result.items)
        } catch (t:Throwable) {
            response.sentenceCodeCompleteResponse(endPointIdentity, MessageStatus.FAILURE, t.message ?: "Thrown exception: ${t::class.simpleName}", emptyList(), emptyList())
        }
    }

    // --- Implementation ---
    protected open fun configureLanguageDefinition(ld: LanguageDefinition<Any, Any>, grammarStr: GrammarString?, crossReferenceModelStr: CrossReferenceString?) {
        // TODO: could be an argument
        ld.configuration = Agl.configurationDefault() as LanguageProcessorConfiguration<Any, Any>
        ld.grammarStr = grammarStr
        ld.crossReferenceModelStr = crossReferenceModelStr
    }

    protected open fun createLanguageDefinition(languageId: LanguageIdentity, grammarStr: GrammarString?, crossReferenceModelStr: CrossReferenceString?): LanguageDefinition<Any, Any> {
        val ld = Agl.registry.findOrPlaceholder<Any, Any>(
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

    protected fun <AsmType : Any, ContextType : Any> parse(
        endPointIdentity: EndPointIdentity,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        processOptions: ProcessOptions<AsmType, ContextType>,
        sentence: String
    ): ParseResult {
        return try {
            response.sentenceParseResponse(endPointIdentity, MessageStatus.START, "Start", emptyList(), null)
            val editorOptions = _editorOptions[endPointIdentity.editorId]
            if (true == editorOptions?.parse) {
                val result = proc.parse(sentence, processOptions.parse)
                val sppt = result.sppt
                if (null == sppt) {
                    response.sentenceParseResponse(endPointIdentity, MessageStatus.FAILURE, "Parse Failed", result.issues.all.toList(), null)
                } else {
                    this.sendLineTokens(endPointIdentity, languageId, SentenceDefault(sentence), sppt, editorOptions.lineTokensChunkSize)
                    if (editorOptions.parseTree) {
                        response.sentenceParseResponse(endPointIdentity, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), sppt)
                    } else {
                        response.sentenceParseResponse(endPointIdentity, MessageStatus.SUCCESS, "ParseTree Interest not registered during Processor Creation", result.issues.all.toList(), null)
                    }
                }
                result
            } else {
                response.sentenceParseResponse(endPointIdentity, MessageStatus.FAILURE, "Parse Interest not registered during Processor Creation", emptyList(), null)
                ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during 'parse' - ${t::class.simpleName} - ${t.message?:""}\n$st"
            response.sentenceParseResponse(endPointIdentity, MessageStatus.FAILURE, msg, emptyList(), null)
            ParseResultDefault(null, IssueHolder(LanguageProcessorPhase.PARSE))
        }
    }

    private fun sendLineTokens(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, sentence: Sentence, sppt: SharedPackedParseTree, lineTokensChunkSize: Int) {
        try {
            val editorOptions = _editorOptions[endPointIdentity.editorId]
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
                        response.sentenceLineTokensResponse(endPointIdentity, MessageStatus.SUCCESS, "Success", chunkstart, lineTokens)
                        chunkstart += chunk.size
                    }
                } else {
                    val lineTokens = tokens.mapIndexed { lineNum, leaves ->
                        style.transformToTokens(leaves)
                    }
                    response.sentenceLineTokensResponse(endPointIdentity, MessageStatus.SUCCESS, "Success", 0, lineTokens)
                }
            } else {
                response.sentenceLineTokensResponse(endPointIdentity, MessageStatus.FAILURE, "ParseLineTokens Interest not registered during Processor Creation", -1, emptyList())
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "${t.message}\n$st"
            response.sentenceLineTokensResponse(endPointIdentity, MessageStatus.FAILURE, msg, -1, emptyList())
        }
    }

    private fun <AsmType : Any, ContextType : Any> syntaxAnalysis(
        endPointIdentity: EndPointIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        options: ProcessOptions<AsmType, ContextType>,
        sppt: SharedPackedParseTree
    ): SyntaxAnalysisResult<AsmType> {
        return try {
            response.sentenceSyntaxAnalysisResponse(endPointIdentity, MessageStatus.START, "Start", emptyList(), null)
            val editorOptions = _editorOptions[endPointIdentity.editorId]
            if (true == editorOptions?.syntaxAnalysis) {
                val result = proc.syntaxAnalysis(sppt, options)
                val asm = result.asm
                if (null == asm) {
                    response.sentenceSyntaxAnalysisResponse(endPointIdentity, MessageStatus.FAILURE, "SyntaxAnalysis Failed", result.issues.all.toList(), null)
                } else {
                    if (editorOptions.syntaxAnalysisAsm) {
                        response.sentenceSyntaxAnalysisResponse(endPointIdentity, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm)
                    } else {
                        response.sentenceSyntaxAnalysisResponse(
                            endPointIdentity,
                            MessageStatus.SUCCESS,
                            "SyntaxAnalysis ASM Interest not registered during Processor Creation",
                            result.issues.all.toList(),
                            null
                        )
                    }
                }
                result
            } else {
                response.sentenceSyntaxAnalysisResponse(endPointIdentity, MessageStatus.FAILURE, "SyntaxAnalysis Interest not registered during Processor Creation", emptyList(), null)
                SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), emptyMap())
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during syntaxAnalysis - ${t::class.simpleName} - ${t.message?:""}\n$st"
            response.sentenceSyntaxAnalysisResponse(endPointIdentity, MessageStatus.FAILURE, msg, emptyList(), null)
            SyntaxAnalysisResultDefault(null, IssueHolder(LanguageProcessorPhase.SYNTAX_ANALYSIS), emptyMap())
        }
    }

    private fun <AsmType : Any, ContextType : Any> semanticAnalysis(
        endPointIdentity: EndPointIdentity,
        languageId: LanguageIdentity,
        proc: LanguageProcessor<AsmType, ContextType>,
        options: ProcessOptions<AsmType, ContextType>,
        asm: AsmType,
        locationMap: Map<Any, InputLocation>
    ) {
        try {
            response.sentenceSemanticAnalysisResponse(endPointIdentity, MessageStatus.START, "Start", emptyList(), null)
            val editorOptions = _editorOptions[endPointIdentity.editorId]
            if (true == editorOptions?.semanticAnalysis) {
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
                            val tm = TransformModelDefault.fromGrammarModel(ld.grammarModel).asm!!.typeModel!!
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
                    response.sentenceSemanticAnalysisResponse(endPointIdentity, MessageStatus.SUCCESS, "Success", result.issues.all.toList(), asm)
                } else {
                    response.sentenceSemanticAnalysisResponse(
                        endPointIdentity,
                        MessageStatus.SUCCESS,
                        "SemanticAnalysis ASM Interest not registered during Processor Creation",
                        result.issues.all.toList(),
                        null
                    )
                }
            } else {
                response.sentenceSemanticAnalysisResponse(endPointIdentity, MessageStatus.FAILURE, "SemanticAnalysis Interest not registered during Processor Creation", emptyList(), null)
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Exception during semanticAnalysis - ${t::class.simpleName} - ${t.message?:"null"}\n$st"
            response.sentenceSemanticAnalysisResponse(endPointIdentity, MessageStatus.FAILURE, msg, emptyList(), null)
        }
    }


    // languageId -> def
    private var _languageDefinition: MutableMap<LanguageIdentity, LanguageDefinition<Any, Any>> = mutableMapOf()

    // languageId -> sh
    private var _styleHandler: MutableMap<LanguageIdentity, AglStyleHandler> = mutableMapOf()

    // editorId -> options
    private var _editorOptions: MutableMap<String, EditorOptions> = mutableMapOf()
}