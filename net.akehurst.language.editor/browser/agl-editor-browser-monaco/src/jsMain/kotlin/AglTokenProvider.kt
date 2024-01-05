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

package net.akehurst.language.editor.browser.monaco

import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.Sentence
import net.akehurst.language.editor.common.AglComponents


class AglLineStateMonaco(
    val lineNumber: Int,
    val lineStartPosition:Int,
    val leftOverText: String
) : monaco.languages.IState {
    override fun clone(): monaco.languages.IState {
        return AglLineStateMonaco(lineNumber, lineStartPosition, leftOverText)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is AglLineStateMonaco -> other.lineNumber == this.lineNumber
            else -> false
        }
    }
}

class AglTokenMonaco(
    style: String,
    column: Int
) : monaco.languages.IToken {
    override val scopes: String = style
    override val startIndex: Int = column - 1
}

class AglLineTokensMonaco(
    override val endState: AglLineStateMonaco,
    override val tokens: Array<monaco.languages.IToken>
) : monaco.languages.ILineTokens {

}
/*
class AglTokenProvider<AsmType : Any, ContextType : Any>(
    val tokenPrefix: String,
    val agl: AglComponents<AsmType, ContextType>
) : monaco.languages.TokensProvider {

    companion object {
        fun NO_TOKENS(nextLineNumber: Int, lineStartPosition:Int) = AglLineTokensMonaco(
            AglLineStateMonaco(nextLineNumber, lineStartPosition,""),
            arrayOf(AglTokenMonaco("", 0))
        )
    }

    override fun getInitialState(): monaco.languages.IState {
        return AglLineStateMonaco(0, 0, "")
    }

    override fun tokenize(line: String, pState: monaco.languages.IState): monaco.languages.ILineTokens {
        val state = pState as AglLineStateMonaco
        try {
            if (null == this.agl.sppt) {
                return this.getLineTokensByScan(line, state)
            } else {
                return this.getLineTokensByParse(line, state)
            }
        } catch (t: Throwable) {
            console.error(t.message)
            return NO_TOKENS(state.lineNumber + 1, state.lineStartPosition+line.length)
        }
    }

    private fun getLineTokensByScan(line: String, pState: monaco.languages.IState): monaco.languages.ILineTokens {
        val state = pState as AglLineStateMonaco
        val proc = this.agl.languageDefinition.processor
        val nextLineNumber = state.lineNumber + 1
        if (null != proc) {
            val text = state.leftOverText + line
            val leafs = proc.scan(text).tokens
            val tokens = leafs.map { leaf ->
                val loc = sentence.locationInLine(state.lineNumber, leaf.position, leaf.length)
                object : monaco.languages.IToken {
                    override val scopes = tokenPrefix + leaf.name //:FIXME: monaco doesn't support multiple classes on a token //mapToCssClasses(leaf).joinToString(separator = ".") { tokenPrefix+it }
                    override val startIndex = loc.column - 1
                }
            }
            val endState = if (leafs.isEmpty()) {
                AglLineStateMonaco(nextLineNumber, state.lineStartPosition+line.length, text)
            } else {
                val lastLeaf = leafs.last()
                val endOfLastLeaf = sentence.positionOfLine(state.lineNumber) + lastLeaf.length
                val leftOverText = line.substring(endOfLastLeaf, line.length)
                AglLineStateMonaco(nextLineNumber,state.lineStartPosition+line.length, leftOverText)
            }
            return object : monaco.languages.ILineTokens {
                override val endState = endState
                override val tokens: Array<monaco.languages.IToken> = tokens.toTypedArray()
            }
        } else {
            return NO_TOKENS(nextLineNumber,state.lineStartPosition+line.length)
        }
    }

    private fun getLineTokensByParse(line: String, pState: monaco.languages.IState): monaco.languages.ILineTokens {
        val state = pState as AglLineStateMonaco
        val nextLineNumber = state.lineNumber + 1
        val sppt = this.agl.sppt!!
        val leafs = sppt.tokensByLine(state.lineNumber)
        return if (leafs.isNotEmpty()) {
            val tokens = leafs.map { leaf ->
                val loc = sentence.locationInLine(state.lineNumber, leaf.position, leaf.length)
                AglTokenMonaco(
                    tokenPrefix + leaf.name, //:FIXME: monaco doesn't support multiple classes on a token //mapToCssClasses(leaf).joinToString(separator = ".") { tokenPrefix+it }
                    loc.column
                )
            }
            AglLineTokensMonaco(
                AglLineStateMonaco(nextLineNumber, state.lineStartPosition+line.length,""),
                tokens.toTypedArray()
            )
        } else {
            NO_TOKENS(nextLineNumber,state.lineStartPosition+line.length)
        }
    }

    private fun mapToCssClasses(leaf: SPPTLeaf): List<String> {
        val metaTagClasses = leaf.metaTags
        val otherClasses = if (!leaf.tagList.isEmpty()) {
            leaf.tagList
        } else {
            listOf(leaf.name)
        }
        val classes = metaTagClasses + otherClasses
        return classes;
    }
}
*/