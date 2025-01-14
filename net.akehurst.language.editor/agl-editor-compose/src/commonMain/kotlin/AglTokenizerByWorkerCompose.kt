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

package net.akehurst.language.editor.compose

import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenizer
import net.akehurst.language.editor.common.AglTokenizerByWorker

data class ComposeStyle(val value: String) {

}

class AglTokenizerByWorkerCompose<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>,
    val logger: AglEditorLogger
) : AglTokenizerByWorker<ComposeStyle> {

    val aglTokenizer = AglTokenizer<AsmType, ContextType, ComposeStyle>(agl,agl.logger)

    fun refresh() {
    }

    override fun reset() {
        logger.log(LogLevel.Trace, "reset()", null)
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        logger.log(LogLevel.Trace, "receiveTokens $startLine, $tokensForLines", null)


    }

}