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

package net.akehurst.language.editor.common.messages

import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.common.AglToken

enum class MessageStatus { START, FAILURE, SUCCESS }

abstract class AglWorkerMessage(
    val action: String
) {
    abstract val languageId: String
    abstract val editorId: String
    abstract val sessionId: String
}

data class MessageProcessorCreate(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val grammarStr: String?
) : AglWorkerMessage("MessageProcessorCreate") {
    override fun toString(): String = "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, grammarStr='...')"
}

data class MessageProcessorCreateResponse(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>
) : AglWorkerMessage("MessageProcessorCreateResponse")

data class MessageSyntaxAnalyserConfigure(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val configuration: Map<String, Any>
) : AglWorkerMessage("MessageSyntaxAnalyserConfigure")

data class MessageSyntaxAnalyserConfigureResponse(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>
) : AglWorkerMessage("MessageSyntaxAnalyserConfigureResponse")


data class MessageProcessRequest<ContextType : Any>(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val context: ContextType?
) : AglWorkerMessage("MessageProcessRequest") {
    override fun toString(): String = "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, goalRuleName=$goalRuleName, context=$context, text='...')"
}

data class MessageParseResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>, // custom serialisation because auto serialisation of SPPT impl classes is too complex
    val treeSerialised: String?
) : AglWorkerMessage("MessageParseResult") {
    override fun toString(): String =
        "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, status=$status, message=$message, issues=$issues, treeSerialised='...')"
}

data class MessageSyntaxAnalysisResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val asm: Any?
) : AglWorkerMessage("MessageSyntaxAnalysisResult") {
    override fun toString(): String =
        "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, status=$status, message=$message, issues=$issues, asm='...')"
}

data class MessageSemanticAnalysisResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val asm: Any?
) : AglWorkerMessage("MessageSemanticAnalysisResult") {
    override fun toString(): String =
        "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, status=$status, message=$message, issues=$issues, asm='...'))"
}

data class MessageParserInterruptRequest(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest")

data class MessageLineTokens(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val lineTokens: List<List<AglToken>>,
) : AglWorkerMessage("MessageLineTokens") {
    override fun toString(): String = "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, status=$status, message=$message, lineTokens='...'))"
}

data class MessageSetStyle(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val css: String
) : AglWorkerMessage("MessageSetStyle") {
    override fun toString(): String = "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId, css='...')"
}

data class MessageSetStyleResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String
) : AglWorkerMessage("MessageSetStyleResult")

data class MessageCodeCompleteRequest(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val position: Int
) : AglWorkerMessage("MessageCodeCompleteRequest")

data class MessageCodeCompleteResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message: String,
    val completionItems: Array<CompletionItem>?
) : AglWorkerMessage("MessageCodeCompleteResult")

data class MessageGrammarAmbiguityAnalysisRequest(
    override val languageId: String, override val editorId: String, override val sessionId: String,
) : AglWorkerMessage("MessageGrammarAmbiguityAnalysisRequest") {
    override fun toString(): String = "${super.action}(languageId=$languageId, editorId=$editorId, sessionId=$sessionId)"
}

data class MessageGrammarAmbiguityAnalysisResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val status: MessageStatus,
    val message:String?,
    val issues: List<LanguageIssue>
): AglWorkerMessage("MessageGrammarAmbiguityAnalysisResult")