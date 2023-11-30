/**
 * Copyright (C) 2023 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.information

import korlibs.io.async.suspendTest
import korlibs.io.file.std.resourcesVfs
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.semanticAnalyser.ContextSimple
import net.akehurst.language.editor.information.examples.AglStyle
import net.akehurst.language.editor.information.examples.BasicTutorial
import net.akehurst.language.typemodel.api.typeModel
import kotlin.test.Test
import kotlin.test.fail

class test_examples {

    companion object {

    }

    suspend fun before() {

        Examples.add(BasicTutorial.example)  // first example

        val resources = resourcesVfs
        val names = resources["examples"].listNames()
        println("Examples: $names")
        names.forEach {
            Examples.read(resources, it)
        }

        Examples.add(AglStyle.example)
        //Examples.add(AglGrammar.example)

    }

    @Test
    fun test() = suspendTest {
        before()
        Examples.map.values.forEach {
            println(it.label)

            println("  CreateProcessor")
            // create processor
            val result = Agl.processorFromStringDefault(
                grammarDefinitionStr = it.grammar,
                crossReferenceModelStr = it.references,
                styleModelStr = it.style
            )
            if (result.issues.errors.isNotEmpty()) {
                fail("CreateProcessor: ${it.id}\n" + result.issues.toString())
            }
            result.processor!!.let { proc ->
                proc.typeModel
                if (proc.issues.errors.isNotEmpty()) {
                    fail("TypeModel: ${it.id}\n" + proc.issues.toString())
                }
//                val additionalTm = typeModel("x",true) {
//                    namespace("external") {
//                        dataType("AnnotationType")
//                        dataType("RegularState")
//                        dataType("BuiltInType")
//                    }
//                }
//                proc.typeModel.addAllNamespace(additionalTm.allNamespace)
                proc.crossReferenceModel
                if (proc.issues.errors.isNotEmpty()) {
                    fail("CrossReferencesModel: ${it.id}\n" + proc.issues.toString())
                }
                proc.syntaxAnalyser
                if (proc.issues.errors.isNotEmpty()) {
                    fail("SyntaxAnalyser: ${it.id}\n" + proc.issues.toString())
                }
                proc.semanticAnalyser
                if (proc.issues.errors.isNotEmpty()) {
                    fail("SemanticAnalyser: ${it.id}\n" + proc.issues.toString())
                }
                proc.formatter
                if (proc.issues.errors.isNotEmpty()) {
                    fail("Formatter: ${it.id}\n" + proc.issues.toString())
                }
                proc.completionProvider
                if (proc.issues.errors.isNotEmpty()) {
                    fail("CompletionProvider: ${it.id}\n" + proc.issues.toString())
                }
            }

            //check parse
            println("  Parse")
            val parse = result.processor!!.parse(it.sentence)
            if (parse.issues.errors.isNotEmpty()) {
                fail("Parse: ${it.id}\n" + parse.issues.toString())
            }

            println("  SyntaxAnalysis")
            val syntaxAnalysis = result.processor!!.syntaxAnalysis(parse.sppt!!)
            if (syntaxAnalysis.issues.errors.isNotEmpty()) {
                fail("SyntaxAnalysis: ${it.id}\n" + syntaxAnalysis.issues.toString())
            }

            println("  SemanticAnalysis")
            val ctx = ExternalContextLanguage.processor.process(it.context).asm

            val semanticAnalysis = result.processor!!.semanticAnalysis(
                syntaxAnalysis.asm!!,
                Agl.options { semanticAnalysis { context(ctx) } }
            )
            if (semanticAnalysis.issues.errors.isNotEmpty()) {
                // allow error here as some examples have them intentionally
               // fail("  SemanticAnalysis: ${it.id}\n" + semanticAnalysis.issues.toString())
            }
        }

    }
}