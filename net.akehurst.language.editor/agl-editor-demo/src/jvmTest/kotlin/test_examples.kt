/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.demo

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.processor.contextFromRegistryGrammars
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.AsmTransformString
import net.akehurst.language.api.processor.CrossReferenceString
import net.akehurst.language.api.processor.FormatString
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.api.processor.TypesString
import net.akehurst.language.api.semanticAnalyser.SentenceContext
import net.akehurst.language.asm.api.Asm
import net.akehurst.language.editor.demo.information.ExternalContextLanguage
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import java.io.File
import kotlin.test.Test

/**
 * Loads each example the way the demo does (blank part = null, simple configuration) and reports
 * the issues of each phase. Report only: some examples contain intentional errors.
 */
class test_examples {

    private val dir = File("src/commonMain/composeResources/files/examples")
    private fun String?.blankAsNull(): String? = this?.takeIf { it.isNotBlank() }
    private fun read(eg: String, file: String) = File(dir, "$eg/$file").takeIf { it.exists() }?.readText()

    @Test
    fun report() {
        Agl.registry.initialise()
        val names = (System.getProperty("examples") ?: "").split(",").filter { it.isNotBlank() }
            .ifEmpty { dir.listFiles()!!.filter { it.isDirectory }.map { it.name }.sorted() }
        val sb = StringBuilder()
        for ((i, eg) in names.withIndex()) {
            val grammar = read(eg, "grammar.agl-grm").blankAsNull()
            val sentence = read(eg, "sentence.txt") ?: ""
            sb.appendLine("=== $eg")
            if (null == grammar) {
                sb.appendLine("  (no grammar file, AGL language example)")
                continue
            }
            try {
                val ld = Agl.registry.register<Asm, SentenceContext>(
                    identity = LanguageIdentity("test-$i"),
                    buildForDefaultGoal = false,
                    aglOptions = Agl.options {
                        semanticAnalysis {
                            sentenceContext(contextFromRegistryGrammars(Agl.registry))
                            option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                        }
                    },
                    configuration = Agl.configurationSimple()
                )
                ld.update(
                    grammarString = GrammarString(grammar),
                    typesString = read(eg, "types.agl-typ").blankAsNull()?.let { TypesString(it) },
                    asmTransformString = read(eg, "asmTransform.agl-trn").blankAsNull()?.let { AsmTransformString(it) },
                    crossReferenceString = read(eg, "references.agl-ref").blankAsNull()?.let { CrossReferenceString(it) },
                    styleString = read(eg, "style.agl-sty").blankAsNull()?.let { StyleString(it) },
                    formatString = read(eg, "format.agl-fmt").blankAsNull()?.let { FormatString(it) },
                )
                val proc = ld.processor
                if (ld.issues.errors.isNotEmpty() || null == proc) {
                    sb.appendLine("  LANGUAGE: ${ld.issues.errors.take(5)}")
                    continue
                }
                proc.typesDomain; proc.crossReferenceDomain; proc.syntaxAnalyser; proc.semanticAnalyser
                if (proc.allIssues.errors.isNotEmpty()) sb.appendLine("  PROCESSOR: ${proc.allIssues.errors.take(5)}")
                val ctx = read(eg, "context.agl-ctx").blankAsNull()?.let { ExternalContextLanguage.processor.process(it).asm } ?: contextAsmSimple()
                val res = proc.process(sentence, Agl.options { semanticAnalysis { sentenceContext(ctx) } })
                val errs = res.allIssues.errors
                sb.appendLine("  sentence: ${if (errs.isEmpty()) "ok" else "${errs.size} errors: ${errs.take(4)}"}")
            } catch (t: Throwable) {
                sb.appendLine("  EXCEPTION: ${t.stackTraceToString().lines().take(12).joinToString("\n    ")}")
            }
        }
        println(sb); File(System.getProperty("java.io.tmpdir"), "examples-report.txt").writeText(sb.toString())
    }
}
