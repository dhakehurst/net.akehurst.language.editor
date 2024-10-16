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

package net.akehurst.language.editor.browser.ck

import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenDefault
import kotlin.test.Test

class test_AglTokenizerByWorkerCk {

    @Test
    fun test() {
        // Given
        val languageId = LanguageIdentity("test-language")
        val editorId = "test-editor"
        val logger = AglEditorLogger({ level, message, t -> println("${level}: $message - $t") })
        val agl = AglComponents<Any, Any>(languageId, editorId, logger)
        val emi = EditorModelIndex()
        val sut = AglTokenizerByWorkerCk<Any, Any>(agl, emi)

        // When
        val tokens = listOf(listOf(AglTokenDefault(listOf("style1"),0,5)))
        sut.receiveTokens(0, tokens)


    }
}