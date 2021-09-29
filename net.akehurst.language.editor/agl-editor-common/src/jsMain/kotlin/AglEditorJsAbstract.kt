/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
import net.akehurst.language.editor.api.*
import org.w3c.dom.Element

abstract class AglEditorJsAbstract(
    val element: Element,
    languageId: String,
    editorId: String,
    workerScriptName: String,
    sharedWorker: Boolean
) : AglEditorAbstract(languageId, editorId) {

    protected abstract fun resetTokenization()
    protected abstract fun doBackgroundTryParse()
    protected abstract fun parseFailure(message: String, location: InputLocation?, expected: Array<String>?, tree: Any?)

    protected var aglWorker = AglWorkerClient(super.agl, workerScriptName, sharedWorker)
    protected lateinit var workerTokenizer: AglTokenizerByWorker

    // must be called by subclass
    protected fun connectWorker(workerTokenizer: AglTokenizerByWorker ) {
        this.workerTokenizer = workerTokenizer
        this.aglWorker.initialise()
        this.aglWorker.setStyleResult = { message -> if (message.success) this.resetTokenization() else this.log(LogLevel.Error, message.message) }
        this.aglWorker.processorCreateResult = this::processorCreateResult
        this.aglWorker.parseStart = { this.notifyParse(ParseEventStart()) }
        this.aglWorker.parseResult = this::parseResult
        this.aglWorker.lineTokens = {
            if (it.success) {
                this.log(LogLevel.Debug, "Debug: new line tokens from successful parse of ${editorId}")
                this.workerTokenizer.receiveTokens(it.lineTokens)
                this.resetTokenization()
            } else {
                this.log(LogLevel.Error, "LineTokens - ${it.message}")
            }
        }
        this.aglWorker.syntaxAnalysisStart = { this.notifySyntaxAnalysis(SyntaxAnalysisEvent(true, "Start", null)) }
        this.aglWorker.syntaxAnalysisResult= { message ->
            if(message.success) {
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(true, "Success", message.asm))
            } else {
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(false, message.message, null))
            }
        }
        this.aglWorker.semanticAnalysisStart = { this.notifySemanticAnalysis(SemanticAnalysisEvent(true, "Start", null)) }
        this.aglWorker.semanticAnalysisResult = { message ->
            if(message.success) {
                this.notifySemanticAnalysis(SemanticAnalysisEvent(true, "Success", message.items))
            } else {
                this.notifySemanticAnalysis(SemanticAnalysisEvent(false, message.message, null))
            }
        }
    }

    private fun processorCreateResult(message: MessageProcessorCreateResponse) {
        if (message.success) {
            when (message.message) {
                "OK" -> {
                    this.log(LogLevel.Debug, "New Processor created for ${editorId}")
                    this.workerTokenizer.acceptingTokens = true
                    this.doBackgroundTryParse()
                    this.resetTokenization()
                }
                "reset" -> {
                    this.log(LogLevel.Debug, "Reset Processor for ${editorId}")
                }
                else -> {
                    this.log(LogLevel.Error, "Unknown result message from create Processor for ${editorId}: $message")
                }
            }
        } else {
            this.log(LogLevel.Error, "Failed to create processor ${message.message}")
        }
    }

    private fun parseResult(event: MessageParseResult) {
        if (event.success) {
            this.parseSuccess(event.tree!!)
        } else {
            this.parseFailure(event.message, event.location, event.expected, event.tree)
        }
    }

    protected fun parseSuccess(tree: Any) {
        this.resetTokenization()
        val event = ParseEventSuccess(tree)
        this.notifyParse(event)
    }
}