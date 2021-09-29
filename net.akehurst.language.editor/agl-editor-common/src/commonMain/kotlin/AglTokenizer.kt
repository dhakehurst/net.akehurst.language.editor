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

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.api.AglEditorLogger

interface AglTokenizerByWorker {

    var acceptingTokens:Boolean

    fun receiveTokens(lineTokens: Array<Array<AglToken>>)
    fun reset()

}

class AglComponents(
    languageId: String,
    val editorId:String,
    val logger: AglEditorLogger
) {
    private var _languageDefinition: LanguageDefinition = Agl.registry.findOrPlaceholder(languageId)
    private var _styleHandler = AglStyleHandler(languageId)

    val languageDefinition get() = _languageDefinition
    var goalRule:String? = languageDefinition.defaultGoalRule

    val styleHandler get() = _styleHandler

    var context:Any? = null
    var sppt: SharedPackedParseTree? = null
    var asm: Any? = null

    var languageIdentity:String get() = languageDefinition.identity
    set(value) {
        val grammarObservers = this._languageDefinition.grammarObservers
        val styleObservers = this._languageDefinition.styleObservers
        val formatObservers = this._languageDefinition.formatObservers
        this._languageDefinition = Agl.registry.findOrPlaceholder(languageIdentity)
        this._languageDefinition.grammarObservers.addAll(grammarObservers)
        this._languageDefinition.styleObservers.addAll(styleObservers)
        this._languageDefinition.formatObservers.addAll(formatObservers)

        this._styleHandler = AglStyleHandler(languageIdentity)
        this.sppt = null
        this.asm = null
    }
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

class AglTokenizer(
        val agl: AglComponents
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

    private fun mapToCssClasses(leaf: SPPTLeaf): List<String> {
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

    fun transformToTokens(leafs: List<SPPTLeaf>): List<AglToken> {
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
        val proc = this.agl.languageDefinition.processor
        return if (null != proc) {
            val text = state.leftOverText + lineText
            val leafs = proc.scan(text);
            val tokens = transformToTokens(leafs)
            val endState = if (leafs.isEmpty()) {
                AglLineState(row, "", emptyList())
            } else {
                val lastLeaf = leafs.last()
                val endOfLastLeaf = lastLeaf.location.column + lastLeaf.location.length
                val leftOverText = lineText.substring(endOfLastLeaf, lineText.length)
                AglLineState(row, leftOverText, tokens)
            }
            return endState
        } else {
            AglLineState(row, "", listOf(AglToken(emptyArray(), lineText, row, 0)))
        }
    }

    fun getLineTokensByParse(lineText: String, state: AglLineState, row: Int): AglLineState {
        val sppt = this.agl.sppt!!
        val leafs = sppt.tokensByLine(row) //TODO: find more efficient way to do this, i.e. using lineText and state
        //return if (null != leafs) {
            val tokens = transformToTokens(leafs)
            val endState = AglLineState(row, "", tokens)
            return endState
        //} else {
        //    AglLineState(row, "", emptyList())
        //}
    }
}