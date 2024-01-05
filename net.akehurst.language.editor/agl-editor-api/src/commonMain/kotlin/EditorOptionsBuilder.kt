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

package net.akehurst.language.editor.api

@DslMarker
annotation class EditorOptionsDslMarker

fun AglEditorOptions(base: EditorOptions, init: EditorOptionsBuilder.() -> Unit): EditorOptions {
    val b = EditorOptionsBuilder(base)
    b.init()
    return b.build()
}

@EditorOptionsDslMarker
class EditorOptionsBuilder(
    base: EditorOptions
) {
    private var _parse: Boolean = base.parse
    private var _parseLineTokens: Boolean = base.parseLineTokens
    private var _lineTokensChunkSize: Int = base.lineTokensChunkSize
    private var _parseTree: Boolean = base.parseTree
    private var _syntaxAnalysis: Boolean = base.syntaxAnalysis
    private var _syntaxAnalysisAsm: Boolean = base.syntaxAnalysisAsm
    private var _semanticAnalysis: Boolean = base.semanticAnalysis
    private var _semanticAnalysisAsm: Boolean = base.semanticAnalysisAsm

    fun lineTokensChunkSize(value: Int) {
        _lineTokensChunkSize = value
    }

    fun build(): EditorOptions {
        return EditorOptionsDefault(
            _parse,
            _parseLineTokens,
            _lineTokensChunkSize,
            _parseTree,
            _syntaxAnalysis,
            _syntaxAnalysisAsm,
            _semanticAnalysis,
            _semanticAnalysisAsm
        )
    }
}

data class EditorOptionsDefault(
    override var parse: Boolean = true,
    override var parseLineTokens: Boolean = true,
    override var lineTokensChunkSize: Int = 0,
    override var parseTree: Boolean = true,
    override var syntaxAnalysis: Boolean = true,
    override var syntaxAnalysisAsm: Boolean = true,
    override var semanticAnalysis: Boolean = true,
    override var semanticAnalysisAsm: Boolean = true,
) : EditorOptions