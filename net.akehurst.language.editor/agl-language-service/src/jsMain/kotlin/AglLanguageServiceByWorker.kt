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

package net.akehurst.language.editor.language.service

import net.akehurst.kotlin.json.JsonString
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.language.service.messages.*
import org.w3c.dom.AbstractWorker
import org.w3c.dom.MessageEvent
import org.w3c.dom.SharedWorker
import org.w3c.dom.Worker
import org.w3c.dom.events.EventTarget
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

class AglLanguageServiceByWorker(
    val worker: AbstractWorker,
    val logger: AglEditorLogger
) : LanguageService {

    override val request: LanguageServiceRequest = object : LanguageServiceRequest {
        override fun processorCreateRequest(endPointIdentity: EndPointIdentity, languageId: String, grammarStr: String, crossReferenceModelStr: String?, editorOptions: EditorOptions) {
            sendToWorker(MessageProcessorCreate(endPointIdentity, languageId, grammarStr, crossReferenceModelStr, editorOptions))
        }

        override fun processorDeleteRequest(endPointIdentity: EndPointIdentity) {
            TODO("not implemented")
        }

        override fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, languageId: String, styleStr: String) {
            sendToWorker(MessageSetStyle(endPointIdentity, languageId, styleStr))
        }

        override fun interruptRequest(endPointIdentity: EndPointIdentity, languageId: String, reason: String) {
            sendToWorker(MessageParserInterruptRequest(endPointIdentity, languageId, reason))
        }

        override fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
            endPointIdentity: EndPointIdentity,
            languageId: String,
            text: String,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            sendToWorker(MessageProcessRequest(endPointIdentity, languageId, text, processOptions))
        }

        override fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
            endPointIdentity: EndPointIdentity,
            languageId: String,
            text: String,
            position: Int,
            processOptions: ProcessOptions<AsmType, ContextType>
        ) {
            sendToWorker(MessageCodeCompleteRequest(endPointIdentity, languageId, text, position, processOptions))
        }

    }

    override fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse) {
        responseObjects[endPointIdentity] = response
    }

    // --- Implementation ---
    private val sharedWorker: Boolean = this.worker is SharedWorker
    private val responseObjects = mutableMapOf<EndPointIdentity, LanguageServiceResponse>()

    init {
        this.worker.onerror = {
            this.logger.log(LogLevel.Error, it.toString(), null)
        }
        val tgt: EventTarget = if (this.sharedWorker) (this.worker as SharedWorker).port else this.worker as Worker
        tgt.addEventListener("message", { ev ->
            try {
                val data = (ev as MessageEvent).data
                if (data is String) {
                    val str = ev.data as String
                    when {
                        str.startsWith("Error:") -> this.logger.log(LogLevel.Error, str.substringAfter("Error:"), null)
                        str.startsWith("Info:") -> this.logger.log(LogLevel.Information, str.substringAfter("Info:"), null)

                        else -> {
                            val tv = measureTimedValue {
                                AglWorkerSerialisation.deserialise<AglWorkerMessage>(str)
                            }
                            this.logger.log(LogLevel.Debug, "Deserialisation of worker message (length=${str.length}) took ${tv.duration.toString(DurationUnit.MILLISECONDS)} ms", null)
                            val msg: AglWorkerMessage = tv.value
                            this.receiveMessageFromWorker(msg)
                        }
                    }
                } else {
                    this.logger.log(LogLevel.Error, "Handling message from Worker, data content should be a String, got - '${ev.data}'", null)
                }
            } catch (e: Throwable) {
                this.logger.log(LogLevel.Error, "Handling message from Worker", e)
            }
        }, objectJS { })
        //need to explicitly start because used addEventListener
        if (this.sharedWorker) {
            (this.worker as SharedWorker).port.start()
        } else {
            this.worker as Worker
        }
    }

    private fun sendToWorker(msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        //val jsObj = msg.toJsObject()
        //val str = AglWorkerMessage.serialise(msg)
        this.logger.log(LogLevel.Trace, "Sending message: $msg", null)
        val tv = measureTimedValue { AglWorkerSerialisation.serialise(msg) }
        this.logger.log(LogLevel.Trace, "Serialisation took ${tv.duration.toString(DurationUnit.MILLISECONDS)}", null)
        val str = tv.value
        if (this.sharedWorker) {
            (this.worker as SharedWorker).port.postMessage(str, transferables)
        } else {
            (this.worker as Worker).postMessage(str, transferables)
        }
    }

    private fun receiveMessageFromWorker(msg: AglWorkerMessage) {
        this.logger.log(LogLevel.Trace, "Received message: $msg", null)
        val endPoint = responseObjects[msg.endPoint]
        if (null != endPoint) { //TODO: should  test for sessionId also
            when (msg) {
                is MessageProcessorCreateResponse -> endPoint.processorCreateResponse(msg.endPoint, msg.status, msg.message, msg.issues, msg.scannerMatchables)
                is MessageSetStyleResponse -> endPoint.processorSetStyleResponse(msg.endPoint, msg.status, msg.message, msg.issues, msg.styleModel)
                is MessageLineTokens -> endPoint.sentenceLineTokensResponse(msg.endPoint, msg.status, msg.message, msg.startLine, msg.lineTokens)
                is MessageParseResult -> endPoint.sentenceParseResponse(msg.endPoint, msg.status, msg.message, msg.issues, deserialiseParseTree(msg.treeSerialised))
                is MessageSyntaxAnalysisResult -> endPoint.sentenceSyntaxAnalysisResponse(msg.endPoint, msg.status, msg.message, msg.issues, msg.asm)
                is MessageSemanticAnalysisResult -> endPoint.sentenceSemanticAnalysisResponse(msg.endPoint, msg.status, msg.message, msg.issues, msg.asm)
                is MessageCodeCompleteResult -> endPoint.sentenceCodeCompleteResponse(msg.endPoint, msg.status, msg.message, msg.issues, msg.completionItems)
                else -> error("Unknown Message type")
            }
        } else {
            // TODO: log something !
            //msg for different editor or language changed and no longer relevant
        }
    }

    private fun deserialiseParseTree(treeSerialised: String?): Any? {
        val tree = treeSerialised?.let {
            //this.agl.languageDefinition.processor!!.spptParser.parse(treeStr)
            val unescaped = JsonString.decode(it) // double decode the string as it itself is json
            JSON.parse<Any>(unescaped)
        }
        return tree
    }

}