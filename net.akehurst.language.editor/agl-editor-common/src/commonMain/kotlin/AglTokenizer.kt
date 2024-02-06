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
package net.akehurst.language.editor.common

import net.akehurst.language.agl.agl.parser.SentenceDefault
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.LogLevel
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

interface AglTokenizerByWorker {

    var acceptingTokens: Boolean
    //val tokensByLine: Map<Int, List<AglToken>>

    fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>)
    fun reset()

}

data class LineTokensResult(
    val tokens: List<AglTokenDefault>,
    val state: AglLineState
)

class AglLineState(
    val lineNumber: Int,
    val nextLineStartPosition: Int,
    val leftOverText: String,
    val tokens: List<AglToken>
) {
}

class AglTokenDefault(
    override val styles: List<String>,
    override val position: Int,
    override val length: Int
) : AglToken {
    override fun toString(): String = "AglToken($position,$length,[${styles.joinToString { it }}])"
    override fun hashCode(): Int = arrayOf(styles, position, length).contentDeepHashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is AglTokenDefault -> false
        other.position != this.position -> false
        other.length != this.length -> false
        other.styles != this.styles -> false
        else -> true
    }
}

class AglTokenizer<AsmType : Any, ContextType : Any>(
    val agl: AglComponents<AsmType, ContextType>
) {

    var acceptingTokens = false
    val tokensByLine = mutableMapOf<Int, List<AglToken>>()

    fun reset() {
        this.acceptingTokens = false
        this.tokensByLine.clear()
    }

    fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        if (this.acceptingTokens) {
            tokensForLines.forEachIndexed { index, tokens ->
                // could get empty tokens for a line from a partial parse
                if (tokens.isNotEmpty()) {
                    this.tokensByLine[startLine + index] = tokens
                } else {
                    // nothing
                }
            }
        }
    }

    /**
     * row - 0 indexed line number
     */
    fun getLineTokens(lineText: String, previousLineState: AglLineState): AglLineState {
        val tokens = this.tokensByLine[previousLineState.lineNumber + 1]
        val validStart = tokens?.firstOrNull()?.position == previousLineState.nextLineStartPosition
        // last token should be length 1 and an eol, so only need its position as linesText does not include the eol
        val validEnd = tokens?.lastOrNull()?.position == previousLineState.nextLineStartPosition + lineText.length
        return if (validStart && validEnd && null != tokens) {
            this.useCachedTokens(tokens, lineText, previousLineState)
        } else {
            this.getLineTokensByScan(lineText, previousLineState)
        }
    }

    /**
     * row assumed to start at 0
     */
    fun getLineTokensByScan(lineText: String, previousLineState: AglLineState): AglLineState {
        return try {
            val scanner = agl.simpleScanner
            val text = previousLineState.leftOverText + lineText
            val offset = previousLineState.nextLineStartPosition - previousLineState.leftOverText.length
            val sentence = SentenceDefault(text)
            val tv = measureTimedValue {
                scanner.scan(sentence, 0, offset)
            }
            this.agl.logger.log(LogLevel.Debug, "Scanning on main thread text took ${tv.duration.toString(DurationUnit.MILLISECONDS)} ms", null)
            val leafs = tv.value.tokens
            val tokens = this.agl.styleHandler.transformToTokens(leafs)
            //val tokens = transformToTokens(leafs)
            if (leafs.isEmpty()) {
                AglLineState(previousLineState.lineNumber + 1, previousLineState.nextLineStartPosition + 1, "", emptyList())
            } else {
                val lastLeaf = leafs.last()
                val endOfLastLeaf = lastLeaf.position - offset + lastLeaf.length
                val leftOverText = lineText.substring(endOfLastLeaf, lineText.length)
                val nextLineStartPosition = previousLineState.nextLineStartPosition + lineText.length + 1
                AglLineState(previousLineState.lineNumber + 1, nextLineStartPosition, leftOverText, tokens)
            }
        } catch (t: Throwable) {
            agl.logger.log(LogLevel.Error, "Unable to create LanguageProcessor", t)
            val tokens = when {
                lineText.isEmpty() -> emptyList()
                else -> listOf(AglTokenDefault(listOf("nostyle"), previousLineState.nextLineStartPosition, lineText.length))
            }
            val nextLineStartPosition = previousLineState.nextLineStartPosition + lineText.length + 1
            AglLineState(previousLineState.lineNumber + 1, nextLineStartPosition, "", tokens)
        }
    }

    /**
     * row assumed to start at 0
     */
    fun useCachedTokens(tokens: List<AglToken>, lineText: String, previousLineState: AglLineState): AglLineState {
        val nextLineStartPosition = previousLineState.nextLineStartPosition + lineText.length + 1
        val endState = AglLineState(previousLineState.lineNumber + 1, nextLineStartPosition, "", tokens)
        return endState
    }
}