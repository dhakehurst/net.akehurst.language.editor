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
package net.akehurst.language.editor.common

import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.api.*
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.sentence.common.SentenceAbstract
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.style.api.AglStyleModel

class SentenceFromEditor<AsmType : Any, ContextType : Any>(
    val editor: AglEditor<AsmType, ContextType>,
    identity:Any?
) : SentenceAbstract(identity) {
    override val text: String get() = editor.text
    override var eolPositions: List<Int> = emptyList()//ScannerOnDemand.eolPositions(text)

    fun textChanged(newText: String) {
        eolPositions = SentenceDefault.eolPositions(newText)
    }
}

abstract class AglEditorAbstract<AsmType : Any, ContextType : Any, EditorStyleType : Any>(
    val languageServiceRequest: LanguageServiceRequest,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    override val endPointIdentity: EndPointIdentity,
    override var editorOptions: EditorOptions,
    logFunction: LogFunction?,
    styleHandler: AglStyleHandler<EditorStyleType>
) : AglEditor<AsmType, ContextType>, LanguageServiceResponse {

    abstract val isConnected: Boolean

    final override val logger by lazy { AglEditorLogger(endPointIdentity.editorId, logFunction) }

    val editorId get() = endPointIdentity.editorId

    //protected val agl = AglComponents<AsmType, ContextType>(languageId, editorId, logger, styleHandler)
    protected val agl = AglComponents<AsmType, ContextType>(languageDefinition, editorId, logger, styleHandler)
    val nextRequestId get() = RequestIdentity(_nextRequestId)

    abstract val workerTokenizer: AglTokenizerByWorker<EditorStyleType>
    abstract val completionProvider: AglEditorCompletionProvider

    init {
        this.agl.options = processOptions
        //this.agl.languageDefinition.processorObservers.add { _, _ -> this.updateProcessor(); this.updateStyle() }
        this.agl.languageDefinition.grammarStrObservers.add { _, _ -> this.refreshProcessor(); this.refreshStyleHandler() }
        this.agl.languageDefinition.typeModelStrObservers.add { _, _ -> this.refreshProcessor(); this.refreshStyleHandler() }
        this.agl.languageDefinition.asmTransformStrObservers.add { _, _ -> this.refreshProcessor(); this.refreshStyleHandler() }
        this.agl.languageDefinition.crossReferenceStrObservers.add { _, _ -> this.refreshProcessor(); this.refreshStyleHandler() }
        this.agl.languageDefinition.styleStrObservers.add { _, _ -> this.refreshStyleHandler() }
        //this.agl.languageDefinition.formatterStrObservers.add { _, _ -> }
    }

    private val _onTextChange = mutableListOf<(String) -> Unit>()
    private val _onIssues = mutableListOf<(List<LanguageIssue>) -> Unit>()
    private val _onParseHandler = mutableListOf<(ParseEvent) -> Unit>()
    private val _onSyntaxAnalysisHandler = mutableListOf<(SyntaxAnalysisEvent) -> Unit>()
    private val _onSemanticAnalysisHandler = mutableListOf<(SemanticAnalysisEvent) -> Unit>()
    private var _editorSpecificStyleStr: StyleString? = null
    private var _nextRequestId: Int = 0

//    override var sentence = SentenceFromEditor(this)

    override val issues = IssueHolder(LanguageProcessorPhase.ALL)

    override val languageIdentity: LanguageIdentity
        get() = this.agl.languageIdentity

    /*    set(value) {
            val oldId = this.agl.languageIdentity
            if (oldId == value) {
                //same, no need to update
            } else {
                this.agl.languageIdentity = value
                this.updateLanguage(oldId)
                this.refreshProcessor()
                this.refreshStyleHandler()
            }
        }
*/
    override val languageDefinition: LanguageDefinition<AsmType, ContextType>
        get() = agl.languageDefinition

    override var editorSpecificStyleStr: StyleString?
        get() = this._editorSpecificStyleStr ?: this.agl.languageDefinition.styleString
        set(value) {
            this._editorSpecificStyleStr = value
            this.refreshStyleHandler()
        }

    override val styleHandler: AglStyleHandler<EditorStyleType> get() = agl.styleHandler as AglStyleHandler<EditorStyleType>

    override var processOptions: () -> ProcessOptions<AsmType, ContextType>
        get() = this.agl.options
        set(value) {
            this.agl.options = value
        }

    override var doUpdate: Boolean = true

    protected open fun onEditorTextChangeInternal(newText: String) {
        //if (doUpdate) {
       //     this.sentence.textChanged(newText)
        //}
        this.notifyTextChange()
    }

    override fun onTextChange(handler: (String) -> Unit) {
        this._onTextChange.add(handler)
    }

    override fun onIssues(handler: (List<LanguageIssue>) -> Unit) {
        this._onIssues.add(handler)
    }

    override fun onParse(handler: (ParseEvent) -> Unit) {
        this._onParseHandler.add(handler)
    }

    override fun onSyntaxAnalysis(handler: (SyntaxAnalysisEvent) -> Unit) {
        this._onSyntaxAnalysisHandler.add(handler)
    }

    override fun onSemanticAnalysis(handler: (SemanticAnalysisEvent) -> Unit) {
        this._onSemanticAnalysisHandler.add(handler)
    }

    protected fun notifyTextChange() {
        this._onTextChange.forEach {
            it.invoke(text)
        }
    }

    protected fun notifyParse(event: ParseEvent) {
        this._onParseHandler.forEach {
            it.invoke(event)
        }
    }

    protected fun notifySyntaxAnalysis(event: SyntaxAnalysisEvent) {
        this._onSyntaxAnalysisHandler.forEach {
            it.invoke(event)
        }
    }

    protected fun notifySemanticAnalysis(event: SemanticAnalysisEvent) {
        this._onSemanticAnalysisHandler.forEach {
            it.invoke(event)
        }
    }

    protected abstract fun resetTokenization(fromLine: Int)
    protected abstract fun createIssueMarkers(issues: List<LanguageIssue>)
    protected abstract fun updateLanguage(oldId: LanguageIdentity?) //TODO: maybe not needed
    protected abstract fun updateEditorStyles()

    protected open fun updateStyleModel(styleModel: AglStyleModel) {
        this.agl.styleHandler.updateStyleModel(styleModel)
    }

    override fun updateLanguageDefinitionWith(
        grammarStr: GrammarString?,
        typeModelStr: TypesString?,
        asmTransformStr: TransformString?,
        crossReferenceStr: CrossReferenceString?,
        styleStr: StyleString?
    ) {
        this.agl.languageDefinition.update(grammarStr, typeModelStr, asmTransformStr, crossReferenceStr, styleStr)
        this.refreshProcessor()
        this.refreshStyleHandler()
    }

    override fun updateLanguageDefinition(languageDefinition: LanguageDefinition<AsmType, ContextType>) {
        logger.logTrace { "updateLanguageDefinition" }
        this.agl.languageDefinition = languageDefinition
        if (this.isConnected) {
            clearIssues()
            val grammarStr = this.agl.languageDefinition.grammarString
            if (grammarStr?.value.isNullOrBlank()) {
                //do nothing
            } else {
                this.languageServiceRequest.processorCreateRequest(
                    this.endPointIdentity, nextRequestId, this.languageIdentity,
                    grammarStr!!,
                    this.agl.languageDefinition.typesString,
                    this.agl.languageDefinition.transformString,
                    this.agl.languageDefinition.crossReferenceString,
                    this.editorOptions
                )
            }
            val styleStr = this.editorSpecificStyleStr
            if (!styleStr?.value.isNullOrEmpty()) {
                this.agl.styleHandler.reset()
                this.languageServiceRequest.processorSetStyleRequest(this.endPointIdentity, nextRequestId, this.languageIdentity, styleStr!!)
            }
            this.workerTokenizer.reset()
            this.resetTokenization(0)
        }
    }

    override fun refreshProcessor() {
        logger.logTrace { "refreshProcessor" }
        clearIssues()
        val grammarStr = this.agl.languageDefinition.grammarString
        if (grammarStr?.value.isNullOrBlank()) {
            //do nothing
        } else {
            this.languageServiceRequest.processorCreateRequest(
                this.endPointIdentity, nextRequestId, this.languageIdentity,
                grammarStr!!,
                this.agl.languageDefinition.typesString,
                this.agl.languageDefinition.transformString,
                this.agl.languageDefinition.crossReferenceString,
                this.editorOptions
            )
            this.workerTokenizer.reset()
            this.resetTokenization(0) //new processor so find new tokens, first by scan
        }
    }

    override fun refreshStyleHandler() {
        logger.logTrace { "refreshStyleHandler" }
        clearIssues()
        if (this.isConnected) {
            val styleStr = this.editorSpecificStyleStr
            if (!styleStr?.value.isNullOrEmpty()) {
                this.agl.styleHandler.reset()
                this.languageServiceRequest.processorSetStyleRequest(this.endPointIdentity, nextRequestId, this.languageIdentity, styleStr!!)
            }
        }
    }

    override fun processSentence(text: String) {
        logger.logTrace { "processSentence" }
        if (doUpdate) {
            clearIssues()
            this.languageServiceRequest.interruptRequest(this.endPointIdentity, nextRequestId, this.languageIdentity, "process Sentence")
            this.languageServiceRequest.sentenceProcessRequest(this.endPointIdentity, nextRequestId, this.languageIdentity, text, this.agl.options.invoke())
        }
    }

    ///
    override fun processorCreateResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        scannerMatchables: List<Matchable>
    ) {
        logger.logTrace { "processorCreateResponse $endPointIdentity, $requestId, $status, $message, $issues, $scannerMatchables" }
        receiveIssues(issues)
        if (status == MessageResponseStatus.SUCCESS) {
            when (message) {
                "OK" -> {
                    logger.logDebug { "New Processor created for ${editorId}" }
//                    this.workerTokenizer.acceptingTokens = true
                    this.agl.scannerMatchables = scannerMatchables
                    this.processSentence(this.text)
                    this.resetTokenization(0)
                }

                "reset" -> {
                    logger.logDebug { "Reset Processor for ${editorId}" }
                }

                else -> {
                    logger.logError { "Unknown result message from create Processor for ${editorId}: $message" }
                }
            }
        } else {
            logger.logError { "Failed to create processor ${message}" }
            issues.forEach {
                logger.logError { " Issue - ${it}" }
            }
        }
    }

    override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String) {
        logger.logTrace { "processorDeleteResponse $endPointIdentity, $requestId, $status, $message " }
        TODO("not implemented")
    }

    override fun processorSetStyleResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        styleModel: AglStyleModel?
    ) {
        logger.logTrace { "processorSetStyleResponse $endPointIdentity, $requestId, $status, $message " }
        receiveIssues(issues)
        if (status == MessageResponseStatus.SUCCESS && null != styleModel) {
            this.updateStyleModel(styleModel)
            this.updateEditorStyles()
            this.resetTokenization(0)
        } else {
            logger.logError { message }
            issues.forEach {
                logger.logError { it.toString() }
            }
        }
    }

    override fun sentenceLineTokensResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageResponseStatus,
        message: String,
        startLine: Int,
        lineTokens: List<List<AglToken>>
    ) {
        logger.logTrace { "sentenceLineTokensResponse $endPointIdentity, $requestId, $status, $message, $startLine, $lineTokens" }
        if (status == MessageResponseStatus.SUCCESS) {
            logger.logDebug { "Debug: new line tokens from successful parse of ${editorId}" }
            this.workerTokenizer.receiveTokens(startLine, lineTokens as List<List<AglToken>>)
            this.resetTokenization(startLine)
        } else {
            logger.logError { "LineTokens - ${message}" }
        }
    }

    override fun sentenceScanResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>) {
        logger.logTrace { "sentenceScanResponse $endPointIdentity, $requestId, $status, $message, $issues" }
        receiveIssues(issues.toList())
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        logger.logTrace { "sentenceParseResponse $endPointIdentity, $requestId, $status, $message, $issues, <tree>" }
        receiveIssues(issues.toList())
        when (status) {
            MessageResponseStatus.RECEIVED -> {
                this.notifyParse(ParseEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageResponseStatus.IGNORED -> Unit
            MessageResponseStatus.FAILURE -> {
                // a failure to parse is not an 'error' in the editor - we expect some parse failures
                logger.logDebug { "Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: ${message}" }
                // parse failed so clear tokens, forcing re-tokenize from scan
                this.workerTokenizer.reset()
                this.resetTokenization(0)
                this.notifyParse(ParseEvent(EventStatus.FAILURE, message, null, issues.toList()))
            }

            MessageResponseStatus.SUCCESS -> {
                this.notifyParse(ParseEvent(EventStatus.SUCCESS, "Success", tree, issues.toList()))
            }
        }
    }

    override fun sentenceSyntaxAnalysisResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        asm: Any?
    ) {
        logger.logTrace { "sentenceSyntaxAnalysisResponse $endPointIdentity, $requestId, $status, $message, $issues, <asm>" }
        this.receiveIssues(issues.toList())
        when (status) {
            MessageResponseStatus.RECEIVED -> this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.START, message, null, emptyList()))
            MessageResponseStatus.IGNORED -> this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.IGNORED, message, asm, issues.toList()))
            MessageResponseStatus.FAILURE -> this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.FAILURE, message, asm, issues.toList()))
            MessageResponseStatus.SUCCESS -> this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.SUCCESS, message, asm, issues.toList()))
        }
    }

    override fun sentenceSemanticAnalysisResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        asm: Any?
    ) {
        logger.logTrace { "sentenceSemanticAnalysisResponse $endPointIdentity, $requestId, $status, $message, $issues, <asm>" }
        this.receiveIssues(issues.toList())
        when (status) {
            MessageResponseStatus.RECEIVED -> this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.START, message, null, emptyList()))
            MessageResponseStatus.IGNORED -> this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.IGNORED, message, asm, issues.toList()))
            MessageResponseStatus.FAILURE -> this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.FAILURE, message, asm, issues.toList()))
            MessageResponseStatus.SUCCESS -> this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.SUCCESS, message, asm, issues.toList()))
        }
    }

    override fun sentenceCodeCompleteResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        completionItems: List<CompletionItem>
    ) {
        logger.logTrace { "sentenceCodeCompleteResponse $endPointIdentity, $requestId, $status, $message, $issues, $completionItems" }
        when (status) {
            MessageResponseStatus.RECEIVED -> logger.logTrace { "CodeCompletion RECEIVED" }
            MessageResponseStatus.IGNORED -> {
                logger.logTrace { "CodeCompletion IGNORED" };
                this.completionProvider.provide(emptyList())
            }

            MessageResponseStatus.SUCCESS -> {
                this.completionProvider.provide(completionItems)
            }

            MessageResponseStatus.FAILURE -> {
                logger.logError { "CodeCompletion FAILURE: $message" };
                this.completionProvider.provide(emptyList())
            }
        }
    }

    private fun clearIssues() {
        this.issues.clear()
        this.clearIssueMarkers()
        _onIssues.forEach { it.invoke(emptyList()) }
    }

    private fun receiveIssues(issues: List<LanguageIssue>) {
        this.issues.addAll(issues)
        createIssueMarkers(issues)
        _onIssues.forEach { it.invoke(issues) }
    }

}