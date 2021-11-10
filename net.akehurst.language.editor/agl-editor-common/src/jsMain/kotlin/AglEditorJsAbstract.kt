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

import net.akehurst.kotlin.json.JsonString
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.messages.*
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
    protected abstract fun createIssueMarkers(issues: List<LanguageIssue>)

    protected var aglWorker = AglWorkerClient(super.agl, workerScriptName, sharedWorker)
    protected lateinit var workerTokenizer: AglTokenizerByWorker

    // must be called by subclass
    protected fun connectWorker(workerTokenizer: AglTokenizerByWorker) {
        this.workerTokenizer = workerTokenizer
        this.aglWorker.initialise()
        this.aglWorker.setStyleResult = { message -> if (message.success) this.resetTokenization() else this.log(LogLevel.Error, message.message,null) }
        this.aglWorker.processorCreateResult = this::processorCreateResult
        this.aglWorker.syntaxAnalyserConfigureResult = this::syntaxAnalyserConfigureResult
        this.aglWorker.parseResult = this::parseResult
        this.aglWorker.lineTokens = this::lineTokens
        this.aglWorker.syntaxAnalysisResult = this::syntaxAnalysisResult
        this.aglWorker.semanticAnalysisResult = this::semanticAnalysisResult
    }

    private fun processorCreateResult(message: MessageProcessorCreateResponse) {
        if (message.success) {
            when (message.message) {
                "OK" -> {
                    this.log(LogLevel.Debug, "New Processor created for ${editorId}",null)
                    this.workerTokenizer.acceptingTokens = true
                    this.doBackgroundTryParse()
                    this.resetTokenization()
                }
                "reset" -> {
                    this.log(LogLevel.Debug, "Reset Processor for ${editorId}",null)
                }
                else -> {
                    this.log(LogLevel.Error, "Unknown result message from create Processor for ${editorId}: $message",null)
                }
            }
        } else {
            this.log(LogLevel.Error, "Failed to create processor ${message.message}",null)
        }
    }

    private fun syntaxAnalyserConfigureResult(message:MessageSyntaxAnalyserConfigureResponse) {
        if (message.success) {
            //?
        } else {
            this.log(LogLevel.Error, "SyntaxAnalyserConfigure failed for ${this.languageIdentity}: ${message.message}",null)
        }
        this.createIssueMarkers(message.issues.toList())
    }

    private fun lineTokens(event: MessageLineTokens) {
        if (event.success) {
            this.log(LogLevel.Debug, "Debug: new line tokens from successful parse of ${editorId}",null)
            this.workerTokenizer.receiveTokens(event.lineTokens)
            this.resetTokenization()
        } else {
            this.log(LogLevel.Error, "LineTokens - ${event.message}",null)
        }
    }

    private fun parseResult(event: MessageParseResult) {
        if (event.success) {
            this.resetTokenization()
            this.createIssueMarkers(event.issues.toList())
            val treeStr = event.treeSerialised
            val treeJS = treeStr?.let {
                val unescaped = JsonString.decode(it) // double decode the string as it itself is json
                JSON.parse<Any>(unescaped)
            }
            this.notifyParse(ParseEvent(true, "Success", treeJS, event.issues.toList()))
        } else {
            if ("Start" == event.message) {
                this.notifyParse(ParseEvent(false, "Start", null, emptyList()))
            } else {
                // a failure to parse is not an 'error' in the editor - we expect some parse failures
                this.log(LogLevel.Debug, "Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: ${event.message}",null)
                // parse failed so re-tokenize from scan
                this.workerTokenizer.reset()
                this.resetTokenization()
                this.createIssueMarkers(event.issues.toList())
                this.notifyParse(ParseEvent(false, event.message, null, event.issues.toList()))
            }
        }
    }

    private fun syntaxAnalysisResult(event: MessageSyntaxAnalysisResult) {
        if (event.success) {
            this.createIssueMarkers(event.issues.toList())
            this.notifySyntaxAnalysis(SyntaxAnalysisEvent(true, "Success", event.asm, event.issues.toList()))
        } else {
            if ("Start" == event.message) {
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(false, "Start", null, emptyList()))
            } else {
                this.createIssueMarkers(event.issues.toList())
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(false, event.message, event.asm, event.issues.toList()))
            }
        }
    }

    private fun semanticAnalysisResult(event: MessageSemanticAnalysisResult) {
        if (event.success) {
            this.createIssueMarkers(event.issues.toList())
            this.notifySemanticAnalysis(SemanticAnalysisEvent(true, "Success", event.issues.toList()))
        } else {
            if ("Start" == event.message) {
                this.notifySemanticAnalysis(SemanticAnalysisEvent(false, "Start", emptyList()))
            } else {
                this.createIssueMarkers(event.issues.toList())
                this.notifySemanticAnalysis(SemanticAnalysisEvent(false, event.message, event.issues.toList()))
            }
        }
    }

}