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

import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.editor.common.AglToken

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
) : AglWorkerMessage("MessageProcessorCreate")
data class MessageProcessorCreateResponse(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
    val message: String
) : AglWorkerMessage("MessageProcessorCreateResponse")

data class MessageSyntaxAnalyserConfigure(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val configuration: Any?
) : AglWorkerMessage("MessageSyntaxAnalyserConfigure")
data class MessageSyntaxAnalyserConfigureResponse(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
    val message: String,
    val issues: List<LanguageIssue>
) : AglWorkerMessage("MessageSyntaxAnalyserConfigureResponse")


data class MessageProcessRequest(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val context: Any?
) : AglWorkerMessage("MessageProcessRequest")

data class MessageParseResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
    val message: String,
    val treeSerialised: String?, // custom serialisation because auto serialisation of SPPT impl classes is too complex
    val issues: List<LanguageIssue>
) : AglWorkerMessage("MessageParseResult")

data class MessageSyntaxAnalysisResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
    val message: String,
    val asm: Any?,
    val issues: List<LanguageIssue>
) : AglWorkerMessage("MessageSyntaxAnalysisResult")

data class MessageSemanticAnalysisResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
    val message: String,
    val issues: List<LanguageIssue>
) : AglWorkerMessage("MessageSemanticAnalysisResult")

data class MessageParserInterruptRequest(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest")

data class MessageLineTokens(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
    val message: String,
    val lineTokens: Array<Array<AglToken>>,
) : AglWorkerMessage("MessageLineTokens")

data class MessageSetStyle(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val css: String
) : AglWorkerMessage("MessageSetStyle")

data class MessageSetStyleResult(
    override val languageId: String, override val editorId: String, override val sessionId: String,
    val success: Boolean,
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
    val success: Boolean,
    val message: String,
    val completionItems: Array<Pair<String, String>>?
) : AglWorkerMessage("MessageCodeCompleteResult")