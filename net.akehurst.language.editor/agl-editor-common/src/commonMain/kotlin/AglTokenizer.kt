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
import net.akehurst.language.api.sppt.LeafData
import net.akehurst.language.editor.api.LogLevel
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

interface AglTokenizerByWorker {

    var acceptingTokens: Boolean
    val tokensByLine: Map<Int, List<AglToken>>

    fun receiveTokens(lineTokens: List<List<AglToken>>)
    fun reset()

}

class AglLineState(
    val lineNumber: Int,
    val nextLineStartPosition: Int,
    val leftOverText: String,
    val tokens: List<AglToken>
) {
}

class AglToken(
    val styles: List<String>,
    val position: Int,
    val length: Int
) {
    override fun toString(): String = "AglToken($position,$length,[${styles.joinToString { it }}])"
    override fun hashCode(): Int = arrayOf(styles, position, length).contentDeepHashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is AglToken -> false
        other.position != this.position -> false
        other.length != this.length -> false
        other.styles != this.styles -> false
        else -> true
    }
}

class AglTokenizer<AsmType : Any, ContextType : Any>(
    val agl: AglComponents<AsmType, ContextType>
) {

    fun getLineTokens(lineText: String, pState: AglLineState, line: Int): AglLineState {
        val sppt = this.agl.sppt
        return if (null == sppt) {
            this.getLineTokensByScan(lineText, pState, line)
        } else {
            this.getLineTokensByParse(lineText, pState, line)
        }
    }

    private fun mapTokenTypeToClass(tokenType: String): String {
        val cssClass = this.agl.styleHandler.mapClass(tokenType)// this.agl.styleHandler.tokenToClassMap.get(tokenType)
        return cssClass
    }

    private fun mapToCssClasses(leaf: LeafData): List<String> {
        val metaTagClasses = leaf.metaTags.map { this.mapTokenTypeToClass(it) }
        val otherClasses = if (!leaf.tagList.isEmpty()) {
            leaf.tagList.map { this.mapTokenTypeToClass(it) }
        } else {
            listOf(this.mapTokenTypeToClass(leaf.name)).map { it }
        }
        val classes = metaTagClasses + otherClasses
        return if (classes.isEmpty()) {
            listOf("nostyle")
        } else {
            classes.toSet().toList()
        }
    }

    fun transformToTokens(leafs: List<LeafData>): List<AglToken> {
        return leafs.map { leaf ->
            val cssClasses = this.mapToCssClasses(leaf)
            AglToken(
                cssClasses.toSet().toList(),
                leaf.position,
                leaf.length
            )
        }
    }

    /**
     * row assumed to start at 0
     */
    fun getLineTokensByScan(lineText: String, state: AglLineState, row: Int): AglLineState {
        return try {
            val scanner = agl.languageDefinition.processor!!.scanner!! //FIXME: don't create the processor here in main thread!! //AglScanner()
            val text = state.leftOverText + lineText
            val offset = state.nextLineStartPosition - state.leftOverText.length
            val sentence = SentenceDefault(text)
            val tv = measureTimedValue {
                scanner.scan(sentence, 0, offset)
            }
            this.agl.logger.log(LogLevel.Debug, "Scanning on main thread text took ${tv.duration.toString(DurationUnit.MILLISECONDS)} ms", null)
            val leafs = tv.value.tokens
            val tokens = transformToTokens(leafs)
            if (leafs.isEmpty()) {
                AglLineState(row, state.nextLineStartPosition+1, "", emptyList())
            } else {
                val lastLeaf = leafs.last()
                val endOfLastLeaf = lastLeaf.position - offset + lastLeaf.length
                val leftOverText = lineText.substring(endOfLastLeaf, lineText.length)
                AglLineState(row, state.nextLineStartPosition + lineText.length+1, leftOverText, tokens)
            }
        } catch (t: Throwable) {
            agl.logger.log(LogLevel.Error, "Unable to create LanguageProcessor", t)
            val tokens = when {
                lineText.isEmpty() -> emptyList()
                else -> listOf(AglToken(listOf("nostyle"), state.nextLineStartPosition, lineText.length))
            }
            AglLineState(row, state.nextLineStartPosition + lineText.length+1, "", tokens)
        }
    }

    /**
     * row assumed to start at 0
     */
    fun getLineTokensByParse(lineText: String, state: AglLineState, row: Int): AglLineState {
        val sppt = this.agl.sppt!!
        val leafs = sppt.tokensByLine(row) //TODO: find more efficient way to do this, i.e. using lineText and state
        val tokens = transformToTokens(leafs)
        val endState = AglLineState(row, state.nextLineStartPosition + lineText.length+1, "", tokens)
        return endState
    }
}