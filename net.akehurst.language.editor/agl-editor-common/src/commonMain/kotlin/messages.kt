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
    val action: String,
    val languageId: String,
    val editorId: String,
    val sessionId: String
)

class MessageProcessorCreate(
    languageId: String, editorId: String, sessionId: String,
    val grammarStr: String?
) : AglWorkerMessage("MessageProcessorCreate", languageId, editorId, sessionId)

class MessageSyntaxAnalyserConfigure(
    languageId: String, editorId: String, sessionId: String,
    val configuration: Any?
) : AglWorkerMessage("MessageSyntaxAnalyserConfigure", languageId, editorId, sessionId)

class MessageProcessorCreateResponse(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String
) : AglWorkerMessage("MessageProcessorCreateResponse", languageId, editorId, sessionId)

class MessageProcessRequest(
    languageId: String, editorId: String, sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val context: Any?
) : AglWorkerMessage("MessageProcessRequest", languageId, editorId, sessionId)

class MessageParseResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val tree: Any?,
    val issues: Array<LanguageIssue>
) : AglWorkerMessage("MessageParseResult", languageId, editorId, sessionId)

class MessageSyntaxAnalysisResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val asm: Any?,
    val issues: Array<LanguageIssue>
) : AglWorkerMessage("MessageSyntaxAnalysisResult", languageId, editorId, sessionId)

class MessageSemanticAnalysisResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val issues: Array<LanguageIssue>
) : AglWorkerMessage("MessageSemanticAnalysisResult", languageId, editorId, sessionId)

class MessageParserInterruptRequest(
    languageId: String, editorId: String, sessionId: String,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest", languageId, editorId, sessionId)

class MessageLineTokens(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val lineTokens: Array<Array<AglToken>>,
) : AglWorkerMessage("MessageLineTokens", languageId, editorId, sessionId)

class MessageSetStyle(
    languageId: String, editorId: String, sessionId: String,
    val css: String
) : AglWorkerMessage("MessageSetStyle", languageId, editorId, sessionId)

class MessageSetStyleResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String
) : AglWorkerMessage("MessageSetStyleResult", languageId, editorId, sessionId)

class MessageCodeCompleteRequest(
    languageId: String, editorId: String, sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val position: Int
) : AglWorkerMessage("MessageCodeCompleteRequest", languageId, editorId, sessionId)

class MessageCodeCompleteResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val completionItems: Array<Pair<String, String>>?
) : AglWorkerMessage("MessageCodeCompleteResult", languageId, editorId, sessionId)