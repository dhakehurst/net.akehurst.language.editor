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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import net.akehurst.kotlin.compose.editor.CodeEditor
import net.akehurst.kotlin.compose.editor.EditorState
import net.akehurst.kotlin.compose.editor.api.AutocompleteSuggestion
import net.akehurst.kotlin.compose.editor.api.ComposeCodeEditor
import net.akehurst.kotlin.compose.editor.api.EditorLineToken
import net.akehurst.kotlin.compose.editor.api.LineTokensFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.compose.attachToComposeEditor
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel

class TextEditor : ComposeCodeEditor {

    //val logger = ConsoleLogger(LogLevel.All)
    val logFunction: LogFunction = { lvl, prefix, msg, t ->  println("$lvl: $prefix - $msg") }//logger.log(lvl, "$prefix - $msg", t) }
    val languageService = LanguageServiceDirectExecution(logFunction)
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

        }

        override fun sentenceLineTokensResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,
            status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>
        ) {

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
    val endPointIdentity: EndPointIdentity = EndPointIdentity("text-editor","<none>")

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
        Agl.attachToComposeEditor(languageService, )
    }

    private fun requestAutocompleteSuggestions(position: Int, text: CharSequence, result: AutocompleteSuggestion) {
       languageService.request.sentenceCodeCompleteRequest(endPointIdentity,)
    }

    fun getLineTokens(lineNumber: Int, lineStartPosition: Int, lineText: String): List<EditorLineToken> {
        val t1 = Regex("[\\\\]red[{](.*)[}]\"").findAll(lineText).map {
            it.range.first
            object : EditorLineToken {
                override val start: Int get() = it.range.first
                override val finish: Int get() = it.range.last+1
                override val style: SpanStyle get() = SpanStyle(color = Color.Red)
            }
        }
        val t2 = Regex("[\\\\]blue[{](.*)[}]").findAll(lineText).map {
            it.range.first
            object : EditorLineToken {
                override val start: Int get() = it.range.first
                override val finish: Int get() = it.range.last+1
                override val style: SpanStyle get() = SpanStyle(color = Color.Blue)
            }
        }
        val t3 = Regex("else|if|[{]|[}]").findAll(lineText).map {
            it.range.first
            object : EditorLineToken {
                override val start: Int get() = it.range.first
                override val finish: Int get() = it.range.last+1
                override val style: SpanStyle get() = SpanStyle(color = Color.Magenta)
            }
        }
        return (t1 + t2 + t3).toList()
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
        set(value) { editorState.setNewText(value) }

    override var getLineTokens: LineTokensFunction
        get() = TODO("not implemented")
        set(value) {}
}