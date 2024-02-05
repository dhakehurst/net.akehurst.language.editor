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

import net.akehurst.language.agl.agl.parser.SentenceAbstract
import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.agl.scanner.ScannerOnDemand
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.*

class SentenceFromEditor<AsmType : Any, ContextType : Any>(
    val editor: AglEditor<AsmType, ContextType>
) : SentenceAbstract() {
    override val text: String get() = editor.text
    override var eolPositions: List<Int> = emptyList()//ScannerOnDemand.eolPositions(text)

    fun textChanged() {
        eolPositions = ScannerOnDemand.eolPositions(text)
    }
}

abstract class AglEditorAbstract<AsmType : Any, ContextType : Any>(
    val languageServiceRequest: LanguageServiceRequest,
    languageId: String,
    final override val editorId: String,
    logFunction: LogFunction?,
) : AglEditor<AsmType, ContextType>, LanguageServiceResponse {

    abstract val sessionId: String
    abstract val isConnected:Boolean

    final override val logger = AglEditorLogger(logFunction)

    protected val agl = AglComponents<AsmType, ContextType>(languageId, editorId, logger)

    val endPointId: EndPointIdentity = EndPointIdentity(editorId, sessionId)

    abstract val workerTokenizer: AglTokenizerByWorker

    init {
        //this.agl.languageDefinition.processorObservers.add { _, _ -> this.updateProcessor(); this.updateStyle() }
        this.agl.languageDefinition.grammarStrObservers.add { _, _ -> this.updateProcessor(); this.requestUpdateStyleModel() }
        this.agl.languageDefinition.scopeStrObservers.add { _, _ -> this.updateProcessor(); this.requestUpdateStyleModel() }
        this.agl.languageDefinition.styleStrObservers.add { _, _ -> this.requestUpdateStyleModel() }
        //this.agl.languageDefinition.formatterStrObservers.add { _, _ -> }
    }

    private val _onParseHandler = mutableListOf<(ParseEvent) -> Unit>()
    private val _onSyntaxAnalysisHandler = mutableListOf<(SyntaxAnalysisEvent) -> Unit>()
    private val _onSemanticAnalysisHandler = mutableListOf<(SemanticAnalysisEvent) -> Unit>()
    private var _editorSpecificStyleStr: String? = null

    override var sentence = SentenceFromEditor(this)

    override var languageIdentity: String
        get() = this.agl.languageIdentity
        set(value) {
            val oldId = this.agl.languageIdentity
            if (oldId == value) {
                //same, no need to update
            } else {
                this.agl.languageIdentity = value
                this.updateLanguage(oldId)
                this.updateProcessor()
                this.requestUpdateStyleModel()
            }
        }

    override val languageDefinition: LanguageDefinition<AsmType, ContextType>
        get() = agl.languageDefinition

    override var editorSpecificStyleStr: String?
        get() = this._editorSpecificStyleStr ?: this.agl.languageDefinition.styleStr
        set(value) {
            this._editorSpecificStyleStr = value
            this.requestUpdateStyleModel()
        }

    override var processOptions: ProcessOptions<AsmType, ContextType>
        get() = this.agl.options
        set(value) {
            this.agl.options = value
        }

    override var editorOptions: EditorOptions = EditorOptionsDefault()

    override var doUpdate: Boolean = true

    protected open fun onEditorTextChange() {
        this.sentence.textChanged()
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

    protected fun log(level: LogLevel, message: String, t: Throwable?) = this.logger.log(level, message, t)

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
    protected abstract fun updateLanguage(oldId: String?)
    protected abstract fun updateProcessor()
    protected abstract fun updateEditorStyles()

    fun requestUpdateStyleModel() {
        if (this.isConnected) {
            val styleStr = this.editorSpecificStyleStr
            if (!styleStr.isNullOrEmpty()) {
                this.agl.styleHandler.reset()
                this.languageServiceRequest.processorSetStyleRequest(this.endPointId, this.languageIdentity, styleStr)
            }
        }
    }

    fun processSentence() {
        if (doUpdate) {
            this.clearErrorMarkers()
            this.languageServiceRequest.interruptRequest(this.endPointId, this.languageIdentity, "process Sentence")
            this.languageServiceRequest.sentenceProcessRequest(this.endPointId, this.languageIdentity, this.text, this.agl.options)
        }
    }

    ///
    override fun processorCreateResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>) {
        if (status == MessageStatus.SUCCESS) {
            when (message) {
                "OK" -> {
                    this.log(LogLevel.Debug, "New Processor created for ${editorId}", null)
                    this.workerTokenizer.acceptingTokens = true
                    this.agl.scannerMatchables = scannerMatchables
                    this.processSentence()
                    this.resetTokenization(0)
                }

                "reset" -> {
                    this.log(LogLevel.Debug, "Reset Processor for ${editorId}", null)
                }

                else -> {
                    this.log(LogLevel.Error, "Unknown result message from create Processor for ${editorId}: $message", null)
                }
            }
        } else {
            this.log(LogLevel.Error, "Failed to create processor ${message}", null)
            issues.forEach {
                this.log(LogLevel.Error, " Issue - ${it}", null)
            }
        }
    }

    override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String) {
        TODO("not implemented")
    }

    override fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleModel?) {
        if (status == MessageStatus.SUCCESS && null != styleModel) {
            this.updateEditorStyles()
            this.resetTokenization(0)
            this.agl.styleHandler.updateStyleModel(styleModel)
        } else {
            this.log(LogLevel.Error, message, null)
            issues.forEach {
                this.log(LogLevel.Error, it.toString(), null)
            }
        }
    }

    override fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>) {
        if (status == MessageStatus.SUCCESS) {
            this.log(LogLevel.Debug, "Debug: new line tokens from successful parse of ${editorId}", null)
            this.workerTokenizer.receiveTokens(startLine, lineTokens)
            this.resetTokenization(startLine)
        } else {
            this.log(LogLevel.Error, "LineTokens - ${message}", null)
        }
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        when (status) {
            MessageStatus.START -> {
                this.notifyParse(ParseEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageStatus.FAILURE -> {
                // a failure to parse is not an 'error' in the editor - we expect some parse failures
                this.log(LogLevel.Debug, "Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: ${message}", null)
                // parse failed so re-tokenize from scan
//                this.workerTokenizer.reset()
//                this.resetTokenization()
                clearErrorMarkers()
                this.createIssueMarkers(issues.toList())
                this.notifyParse(ParseEvent(EventStatus.FAILURE, message, null, issues.toList()))
            }

            MessageStatus.SUCCESS -> {
                clearErrorMarkers()
                this.createIssueMarkers(issues.toList())
                this.notifyParse(ParseEvent(EventStatus.SUCCESS, "Success", tree, issues.toList()))
            }
        }
    }

    override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
        when (status) {
            MessageStatus.START -> {
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageStatus.FAILURE -> {
                clearErrorMarkers()
                this.createIssueMarkers(issues.toList())
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.FAILURE, message, asm, issues.toList()))
            }

            MessageStatus.SUCCESS -> {
                clearErrorMarkers()
                this.createIssueMarkers(issues.toList())
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.SUCCESS, "Success", asm, issues.toList()))
            }
        }
    }

    override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
        when (status) {
            MessageStatus.START -> {
                this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageStatus.FAILURE -> {
                clearErrorMarkers()
                this.createIssueMarkers(issues.toList())
                this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.FAILURE, message, asm, issues.toList()))
            }

            MessageStatus.SUCCESS -> {
                clearErrorMarkers()
                this.createIssueMarkers(issues.toList())
                this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.SUCCESS, "Success", asm, issues.toList()))
            }
        }
    }

    override fun sentenceCodeCompleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems: List<CompletionItem>) {
        TODO("not implemented")
    }

}