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
import org.w3c.dom.*
import org.w3c.dom.events.EventTarget

class AglWorkerClient(
    val agl: AglComponents,
    val workerScriptName: String,
    val sharedWorker: Boolean
) {

    lateinit var worker: AbstractWorker
    var setStyleResult: (message: MessageSetStyleResult) -> Unit = { _ -> }
    var processorCreateResult: (message: MessageProcessorCreateResponse) -> Unit = { _ -> }
    var parseStart: (message: MessageParseStart) -> Unit = { }
    var parseResult: (message: MessageParseResult) -> Unit = { _ -> }
    var lineTokens: (message: MessageLineTokens) -> Unit = { _ -> }
    var syntaxAnalysisStart: (message: MessageSyntaxAnalysisStart) -> Unit = { }
    var syntaxAnalysisResult: (message: MessageSyntaxAnalysisResult) -> Unit = { _ -> }
    var semanticAnalysisStart: (message: MessageSemanticAnalysisStart) -> Unit = { }
    var semanticAnalysisResult: (message: MessageSemanticAnalysisResult) -> Unit = { _ -> }
    var codeCompleteResult: (message: MessageCodeCompleteResult) -> Unit = { _ -> }

    fun initialise() {
        this.worker = if (this.sharedWorker) {
            SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
        } else {
            Worker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
        }
        this.worker.onerror = {
            this.agl.logger.log(LogLevel.Error, it.toString())
        }
        val tgt: EventTarget = if (this.sharedWorker) (this.worker as SharedWorker).port else this.worker as Worker
        tgt.addEventListener("message", { ev ->
            val jsObj = (ev as MessageEvent).data.asDynamic()
            val msg: AglWorkerMessage? = AglWorkerMessage.fromJsObject(jsObj)
            if (null == msg) {
                this.agl.logger.log(LogLevel.Error, "Worker message not handled: $jsObj")
            } else {
                if (this.agl.editorId == msg.editorId) { //TODO: should  test for sessionId also
                    when (msg) {
                        is MessageSetStyleResult -> this.setStyleResult(msg)
                        is MessageProcessorCreateResponse -> this.processorCreateResult(msg)
                        is MessageParseStart -> this.parseStart(msg)
                        is MessageParseResult -> this.parseResult(msg)
                        is MessageLineTokens -> this.lineTokens(msg)
                        is MessageSyntaxAnalysisStart -> this.syntaxAnalysisStart(msg)
                        is MessageSyntaxAnalysisResult -> this.syntaxAnalysisResult(msg)
                        is MessageSemanticAnalysisStart -> this.semanticAnalysisStart(msg)
                        is MessageSemanticAnalysisResult -> this.semanticAnalysisResult(msg)
                        is MessageCodeCompleteResult -> this.codeCompleteResult(msg)
                        else -> error("Unknown Message type")
                    }
                } else {
                    //msg for different editor
                }
            }
        }, objectJS { })
        //need to explicitly start because used addEventListener
        if (this.sharedWorker) {
            (this.worker as SharedWorker).port.start()
        } else {
            this.worker as Worker
        }
    }

    fun sendToWorker(msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        val jsObj = msg.toObjectJS()
        if (this.sharedWorker) {
            (this.worker as SharedWorker).port.postMessage(jsObj, transferables)
        } else {
            (this.worker as Worker).postMessage(jsObj, transferables)
        }
    }

    fun createProcessor(languageId: String, editorId: String, sessionId: String, grammarStr: String?) {
        this.sendToWorker(MessageProcessorCreate(languageId, editorId, sessionId, grammarStr))
    }

    fun interrupt(languageId: String, editorId: String, sessionId: String) {
        this.sendToWorker(MessageParserInterruptRequest(languageId, editorId, sessionId, "New parse request"))
    }

    fun tryParse(languageId: String, editorId: String, sessionId: String, goalRuleName: String?, sentence: String, context:Any?) {
        this.sendToWorker(MessageProcessRequest(languageId, editorId, sessionId, goalRuleName, sentence, context))
    }

    fun setStyle(languageId: String, editorId: String, sessionId: String, css: String) {
        this.sendToWorker(MessageSetStyle(languageId, editorId, sessionId, css))
    }

}