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

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CompletableDeferred
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.LogThreshold
import net.akehurst.kotlinx.logging.api.LoggingByConsole
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution

suspend fun main() {
    LoggingManager.use(LoggingByConsole)
    LoggingManager.rootLoggingLevel = LogThreshold.All
    val logFunction: LogFunction = { logLevel, prefix, t, msg ->
        println("$logLevel: $prefix - ${msg.invoke()}")
        t?.let { println("$logLevel: $t") }
    }
    val languageService = LanguageServiceDirectExecution(logFunction)
    val app = EditorDemoApplication(languageService)
    app.start { gui ->
        gui.doStart()
        // runs on the main thread, returns when the window is closed
        singleWindowApplication(title = "AGL Editor", state = WindowState(size = DpSize(1400.dp, 900.dp))) {
            gui.content()
        }
        CompletableDeferred(Unit)
    }
}
