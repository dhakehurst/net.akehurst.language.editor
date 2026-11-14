/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.coroutines.CompletableDeferred
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.LogThreshold
import net.akehurst.kotlinx.logging.api.LoggingByConsole
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    LoggingManager.use(LoggingByConsole)
    LoggingManager.rootLoggingLevel = LogThreshold.All
    val logFunction: LogFunction = { logLevel, prefix, t, msg ->
        println("$logLevel: $prefix - ${msg.invoke()}")
        t?.let { println("$logLevel: $t") }
    }
    // wasm has no worker based language service, so the language service runs on the UI thread
    val languageService = LanguageServiceDirectExecution(logFunction)
    val app = EditorDemoApplication(languageService)
    app.start { gui ->
        gui.doStart()
        ComposeViewport(viewportContainerId = "ComposeTarget") {
            LaunchedEffect(Unit) {
                document.getElementById("loading-indicator")?.remove()
            }
            gui.content()
        }
        CompletableDeferred(Unit)
    }
}
