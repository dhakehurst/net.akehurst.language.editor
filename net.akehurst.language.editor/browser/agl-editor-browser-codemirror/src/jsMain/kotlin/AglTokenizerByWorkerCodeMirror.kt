/**
 * Copyright (C) 2022 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
package net.akehurst.language.editor.browser.codemirror

import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.common.*

class AglTokenizerByWorkerCodeMirror<AsmType : Any, ContextType : Any>(
    val codemirror: codemirror.ICodeMirror,
    val cmEditorView: codemirror.view.IEditorView,
    agl: AglComponents<AsmType, ContextType>
) : AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)
//    override var acceptingTokens
//        get() = aglTokenizer.acceptingTokens
//        set(value) {
//            aglTokenizer.acceptingTokens = value
//        }

    private val _updateTokens = codemirror.state.StateEffect.define<Boolean>()
    private val _tokenEffect = codemirror.state.StateEffect.define<List<AglToken>>()

    // if the doc changes or the visible part of the doc changes, we also need to update the decorations
    val _tokenUpdateListener = codemirror.view.EditorView.updateListener.of({ view: codemirror.view.IViewUpdate ->
        if (view.docChanged || view.viewportChanged) {
            this.updateVisibleTokens(view.view)
        } else {
            //do nothing
        }
    })

    val _decorationUpdater = codemirror.state.StateField.define(object : codemirror.state.StateFieldConfig<codemirror.view.DecorationSet> {
        override var create: (state: codemirror.state.IEditorState) -> codemirror.view.DecorationSet = {
            codemirror.view.Decoration.none
        }

        override var update: (value: codemirror.view.DecorationSet, transaction: codemirror.state.ITransaction) -> codemirror.view.DecorationSet = { value, transaction ->
            val decorationsRangeSet = value.map(transaction.changes)
            val marks = mutableListOf<codemirror.state.IRange<codemirror.view.IDecoration>>()
            for (e in transaction.effects) {
                if (e._is(_tokenEffect)) {
                    val lineTokens = e.value as List<AglTokenDefault>
                    for (tk in lineTokens) {
                        val mark = codemirror.view.Decoration.mark(
                            objectJSTyped<codemirror.view.MarkDecorationSpec> {
                                inclusiveEnd = true
                                _class = tk.styles.joinToString(separator = " ")
                            }
                        ).range(tk.position, tk.position + tk.length)
                        marks.add(mark  as codemirror.state.IRange<codemirror.view.IDecoration>)
                    }
                }
            }
            if (marks.isEmpty()) {
                decorationsRangeSet
            } else {
                decorationsRangeSet.update(objectJSTyped<codemirror.state.RangeSetUpdate<codemirror.view.IDecoration>> {
                    add = marks.toTypedArray()
                    filter = { _, _, _ -> false } // remove existing
                })
            }
        }
        override var compare: ((a: codemirror.view.DecorationSet, b: codemirror.view.DecorationSet) -> Boolean)? = null
        override var provide: ((codemirror.state.IStateField<codemirror.view.DecorationSet>) -> dynamic)? = { f -> codemirror.view.EditorView.decorations.from(f) }
        override var toJSON: ((codemirror.view.DecorationSet, codemirror.state.IEditorState) -> Any)? = null
        override var fromJSON: ((json: Any, state: codemirror.state.IEditorState) -> codemirror.view.DecorationSet)? = null
    })

    override fun reset() {
        this.aglTokenizer.reset()
    }

    fun updateVisibleTokens(view: codemirror.view.IEditorView) {
        val tokenEffects = mutableListOf<codemirror.state.IStateEffect<List<AglToken>>>()
        for (rng in view.visibleRanges) {
            val fromLine = view.state.doc.lineAt(rng.from).number
            val toLine = view.state.doc.lineAt(rng.to).number
            for (ln in fromLine..toLine) {
                val lineToks = aglTokenizer.tokensByLine[ln-1]
                if (null != lineToks) {
                    tokenEffects.add(_tokenEffect.of(lineToks))
                } else {
                    //TODO: get tokens using scanner
                    //aglTokenizer.
                }
            }
        }
        if (tokenEffects.isNotEmpty()) {
            //this will trigger the 'update' of _decorationUpdater
            view.dispatch(objectJSTyped<codemirror.state.TransactionSpec> {
                effects = tokenEffects.toTypedArray()
            })
        }
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        aglTokenizer.receiveTokens(startLine, tokensForLines)
        this.updateVisibleTokens(this.cmEditorView)
    }

    // --- ace.Ace.Tokenizer ---
    /*override fun getLineTokens(line: String, pState: ace.LineState?, row: Int): ace.LineTokens {
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


     */

}