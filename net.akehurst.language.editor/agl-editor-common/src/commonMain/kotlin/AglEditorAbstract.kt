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

import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.SentenceContext
import net.akehurst.language.editor.api.*

abstract class AglEditorAbstract<AsmType : Any, ContextType : Any>(
    languageId: String,
    override val editorId: String
) : AglEditor<AsmType, ContextType> {

    override val logger = AglEditorLogger()

    protected val agl = AglComponents<AsmType, ContextType> (languageId, editorId, logger)

    init {
        this.agl.languageDefinition.grammarObservers.add { _, _ -> this.updateGrammar(); this.updateStyle() }
        this.agl.languageDefinition.styleObservers.add { _, _ -> this.updateStyle() }
    }

    private val _onParseHandler = mutableListOf<(ParseEvent) -> Unit>()
    private val _onSyntaxAnalysisHandler = mutableListOf<(SyntaxAnalysisEvent) -> Unit>()
    private val _onSemanticAnalysisHandler = mutableListOf<(SemanticAnalysisEvent) -> Unit>()
    private var _editorSpecificStyleStr: String? = null

    override var languageIdentity: String
        get() = this.agl.languageIdentity
        set(value) {
            val oldId = this.agl.languageIdentity
            this.agl.languageIdentity = value
            this.updateLanguage(oldId)
            this.updateGrammar()
            this.updateStyle()
        }

    override val languageDefinition: LanguageDefinition<AsmType, ContextType>
        get() = agl.languageDefinition

    override var goalRuleName: String?
        get() = this.agl.goalRule
        set(value) {
            this.agl.goalRule = value
        }

    override var editorSpecificStyleStr: String?
        get() = this._editorSpecificStyleStr ?: this.agl.languageDefinition.style
        set(value) {
            this._editorSpecificStyleStr = value
            this.updateStyle()
        }

    override var sentenceContext: SentenceContext?
        get() = this.agl.context
        set(value) {
            this.agl.context = value
            this.processSentence()
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

    protected fun log(level: LogLevel, message: String,t:Throwable?) = this.logger.log(level, message,t)


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
    protected abstract fun updateGrammar()
    protected abstract fun updateStyle()
    protected abstract fun processSentence()
}