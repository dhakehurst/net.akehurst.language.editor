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

import net.akehurst.language.api.processor.*
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.sppt.api.LeafData
import net.akehurst.language.style.api.AglStyleModel
import net.akehurst.language.style.api.AglStyleSelector

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
    val endPointIdentity: EndPointIdentity

    /**
     * the language identity for this editor
     */
    val languageIdentity: LanguageIdentity

    /**
     * The language definition for the editor, found or created using the languageId
     */
    val languageDefinition: LanguageDefinition<AsmType, ContextType>

    /**
     * Set style specific to this editor (rather than using the one from LanguageDefinition associated with the languageId).
     * If null, then the style from the LanguageDefinition is used. (default is null)
     */
    var editorSpecificStyleStr: StyleString?

    val styleHandler:AglStyleHandler<*>

//    /**
//     * The context for syntax and semantic analysis of the sentence (text) in the editor
//     */
//    var sentenceContext: ContextType?

    /**
     * the content of the editor
     */
    var text: String

    val sentence: Sentence

    val issues: IssueHolder

    /**
     * options passed to the processor when processing the editor text
     */
    var processOptions: ()->ProcessOptions<AsmType, ContextType>

    /**
     * options about what information (events) are triggered
     * by this editor when a sentence is processed
     */
    var editorOptions: EditorOptions

    /**
     * update the editor and process the text if true
     * (can be useful to be able to switch this off)
     */
    var doUpdate: Boolean

    /**
     * destination for logging messages
     */
    val logger: AglEditorLogger

    //fun configureSyntaxAnalyser(configuration: Map<String, Any>)

    /**
     * trigger processing the text
     */
    fun processSentence(text:String)

    fun onTextChange(handler: (String) -> Unit)

    fun onIssues(handler: (List<LanguageIssue>) -> Unit)

    fun onParse(handler: (ParseEvent) -> Unit)

    fun onSyntaxAnalysis(handler: (SyntaxAnalysisEvent) -> Unit)

    fun onSemanticAnalysis(handler: (SemanticAnalysisEvent) -> Unit)

    fun clearIssueMarkers()

    fun updateLanguageDefinitionWith(
        grammarStr: GrammarString?=null,
        typeModelStr: TypeModelString?=null,
        asmTransformStr: TransformString?=null,
        crossReferenceStr: CrossReferenceString?=null,
        styleStr: StyleString?=null
    )
    fun updateLanguageDefinition(languageDefinition: LanguageDefinition<AsmType, ContextType>)

    fun refreshProcessor()
    fun refreshStyleHandler()

    fun destroyAglEditor()
    fun destroyBaseEditor()

}

typealias StyleCompletionItem = (item:CompletionItem) -> String //TODO: return some other than String perhaps !

interface EditorOptions {
    var scan:Boolean
    var parse: Boolean
    var parseLineTokens: Boolean
    var lineTokensChunkSize: Int
    var parseTree: Boolean
    var syntaxAnalysis: Boolean
    var syntaxAnalysisAsm: Boolean
    var semanticAnalysis: Boolean
    var semanticAnalysisAsm: Boolean
    var styleCompletionItem: StyleCompletionItem?
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

interface AglEditorCompletionProvider {
    /**
     * does editor specific provision
     */
    fun provide(completionItems: List<CompletionItem>)
}

interface AglStyleHandler<EditorStyleType : Any> {
    val styleModel: AglStyleModel

    fun reset()
    fun updateStyleModel(styleModel: AglStyleModel)
    fun editorStyleFor(identity: EditorStyleIdentity): EditorStyleType?
    fun convert(selector: AglStyleSelector): EditorStyleType

    fun transformToTokens(leafs: List<LeafData>): List<AglToken>
}