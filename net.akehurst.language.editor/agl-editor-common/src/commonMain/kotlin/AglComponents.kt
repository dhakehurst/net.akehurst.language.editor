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
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.scanner.api.Scanner
import net.akehurst.language.scanner.api.ScannerKind
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.AglStyleHandler
import net.akehurst.language.grammar.api.GrammarRuleName
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.grammar.processor.ContextFromGrammarRegistry
import net.akehurst.language.parser.api.Rule
import net.akehurst.language.regex.agl.RegexEngineAgl
import net.akehurst.language.regex.agl.RegexEnginePlatform
import net.akehurst.language.regex.api.RegexEngineKind
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.scanner.common.ScannerAbstract
import net.akehurst.language.scanner.common.ScannerFromMatchables
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.sppt.treedata.CompleteTreeDataNode

class AglComponents<AsmType : Any, ContextType : Any>(
//    languageId: LanguageIdentity,
    var languageDefinition: LanguageDefinition<AsmType, ContextType>,
    val editorId: String,
    val logger: AglEditorLogger,
    styleHandler: AglStyleHandler<*>
) {
    // private var _languageDefinition: LanguageDefinition<AsmType, ContextType> = Agl.registry.findOrPlaceholder<AsmType, ContextType>(languageId)
    private var _styleHandler = styleHandler
//    private var _languageIdentity = languageId

    /*
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
*/

    var options:()-> ProcessOptions<AsmType, ContextType> = { Agl.options<AsmType, ContextType> {} }
    var goalRule: GrammarRuleName? = languageDefinition.defaultGoalRule

    val styleHandler get() = _styleHandler

    var context: ContextType? = null
    //var sppt: SharedPackedParseTree? = null

    // provided by worker when processor created
    private var _scannerMatchables = listOf<Matchable>()
    var scannerMatchables
        get() = _scannerMatchables
        set(value) {
            val regexEngine = when (this.languageDefinition.configuration.regexEngineKind) {
                RegexEngineKind.PLATFORM -> RegexEnginePlatform
                RegexEngineKind.AGL -> RegexEngineAgl
            }
            _scannerMatchables = value.map { it.using(regexEngine) }
        }

    val languageIdentity get() = languageDefinition.identity

    val simpleScanner: Scanner by lazy {
        val regexEngine = when (this.languageDefinition.configuration.regexEngineKind) {
            RegexEngineKind.PLATFORM -> RegexEnginePlatform
            RegexEngineKind.AGL -> RegexEngineAgl
        }
        ScannerFromMatchables(regexEngine) { scannerMatchables }
    }
}