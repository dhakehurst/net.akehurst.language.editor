package net.akehurst.language.editor.ide.intelliJ.agl.grammar

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class AglGrammarIntelliJFileType : LanguageFileType(AglGrammarIntelliJLanguage.INSTANCE) {

    companion object {
        val ICON = IconLoader.getIcon("/agl-editor-icon.ico", AglGrammarIntelliJFileType::class.java)
    }

    override fun getName(): String  = "Agl Grammar"
    override fun getDescription(): String = "Agl grammar file"
    override fun getDefaultExtension(): String = "agl"
    override fun getIcon(): Icon = ICON

}