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

package net.akehurst.language.editor.browser.ck

import kotlinx.browser.window
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItemKind
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.browser.ck.autocomplete.CkAutocomplete
import net.akehurst.language.editor.common.*
import net.akehurst.language.editor.common.AglStyleHandlerAsHtml.Companion.encodeForHtml
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.style.api.AglStyleDomain
import org.w3c.dom.Element

fun <AsmType : Any, ContextType : Any> Agl.attachToCk(
    languageService: LanguageService,
    containerElement: Element,
    ckEditor: ck.core.editor.Editor,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCk<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        ckEditor = ckEditor,
        languageDefinition = languageDefinition,
        processOptions = processOptions,
        editorId = editorId,
        editorOptions = editorOptions,
        logFunction = logFunction
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    aglEditor.initialise()
    return aglEditor
}

private class AglEditorCk<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val ckEditor: ck.core.editor.Editor,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction
) : AglEditorAbstract<AsmType, ContextType, CkStyle>(
    languageServiceRequest, languageDefinition, processOptions,EndPointIdentity(editorId, "none"),
    editorOptions, logFunction, AglStyleHandlerCkStyle(languageDefinition.identity)
) {

    override val baseEditor: Any = ckEditor
    override var text: String
        get() = emi.rawText
        set(value) {
            logger.logTrace{"Editor '${this.editorId}' text set to '$value'"}
            val data = value.split("\n")
                .map {
                    "<p>$it</p>"
                }
                .joinToString(separator = "\n")
            emi.clear()
            ckEditor.setData(data)
            emi.update(ckEditor.model)
            this.processSentence(value) // fire this explicitly to ensure it happens immediately
        }

    override val isConnected: Boolean get() = this.containerElement.isConnected

    override val completionProvider: AglEditorCompletionProvider get() = _autocomplete

    private var parseTimeout: dynamic = null
    private var emi: EditorModelIndex = EditorModelIndex()

    override val workerTokenizer: AglTokenizerByWorkerCk<AsmType, ContextType> = AglTokenizerByWorkerCk(this.agl, this.emi, logger)

    private lateinit var _contextualBalloon: ck.ui.panel.balloon.ContextualBalloon
    private lateinit var _autocomplete: CkAutocomplete
    private val _autocompleteLabelStyleHandler = AglStyleHandlerAsHtml(languageDefinition.identity)
    private var _autocompleteDepthMax = 3
    private var _autocompleteDepthIncrement = 0

    fun initialise() {
        this.updateLanguageDefinition(languageDefinition)

        CkEditorHelper.createAglAttributes(logger, ckEditor)

        // CTRL+SPACE
        ckEditor.keystrokes.set(arrayOf("ctrl!", 32), {
            if (_autocomplete.isVisible) {
                _autocomplete.clear()
                _autocompleteDepthIncrement = minOf(_autocompleteDepthMax, _autocompleteDepthIncrement + 1)
                requestAutocomplete()
            } else {
                _autocompleteDepthIncrement = 0
                requestAutocomplete()
            }
        })
        _contextualBalloon = ckEditor.plugins.get(ck.ui.panel.balloon.ContextualBalloon::class.js)
        val styleCompleteItem = editorOptions.styleCompletionItem
            ?: { item ->
                val senDef =SentenceDefault(item.text, agl.options.invoke().parse.sentenceIdentity.invoke())
                val scanRes = agl.simpleScanner.scan(senDef)
                val aglTokens = agl.styleHandler.transformToTokens(scanRes.allTokens)
                val html = _autocompleteLabelStyleHandler.applyHtmlStyling(senDef, aglTokens, emptyList())

                when (item.kind) {
                    CompletionItemKind.LITERAL -> html
                    CompletionItemKind.PATTERN -> "<span>${encodeForHtml(item.text)}</span><span> (${encodeForHtml(item.label)})</span>"
                    CompletionItemKind.SEGMENT -> "<span>${encodeForHtml(item.label)}: </span>$html"
                    CompletionItemKind.REFERRED -> "<span>${encodeForHtml(item.text)}</span><span> (${encodeForHtml(item.label)})</span>"
                }
            }
        _autocomplete = CkAutocomplete(logger, ckEditor, _contextualBalloon, styleCompleteItem)

        ckEditor.model.document.on("change:data") { onEditorTextChangeInternal(this.text) } //TODO get text from event

        ////this.updateLanguage(null)
        //this.refreshProcessor()
        //this.refreshStyleHandler()

        // trigger first sentence process
        onEditorTextChangeInternal(this.text)
    }

    override fun resetTokenization(fromLine: Int) {
        logger.logTrace { "resetTokenization $fromLine" }
        workerTokenizer.refresh()
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        logger.logTrace { "updateLanguage $oldId" }
    }

    override fun updateStyleModel(styleModel: AglStyleDomain) {
        super.updateStyleModel(styleModel)
        this._autocompleteLabelStyleHandler.updateStyleModel(styleModel)
    }

    override fun updateEditorStyles() {
        logger.logTrace { "updateEditorStyles" }
    }

    override fun clearIssueMarkers() {
        logger.logTrace { "clearIssueMarkers" }
        try {
            ckEditor.model.enqueueChange { writer ->
                try {
                    CkEditorHelper.removeAttributes(logger, writer, setOf(CkEditorHelper.ATTRIBUTE_NAME_ERROR_MARKER))
                } catch (t: Throwable) {
                    logger.logError(t) { "exception during clearIssueMarkers...enqueueChange : " }
                }
            }
        } catch (t: Throwable) {
            logger.logError(t) { "exception during clearIssueMarkers: " }
        }

        //TODO: Maybe not explicitly do this, rather remove issues when removing styles
        // as issues are added to ck like styles
        // styles are cleared before calling this method

    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        logger.logTrace { "createIssueMarkers $issues" }
        val atts = issues.map { iss ->
            val fp = emi.toModelPosition(iss.location?.position ?: 0)
            val lp = emi.toModelPosition(iss.location?.endPosition ?: 1)
            val attName = when (iss.kind) {
                LanguageIssueKind.ERROR -> CkEditorHelper.ATTRIBUTE_NAME_ERROR_MARKER
                LanguageIssueKind.WARNING -> CkEditorHelper.ATTRIBUTE_NAME_WARN_MARKER
                LanguageIssueKind.INFORMATION -> CkEditorHelper.ATTRIBUTE_NAME_INFO_MARKER
            }
            CkAttributeData(fp, lp, mapOf(attName to "true"))
        }
        CkEditorHelper.addAttributes(logger, ckEditor.model, atts, CkEditorHelper.ATTRIBUTE_SET_ISSUE_MARKERS)
    }

    override fun destroyAglEditor() {
    }

    override fun destroyBaseEditor() {
        this.ckEditor.destroy()
    }

    // --- AglEditorAbstract ---
    override fun onEditorTextChangeInternal(newText: String) {
        logger.logTrace { "onEditorTextChangeInternal" }
        //console.log("onEditorTextChangeInternal, editor '${this.editorId}' text is '${this.text}'")
        if (doUpdate) {
            //console.log("doUpdate")
            super.onEditorTextChangeInternal(newText)
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
                //console.log("new timeout")
                val oldText = this.text
                emi.update(ckEditor.model)
                if (emi.rawText != oldText) {
                    //console.log("rawtext changed")
                    this.processSentence(newText)
                }
            }, 500)
        }
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        super.sentenceParseResponse(endPointIdentity, requestId, status, message, issues, tree)
        when (status) {
            MessageResponseStatus.FAILURE -> this.resetTokenization(0) // reset to trigger use of scan tokens
            else -> Unit
        }
    }

    // ---
    fun requestAutocomplete() {
        logger.logTrace { "invokeAutocomplete"}
        val cursorPos = ckEditor.model.document.selection.getFirstPosition() ?: error("Should always be non-null!")
        emi.update(ckEditor.model)
        val options = this.agl.options.invoke()
        options.completionProvider.depth += _autocompleteDepthIncrement
        languageServiceRequest.sentenceCodeCompleteRequest(endPointIdentity, nextRequestId, agl.languageIdentity, text, emi.toSentencePosition(cursorPos), options)
        _autocomplete.show()
    }
}