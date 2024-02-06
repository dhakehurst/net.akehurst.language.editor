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

package net.akehurst.language.editor.language.service

import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.agl.sppt.TreeData
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.EditorOptions
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.MessageStatus
import net.akehurst.language.editor.common.AglTokenDefault
import kotlin.math.min

abstract class AglWorkerMessage(
    val action: String
) {
    abstract val endPoint: EndPointIdentity
}

abstract class AglWorkerMessageResponse(action: String) : AglWorkerMessage(action) {
    abstract val status: MessageStatus
}

data class MessageProcessorCreate(
    override val endPoint: EndPointIdentity,
    val languageId:String,
    val grammarStr: String,
    val crossReferenceModelStr: String?,
    val editorOptions: EditorOptions
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
    val issues: List<LanguageIssue>,
    val scannerMatchables: List<Matchable>
) : AglWorkerMessageResponse("MessageProcessorCreateResponse")

data class MessageProcessorDelete(
    override val endPoint: EndPointIdentity,
    val languageId:String
) : AglWorkerMessage("MessageProcessorDelete") {
}

data class MessageProcessorDeleteResponse(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String
) : AglWorkerMessageResponse("MessageProcessorDeleteResponse")

data class MessageProcessRequest<AsmType : Any, ContextType : Any>(
    override val endPoint: EndPointIdentity,
    val languageId:String,
    val text: String,
    val options: ProcessOptions<AsmType,ContextType>
) : AglWorkerMessage("MessageProcessRequest") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint, text='${
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
    val lineTokens: List<AglTokenDefault>
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

data class MessageParseResult2(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val treeData: TreeData
) : AglWorkerMessageResponse("MessageParseResult") {
    override fun toString(): String =
        "${super.action}(endPoint=$endPoint, status=$status, message=$message, issues=$issues, treeData='...')"
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
    val languageId:String,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest")

data class MessageLineTokens(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val startLine:Int,
    val lineTokens: List<List<AglToken>>,
) : AglWorkerMessageResponse("MessageLineTokens") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint, status=$status, message=$message, lineTokens='...'))"
}

data class MessageSetStyle(
    override val endPoint: EndPointIdentity,
    val languageId:String,
    val styleStr: String
) : AglWorkerMessage("MessageSetStyle") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint, styleStr='...')"
}

data class MessageSetStyleResponse(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val styleModel: AglStyleModel?
) : AglWorkerMessageResponse("MessageSetStyleResult")

data class MessageCodeCompleteRequest(
    override val endPoint: EndPointIdentity,
    val languageId:String,
    val goalRuleName: String?,
    val text: String,
    val position: Int
) : AglWorkerMessage("MessageCodeCompleteRequest")

data class MessageCodeCompleteResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String,
    val issues: List<LanguageIssue>,
    val completionItems: List<CompletionItem>
) : AglWorkerMessageResponse("MessageCodeCompleteResult")

data class MessageGrammarAmbiguityAnalysisRequest(
    override val endPoint: EndPointIdentity,
    val languageId:String,
) : AglWorkerMessage("MessageGrammarAmbiguityAnalysisRequest") {
    override fun toString(): String = "${super.action}(endPoint=$endPoint)"
}

data class MessageGrammarAmbiguityAnalysisResult(
    override val endPoint: EndPointIdentity,
    override val status: MessageStatus,
    val message: String?,
    val issues: List<LanguageIssue>
) : AglWorkerMessageResponse("MessageGrammarAmbiguityAnalysisResult")