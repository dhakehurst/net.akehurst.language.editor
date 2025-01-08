/*
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.akehurst.language.testEditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import net.akehurst.kotlin.compose.editor.CodeEditor
import net.akehurst.kotlin.compose.editor.EditorState
import net.akehurst.kotlin.compose.editor.api.*
import net.akehurst.kotlin.compose.editor.api.simple.AutocompleteItemSimple
import net.akehurst.kotlin.compose.editor.api.simple.EditorLineTokenSimple
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.aglEditorOptions
import net.akehurst.language.editor.common.compose.attachToComposeEditor
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel

class TextEditor(

) : ComposeCodeEditor {

    var languageIdentity = LanguageIdentity("<unknown>")
    var processOptions = Agl.options<Any, Any> { }
    var editorOptions = aglEditorOptions { }

    val editorId = "text-editor"
    val endPointIdentity: EndPointIdentity = EndPointIdentity(editorId, "<none>")

    //val logger = ConsoleLogger(LogLevel.All)
    val logFunction: LogFunction = { lvl, prefix, msg, t -> println("$lvl: $prefix - $msg") }//logger.log(lvl, "$prefix - $msg", t) }
    val languageService = LanguageServiceDirectExecution(logFunction)

    val lineTokenCache = mutableMapOf<Int, List<AglToken>>()
    val requestAutocomplete = mutableMapOf<RequestIdentity<Int>, AutocompleteSuggestion>()
    val langServiceResponse = object : LanguageServiceResponse {
        override fun processorCreateResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,
            status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>
        ) {

        }

        override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String) {

        }

        override fun processorSetStyleResponse(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            status: MessageStatus,
            message: String,
            issues: List<LanguageIssue>,
            styleModel: AglStyleModel?
        ) {

        }

        override fun sentenceCodeCompleteResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,
            status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems: List<CompletionItem>
        ) {
            val result = requestAutocomplete.remove(requestId)
            result?.let {
                val items = completionItems.map { AutocompleteItemSimple(it.text, it.label) }
                it.provide(items)
            }
        }

        override fun sentenceLineTokensResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,
            status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>
        ) {
            for(ln in startLine until lineTokens.size) {
                lineTokenCache[ln] = lineTokens[ln-startLine]
            }
        }

        override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {

        }

        override fun sentenceSemanticAnalysisResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,
            status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?
        ) {
            TODO("not implemented")
        }

        override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {

        }
    }

    val editorState = remember {
        EditorState(
            initialText = """
                    \red{Hello} \blue{World}
                """.trimIndent(),
            //onTextChange = { onTextChange.invoke(it) },
            getLineTokens = { lineNumber, lineStartPosition, lineText -> getLineTokens(lineNumber, lineStartPosition, lineText) },
            requestAutocompleteSuggestions = { position, text, result -> requestAutocompleteSuggestions(position, text, result) }
        )
    }

    init {
        languageService.addResponseListener(endPointIdentity, langServiceResponse)
        Agl.attachToComposeEditor<Any, Any>(
            languageService, languageIdentity, editorId,
            editorOptions, logFunction, this
        )
    }

    @Composable
    fun content() {
        Surface {
            CodeEditor(
                modifier = Modifier
                    .fillMaxSize(),
                editorState = editorState
            )
        }
    }

    // --- ComposeCodeEditor ---

    override var text: String
        get() = editorState.inputRawText
        set(value) {
            editorState.setNewText(value)
        }

    override var onTextChange: (String) -> Unit = { text ->
        val nextRequest = RequestIdentity(1)
        languageService.request.sentenceProcessRequest(
            endPointIdentity, nextRequest, languageIdentity,
            text, processOptions
        )
    }

    override var getLineTokens: LineTokensFunction = { lineNumber, lineStartPosition, lineText ->
        lineTokenCache[lineNumber]?.map {
            val st = it.position
            val fn = st + it.length
            val style = mapToSpanStyle(it.styles)
            EditorLineTokenSimple(st,fn,style)
        } ?: emptyList()
    }

    override var requestAutocompleteSuggestions: AutocompleteFunction = { position, text, result ->
        val nextRequest = RequestIdentity(1)
        requestAutocomplete[nextRequest] = result
        languageService.request.sentenceCodeCompleteRequest(endPointIdentity, nextRequest, languageIdentity, text.toString(), position, processOptions)
    }

    override fun refreshTokens() {
        editorState.refresh()
    }

    override fun destroy() {
        TODO("not implemented")
    }

    private fun mapToSpanStyle(aglStyles: List<String>):SpanStyle {
        val spanStyles = aglStyles.map {
            editorStyles[it]
        }
        return spanStyle
    }
}