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

package net.akehurst.language.editor.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextDecorationLineStyle
import androidx.compose.ui.text.style.TextDecoration
import net.akehurst.kotlin.compose.editor.api.AutocompleteItem
import net.akehurst.kotlin.compose.editor.api.AutocompleteItemDivider
import net.akehurst.kotlin.compose.editor.api.AutocompleteRequestData
import net.akehurst.kotlin.compose.editor.api.AutocompleteSuggestion
import net.akehurst.kotlin.compose.editor.api.ComposeCodeEditor
import net.akehurst.kotlin.compose.editor.api.TextDecorationStyle
import net.akehurst.kotlin.compose.editor.api.simple.AutocompleteItemSimple
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.CompletionItemKind
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind

@OptIn(ExperimentalTextApi::class)
val PlatformSpanStyle_TextDecorationLineStyle_WAVY get() = PlatformSpanStyle(textDecorationLineStyle = TextDecorationLineStyle.Wavy)

fun <AsmType : Any, ContextType : Any> Agl.attachToComposeEditor(
    languageService: LanguageService,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction,
    composeEditor: ComposeCodeEditor
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCompose<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        languageDefinition = languageDefinition,
        processOptions = processOptions,
        editorId = editorId,
        editorOptions = editorOptions,
        logFunction = logFunction,
        composeEditor = composeEditor
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    aglEditor.initialise()
    return aglEditor
}


