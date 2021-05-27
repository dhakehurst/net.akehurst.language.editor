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

package net.akehurst.language.editor.monaco

import monaco.IRange
import monaco.editor.IModelDecorationOptions
import monaco.editor.IModelDeltaDecoration
import net.akehurst.language.editor.common.*

class ModelDecorationOptions(
    override var afterContentClassName: String? = null,
    override var beforeContentClassName: String? = null,
    override var className: String? = null,
    override var glyphMarginClassName: String? = null,
    override var glyphMarginHoverMessage: dynamic = null,
    override var hoverMessage: dynamic = null,
    override var inlineClassName: String? = null,
    override var inlineClassNameAffectsLetterSpacing: Boolean? = null,
    override var isWholeLine: Boolean? = null,
    override var linesDecorationsClassName: String? = null,
    override var marginClassName: String? = null,
    override var minimap: dynamic = null,
    override var overviewRuler: dynamic = null,
    override var stickiness: dynamic = null,
    override var zindex: dynamic = null
) : IModelDecorationOptions

class AglTokenizerByWorkerMonaco(
    val aglEditor: AglEditorMonaco,
    val agl: AglComponents
) : monaco.languages.TokensProvider {

    val aglTokenizer = AglTokenizer(agl)
    var acceptingTokens = false
    val tokensByLine = mutableMapOf<Int, List<AglToken>>()
    val decs = mutableMapOf<Int, Array<String>>()

    fun reset() {
        this.acceptingTokens = false
        this.tokensByLine.clear()
    }

    fun receiveTokens(tokens: Array<Array<AglToken>>) {
        if (this.acceptingTokens) {
            tokens.forEachIndexed { index, tokens ->
                this.tokensByLine[index] = tokens.toList()
            }
        }
    }

    // --- monaco.langugaes.Tokenizer ---

    override fun getInitialState(): monaco.languages.IState {
        return AglLineStateMonaco(0, "")
    }

    override fun tokenize(line: String, pState: monaco.languages.IState): monaco.languages.ILineTokens {
        val mcState = pState as AglLineStateMonaco
        val row = mcState.lineNumber + 1
        val tokens = this.tokensByLine[row - 1]
        return if (null == tokens) {
            // no tokens received from worker, try local scan
            val stateAgl = AglLineState(mcState.lineNumber, mcState.leftOverText, emptyList()) //not really emptyList, but its not needed as input so ok to use
            val ltokens = this.aglTokenizer.getLineTokensByScan(line, stateAgl, row)
            this.decorateLine(row, ltokens.tokens)
            val lineTokens: List<AglTokenMonaco> = ltokens.tokens.map {
                AglTokenMonaco(
                    it.styles.firstOrNull() ?: "",
                    it.column
                )
            }
            val lt: Array<monaco.languages.IToken> = lineTokens.toTypedArray()
            AglLineTokensMonaco(
                AglLineStateMonaco(row, ""),
                lt
            )
        } else {
            this.decorateLine(row, tokens)
            val lineTokens: List<AglTokenMonaco> = tokens.map {
                AglTokenMonaco(
                    it.styles.firstOrNull() ?: "",
                    it.column
                )
            }
            val lt: Array<monaco.languages.IToken> = lineTokens.toTypedArray()
            AglLineTokensMonaco(
                AglLineStateMonaco(row, ""),
                lt
            )
        }
    }

    fun decorateLine(lineNum: Int, tokens: List<AglToken>) {
        val decs: Array<IModelDeltaDecoration> = tokens.map { aglTok ->
            objectJSTyped<IModelDeltaDecoration> {
                range = objectJSTyped<IRange> {
                    startColumn = aglTok.column
                    endColumn = aglTok.column + aglTok.value.length
                    startLineNumber = aglTok.line
                    endLineNumber = aglTok.line
                }
                options = ModelDecorationOptions(
                    inlineClassName = aglTok.styles.joinToString(separator = " ") { "monaco_${it}" }
                )
            }
        }.toTypedArray()
        val curDes = this.decs[lineNum] ?: emptyArray()
        val d = aglEditor.monacoEditor.deltaDecorations(curDes, decs)
        this.decs[lineNum] = d
    }

}