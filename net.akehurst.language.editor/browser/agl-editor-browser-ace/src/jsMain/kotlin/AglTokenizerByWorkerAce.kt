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


import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.common.*

internal class AglTokenizerByWorkerAce<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>
) : ace.Tokenizer, AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)
    override var acceptingTokens
        get() = aglTokenizer.acceptingTokens
        set(value) {
            aglTokenizer.acceptingTokens = value
        }

    override fun reset() {
        this.aglTokenizer.reset()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        this.aglTokenizer.receiveTokens(startLine, tokensForLines)
    }

    // --- ace.Ace.Tokenizer ---
    // first line has state==null
    override fun getLineTokens(line: String, state: ace.LineState?): ace.LineTokens {
        val stateAgl = if (null == state) {
            AglLineState(-1, 0, "", emptyList()) //not really emptyList, but its not needed as input so ok to use
        } else {
           val aceState = state as AglLineStateAce
            AglLineState(aceState.lineNumber, aceState.nextLineStartPosition, aceState.leftOverText, emptyList()) //not really emptyList, but its not needed as input so ok to use
        }
        val nextState = this.aglTokenizer.getLineTokens(line, stateAgl)
        val tokens = nextState.tokens
        val lineTokens = tokens.map {aglTok ->
                val col = aglTok.position - stateAgl.nextLineStartPosition
                val value = line.substring(col, col + aglTok.length)
                AglTokenAce(
                    styles = aglTok.styles.toTypedArray(),
                    value = value,
                    column = col,
                    index = null
                )
            }
        val lt: Array<ace.Token> = lineTokens.toTypedArray()
        return AglLineTokensAce(
            state = AglLineStateAce(nextState.lineNumber, nextState.nextLineStartPosition, ""),
           tokens = lt
        )
    }


}