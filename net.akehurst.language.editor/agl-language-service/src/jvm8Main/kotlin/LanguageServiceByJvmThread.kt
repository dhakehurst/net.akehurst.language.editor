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

package net.akehurst.language.editor.common

import net.akehurst.language.agl.CrossReferenceString
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.agl.StyleString
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.language.service.LanguageServiceRequestDirectExecution
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel
import java.util.concurrent.ExecutorService

open class LanguageServiceByJvmThread(
    val executorService: ExecutorService,
    logFunction: LogFunction?
) : LanguageService {

    // --- LanguageService ---
    override val request: LanguageServiceRequest = object : LanguageServiceRequest {
        override fun processorCreateRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, grammarStr: GrammarString, crossReferenceModelStr: CrossReferenceString?, editorOptions: EditorOptions) {
            submit { direct.processorCreateRequest(endPointIdentity, languageId, grammarStr, crossReferenceModelStr, editorOptions) }
        }

        override fun processorDeleteRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity) {
            submit { direct.processorDeleteRequest(endPointIdentity, languageId) }
        }

        override fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, styleStr: StyleString) {
            submit { direct.processorSetStyleRequest(endPointIdentity, languageId, styleStr) }
        }

        override fun interruptRequest(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, reason: String) {
            submit { direct.interruptRequest(endPointIdentity, languageId, "New parse request") }
        }

        override fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
            endPointIdentity: EndPointIdentity,
            languageId: LanguageIdentity,
            sentence: String,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            submit { direct.sentenceProcessRequest(endPointIdentity, languageId, sentence, processOptions) }
        }

        override fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
            endPointIdentity: EndPointIdentity,
            languageId: LanguageIdentity,
            sentence: String,
            position: Int,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            submit { direct.sentenceCodeCompleteRequest(endPointIdentity, languageId, sentence, position, processOptions) }
        }
    }

    override fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse) {
        responseObjects[endPointIdentity] = response
    }

    // --- Implementation ---
    private val responseObjects = mutableMapOf<EndPointIdentity, LanguageServiceResponse>()

    private val response = object : LanguageServiceResponse {
        override fun processorCreateResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>) {
            responseObjects[endPointIdentity]?.processorCreateResponse(endPointIdentity, status, message, issues, scannerMatchables)
        }

        override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String) {
            responseObjects[endPointIdentity]?.processorDeleteResponse(endPointIdentity, status, message)
        }

        override fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleModel?) {
            responseObjects[endPointIdentity]?.processorSetStyleResponse(endPointIdentity, status, message, issues, styleModel)
        }

        override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
            responseObjects[endPointIdentity]?.sentenceParseResponse(endPointIdentity, status, message, issues, tree)
        }

        override fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>) {
            responseObjects[endPointIdentity]?.sentenceLineTokensResponse(endPointIdentity, status, message, startLine, lineTokens)
        }

        override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
            responseObjects[endPointIdentity]?.sentenceSyntaxAnalysisResponse(endPointIdentity, status, message, issues, asm)
        }

        override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
            responseObjects[endPointIdentity]?.sentenceSemanticAnalysisResponse(endPointIdentity, status, message, issues, asm)
        }

        override fun sentenceCodeCompleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems: List<CompletionItem>) {
            responseObjects[endPointIdentity]?.sentenceCodeCompleteResponse(endPointIdentity, status, message, issues, completionItems)
        }
    }

    // languageId -> def
    private val direct = LanguageServiceRequestDirectExecution(response, logFunction)

    private fun submit(task: () -> Unit) {
        this.executorService.submit(task)
    }

}