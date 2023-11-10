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
import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.api.AglEditorLogger

class AglComponents<AsmType : Any, ContextType : Any>(
    languageId: String,
    val editorId: String,
    val logger: AglEditorLogger
) {
   // private var _languageDefinition: LanguageDefinition<AsmType, ContextType> = Agl.registry.findOrPlaceholder<AsmType, ContextType>(languageId)
    private var _styleHandler = AglStyleHandler(languageId)
    private var _languageIdentity = languageId

    val languageDefinition get() = Agl.registry.findOrPlaceholder<AsmType, ContextType>(
        _languageIdentity,
        aglOptions = null,
        configuration = Agl.configurationEmpty()
    )
    var goalRule: String? = languageDefinition.defaultGoalRule

    val styleHandler get() = _styleHandler

    var context: ContextType? = null
    var sppt: SharedPackedParseTree? = null
    var scannerMatchables = listOf<Matchable>()

    var languageIdentity: String
        get() = languageDefinition.identity
        set(value) {
            if (languageDefinition.identity ==value) {
                //do NOT update, could end up in a loop and run out of memory with observer adding!
                // it did for version 1.9.0-RC of kotlin on JS
            } else {
                val old = this.languageDefinition
                val grammarStrObservers = old.grammarStrObservers
                val scopeStrObservers = old.scopeStrObservers
                val styleStrObservers = old.styleStrObservers
                val formatterStrObservers = old.formatterStrObservers
                this._languageIdentity = value
                val new = this.languageDefinition
                new.grammarStrObservers.addAll(grammarStrObservers)
                new.scopeStrObservers.addAll(scopeStrObservers)
                new.styleStrObservers.addAll(styleStrObservers)
                new.formatterStrObservers.addAll(formatterStrObservers)
                this._styleHandler = AglStyleHandler(value)
                this.sppt = null
            }
        }

}