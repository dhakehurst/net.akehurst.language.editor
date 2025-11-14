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

import androidx.compose.material3.Surface
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.kotlinx.logging.common.LoggingByConsole

fun main() {
    LoggingManager.use(LoggingByConsole)
    val aglEditor = AglComposeTextEditor()
    CoroutineScope(Dispatchers.Default).async {
        ComposeViewport(viewportContainerId = "ComposeTarget") {
            Surface {
                aglEditor.content()
            }
        }
    }
}