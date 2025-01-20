package net.akehurst.language.editor.common

import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.EditorStyle
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.style.api.AglStyleRule

data class HtmlStyle(override val identity: EditorStyleIdentity) : EditorStyle {
    var css: String = ""
    var isBold: Boolean = false
    var isItalic: Boolean = false
}

class AglStyleHandlerAsHtml(
    languageId: LanguageIdentity,
) : AglStyleHandlerAbstract<HtmlStyle>(languageId) {

    companion object {
        val STRING_TO_HTML = mapOf(
            "&" to "&amp;",
            "<" to "&lt;",
            ">" to "&gt;",
            "\"" to "&quot;",
            "'" to "&apos;",
        )

       //val String.escapeForHtml get() = this.replace(Regex("[&<>'\"]")) {mr -> STRING_TO_HTML[mr.value]!!}
       fun escapeForHtml(str:String) = str.replace(Regex("[&<>'\"]")) {mr -> STRING_TO_HTML[mr.value]!!}

    }

    override val EDITOR_NO_STYLE: HtmlStyle = HtmlStyle(EditorStyleIdentity.NO_STYLE)

    override fun createEditorStyleType(identity: EditorStyleIdentity): HtmlStyle = HtmlStyle(identity)
    override fun updateEditorStyles(editorStyles: List<HtmlStyle>, sr: AglStyleRule) {
        val mergedCss = mutableMapOf<String, String>()
        var mergedIsBold = false
        var mergedIsItalic = false
        sr.declaration.values.forEach { oldStyle ->
            when (oldStyle.name) {
                "foreground" -> mergedCss["color"] = oldStyle.value
                "background" -> mergedCss["background-color"] = oldStyle.value
                "font-style" -> when (oldStyle.value) {
                    "bold" -> mergedIsBold = true
                    "italic" -> mergedIsItalic = true
                    else -> Pair(oldStyle.name, oldStyle.value)
                }

                else -> Pair(oldStyle.name, oldStyle.value)
            }
        }
        val css = mergedCss.map { (k, v) -> "$k:$v;" }.joinToString(separator = "")
        editorStyles.forEach { style ->
            style.css = css
            style.isBold = mergedIsBold
            style.isItalic = mergedIsItalic
        }
    }

    fun applyHtmlStyling(sentence: Sentence, lineTokens: List<AglToken>): String = when {
        lineTokens.isEmpty() -> ""
        else -> {
            val sb = StringBuilder()
            for (tok in lineTokens) {
                val txt = sentence.textAt(tok.position, tok.length)
                val styled = applyStyle(txt, tok.styles)
                sb.append(styled)
            }
            sb.toString()
        }
    }


    private fun applyStyle(text: String, styles: List<EditorStyleIdentity>): String {
        val sb = StringBuilder()
        val edStyles = styles.mapNotNull { this.editorStyleFor(it) }
        val cssStyle = edStyles.joinToString(separator = "") { it.css }
        var wrappedText = escapeForHtml(text)
        if (edStyles.any { it.isBold }) {
            wrappedText = "<b>$wrappedText</b>"
        }
        if (edStyles.any { it.isItalic }) {
            wrappedText = "<i>$wrappedText</i>"
        }
        val styledText = when {
            //cssStyle.isBlank() -> wrappedText
            else -> "<span style='$cssStyle'>$wrappedText</span>"
        }
        sb.append(styledText)
        return sb.toString()
    }
}