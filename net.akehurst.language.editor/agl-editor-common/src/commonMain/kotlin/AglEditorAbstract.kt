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

import net.akehurst.language.agl.agl.parser.SentenceAbstract
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.scanner.ScannerOnDemand
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.sppt.Sentence
import net.akehurst.language.api.sppt.SpptDataNode
import net.akehurst.language.editor.api.*

class SentenceFromEditor<AsmType : Any, ContextType : Any>(
    val editor: AglEditor<AsmType, ContextType>
) : SentenceAbstract() {
    override val text: String get() = editor.text
    override var eolPositions: List<Int> = emptyList()//ScannerOnDemand.eolPositions(text)

    fun textChanged() {
        eolPositions = ScannerOnDemand.eolPositions(text)
    }
}

abstract class AglEditorAbstract<AsmType : Any, ContextType : Any>(
    languageId: String,
    final override val editorId: String,
    logFunction: LogFunction?
) : AglEditor<AsmType, ContextType> {

    abstract val sessionId: String

    final override val logger = AglEditorLogger(logFunction)

    protected val agl = AglComponents<AsmType, ContextType>(languageId, editorId, logger)

    init {
        //this.agl.languageDefinition.processorObservers.add { _, _ -> this.updateProcessor(); this.updateStyle() }
        this.agl.languageDefinition.grammarStrObservers.add { _, _ -> this.updateProcessor(); this.updateStyle() }
        this.agl.languageDefinition.scopeStrObservers.add { _, _ -> this.updateProcessor(); this.updateStyle() }
        this.agl.languageDefinition.styleStrObservers.add { _, _ -> this.updateStyle() }
        //this.agl.languageDefinition.formatterStrObservers.add { _, _ -> }
    }

    private val _onParseHandler = mutableListOf<(ParseEvent) -> Unit>()
    private val _onSyntaxAnalysisHandler = mutableListOf<(SyntaxAnalysisEvent) -> Unit>()
    private val _onSemanticAnalysisHandler = mutableListOf<(SemanticAnalysisEvent) -> Unit>()
    private var _editorSpecificStyleStr: String? = null

    override var sentence = SentenceFromEditor(this)

    override var languageIdentity: String
        get() = this.agl.languageIdentity
        set(value) {
            val oldId = this.agl.languageIdentity
            if (oldId == value) {
                //same, no need to update
            } else {
                this.agl.languageIdentity = value
                this.updateLanguage(oldId)
                this.updateProcessor()
                this.updateStyle()
            }
        }

    override val languageDefinition: LanguageDefinition<AsmType, ContextType>
        get() = agl.languageDefinition

//    override var goalRuleName: String?
//        get() = this.agl.goalRule
//        set(value) {
//            this.agl.goalRule = value
//        }

    override var editorSpecificStyleStr: String?
        get() = this._editorSpecificStyleStr ?: this.agl.languageDefinition.styleStr
        set(value) {
            this._editorSpecificStyleStr = value
            this.updateStyle()
        }

//    override var sentenceContext: ContextType?
//        get() = this.agl.context
//        set(value) {
//            this.agl.context = value
//            this.processSentence()
//        }

    override var processOptions: ProcessOptions<AsmType, ContextType>
        get() = this.agl.options
        set(value) {
            this.agl.options = value
        }

    override var editorOptions: EditorOptions = EditorOptionsDefault()

    override var doUpdate: Boolean = true

    protected open fun onEditorTextChange() {
        this.sentence.textChanged()
    }

    override fun onParse(handler: (ParseEvent) -> Unit) {
        this._onParseHandler.add(handler)
    }

    override fun onSyntaxAnalysis(handler: (SyntaxAnalysisEvent) -> Unit) {
        this._onSyntaxAnalysisHandler.add(handler)
    }

    override fun onSemanticAnalysis(handler: (SemanticAnalysisEvent) -> Unit) {
        this._onSemanticAnalysisHandler.add(handler)
    }

    protected fun log(level: LogLevel, message: String, t: Throwable?) = this.logger.log(level, message, t)

    protected fun notifyParse(event: ParseEvent) {
        this._onParseHandler.forEach {
            it.invoke(event)
        }
    }

    protected fun notifySyntaxAnalysis(event: SyntaxAnalysisEvent) {
        this._onSyntaxAnalysisHandler.forEach {
            it.invoke(event)
        }
    }

    protected fun notifySemanticAnalysis(event: SemanticAnalysisEvent) {
        this._onSemanticAnalysisHandler.forEach {
            it.invoke(event)
        }
    }

    protected abstract fun updateLanguage(oldId: String?)
    protected abstract fun updateProcessor()
    protected abstract fun updateStyle()
    protected abstract fun processSentence()
}