class AglEditorCompose<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction,
    val composeEditor: ComposeCodeEditor
) : AglEditorAbstract<AsmType, ContextType, ComposeStyle>(
    languageServiceRequest, languageDefinition, processOptions, EndPointIdentity(editorId, "none"),
    editorOptions, logFunction, AglStyleHandlerComposeStyle(languageDefinition.identity)
) {

    override val baseEditor: Any get() = composeEditor
    override val isConnected: Boolean get() = true
    override var text: String
        get() = composeEditor.rawText
        set(value) {
            composeEditor.rawText = value
        }

    //TODO: add setter! - currently cannot 'set' annotatedString in TextFieldState
    val styledText get() = composeEditor.annotatedText

    var errorMessageProvider = { issue: LanguageIssue -> issue.message }
    var errorMarkerColorProvider = { issue: LanguageIssue -> when(issue.kind) {
        LanguageIssueKind.INFORMATION -> Color.Blue
        LanguageIssueKind.WARNING -> EditorIcons.ORANGE
        LanguageIssueKind.ERROR -> Color.Red
    } }

    override var workerTokenizer = AglTokenizerByWorkerCompose(this.agl, this.logger)

    override val completionProvider = object : AglEditorCompletionProvider {
        override fun provide(offset:Int, completionItems: List<CompletionItem>) {
            _lastProvidedCompletionItem = completionItems
            _completionsResult?.let { cmplRes ->
                val refItems = mutableListOf<AutocompleteItem>()
                val segmentItems = mutableListOf<AutocompleteItem>()
                val constItems = mutableListOf<AutocompleteItem>()
                completionItems.forEach {
                    when (it.kind) {
                        CompletionItemKind.REFERRED -> refItems.add(AutocompleteItemSimple(it.text, offset, it.label))
                        CompletionItemKind.SEGMENT -> segmentItems.add(AutocompleteItemSimple(it.text,offset, it.label))
                        CompletionItemKind.LITERAL -> constItems.add(AutocompleteItemSimple(it.text, offset,it.label))
                        CompletionItemKind.PATTERN -> constItems.add(AutocompleteItemSimple(it.text, offset,it.label))
                    }
                }
                val edItems = refItems.toMutableList()
                if (refItems.isNotEmpty()) edItems.add(AutocompleteItemDivider)
                edItems.addAll(segmentItems)
                if (segmentItems.isNotEmpty()) edItems.add(AutocompleteItemDivider)
                edItems.addAll(constItems)
                cmplRes.provide(edItems)
                _completionsResult = null
            }
        }
    }

    private var _completionsResult: AutocompleteSuggestion? = null
    private var _lastProvidedCompletionItem = listOf<CompletionItem>()
    private var _autocompleteDepthMax = 3
    private var _autocompleteDepthIncrement = 0

    // List<(Depth, PropIndex)>
    private var _autocompletePath = mutableListOf<Pair<Int, Int>>()

    fun initialise() {
        this.updateLanguageDefinition(languageDefinition)
        composeEditor.requestAutocompleteSuggestions = { request, result ->
            try {
                requestAutocomplete(request, result)
            } catch (t: Throwable) {
                logger.logError(t) { "Failed to requestAutocompleteSuggestions" }
            }
        }
        composeEditor.onTextChange = { txt ->
            try {
                onEditorTextChangeInternal(txt.toString())
            } catch (t: Throwable) {
                logger.logError(t) { "Failed to onTextChange" }
            }
        }

        // trigger first sentence process
        onEditorTextChangeInternal(composeEditor.rawText)
    }

    override fun resetTokenization(fromLine: Int) {
        logger.logTrace { "AglEditorCompose.resetTokenization $fromLine" }
        workerTokenizer.refresh()
        //TODO: maybe do this different!
        //FIXME: is 'this.text' the correct value here ?
        composeEditor.lineStyles = workerTokenizer.aglTokenizer.getAllTokensByLine(text).mapValues { (k, v) ->
            workerTokenizer.toEditorTokens(v)
        }
//        composeEditor.refreshTokens() //calling this causes undo/redo to stop working
    }

    override fun destroyBaseEditor() {

    }

    override fun destroyAglEditor() {
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        logger.logTrace { "AglEditorCompose.updateLanguage $oldId" }
    }

    override fun updateEditorStyles() {
        logger.logTrace { "AglEditorCompose.updateEditorStyles" }
    }

    override fun clearIssueMarkers() {
        logger.logTrace { "AglEditorCompose.clearIssueMarkers" }
        try {
            composeEditor.clearMarginItems()
            composeEditor.clearTextMarkers()
        } catch (t: Throwable) {
            logger.logError(t) { "AglEditorCompose.exception during clearIssueMarkers: " }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        logger.logTrace { "AglEditorCompose.createIssueMarkers $issues" }
        try {
            issues.forEach {
                val pos = if (1 == it.location?.length) {
                    maxOf(0,it.location?.position?.minus(2) ?: 0)
                } else {
                    it.location?.position ?: 0
                }
                val len = if (1 == it.location?.length) {
                    4
                } else {
                    it.location?.length ?: 4
                }
                val line = it.location?.line ?: 1
                val colour =  errorMarkerColorProvider(it)
                val (icon, style) = when (it.kind) {
                    LanguageIssueKind.ERROR -> Pair(
                        EditorIcons.Error,
                        SpanStyle(color = colour)//, textDecoration = TextDecoration.Underline, platformStyle = PlatformSpanStyle_TextDecorationLineStyle_WAVY)
                    )

                    LanguageIssueKind.WARNING -> Pair(
                        EditorIcons.Warning,
                        SpanStyle(color = colour)//, textDecoration = TextDecoration.Underline, platformStyle = PlatformSpanStyle_TextDecorationLineStyle_WAVY)
                    )

                    LanguageIssueKind.INFORMATION -> Pair(
                        EditorIcons.Information,
                        SpanStyle(color = colour)//, textDecoration = TextDecoration.Underline, platformStyle = PlatformSpanStyle_TextDecorationLineStyle_WAVY)
                    )
                }
                composeEditor.addMarginItem(line - 1, it.kind.toString(), errorMessageProvider(it), icon, colour)
                composeEditor.addTextMarker(pos, len, style, TextDecorationStyle.SQUIGGLY)
            }
        } catch (t: Throwable) {
            logger.logError(t) { "AglEditorCompose.exception during clearIssueMarkers: " }
        }
    }

    fun requestAutocomplete(request: AutocompleteRequestData, result: AutocompleteSuggestion) {
        logger.logTrace { "AglEditorCompose.requestAutocomplete" }
        if (request.isOpen) {
            // subsequent request
            when {
                +1 == request.proposalPathDelta -> {
                    if(_autocompletePath.isEmpty()) {
                        _autocompletePath += Pair(0, -1)
                    }
                    val ciId = _lastProvidedCompletionItem.getOrNull(request.currentIndex)?.id
                    if (null == ciId) {
                        return //do not expand further
                    } else {
                        val last = _autocompletePath.last()
                        _autocompletePath.removeLast()
                        _autocompletePath += Pair(last.first, ciId)
                        _autocompletePath += Pair(1,-1) //TODO: add cur depth here
                    }
                }
                -1 == request.proposalPathDelta -> {
                    _autocompletePath.removeLast()
                }
                else -> { // assume 0
                    _autocompleteDepthIncrement = maxOf(0, _autocompleteDepthIncrement + request.depthDelta)
                    _autocompleteDepthIncrement = minOf(_autocompleteDepthMax, _autocompleteDepthIncrement)
                }
            }

        } else {
            // first request
            _autocompleteDepthIncrement = 0
            _autocompletePath.clear()
        }
        _completionsResult = result
        val options = this.agl.options.invoke()
        options.completionProvider.depth += _autocompleteDepthIncrement
        options.completionProvider.path = _autocompletePath
        languageServiceRequest.sentenceCodeCompleteRequest(endPointIdentity, nextRequestId, agl.languageIdentity, request.text.toString(), request.position, options)
    }

    // --- AglEditorAbstract ---
    override fun onEditorTextChangeInternal(newText: String) {
        logger.logTrace { "AglEditorCompose.onEditorTextChangeInternal" }
        //console.log("onEditorTextChangeInternal, editor '${this.editorId}' text is '${this.text}'")
        if (doUpdate) {
            //console.log("doUpdate")
            super.onEditorTextChangeInternal(newText)
            //window.clearTimeout(parseTimeout)
            //this.parseTimeout = window.setTimeout({
            this.processSentence(newText)
            //}, 500)
        }
    }

}
