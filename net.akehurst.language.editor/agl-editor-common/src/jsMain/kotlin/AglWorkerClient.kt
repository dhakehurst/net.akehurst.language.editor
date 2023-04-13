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

package net.akehurst.language.editor.common

import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.messages.*
import org.w3c.dom.*
import org.w3c.dom.events.EventTarget

class AglWorkerClient<AsmType : Any, ContextType : Any>(
    val agl: AglComponents<AsmType, ContextType>,
    val workerScriptName: String,
    val sharedWorker: Boolean
) {

    lateinit var worker: AbstractWorker
    var setStyleResult: (message: MessageSetStyleResult) -> Unit = { _ -> }
    var processorCreateResult: (message: MessageProcessorCreateResponse) -> Unit = { _ -> }
    var syntaxAnalyserConfigureResult: (message: MessageSyntaxAnalyserConfigureResponse) -> Unit = { _ -> }
    var parseResult: (message: MessageParseResult) -> Unit = { _ -> }
    var lineTokens: (message: MessageLineTokens) -> Unit = { _ -> }
    var syntaxAnalysisResult: (message: MessageSyntaxAnalysisResult) -> Unit = { _ -> }
    var semanticAnalysisResult: (message: MessageSemanticAnalysisResult) -> Unit = { _ -> }
    var codeCompleteResult: (message: MessageCodeCompleteResult) -> Unit = { _ -> }

    fun initialise() {
        this.worker = if (this.sharedWorker) {
            SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
        } else {
            Worker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
        }
        this.worker.onerror = {
            this.agl.logger.log(LogLevel.Error, it.toString(), null)
        }
        val tgt: EventTarget = if (this.sharedWorker) (this.worker as SharedWorker).port else this.worker as Worker
        tgt.addEventListener("message", { ev ->
            try {
                val data = (ev as MessageEvent).data
                if (data is String) {
                    val str = (ev as MessageEvent).data as String
                    if (str.startsWith("Error:")) {
                        this.agl.logger.log(LogLevel.Error, str.substringAfter("Error:"), null)
                    } else {
                        //val msg: AglWorkerMessage? = AglWorkerMessage.deserialise(str)
                        val msg: AglWorkerMessage? = AglWorkerSerialisation.deserialise(str)
                        if (null == msg) {
                            this.agl.logger.log(LogLevel.Error, "Message from Worker not handled: $str", null)
                        } else {
                            this.receiveMessageFromWorker(msg)
                        }
                    }
                } else {
                    this.agl.logger.log(LogLevel.Error, "Handling message from Worker, data content should be a String, got - '${ev.data}'", null)
                }
            } catch (e: Throwable) {
                this.agl.logger.log(LogLevel.Error, "Handling message from Worker", e)
            }
        }, objectJS { })
        //need to explicitly start because used addEventListener
        if (this.sharedWorker) {
            (this.worker as SharedWorker).port.start()
        } else {
            this.worker as Worker
        }
    }

    private fun receiveMessageFromWorker(msg: AglWorkerMessage) {
        this.agl.logger.log(LogLevel.Trace, "Received message: $msg",null)
        if (this.agl.languageIdentity==msg.languageId && this.agl.editorId == msg.editorId) { //TODO: should  test for sessionId also
            when (msg) {
                is MessageSetStyleResult -> this.setStyleResult(msg)
                is MessageProcessorCreateResponse -> this.processorCreateResult(msg)
                is MessageSyntaxAnalyserConfigureResponse -> this.syntaxAnalyserConfigureResult(msg)
                is MessageParseResult -> this.parseResult(msg)
                is MessageLineTokens -> this.lineTokens(msg)
                is MessageSyntaxAnalysisResult -> this.syntaxAnalysisResult(msg)
                is MessageSemanticAnalysisResult -> this.semanticAnalysisResult(msg)
                is MessageCodeCompleteResult -> this.codeCompleteResult(msg)
                else -> error("Unknown Message type")
            }
        } else {
            //msg for different editor or language changed and no longer relevant
        }
    }

    fun sendToWorker(msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        //val jsObj = msg.toJsObject()
        //val str = AglWorkerMessage.serialise(msg)
        this.agl.logger.log(LogLevel.Trace, "Sending message: $msg",null)
        val str = AglWorkerSerialisation.serialise(msg)
        if (this.sharedWorker) {
            (this.worker as SharedWorker).port.postMessage(str, transferables)
        } else {
            (this.worker as Worker).postMessage(str, transferables)
        }
    }

    fun createProcessor(languageId: String, editorId: String, sessionId: String, grammarStr: String?) {
        this.sendToWorker(MessageProcessorCreate(languageId, editorId, sessionId, grammarStr))
    }

    fun configureSyntaxAnalyser(languageId: String, editorId: String, sessionId: String, configuration: Map<String,Any>) {
        this.sendToWorker(MessageSyntaxAnalyserConfigure(languageId, editorId, sessionId, configuration))
    }

    fun interrupt(languageId: String, editorId: String, sessionId: String) {
        this.sendToWorker(MessageParserInterruptRequest(languageId, editorId, sessionId, "New parse request"))
    }

    fun processSentence(languageId: String, editorId: String, sessionId: String, goalRuleName: String?, sentence: String, context: Any?) {
        this.sendToWorker(MessageProcessRequest(languageId, editorId, sessionId, goalRuleName, sentence, context))
    }

    fun setStyle(languageId: String, editorId: String, sessionId: String, css: String) {
        this.sendToWorker(MessageSetStyle(languageId, editorId, sessionId, css))
    }

}