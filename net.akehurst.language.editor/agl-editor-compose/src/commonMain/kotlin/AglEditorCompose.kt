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

import net.akehurst.kotlin.compose.editor.api.AutocompleteSuggestion
import net.akehurst.kotlin.compose.editor.api.ComposeCodeEditor
import net.akehurst.kotlin.compose.editor.api.simple.AutocompleteItemSimple
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.issues.api.LanguageIssue

fun <AsmType : Any, ContextType : Any> Agl.attachToComposeEditor(
    languageService: LanguageService,
    languageId: LanguageIdentity,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?,
    composeEditor: ComposeCodeEditor
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCompose<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        languageId = languageId,
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
    languageId: LanguageIdentity,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?,
    val composeEditor: ComposeCodeEditor
) : AglEditorAbstract<AsmType, ContextType, ComposeStyle>(
    languageServiceRequest, languageId, EndPointIdentity(editorId,"none"),
   editorOptions, logFunction, AglStyleHandlerComposeStyle()
) {

    override val baseEditor: Any get() = composeEditor
    override val isConnected: Boolean get() = true
    override var text: String
        get() = composeEditor.text
        set(value) {
            composeEditor.text = value
        }

    override var workerTokenizer = AglTokenizerByWorkerCompose(this.agl, this.logger)

    override val completionProvider = object : AglEditorCompletionProvider {
        override fun provide(completionItems: List<CompletionItem>) {
            _completionsResult?.let {
                val edItems = completionItems.map {
                    AutocompleteItemSimple(it.text, it.label)
                }
                it.provide(edItems)
                _completionsResult = null
            }
        }
    }

    private var _completionsResult:AutocompleteSuggestion? = null
    private var _autocompleteDepthMax = 3
    private var _autocompleteDepthIncrement = 0

    fun initialise() {

        //composeEditor.getLineTokens = { lineNumber, lineStartPosition, lineText ->
        //    workerTokenizer.aglTokenizer.getLineTokens(lineText,)
       // }

        composeEditor.onTextChange = { _ -> onEditorTextChangeInternal() }
        composeEditor.requestAutocompleteSuggestions = { position, text, result ->
            requestAutocomplete(position, text, result)
        }

        this.updateLanguage(null)
        this.refreshProcessor()
        this.refreshStyleHandler()

        // trigger first sentence process
        //onEditorTextChangeInternal()
    }

    override fun resetTokenization(fromLine: Int) {
        logger.log(LogLevel.Trace, "AglEditorCompose.resetTokenization $fromLine")
        workerTokenizer.refresh()
    }

    override fun destroyBaseEditor() {
        composeEditor.destroy()
    }

    override fun destroyAglEditor() {
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        logger.log(LogLevel.Trace, "AglEditorCompose.updateLanguage $oldId")
    }

    override fun updateEditorStyles() {
        logger.log(LogLevel.Trace, "AglEditorCompose.updateEditorStyles")
    }

    override fun clearIssueMarkers() {
        logger.log(LogLevel.Trace, "AglEditorCompose.clearIssueMarkers")
        try {
            //TODO:
        } catch (t: Throwable) {
            logger.logError("AglEditorCompose.exception during clearIssueMarkers: ", t)
        }
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        logger.log(LogLevel.Trace, "AglEditorCompose.createIssueMarkers $issues")
        try {
            //TODO:
        } catch (t: Throwable) {
            logger.logError("AglEditorCompose.exception during clearIssueMarkers: ", t)
        }
    }

    fun requestAutocomplete(position: Int, text1: CharSequence, result: AutocompleteSuggestion) {
        logger.logTrace("AglEditorCompose.requestAutocomplete")
        if(composeEditor.autocomplete.isVisible) {
            // subsequent request
            _autocompleteDepthIncrement = minOf(_autocompleteDepthMax, _autocompleteDepthIncrement+1)
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
    override fun onEditorTextChangeInternal() {
        logger.log(LogLevel.Trace, "AglEditorCompose.onEditorTextChangeInternal")
        //console.log("onEditorTextChangeInternal, editor '${this.editorId}' text is '${this.text}'")
        if (doUpdate) {
            //console.log("doUpdate")
            super.onEditorTextChangeInternal()
            //window.clearTimeout(parseTimeout)
            //this.parseTimeout = window.setTimeout({
                this.processSentence()
            //}, 500)
        }
    }

}
