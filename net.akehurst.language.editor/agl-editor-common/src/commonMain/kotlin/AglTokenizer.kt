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

import net.akehurst.language.agl.scanner.AglScanner
import net.akehurst.language.api.sppt.LeafData
import net.akehurst.language.api.sppt.SPPTLeaf
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
    val leftOverText: String,
    val tokens: List<AglToken>
) {
}

class AglToken(
    val styles: Array<String>,
    val value: String,
    val line: Int,
    val column: Int
)

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
            var beforeEOL = leaf.matchedText
            val eolIndex = leaf.matchedText.indexOf('\n');
            if (-1 != eolIndex) {
                beforeEOL = leaf.matchedText.substring(0, eolIndex);
            }
            AglToken(
                cssClasses.toSet().toTypedArray(),
                beforeEOL,
                leaf.location.line, //ace first line is 0
                leaf.location.column
            )
        }
    }

    fun getLineTokensByScan(lineText: String, state: AglLineState, row: Int): AglLineState {
        return try {
            val scanner = AglScanner()
            val text = state.leftOverText + lineText
            val tv = measureTimedValue {
                scanner.scan(text, agl.scannerMatchables)
            }
            this.agl.logger.log(LogLevel.Debug, "Scanning on main thread text took ${tv.duration.toString(DurationUnit.MILLISECONDS)} ms", null)
            val leafs = tv.value.tokens
            val tokens = transformToTokens(leafs)
            if (leafs.isEmpty()) {
                AglLineState(row, "", emptyList())
            } else {
                val lastLeaf = leafs.last()
                val endOfLastLeaf = lastLeaf.location.column + lastLeaf.location.length
                val leftOverText = lineText.substring(endOfLastLeaf, lineText.length)
                AglLineState(row, leftOverText, tokens)
            }
        } catch (t: Throwable) {
            agl.logger.log(LogLevel.Error, "Unable to create LanguageProcessor", t)
            AglLineState(row, "", listOf(AglToken(arrayOf("nostyle"), lineText, row, 0)))
        }
    }

    fun getLineTokensByParse(lineText: String, state: AglLineState, row: Int): AglLineState {
        val sppt = this.agl.sppt!!
        val leafs = sppt.tokensByLine(row) //TODO: find more efficient way to do this, i.e. using lineText and state
        val tokens = transformToTokens(leafs)
        val endState = AglLineState(row, "", tokens)
        return endState
    }
}