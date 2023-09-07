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

package net.akehurst.language.editor.api

import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.SentenceContext


enum class LogLevel { None, Fatal, Error, Warning, Information, Debug, Trace, All }

interface AglEditor<AsmType : Any, ContextType : Any> {

    /**
     * the underlying editor
     */
    val baseEditor: Any

    /*
     * identifies the editor, important that it is unique wrt to each SharedWorker
     * used to identify source and target of messages to/from the worker
     */
    val editorId: String

    /**
     * the language identity for this editor
     */
    var languageIdentity: String

    /**
     * The language definition for the editor, found or created using the languageId
     */
    val languageDefinition: LanguageDefinition<AsmType, ContextType>

    /**
     * The name of a rule in the grammar from which to start the parse.
     * If null, the first non-skip rule will be used
     */
    var goalRuleName: String?

    /**
     * Set style specific to this editor (rather than using the one from LanguageDefinition associated with the languageId).
     * If null, then the style from the LanguageDefinition is used. (default is null)
     */
    var editorSpecificStyleStr: String?

    /**
     * The context for syntax and semantic analysis of the sentence (text) in the editor
     */
    var sentenceContext: ContextType?

    /**
     * the content of the editor
     */
    var text: String

    /**
     * update the editor and process the text if true
     * (can be useful to be able to switch this off)
     */
    var doUpdate: Boolean

    /**
     * destination for logging messages
     */
    val logger: AglEditorLogger

    fun configureSyntaxAnalyser(configuration: Map<String, Any>)

    fun onParse(handler: (ParseEvent) -> Unit)

    fun onSyntaxAnalysis(handler: (SyntaxAnalysisEvent) -> Unit)

    fun onSemanticAnalysis(handler: (SemanticAnalysisEvent) -> Unit)

    fun clearErrorMarkers()

    fun destroy()
}

enum class EventStatus { START, FAILURE, SUCCESS }

/**
 * Three kinds of event,
 * Start -> success==false, message==Start
 * Success -> success==true
 * Failure -> success==false
 */
class ParseEvent(
    val status: EventStatus,
    val message: String,
    val tree: Any?,
    val issues: List<LanguageIssue>
) {
    val isStart: Boolean = status == EventStatus.START
    val failure: Boolean = status == EventStatus.FAILURE
}

/**
 * Three kinds of event,
 * Start -> success==false, message==Start
 * Success -> success==true
 * Failure -> success==false
 */
class SyntaxAnalysisEvent(
    val status: EventStatus,
    val message: String,
    val asm: Any?,
    val issues: List<LanguageIssue>
) {
    val isStart: Boolean = status == EventStatus.START
    val failure: Boolean = status == EventStatus.FAILURE
}

/**
 * Three kinds of event,
 * Start -> success==false, message==Start
 * Success -> success==true
 * Failure -> success==false
 */
class SemanticAnalysisEvent(
    val status: EventStatus,
    val message: String,
    val asm: Any?,
    val issues: List<LanguageIssue>
) {
    val isStart: Boolean = status == EventStatus.START
    val failure: Boolean = status == EventStatus.FAILURE
}