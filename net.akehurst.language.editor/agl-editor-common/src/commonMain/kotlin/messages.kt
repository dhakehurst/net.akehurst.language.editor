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

import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.common.AglToken
import kotlin.math.min

enum class MessageStatus { START, FAILURE, SUCCESS }

abstract class AglWorkerMessage(
    val action: String
) {
    abstract val endPoint: EndPointIdentity
}

abstract class AglWorkerMessageResponse(action: String) : AglWorkerMessage(action) {
    abstract val status: MessageStatus
}

data class EndPointIdentity(
    val languageId: String,
    val editorId: String,
    val sessionId: String
) {
    override fun toString(): String = "languageId=$languageId, editorId=$editorId, sessionId=$sessionId"
}

data class MessageProcessorCreate(
    override val endPoint: EndPointIdentity,
    val grammarStr: String,
    val crossReferenceModelStr: String?
) : AglWorkerMessage("MessageProcessorCreate") {
    override fun toString(): String {
        val gs = when {
            //null == grammarStr -> "null"
            grammarStr.isBlank() -> "''"
            else -> "'...'"
        }
        val ss = when {
            null == crossReferenceModelStr -> "null"
            crossReferenceModelStr.isBlank() -> "''"
            else -> "'...'"
        }
        return "${super.action}(endPoint=$endPoint, grammarStr=$gs, scopeModelStr=$ss)"
    }
}

data class MessageProcessorCreateResponse(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val scannerMatchables: List<Matchable>,
    val issues: List<LanguageIssue>
) : AglWorkerMessageResponse("MessageProcessorCreateResponse")

data class MessageProcessorDelete(
    override val endPoint: EndPointIdentity,
) : AglWorkerMessage("MessageProcessorDelete") {
}

data class MessageProcessorDeleteResponse(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String
) : AglWorkerMessageResponse("MessageProcessorDeleteResponse")

data class MessageSyntaxAnalyserConfigure(
    override val endPoint: EndPointIdentity,
    val configuration: Map<String, Any>
) : AglWorkerMessage("MessageSyntaxAnalyserConfigure")

data class MessageSyntaxAnalyserConfigureResponse(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>
) : AglWorkerMessageResponse("MessageSyntaxAnalyserConfigureResponse")


data class MessageProcessRequest<ContextType : Any>(
    override val endPoint: EndPointIdentity,
    val goalRuleName: String?,
    val text: String,
    val context: ContextType?
) : AglWorkerMessage("MessageProcessRequest") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint, goalRuleName=$goalRuleName, context=$context, text='${
        text.substring(
            0,
            min(10, text.length)
        )
    }')"
}

data class MessageScanResult(
    override val endPoint: EndPointIdentity,
    val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val lineTokens: List<AglToken>
) : AglWorkerMessage("MessageScanResult") {
    override fun toString(): String =
        "${super.action}(endPoint=$endPoint, status=$status, message=$message, issues=$issues, lineTokens='...')"
}

data class MessageParseResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val treeSerialised: String? // custom serialisation because auto serialisation of SPPT impl classes is too complex
) : AglWorkerMessageResponse("MessageParseResult") {
    override fun toString(): String =
        "${super.action}(endPoint=$endPoint, status=$status, message=$message, issues=$issues, treeSerialised='...')"
}

data class MessageSyntaxAnalysisResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val asm: Any?
) : AglWorkerMessageResponse("MessageSyntaxAnalysisResult") {
    override fun toString(): String =
        "${super.action}(endPoint=$endPoint, status=$status, message=$message, issues=$issues, asm='...')"
}

data class MessageSemanticAnalysisResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val asm: Any?
) : AglWorkerMessageResponse("MessageSemanticAnalysisResult") {
    override fun toString(): String =
        "${super.action}(languageId=$endPoint=$endPoint, status=$status, message=$message, issues=$issues, asm='...'))"
}

data class MessageParserInterruptRequest(
    override val endPoint: EndPointIdentity,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest")

data class MessageLineTokens(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val lineTokens: List<List<AglToken>>,
) : AglWorkerMessageResponse("MessageLineTokens") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint, status=$status, message=$message, lineTokens='...'))"
}

data class MessageSetStyle(
    override val endPoint: EndPointIdentity,
    val css: String
) : AglWorkerMessage("MessageSetStyle") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint, css='...')"
}

data class MessageSetStyleResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String
) : AglWorkerMessageResponse("MessageSetStyleResult")

data class MessageCodeCompleteRequest(
    override val endPoint: EndPointIdentity,
    val goalRuleName: String?,
    val text: String,
    val position: Int
) : AglWorkerMessage("MessageCodeCompleteRequest")

data class MessageCodeCompleteResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val completionItems: List<CompletionItem>?
) : AglWorkerMessageResponse("MessageCodeCompleteResult")

data class MessageGrammarAmbiguityAnalysisRequest(
    override val endPoint: EndPointIdentity,
) : AglWorkerMessage("MessageGrammarAmbiguityAnalysisRequest") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint)"
}

data class MessageGrammarAmbiguityAnalysisResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String?,
    val issues: List<LanguageIssue>
) : AglWorkerMessageResponse("MessageGrammarAmbiguityAnalysisResult")