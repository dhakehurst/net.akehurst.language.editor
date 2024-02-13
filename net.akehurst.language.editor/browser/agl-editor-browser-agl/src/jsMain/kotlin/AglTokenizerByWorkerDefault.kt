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

package net.akehurst.language.editor.browser.agl

import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenDefault
import net.akehurst.language.editor.common.AglTokenizerByWorker

class AglTokenizerByWorkerDefault<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>
) : AglTokenizerByWorker {

     val tokensByLine = mutableMapOf<Int, List<AglToken>>()

//    override var acceptingTokens = false
    override fun reset() {
//        this.acceptingTokens = false
        this.tokensByLine.clear()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        TODO("not implemented")
    }

}