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
import net.akehurst.language.api.sppt.SPPTParser
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.messages.*
import org.w3c.dom.AbstractWorker
import org.w3c.dom.Element

abstract class AglEditorJsAbstract<AsmType : Any, ContextType : Any>(
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    worker: AbstractWorker
) : AglEditorAbstract<AsmType, ContextType>(languageId, editorId, logFunction) {

    protected abstract fun resetTokenization()
    protected abstract fun createIssueMarkers(issues: List<LanguageIssue>)

    protected var aglWorker = AglWorkerClient(super.agl, worker)
    protected lateinit var workerTokenizer: AglTokenizerByWorker

    // must be called by subclass
    protected fun connectWorker(workerTokenizer: AglTokenizerByWorker) {
        this.workerTokenizer = workerTokenizer
        this.aglWorker.initialise()
        this.aglWorker.setStyleResult = { message -> if (message.status == MessageStatus.SUCCESS) this.resetTokenization() else this.log(LogLevel.Error, message.message, null) }
        this.aglWorker.processorCreateResult = this::processorCreateResult
        this.aglWorker.syntaxAnalyserConfigureResult = this::syntaxAnalyserConfigureResult
        this.aglWorker.parseResult = this::parseResult
        this.aglWorker.lineTokens = this::lineTokens
        this.aglWorker.syntaxAnalysisResult = this::syntaxAnalysisResult
        this.aglWorker.semanticAnalysisResult = this::semanticAnalysisResult
    }

    private fun processorCreateResult(message: MessageProcessorCreateResponse) {
        if (message.endPoint.editorId == this.editorId && message.endPoint.languageId == this.languageIdentity) {
            if (message.status == MessageStatus.SUCCESS) {
                when (message.message) {
                    "OK" -> {
                        this.log(LogLevel.Debug, "New Processor created for ${editorId}", null)
                        this.workerTokenizer.acceptingTokens = true
                        this.agl.scannerMatchables = message.scannerMatchables
                        this.processSentence()
                        this.resetTokenization()
                    }

                    "reset" -> {
                        this.log(LogLevel.Debug, "Reset Processor for ${editorId}", null)
                    }

                    else -> {
                        this.log(LogLevel.Error, "Unknown result message from create Processor for ${editorId}: $message", null)
                    }
                }
            } else {
                this.log(LogLevel.Error, "Failed to create processor ${message.message}", null)
                message.issues.forEach {
                    this.log(LogLevel.Error, " Issue - ${it}", null)
                }
            }
        } else {
            //ignore because message no longer relevant
        }
    }

    private fun syntaxAnalyserConfigureResult(message: MessageSyntaxAnalyserConfigureResponse) {
        if (message.status == MessageStatus.FAILURE) {
            this.log(LogLevel.Error, "SyntaxAnalyserConfigure failed for ${this.languageIdentity}: ${message.message}", null)
        } else {
            // nothing
        }
        this.createIssueMarkers(message.issues.toList())
    }

    private fun lineTokens(event: MessageLineTokens) {
        if (event.status == MessageStatus.SUCCESS) {
            this.log(LogLevel.Debug, "Debug: new line tokens from successful parse of ${editorId}", null)
            this.workerTokenizer.receiveTokens(event.lineTokens)
            this.resetTokenization()
        } else {
            this.log(LogLevel.Error, "LineTokens - ${event.message}", null)
        }
    }

    private fun parseResult(event: MessageParseResult) {
        when (event.status) {
            MessageStatus.START -> {
                this.notifyParse(ParseEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageStatus.FAILURE -> {
                // a failure to parse is not an 'error' in the editor - we expect some parse failures
                this.log(LogLevel.Debug, "Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: ${event.message}", null)
                // parse failed so re-tokenize from scan
                this.workerTokenizer.reset()
                this.resetTokenization()
                clearErrorMarkers()
                this.createIssueMarkers(event.issues.toList())
                this.notifyParse(ParseEvent(EventStatus.FAILURE, event.message, null, event.issues.toList()))
            }

            MessageStatus.SUCCESS -> {
                this.resetTokenization()
                clearErrorMarkers()
                this.createIssueMarkers(event.issues.toList())
                val treeStr = event.treeSerialised

                val treeJS = treeStr?.let {
                    //this.agl.languageDefinition.processor!!.spptParser.parse(treeStr)
                    val unescaped = JsonString.decode(it) // double decode the string as it itself is json
                    JSON.parse<Any>(unescaped)
                }
                this.notifyParse(ParseEvent(EventStatus.SUCCESS, "Success", treeJS, event.issues.toList()))
            }
        }
    }

    private fun syntaxAnalysisResult(event: MessageSyntaxAnalysisResult) {
        when (event.status) {
            MessageStatus.START -> {
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageStatus.FAILURE -> {
                clearErrorMarkers()
                this.createIssueMarkers(event.issues.toList())
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.FAILURE, event.message, event.asm, event.issues.toList()))
            }

            MessageStatus.SUCCESS -> {
                clearErrorMarkers()
                this.createIssueMarkers(event.issues.toList())
                this.notifySyntaxAnalysis(SyntaxAnalysisEvent(EventStatus.SUCCESS, "Success", event.asm, event.issues.toList()))
            }
        }
    }

    private fun semanticAnalysisResult(event: MessageSemanticAnalysisResult) {
        when (event.status) {
            MessageStatus.START -> {
                this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.START, "Start", null, emptyList()))
            }

            MessageStatus.FAILURE -> {
                clearErrorMarkers()
                this.createIssueMarkers(event.issues.toList())
                this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.FAILURE, event.message, event.asm, event.issues.toList()))
            }

            MessageStatus.SUCCESS -> {
                clearErrorMarkers()
                this.createIssueMarkers(event.issues.toList())
                this.notifySemanticAnalysis(SemanticAnalysisEvent(EventStatus.SUCCESS, "Success", event.asm, event.issues.toList()))
            }
        }
    }

    override fun updateProcessor() {
        val grammarStr = this.agl.languageDefinition.grammarStr
        if (grammarStr.isNullOrBlank()) {
            //do nothing
        } else {
            this.clearErrorMarkers()
            this.aglWorker.createProcessor(
                this.languageIdentity,
                this.editorId,
                this.sessionId,
                grammarStr,
                this.agl.languageDefinition.crossReferenceModelStr
            ) //TODO: sessionId
            this.workerTokenizer.reset()
            this.resetTokenization() //new processor so find new tokens, first by scan
        }
    }

}