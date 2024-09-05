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

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.api.runtime.Rule
import net.akehurst.language.agl.language.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.language.grammar.ContextFromGrammarRegistry
import net.akehurst.language.agl.regex.RegexEngine
import net.akehurst.language.agl.regex.RegexEngineAgl
import net.akehurst.language.agl.regex.RegexEnginePlatform
import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.agl.scanner.ScannerAbstract
import net.akehurst.language.agl.scanner.ScannerClassic
import net.akehurst.language.agl.scanner.ScannerOnDemand
import net.akehurst.language.agl.sppt.CompleteTreeDataNode
import net.akehurst.language.api.language.base.QualifiedName
import net.akehurst.language.api.language.grammar.GrammarRuleName
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.processor.RegexEngineKind
import net.akehurst.language.api.processor.ScannerKind
import net.akehurst.language.api.scanner.Scanner
import net.akehurst.language.api.sppt.Sentence
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.api.AglEditorLogger

class AglComponents<AsmType : Any, ContextType : Any>(
    languageId: QualifiedName,
    val editorId: String,
    val logger: AglEditorLogger
) {
    // private var _languageDefinition: LanguageDefinition<AsmType, ContextType> = Agl.registry.findOrPlaceholder<AsmType, ContextType>(languageId)
    private var _styleHandler = AglStyleHandler(languageId)
    private var _languageIdentity = languageId

    val languageDefinition
        get() = Agl.registry.findOrPlaceholder<AsmType, ContextType>(
            _languageIdentity,
            aglOptions = Agl.options {
                semanticAnalysis {
                    context(ContextFromGrammarRegistry(Agl.registry))
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            configuration = Agl.configurationBase()
        )

    var options = Agl.options<AsmType, ContextType> {}
    var goalRule: GrammarRuleName? = languageDefinition.defaultGoalRule

    val styleHandler get() = _styleHandler

    var context: ContextType? = null
    //var sppt: SharedPackedParseTree? = null

    // provided by worker when processor created
    private var _scannerMatchables= listOf<Matchable>()
    var scannerMatchables
        get() = _scannerMatchables
        set(value) {
            val regexEngine = when (this.languageDefinition.configuration.regexEngineKind) {
                RegexEngineKind.PLATFORM -> RegexEnginePlatform
                RegexEngineKind.AGL -> RegexEngineAgl
            }
            _scannerMatchables = value.map { it.using(regexEngine) }
        }

    var languageIdentity: QualifiedName
        get() = languageDefinition.identity
        set(value) {
            if (languageDefinition.identity == value) {
                //do NOT update, could end up in a loop and run out of memory with observer adding!
                // it did for version 1.9.0-RC of kotlin on JS
            } else {
                val old = this.languageDefinition
                val grammarStrObservers = old.grammarStrObservers
                val crossReferenceModelStrObservers = old.crossReferenceModelStrObservers
                val styleStrObservers = old.styleStrObservers
                val formatterStrObservers = old.formatterStrObservers
                this._languageIdentity = value
                val new = this.languageDefinition
                new.grammarStrObservers.addAll(grammarStrObservers)
                new.crossReferenceModelStrObservers.addAll(crossReferenceModelStrObservers)
                new.styleStrObservers.addAll(styleStrObservers)
                new.formatterStrObservers.addAll(formatterStrObservers)
                this._styleHandler = AglStyleHandler(value)
//                this.sppt = null
            }
        }

    val simpleScanner: Scanner by lazy {
        val regexEngine = when (this.languageDefinition.configuration.regexEngineKind) {
            RegexEngineKind.PLATFORM -> RegexEnginePlatform
            RegexEngineKind.AGL -> RegexEngineAgl
        }
        object : ScannerAbstract(regexEngine) {
            override val kind: ScannerKind get() = error("Not used")
            override val matchables: List<Matchable> get() = scannerMatchables
            override val validTerminals: List<Rule> get() = error("Not used")
            override fun reset() {}
            override fun isLookingAt(sentence: Sentence, position: Int, terminalRule: Rule): Boolean = error("Not used")
            override fun findOrTryCreateLeaf(sentence: Sentence, position: Int, terminalRule: Rule): CompleteTreeDataNode = error("Not used")
        }
    }
}