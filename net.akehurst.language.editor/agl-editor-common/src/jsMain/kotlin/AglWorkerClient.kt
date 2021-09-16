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

import net.akehurst.language.api.parser.InputLocation
import org.w3c.dom.*
import org.w3c.dom.events.EventTarget

class AglWorkerClient(
        val workerScriptName: String,
        val sharedWorker:Boolean
) {

    lateinit var worker: AbstractWorker
    var setStyleResult: (success: Boolean, message: String) -> Unit = { _, _ -> }
    var processorCreateSuccess: (message: String) -> Unit = { _ -> }
    var processorCreateFailure: (message: String) -> Unit = { _ -> }
    var parseStart: () -> Unit = {  }
    var parseSuccess: (tree: Any) -> Unit = { _ -> }
    var parseFailure: (message: String, location: InputLocation?, expected: Array<String>, tree: Any?) -> Unit = { _, _, _, _ -> }
    var lineTokens: (MessageLineTokens) -> Unit = { _ -> }
    var processStart: () -> Unit = {  }
    var processSuccess: (tree: Any) -> Unit = { _ -> }
    var processFailure: (message: String) -> Unit = { _ -> }

    fun initialise() {
        // currently can't make SharedWorker work
        this.worker = if(this.sharedWorker) {
            SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
        } else {
            Worker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
        }
        this.worker.onerror = {
            console.error(it)
        }
        val tgt:EventTarget = if(this.sharedWorker) (this.worker as SharedWorker).port else this.worker as Worker
        tgt.addEventListener("message", { ev ->
            val msg = (ev as MessageEvent).data.asDynamic()
            when (msg.action) {
                "MessageSetStyleResult" -> this.setStyleResult(msg.success, msg.message)
                "MessageProcessorCreateSuccess" -> this.processorCreateSuccess(msg.message)
                "MessageProcessorCreateFailure" -> this.processorCreateFailure(msg.message)
                "MessageParseStart" -> this.parseStart()
                "MessageParseSuccess" -> this.parseSuccess(msg.tree)
                "MessageParseFailure" -> this.parseFailure(msg.message, msg.location, msg.expected, msg.tree)
                "MessageLineTokens" -> this.lineTokens(MessageLineTokens.fromJsObject(msg))
                "MessageProcessStart" -> this.processStart()
                "MessageProcessSuccess" -> this.processSuccess(msg.asm)
                "MessageProcessFailure" -> this.processFailure(msg.message)
                else -> error("Unknown Message type")
            }
        }, objectJS {  })
        //need to explicitly start because used addEventListener
        if(this.sharedWorker) {
            (this.worker as SharedWorker).port.start()
        } else {
            this.worker as Worker
        }
    }

    fun sendToWorker(msg: AglWorkerMessage, transferables: Array<dynamic> = emptyArray()) {
        if(this.sharedWorker) {
            (this.worker as SharedWorker).port.postMessage(msg.toObjectJS(), transferables)
        } else {
            (this.worker as Worker).postMessage(msg.toObjectJS(), transferables)
        }
    }

    fun createProcessor(languageId: String, editorId: String, grammarStr: String?) {
        this.sendToWorker(MessageProcessorCreate(languageId, editorId, grammarStr))
    }

    fun interrupt(languageId: String, editorId: String) {
        this.sendToWorker(MessageParserInterruptRequest(languageId, editorId, "New parse request"))
    }

    fun tryParse(languageId: String, editorId: String, goalRuleName:String?, sentence: String) {
        this.sendToWorker(MessageParseRequest(languageId, editorId, goalRuleName, sentence))
    }

    fun setStyle(languageId: String, editorId: String, css: String) {
        this.sendToWorker(MessageSetStyle(languageId, editorId, css))
    }

}