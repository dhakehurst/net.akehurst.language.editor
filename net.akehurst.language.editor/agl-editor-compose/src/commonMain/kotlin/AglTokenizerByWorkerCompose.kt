/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.compose

import androidx.compose.ui.text.SpanStyle
import net.akehurst.kotlin.compose.editor.api.EditorLineToken
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglLineState
import net.akehurst.language.editor.common.AglTokenizer
import net.akehurst.language.editor.common.AglTokenizerByWorker
import kotlin.collections.set

class AglTokenizerByWorkerCompose<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>,
    val logger: AglEditorLogger
) : AglTokenizerByWorker<ComposeStyle> {

    val aglTokenizer = AglTokenizer<AsmType, ContextType, ComposeStyle>(agl,agl.logger)

    private var _lineStates = mutableMapOf<Int, AglLineState>()

    fun refresh() {
    }

    override fun reset() {
        logger.log(LogLevel.Trace, "AglTokenizerByWorkerCompose.reset()", null)
        this.aglTokenizer.reset()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        logger.log(LogLevel.Trace, "AglTokenizerByWorkerCompose.receiveTokens $startLine, $tokensForLines", null)
        this.aglTokenizer.receiveTokens(startLine, tokensForLines)
        refresh()
    }

    fun getLineTokens(lineNumber: Int, lineStartPosition: Int, lineText: String): List<EditorLineToken> {
        val aglState = _lineStates[lineNumber - 1] ?: let {
            val newstate = AglLineState(lineNumber-1, 0, "")
            _lineStates[lineNumber-1] = newstate
            newstate
        }
        val (state, toks) = aglTokenizer.getLineTokens(lineText, aglState)
        _lineStates[lineNumber] = state
        val edToks = toks.map {
            val styles = it.styles.mapNotNull { (aglTokenizer.agl.styleHandler.editorStyleFor(it) as ComposeStyle?)?.composeStyle }
            val style = styles.fold(ComposeStyle(EditorStyleIdentity.NO_STYLE).composeStyle) { acc, it -> acc.merge(it) }
            object : EditorLineToken {
                override val start: Int = it.position - lineStartPosition
                override val finish: Int = (it.position + it.length) - lineStartPosition
                override val style: SpanStyle = style
            }
        }
        return edToks
    }
}