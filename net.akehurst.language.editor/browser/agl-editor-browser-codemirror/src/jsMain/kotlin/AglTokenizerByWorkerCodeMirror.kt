/**
 * Copyright (C) 2022 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
package net.akehurst.language.editor.browser.codemirror

import net.akehurst.language.api.sppt.Sentence
import net.akehurst.language.editor.common.*

class AglTokenizerByWorkerCodeMirror<AsmType : Any, ContextType : Any>(
    sentence: Sentence,
    agl:AglComponents<AsmType, ContextType>
) : AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)
    override var acceptingTokens = false

    override fun reset() {
        this.aglTokenizer.reset()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        aglTokenizer.receiveTokens(startLine, tokensForLines)
    }

    // --- ace.Ace.Tokenizer ---
    /*override fun getLineTokens(line: String, pState: ace.LineState?, row: Int): ace.LineTokens {
        val tokens = this.tokensByLine[row]
        return if (null == tokens) {
            // no tokens received from worker, try local scan
            val aceState = if (null==pState) AglLineStateAce(row,"") else pState as AglLineStateAce
            val stateAgl = AglLineState(aceState.lineNumber, aceState.leftOverText, emptyList()) //not really emptyList, but its not needed as input so ok to use
            val ltokens = this.aglTokenizer.getLineTokensByScan(line, stateAgl, row)
            val lineTokens: List<AglTokenAce> = ltokens.tokens.map {
                AglTokenAce(
                        it.styles,
                        it.value,
                        it.line,
                        it.column
                )
            }
            val lt: Array<ace.Token> = lineTokens.toTypedArray()
            AglLineTokensAce(
                    AglLineStateAce(row, ""),
                    lt
            )
        } else {
            val lineTokens: List<AglTokenAce> = tokens.map {
                AglTokenAce(
                        it.styles,
                        it.value,
                        it.line,
                        it.column
                )
            }
            val lt: Array<ace.Token> = lineTokens.toTypedArray()
            AglLineTokensAce(
                    AglLineStateAce(row, ""),
                    lt
            )
        }
    }


     */

}