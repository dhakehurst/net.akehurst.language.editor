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

import monaco.IRange
import monaco.editor.IModelDecorationOptions
import monaco.editor.IModelDeltaDecoration
import monaco.editor.IStandaloneCodeEditor
import net.akehurst.language.api.sppt.Sentence
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

internal class AglTokenizerByWorkerMonaco<AsmType : Any, ContextType : Any>(
    val monacoEditor: IStandaloneCodeEditor,
    agl: AglComponents<AsmType, ContextType>
) : monaco.languages.TokensProvider, AglTokenizerByWorker {

    val decs = mutableMapOf<Int, Array<String>>()

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

    // --- monaco.languaes.Tokenizer ---
    override fun getInitialState(): monaco.languages.IState {
        return AglLineStateMonaco(0, 0, "")
    }

    override fun tokenize(line: String, pState: monaco.languages.IState): monaco.languages.ILineTokens {
        val mcState = pState as AglLineStateMonaco
        val row = mcState.lineNumber + 1
        val stateAgl = AglLineState(mcState.lineNumber, mcState.lineStartPosition, mcState.leftOverText, emptyList()) //not really emptyList, but its not needed as input so ok to use
        val nextState = this.aglTokenizer.getLineTokens(line, stateAgl)
        val tokens = nextState.tokens
        this.decorateLine(row, tokens) //TODO: move inside the loop
        val lineTokens: List<AglTokenMonaco> = tokens.map {aglTok->
            val col = aglTok.position - mcState.lineStartPosition
            AglTokenMonaco(
                aglTok.styles.firstOrNull() ?: "",
                col
            )
        }
        val lt: Array<monaco.languages.IToken> = lineTokens.toTypedArray()
        return AglLineTokensMonaco(
            AglLineStateMonaco(row, mcState.lineStartPosition + line.length, ""),
            lt
        )
    }

    private fun decorateLine(lineNum: Int, tokens: List<AglToken>) {
        val lineStartPosition = tokens.first().position
        val decs: Array<IModelDeltaDecoration> = tokens.map { aglTok ->
            val col = aglTok.position - lineStartPosition
            objectJSTyped<IModelDeltaDecoration> {
                range = objectJSTyped<IRange> {
                    startColumn = col
                    endColumn = col + aglTok.length
                    startLineNumber = lineNum
                    endLineNumber = lineNum
                }
                options = ModelDecorationOptions(
                    inlineClassName = aglTok.styles.joinToString(separator = " ") { "monaco_${it}" }
                )
            }
        }.toTypedArray()
        val curDes = this.decs[lineNum] ?: emptyArray()
        val d = monacoEditor.deltaDecorations(curDes, decs)
        this.decs[lineNum] = d
    }

}