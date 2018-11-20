/**
 * Copyright (C) 2018 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

@file:JvmName("Ogl")

package net.akehurst.language.processor

import net.akehurst.language.api.sppt2ast.Sppt2AstTransformer
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.ogl.grammar.OglGrammar
import net.akehurst.language.ogl.sppt2ast.OglSppt2AstTransformer
import kotlin.js.JsName
import kotlin.jvm.JvmName


private val oglProcessor: LanguageProcessor by lazy {
    //println("Creating ogl processor")
    val grammar = OglGrammar()
    val sppt2ast: Sppt2AstTransformer = OglSppt2AstTransformer()
    processor(grammar, sppt2ast)
}

@JsName("processorFromGrammar")
fun processor(grammar: Grammar): LanguageProcessor {
    return LanguageProcessorDefault(grammar, null)
}

@JsName("processorFromGrammarWithSemanticAnalyser")
fun processor(grammar: Grammar, semanticAnalyser: Sppt2AstTransformer): LanguageProcessor {
    return LanguageProcessorDefault(grammar, semanticAnalyser)
}

@JsName("processor")
fun processor(grammarDefinitionStr: String): LanguageProcessor {
    try {
        val grammar = oglProcessor.process<Grammar>("grammarDefinition", grammarDefinitionStr)
        return processor(grammar)
    } catch (e: ParseFailedException) {
        //TODO: better, different exception to detect which list item fails
        throw ParseFailedException("Unable to parse grammarDefinitionStr ", e.longestMatch, e.location)
    }
}

@JsName("processorWithSemanticAnalyser")
fun processor(grammarDefinitionStr: String, semanticAnalyser: Sppt2AstTransformer): LanguageProcessor {
    try {
        val grammar = oglProcessor.process<Grammar>("grammarDefinition", grammarDefinitionStr)
        return processor(grammar, semanticAnalyser)
    } catch (e: ParseFailedException) {
        //TODO: better, different exception to detect which list item fails
        throw ParseFailedException("Unable to parse grammarDefinitionStr ", e.longestMatch, e.location)
    }
}

@JsName("processorFromRuleList")
fun processor(rules: List<String>): LanguageProcessor {
    val prefix = "namespace temp grammar Temp { "
    val grammarStr = prefix + rules.joinToString(" ") + "}"
    try {
        val grammar = oglProcessor.process<Grammar>("grammarDefinition", grammarStr)
        return LanguageProcessorDefault(grammar, null)
    } catch (e: ParseFailedException) {
        //TODO: better, different exception to detect which list item fails
        val newCol = e.location.column.minus(prefix.length) ?: 0
        val location = InputLocation(newCol, 1,0)
        throw ParseFailedException("Unable to parse list of rules", e.longestMatch, location)
    }
}


