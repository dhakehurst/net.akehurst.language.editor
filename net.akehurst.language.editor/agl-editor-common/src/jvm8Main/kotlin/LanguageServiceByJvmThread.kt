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

import net.akehurst.language.agl.language.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.language.grammar.ContextFromGrammarRegistry
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.api.processor.*
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.messages.*
import java.util.concurrent.ExecutorService

open class LanguageServiceByJvmThread(
    val executorService: ExecutorService
) : LanguageService {

    // --- LanguageService ---
    override val request: LanguageServiceRequest = object : LanguageServiceRequest {
        override fun processorCreateRequest(endPointIdentity: EndPointIdentity, languageId: String, grammarStr: String, crossReferenceModelStr: String?, editorOptions: EditorOptions) {
            submit { direct.processorCreateRequest(endPointIdentity, languageId, grammarStr, crossReferenceModelStr, editorOptions) }
        }

        override fun processorDeleteRequest(endPointIdentity: EndPointIdentity) {
            submit { direct.processorDeleteRequest(endPointIdentity) }
        }

        override fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, languageId: String, styleStr: String) {
            submit { direct.processorSetStyleRequest(endPointIdentity, languageId, styleStr) }
        }

        override fun interruptRequest(endPointIdentity: EndPointIdentity, languageId: String, reason: String) {
            submit { direct.interruptRequest(endPointIdentity, languageId, "New parse request") }
        }

        override fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
            endPointIdentity: EndPointIdentity,
            languageId: String,
            text: String,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            submit { direct.sentenceProcessRequest(endPointIdentity, languageId, text, processOptions) }
        }

        override fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
            endPointIdentity: EndPointIdentity,
            languageId: String,
            text: String,
            position: Int,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            submit { direct.sentenceCodeCompleteRequest(endPointIdentity, languageId, text, position, processOptions) }
        }
    }

    override fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse) {
        responseObjects[endPointIdentity] = response
    }

    // --- Implementation ---
    private val responseObjects = mutableMapOf<EndPointIdentity, LanguageServiceResponse>()

    // languageId -> def
    private val direct = LanguageServiceDirectExecution(
        object : LanguageServiceResponse {
            override fun processorCreateResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>) {
                responseObjects[endPointIdentity]?.processorCreateResponse(endPointIdentity, status, message, issues, scannerMatchables)
            }

            override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String) {
                responseObjects[endPointIdentity]?.processorDeleteResponse(endPointIdentity, status, message)
            }

            override fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, styleModel: AglStyleModel?) {
                responseObjects[endPointIdentity]?.processorSetStyleResponse(endPointIdentity, status, message, styleModel)
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
    )

    private fun submit(task: () -> Unit) {
        this.executorService.submit(task)
    }

}