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
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItemKind
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.browser.ck.autocomplete.CkAutocomplete
import net.akehurst.language.editor.common.*
import net.akehurst.language.editor.common.AglStyleHandlerAsHtml.Companion.escapeForHtml
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.style.api.AglStyleModel
import org.w3c.dom.Element

fun <AsmType : Any, ContextType : Any> Agl.attachToCk(
    languageService: LanguageService,
    containerElement: Element,
    ckEditor: ck.core.editor.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCk<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        ckEditor = ckEditor,
        languageId = languageId,
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
    languageId: LanguageIdentity,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?
) : AglEditorAbstract<AsmType, ContextType, CkStyle>(
    languageServiceRequest, languageId, EndPointIdentity(editorId, "none"),
    editorOptions, logFunction, AglStyleHandlerCkStyle(languageId)
) {

    override val baseEditor: Any = ckEditor
    override var text: String
        get() = emi.rawText
        set(value) {
            logger.logTrace("Editor '${this.editorId}' text set to '$value'")
            val data = value.split("\n")
                .map {
                    "<p>$it</p>"
                }
                .joinToString(separator = "\n")
            emi.clear()
            ckEditor.setData(data)
            emi.update(ckEditor.model)
            this.processSentence() // fire this explicitly to ensure it happens immediately
        }

    override val isConnected: Boolean get() = this.containerElement.isConnected

    override val completionProvider: AglEditorCompletionProvider get() = _autocomplete

    private var parseTimeout: dynamic = null
    private var emi: EditorModelIndex = EditorModelIndex()

    override val workerTokenizer: AglTokenizerByWorkerCk<AsmType, ContextType> = AglTokenizerByWorkerCk(this.agl, this.emi, logger)

    private lateinit var _contextualBalloon: ck.ui.panel.balloon.ContextualBalloon
    private lateinit var _autocomplete: CkAutocomplete
    private val _autocompleteLabelStyleHandler = AglStyleHandlerAsHtml(languageId)
    private var _autocompleteDepthMax = 3
    private var _autocompleteDepthIncrement = 0

    fun initialise() {
        CkEditorHelper.createAglAttributes(logger, ckEditor)

        // CTRL+SPACE
        ckEditor.keystrokes.set(arrayOf("ctrl!", 32), {
            if (_autocomplete.isVisible) {
                _autocomplete.clear()
                _autocompleteDepthIncrement = minOf(_autocompleteDepthMax, _autocompleteDepthIncrement+1)
                invokeAutocomplete()
            } else {
                _autocompleteDepthIncrement = 0
                invokeAutocomplete()
            }
        })
        _contextualBalloon = ckEditor.plugins.get(ck.ui.panel.balloon.ContextualBalloon::class.js)
        val styleCompleteItem = editorOptions.styleCompletionItem
            ?: { item ->
                val scanRes = agl.simpleScanner.scan(SentenceDefault(item.text))
                val aglTokens = agl.styleHandler.transformToTokens(scanRes.tokens)
                val html = _autocompleteLabelStyleHandler.applyHtmlStyling(SentenceDefault(item.text), aglTokens)

                when (item.kind) {
                    CompletionItemKind.LITERAL -> html
                    CompletionItemKind.PATTERN -> "<span>${escapeForHtml(item.text)}</span><span> (${escapeForHtml(item.label)})</span>"
                    CompletionItemKind.SEGMENT -> "<span>${escapeForHtml(item.label)}: </span>$html"
                    CompletionItemKind.REFERRED -> "<span>${escapeForHtml(item.text)}</span><span> (${escapeForHtml(item.label)})</span>"
                }
            }
        _autocomplete = CkAutocomplete(logger, ckEditor, _contextualBalloon, styleCompleteItem)

        ckEditor.model.document.on("change:data") { onEditorTextChangeInternal() }

        this.updateLanguage(null)
        this.updateProcessor()
        this.requestUpdateStyleModel()

        // trigger first sentence process
        onEditorTextChangeInternal()
    }

    override fun resetTokenization(fromLine: Int) {
        logger.log(LogLevel.Trace, "resetTokenization $fromLine")
        workerTokenizer.refresh()
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        logger.log(LogLevel.Trace, "updateLanguage $oldId")
    }

    override fun updateStyleModel(styleModel: AglStyleModel) {
        super.updateStyleModel(styleModel)
        this._autocompleteLabelStyleHandler.updateStyleModel(styleModel)
    }

    override fun updateEditorStyles() {
        logger.log(LogLevel.Trace, "updateEditorStyles")
    }

    override fun clearIssueMarkers() {
        logger.log(LogLevel.Trace, "clearIssueMarkers")
        try {
            ckEditor.model.enqueueChange { writer ->
                try {
                    CkEditorHelper.removeAttributes(logger, writer, setOf(CkEditorHelper.ATTRIBUTE_NAME_ERROR_MARKER))
                } catch (t: Throwable) {
                    logger.logError("exception during clearIssueMarkers...enqueueChange : ", t)
                }
            }
        } catch (t: Throwable) {
            logger.logError("exception during clearIssueMarkers: ", t)
        }

        //TODO: Maybe not explicitly do this, rather remove issues when removing styles
        // as issues are added to ck like styles
        // styles are cleared before calling this method

    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        logger.log(LogLevel.Trace, "createIssueMarkers $issues")
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
    override fun onEditorTextChangeInternal() {
        logger.log(LogLevel.Trace, "onEditorTextChangeInternal")
        //console.log("onEditorTextChangeInternal, editor '${this.editorId}' text is '${this.text}'")
        if (doUpdate) {
            //console.log("doUpdate")
            super.onEditorTextChangeInternal()
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
                //console.log("new timeout")
                val oldText = this.text
                emi.update(ckEditor.model)
                if (emi.rawText != oldText) {
                    //console.log("rawtext changed")
                    this.processSentence()
                }
            }, 500)
        }
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        super.sentenceParseResponse(endPointIdentity, requestId, status, message, issues, tree)
        when (status) {
            MessageStatus.FAILURE -> this.resetTokenization(0) // reset to trigger use of scan tokens
            else -> Unit
        }
    }

    // ---
    fun invokeAutocomplete() {
        logger.logTrace("invokeAutocomplete")
        val cursorPos = ckEditor.model.document.selection.getFirstPosition() ?: error("Should always be non-null!")
        emi.update(ckEditor.model)
        val options = this.agl.options.invoke()
        options.completionProvider.depth += _autocompleteDepthIncrement
        languageServiceRequest.sentenceCodeCompleteRequest(endPointIdentity, nextRequestId, agl.languageIdentity, text, emi.toSentencePosition(cursorPos), options)
        _autocomplete.show()
    }
}