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
        val REGEX_MATCH_SPECIAL = Regex("[&<>'\"]")
        val REGEX_MATCH_ENCODED_SPECIAL = Regex("&[^;]+;")
        val STRING_TO_HTML = mapOf(
            "&" to "&amp;",
            "<" to "&lt;",
            ">" to "&gt;",
            "\"" to "&quot;",
            "'" to "&apos;",
        )
        val HTML_TO_STRING = STRING_TO_HTML.entries.associate { (k, v) -> v to k }

        fun encodeForHtml(str: String) = str.replace(REGEX_MATCH_SPECIAL) { mr ->
            val matched = mr.value
            STRING_TO_HTML[matched]
                ?: error("No replacement configured for ${mr.value}")
        }

        fun decodeFromHtml(encodedHtml: String) = decodeFromHtml1(decodeFromHtml1(encodedHtml))
        fun decodeFromHtml1(encodedHtml: String) = encodedHtml.replace(REGEX_MATCH_ENCODED_SPECIAL) { mr ->
            val matched = mr.value
            HTML_TO_STRING[matched]
                ?: error("No replacement configured for ${mr.value}")
        }

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
        var wrappedText = encodeForHtml(text)
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