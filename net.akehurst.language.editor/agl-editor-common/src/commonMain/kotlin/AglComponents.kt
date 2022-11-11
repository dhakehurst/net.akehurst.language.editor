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

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.SentenceContext
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.LogLevel

class AglComponents<AsmType : Any, ContextType : Any>(
    languageId: String,
    val editorId: String,
    val logger: AglEditorLogger
) {
    private var _languageDefinition: LanguageDefinition<AsmType, ContextType> = Agl.registry.findOrPlaceholder<AsmType, ContextType>(languageId)
        .also {
            it.aglOptions = Agl.options {
                semanticAnalysis {
                    active(false)
                }
            }
        }
    private var _styleHandler = AglStyleHandler(languageId)

    val languageDefinition get() = _languageDefinition
    var goalRule: String? = languageDefinition.defaultGoalRule

    val styleHandler get() = _styleHandler

    var context: SentenceContext? = null
    var sppt: SharedPackedParseTree? = null
    //var asm: Any? = null

    var languageIdentity: String
        get() = languageDefinition.identity
        set(value) {
            val grammarObservers = this._languageDefinition.grammarObservers
            val styleObservers = this._languageDefinition.styleObservers
            val formatObservers = this._languageDefinition.formatObservers
            this._languageDefinition = Agl.registry.findOrPlaceholder(value)
            this._languageDefinition.grammarObservers.addAll(grammarObservers)
            this._languageDefinition.styleObservers.addAll(styleObservers)
            this._languageDefinition.formatObservers.addAll(formatObservers)

            this._styleHandler = AglStyleHandler(value)
            this.sppt = null
            //this.asm = null
        }
}