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

package net.akehurst.language.editor.compose.test

import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import net.akehurst.kotlin.compose.editor.AutocompleteStateCompose
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlin.compose.editor.CodeEditorView
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.kotlinx.logging.common.LoggingByConsole
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.editor.compose.attachToComposeEditor
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    LoggingManager.use(LoggingByConsole)
    val editorState = CodeEditorStateHolder(
        initialText = TestLanguageSql.INITIAL_TEXT,
    )
    coroutineScope {
        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    CodeEditorView(
                        editorState,
                        //autocompleteModifier = Modifier.widthIn(100.dp, 400.dp).heightIn(30.dp, 300.dp)
                    )
                }
            }
        }
        delay(1.seconds) //wait for compose to start
        val aglEditor = Agl.attachToComposeEditor(
            TestLanguageSql.languageService,
            TestLanguageSql.languageDefinition,
            { Agl.options { semanticAnalysis { sentenceContext(contextAsmSimple()) } } },
            TestLanguageSql.editorId,
            TestLanguageSql.editorOptions,
            TestLanguageSql.logFunction,
            editorState
        )
        println("Attached AGL")
        defr.await()
    }
}
