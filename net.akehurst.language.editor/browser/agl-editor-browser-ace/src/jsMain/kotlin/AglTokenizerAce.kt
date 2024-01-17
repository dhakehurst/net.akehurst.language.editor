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


//class AglBackgroundTokenizer(
//        tok: ace.Tokenizer,
//        ed: ace.Editor
//) : ace.BackgroundTokenizer(tok, ed) {
//}

data class AglLineStateAce(
    val lineNumber: Int,
    val nextLineStartPosition: Int,
    val leftOverText: String
) : ace.LineState {
}

class AglTokenAce(
    styles: Array<String>,
    override val value: String,
    override var index: Int?,
    column: Int
) : ace.Token {
    override val type = styles.joinToString(".")
    override var start:Int? = column
}

class AglLineTokensAce(
    override val state: ace.LineState,
    override val tokens: Array<ace.Token>
) : ace.LineTokens {}
/*
class AglTokenizerAce<AsmType : Any, ContextType : Any>(
        val agl: AglComponents<AsmType, ContextType>,
        val aglStyleHandler: AglStyleHandler
) : ace.Tokenizer {

    val aglTokenizer = AglTokenizer(agl)

    // --- ace.Ace.Tokenizer ---
    override fun getLineTokens(line: String, state: ace.LineState?, row: Int): ace.LineTokens {
        val sppt = this.agl.sppt
        val state2 = if (null == state) AglLineStateAce(0, "") else state as AglLineStateAce
        return if (null == sppt) {
            this.getLineTokensByScan(line, state2, row)
        } else {
            this.getLineTokensByParse(line, state2, row)
        }
    }

    fun getLineTokensByScan(line: String, state: AglLineStateAce, row: Int): ace.LineTokens {
        TODO()
    }

    fun getLineTokensByParse(line: String, state: AglLineStateAce, row: Int): ace.LineTokens {
        TODO()
    }


}
 */