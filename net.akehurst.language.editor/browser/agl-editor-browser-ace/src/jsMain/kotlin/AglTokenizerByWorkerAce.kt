/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.browser.ace


import net.akehurst.language.editor.common.*

class AglTokenizerByWorkerAce<AsmType : Any, ContextType : Any>(
    agl:AglComponents<AsmType, ContextType>
) : ace.Tokenizer, AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)
    override var acceptingTokens = false
    override val tokensByLine = mutableMapOf<Int, List<AglToken>>()

    override fun reset() {
        this.acceptingTokens = false
        this.tokensByLine.clear()
    }

    override fun receiveTokens(lineTokens: List<List<AglToken>>) {
        if (this.acceptingTokens) {
            lineTokens.forEachIndexed { index, tokens ->
                // could get empty tokens for a line from a partial parse
                if (tokens.isNotEmpty()) {
                    this.tokensByLine[index] = tokens.toList()
                } else {
                    // nothing
                }
            }
        }
    }

    // --- ace.Ace.Tokenizer ---
    override fun getLineTokens(line: String, pState: ace.LineState?, row: Int): ace.LineTokens {
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


}