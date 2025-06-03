/**
 * Copyright (C) 2025 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.api.*
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel

open class LanguageServiceByCoroutine(
    val scope: CoroutineScope,
    logFunction: LogFunction
) : LanguageService {

    private val _aglRegLock = Mutex()

    // --- LanguageService ---
    override val request: LanguageServiceRequest = object : LanguageServiceRequest {
        override fun processorCreateRequest(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            languageId: LanguageIdentity,
            grammarStr: GrammarString,
            typeModelStr: TypesString?,
            asmTransformStr: TransformString?,
            crossReferenceModelStr: CrossReferenceString?,
            editorOptions: EditorOptions
        ) {
            submit {
                _aglRegLock.withLock {
                    // this calls createLanguageDefinition which uses the Agl.registry and could cause race conditions wrt registered languages
                    direct.processorCreateRequest(endPointIdentity, requestId, languageId, grammarStr, typeModelStr, asmTransformStr, crossReferenceModelStr, editorOptions)
                }
            }
        }

        override fun processorDeleteRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity) {
            submit { direct.processorDeleteRequest(endPointIdentity, requestId, languageId) }
        }

        override fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity, styleStr: StyleString) {
            submit { direct.processorSetStyleRequest(endPointIdentity, requestId, languageId, styleStr) }
        }

        override fun interruptRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity, reason: String) {
            submit { direct.interruptRequest(endPointIdentity, requestId, languageId, "New parse request") }
        }

        override fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            languageId: LanguageIdentity,
            sentence: String,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            submit { direct.sentenceProcessRequest(endPointIdentity, requestId, languageId, sentence, processOptions) }
        }

        override fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            languageId: LanguageIdentity,
            sentence: String,
            position: Int,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            submit { direct.sentenceCodeCompleteRequest(endPointIdentity, requestId, languageId, sentence, position, processOptions) }
        }
    }

    override fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse) {
        responseObjects[endPointIdentity] = response
    }

    // --- Implementation ---
    private val responseObjects = mutableMapOf<EndPointIdentity, LanguageServiceResponse>()

    private val response = object : LanguageServiceResponse {
        override fun processorCreateResponse(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            status: MessageResponseStatus,
            message: String,
            issues: List<LanguageIssue>,
            scannerMatchables: List<Matchable>
        ) {
            responseObjects[endPointIdentity]?.processorCreateResponse(endPointIdentity, requestId, status, message, issues, scannerMatchables)
        }

        override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String) {
            responseObjects[endPointIdentity]?.processorDeleteResponse(endPointIdentity, requestId, status, message)
        }

        override fun processorSetStyleResponse(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            status: MessageResponseStatus,
            message: String,
            issues: List<LanguageIssue>,
            styleModel: AglStyleModel?
        ) {
            responseObjects[endPointIdentity]?.processorSetStyleResponse(endPointIdentity, requestId, status, message, issues, styleModel)
        }

        override fun sentenceScanResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>) {
            responseObjects[endPointIdentity]?.sentenceScanResponse(endPointIdentity, requestId, status, message, issues)
        }

        override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
            responseObjects[endPointIdentity]?.sentenceParseResponse(endPointIdentity, requestId, status, message, issues, tree)
        }

        override fun sentenceLineTokensResponse(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            status: MessageResponseStatus,
            message: String,
            startLine: Int,
            lineTokens: List<List<AglToken>>
        ) {
            responseObjects[endPointIdentity]?.sentenceLineTokensResponse(endPointIdentity, requestId, status, message, startLine, lineTokens)
        }

        override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
            responseObjects[endPointIdentity]?.sentenceSyntaxAnalysisResponse(endPointIdentity, requestId, status, message, issues, asm)
        }

        override fun sentenceSemanticAnalysisResponse(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            status: MessageResponseStatus,
            message: String,
            issues: List<LanguageIssue>,
            asm: Any?
        ) {
            responseObjects[endPointIdentity]?.sentenceSemanticAnalysisResponse(endPointIdentity, requestId, status, message, issues, asm)
        }

        override fun sentenceCodeCompleteResponse(
            endPointIdentity: EndPointIdentity,
            requestId: RequestIdentity<*>,
            status: MessageResponseStatus,
            message: String,
            issues: List<LanguageIssue>,
            offset: Int,
            completionItems: List<CompletionItem>
        ) {
            responseObjects[endPointIdentity]?.sentenceCodeCompleteResponse(endPointIdentity, requestId, status, message, issues, offset, completionItems)
        }
    }

    // languageId -> def
    private val direct = LanguageServiceRequestDirectExecution(response, logFunction)

    private fun submit(task: suspend () -> Unit) {
        scope.async { task()  }
    }

}