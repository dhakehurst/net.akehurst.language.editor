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


enum class LogLevel { None, Fatal, Error, Warning, Debug, Trace, All }

interface AglEditor {

    val editorId: String

    /**
     * the language identity for this editor
     */
    var languageIdentity: String

    /**
     * The language definition for the editor, found or created using the languageId
     */
    val languageDefinition: LanguageDefinition

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
     * The context for syntax and semantic analysis
     */
    var context: Any?

    /**
     * the content of the editor
     */
    var text: String

    /**
     * destination for logging messages
     */
    val logger: AglEditorLogger

    fun onParse(handler: (ParseEvent) -> Unit)

    fun onSyntaxAnalysis(handler: (SyntaxAnalysisEvent) -> Unit)

    fun onSemanticAnalysis(handler: (SemanticAnalysisEvent) -> Unit)

    fun clearErrorMarkers()

    fun finalize()

    fun destroy()
}

/**
 * Three kinds of event,
 * Start -> success==false, message==Start
 * Success -> success==true
 * Failure -> success==false
 */
class ParseEvent(
    val success: Boolean,
    val message: String,
    val tree: Any?,
    val issues: List<LanguageIssue>
) {
    val isStart:Boolean = false==success && "Start"==message
    val failure:Boolean= success.not() && isStart.not()
}

/**
 * Three kinds of event,
 * Start -> success==false, message==Start
 * Success -> success==true
 * Failure -> success==false
 */
class SyntaxAnalysisEvent(
    val success:Boolean,
    val message: String,
    val asm:Any?,
    val issues: List<LanguageIssue>
) {
    val isStart:Boolean = false==success && "Start"==message
    val failure:Boolean= success.not() && isStart.not()
}

/**
 * Three kinds of event,
 * Start -> success==false, message==Start
 * Success -> success==true
 * Failure -> success==false
 */
class SemanticAnalysisEvent(
    val success:Boolean,
    val message: String,
    val issues: List<LanguageIssue>
) {
    val isStart:Boolean = false==success && "Start"==message
    val failure:Boolean= success.not() && isStart.not()
}