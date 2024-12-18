package net.akehurst.language.editor.compose

import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenizer
import net.akehurst.language.editor.common.AglTokenizerByWorker

class AglTokenizerByWorkerCompose<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>,
    val logger: AglEditorLogger
) : AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)

    override fun reset() {
        logger.log(LogLevel.Trace, "reset()", null)
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        logger.log(LogLevel.Trace, "receiveTokens $startLine, $tokensForLines", null)


    }

}