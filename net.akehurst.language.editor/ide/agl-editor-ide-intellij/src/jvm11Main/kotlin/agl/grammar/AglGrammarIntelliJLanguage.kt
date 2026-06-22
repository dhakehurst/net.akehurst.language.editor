package net.akehurst.language.editor.ide.intelliJ.agl.grammar

import com.intellij.lang.Language


class AglGrammarIntelliJLanguage private constructor(): Language("") {
    companion object {
        val INSTANCE = AglGrammarIntelliJLanguage()
    }
}