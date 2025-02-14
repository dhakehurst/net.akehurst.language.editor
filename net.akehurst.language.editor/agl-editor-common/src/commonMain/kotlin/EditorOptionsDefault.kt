package net.akehurst.language.editor.common

import net.akehurst.language.editor.api.EditorOptions
import net.akehurst.language.editor.api.StyleCompletionItem

data class EditorOptionsDefault(
    override var parse: Boolean = true,
    override var parseLineTokens: Boolean = true,
    override var lineTokensChunkSize: Int = 0,
    override var parseTree: Boolean = true,
    override var syntaxAnalysis: Boolean = true,
    override var syntaxAnalysisAsm: Boolean = true,
    override var semanticAnalysis: Boolean = true,
    override var semanticAnalysisAsm: Boolean = true,
    override var styleCompletionItem: StyleCompletionItem? = null
) : EditorOptions