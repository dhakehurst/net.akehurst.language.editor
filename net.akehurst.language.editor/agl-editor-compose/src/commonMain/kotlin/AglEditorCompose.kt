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
import net.akehurst.kotlin.compose.editor.api.AutocompleteSuggestion
import net.akehurst.kotlin.compose.editor.api.ComposeCodeEditor
import net.akehurst.kotlin.compose.editor.api.simple.AutocompleteItemSimple
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
    processOptions: ()-> ProcessOptions<AsmType, ContextType> ,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?,
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
    processOptions: ()-> ProcessOptions<AsmType, ContextType> ,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?,
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

    override var workerTokenizer = AglTokenizerByWorkerCompose(this.agl, this.logger)

    override val completionProvider = object : AglEditorCompletionProvider {
        override fun provide(completionItems: List<CompletionItem>) {
            _completionsResult?.let {
                val refItems = mutableListOf<AutocompleteItem>()
                val segmentItems = mutableListOf<AutocompleteItem>()
                val constItems = mutableListOf<AutocompleteItem>()
                completionItems.forEach {
                    when (it.kind) {
                        CompletionItemKind.REFERRED ->  refItems.add(AutocompleteItemSimple(it.text, it.label))
                        CompletionItemKind.SEGMENT -> segmentItems.add(AutocompleteItemSimple(it.text, it.label))
                        CompletionItemKind.LITERAL ->  constItems.add(AutocompleteItemSimple(it.text, it.label))
                        CompletionItemKind.PATTERN ->   constItems.add(AutocompleteItemSimple(it.text, it.label))
                    }
                }
                val edItems = refItems.toMutableList()
                if (refItems.isNotEmpty()) edItems.add(AutocompleteItemDivider)
                edItems.addAll(segmentItems)
                if (segmentItems.isNotEmpty()) edItems.add(AutocompleteItemDivider)
                edItems.addAll(constItems)
                it.provide(edItems)
                _completionsResult = null
            }
        }
    }

    private var _completionsResult: AutocompleteSuggestion? = null
    private var _autocompleteDepthMax = 3
    private var _autocompleteDepthIncrement = 0

    fun initialise() {
        this.updateLanguageDefinition(languageDefinition)
        composeEditor.requestAutocompleteSuggestions = { position, text, result ->
            try {
                requestAutocomplete(position, text, result)
            } catch (t: Throwable) {
                logger.logError(t) { "Failed to requestAutocompleteSuggestions" }
            }
        }
        composeEditor.onTextChange = { txt ->
            try {
                onEditorTextChangeInternal(txt)
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
        composeEditor.lineStyles = workerTokenizer.aglTokenizer.tokensByLine.mapValues { (k,v) ->
            workerTokenizer.toEditorTokens(v)
        }
        composeEditor.refreshTokens()
    }

    override fun destroyBaseEditor() {
        composeEditor.destroy()
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
                val pos = it.location?.position ?: 0
                val len = it.location?.length ?: 2
                val line = it.location?.line ?: 1
                val (icon, colour, style) = when (it.kind) {
                    LanguageIssueKind.ERROR -> Triple(
                        EditorIcons.Error,
                        Color.Red,
                        SpanStyle(color = Color.Red, textDecoration = TextDecoration.Underline, platformStyle = PlatformSpanStyle_TextDecorationLineStyle_WAVY)
                    )

                    LanguageIssueKind.WARNING -> Triple(
                        EditorIcons.Warning,
                        EditorIcons.ORANGE,
                        SpanStyle(color = EditorIcons.ORANGE, textDecoration = TextDecoration.Underline, platformStyle = PlatformSpanStyle_TextDecorationLineStyle_WAVY)
                    )

                    LanguageIssueKind.INFORMATION -> Triple(
                        EditorIcons.Information,
                        Color.Blue,
                        SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline, platformStyle = PlatformSpanStyle_TextDecorationLineStyle_WAVY)
                    )
                }
                composeEditor.addMarginItem(line - 1, it.kind.toString(), it.message, icon, colour)
                composeEditor.addTextMarker(pos, len, style)
            }
        } catch (t: Throwable) {
            logger.logError(t) { "AglEditorCompose.exception during clearIssueMarkers: " }
        }
    }

    fun requestAutocomplete(position: Int, text1: CharSequence, result: AutocompleteSuggestion) {
        logger.logTrace { "AglEditorCompose.requestAutocomplete" }
        if (composeEditor.autocomplete.isVisible) {
            // subsequent request
            composeEditor.autocomplete.clear()
            _autocompleteDepthIncrement = minOf(_autocompleteDepthMax, _autocompleteDepthIncrement + 1)
        } else {
            // first request
            _autocompleteDepthIncrement = 0
        }
        _completionsResult = result
        val options = this.agl.options.invoke()
        options.completionProvider.depth += _autocompleteDepthIncrement
        languageServiceRequest.sentenceCodeCompleteRequest(endPointIdentity, nextRequestId, agl.languageIdentity, text, position, options)
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
