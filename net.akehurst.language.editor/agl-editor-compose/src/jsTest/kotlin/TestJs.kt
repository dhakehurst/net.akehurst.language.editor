/**
 * Copyright (C) 2025 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

@file:OptIn(ExperimentalComposeUiApi::class)

package net.akehurst.language.editor.compose

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlin.compose.editor.CodeEditorView
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.kotlinx.logging.common.LoggingByConsole
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.editor.compose.TestLanguageSql.editorId
import net.akehurst.language.editor.compose.TestLanguageSql.editorOptions
import net.akehurst.language.editor.compose.TestLanguageSql.languageDefinition
import net.akehurst.language.editor.compose.TestLanguageSql.languageService
import net.akehurst.language.editor.compose.TestLanguageSql.logFunction
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class test_AglEditorCompose {

    @BeforeTest
    fun before() {
        LoggingManager.use(LoggingByConsole)
    }


    @Test
    fun run_AglComposeTextEditor() {
        val aglEditor = AglComposeTextEditor()
        CoroutineScope(Dispatchers.Default).async {
            ComposeViewport(viewportContainerId = "ComposeTarget") {
                Surface {
                    aglEditor.content()
                }
            }
        }
    }

    @Ignore
    @Test
    fun run_AglComposeTextEditor_SQL_style_norefs() {
        val aglComposeEditor = AglComposeTextEditor(
            initialText = TestLanguageSql.INITIAL_TEXT,
            grammarString = GrammarString(TestLanguageSql.GRAMMAR),
            styleString = StyleString(TestLanguageSql.STYLE)
        )
        CoroutineScope(Dispatchers.Default).async {
            ComposeViewport(viewportContainerId = "ComposeTarget") {
                Surface {
                    aglComposeEditor.content()
                }
            }
        }
    }

    /*
        @Test
        fun run_ComposableCodeEditor2() = runBlocking {

            var composeEditor = ComposableCodeEditor2()

            val defr = async {
                singleWindowApplication(
                    title = "Code Editor Test",
                ) {
                    Surface {
                        composeEditor.content()
                    }
                }
            }

            val logFunction: LogFunction = { level, prefix, t, message -> println("$level - $prefix: ${message()}"); t?.printStackTrace() }
            val editorOptions = aglEditorOptions() {
            }
            val editorId = "test"
            val languageId = LanguageIdentity("test")
            val languageDefinition = Agl.registry.findOrPlaceholder(
                languageId,
                aglOptions = Agl.options { },
                configuration = Agl.configurationSimple()
            )
            val languageService = LanguageServiceDirectExecution(logFunction)

            delay(1000) //wait for compose to start

            val aglEditor = Agl.attachToComposeEditor(
                languageService, languageDefinition,
                { Agl.options { semanticAnalysis { context(contextAsmSimple()) } } },
                editorId, editorOptions, logFunction, composeEditor!!
            )
            println("Attached AGL")

            defr.await()

        }
    */
    @Ignore
    @Test
    fun run_AglComposeTextEditor_SQL_style_and_refs() {
        val editorState = CodeEditorStateHolder(
            initialText = TestLanguageSql.INITIAL_TEXT,
        )
        CoroutineScope(Dispatchers.Default).async {
            ComposeViewport(viewportContainerId = "ComposeTarget") {
                Surface {
                    CodeEditorView(
                        editorState,
                        autocompleteModifier = Modifier.widthIn(100.dp, 400.dp).heightIn(30.dp, 300.dp)
                    )
                }
            }
            delay(1000)
            val aglEditor = Agl.attachToComposeEditor(
                languageService,
                languageDefinition,
                { Agl.options { semanticAnalysis { context(contextAsmSimple()) } } },
                editorId, editorOptions, logFunction, editorState
            )
        }

    }

    /*
        @Test
        fun run_ComposableCodeEditor2c() = runBlocking {

            var composeEditor = ComposableCodeEditor2(
                initialText = """
                    namespace test
                    grammar Test {
                      S = 'a' ;
                    }
                """.trimIndent(),
            )

            val defr = async {
                singleWindowApplication(
                    title = "Code Editor Test",
                ) {
                    Surface {
                        composeEditor.content(autocompleteModifier = Modifier.width(400.dp).height(300.dp))
                    }
                }
            }

            val logFunction: LogFunction = { level, prefix, t, message -> println("$level - $prefix: ${message()}"); t?.printStackTrace() }
            val editorOptions = aglEditorOptions() {
            }
            val editorId = "test"
            val languageId = Agl.registry.agl.grammarLanguageIdentity
            val languageDefinition = Agl.languageDefinitionFromString<Any, Any>(
                languageId,
                grammarDefinitionStr = GrammarString(GRAMMAR),
                styleStr = StyleString(STYLE)
            )
            val languageService = LanguageServiceDirectExecution(logFunction)

            delay(1000) //wait for compose to start

            val aglEditor = Agl.attachToComposeEditor(
                languageService, languageDefinition,
                { Agl.options { semanticAnalysis { context(contextAsmSimple()) } } },
                editorId,
                editorOptions, logFunction, composeEditor!!
            )
            println("Attached AGL")

            aglEditor.updateLanguageDefinitionWith(
                grammarStr = GrammarString(AglGrammar.grammarString),
                styleStr = StyleString(AglGrammar.styleString)
            )

            defr.await()

        }
    */
}