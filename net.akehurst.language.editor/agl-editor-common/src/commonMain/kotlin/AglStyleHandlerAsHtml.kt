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
import net.akehurst.language.style.processor.AglStyle

data class HtmlStyle(override val identity: EditorStyleIdentity) : EditorStyle {
    var css: Map<String, String> = mutableMapOf()
    var isBold: Boolean? = null
    var isItalic: Boolean? = null
}

data class CssToken(
    var css: Map<String, String>,
    var isBold: Boolean?,
    var isItalic: Boolean?,
    val position: Int,
    val length: Int
) {
    fun styleMatches(other: CssToken): Boolean {
        if (this.isBold != other.isBold) return false
        if (this.isItalic != other.isItalic) return false
        if (this.css != other.css) return false
        return true
    }
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

        /**
         * encode HTML special characters
         * * `&` -> `&amp;`</li>
         * * `<` -> `&lt;`</li>
         * * `>` -> `&gt;`</li>
         * * `"` -> `&quot;`</li>
         * * `'` -> `&apos;`</li>
         */
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
        var mergedIsBold: Boolean? = null
        var mergedIsItalic: Boolean? = null
        sr.declaration.values.forEach { oldStyle ->
            when (oldStyle.name) {
                "foreground" -> mergedCss["color"] = oldStyle.value
                "background" -> mergedCss["background-color"] = oldStyle.value
                "font-style" -> when (oldStyle.value) {
                    "italic" -> mergedIsItalic = true
                    "normal" -> mergedIsItalic = false
                    else -> Pair(oldStyle.name, oldStyle.value)
                }

                "font-weight" -> when (oldStyle.value) {
                    "bold" -> mergedIsBold = true
                    "normal" -> mergedIsBold = false
                    else -> Pair(oldStyle.name, oldStyle.value)
                }

                else -> Pair(oldStyle.name, oldStyle.value)
            }
        }
        //val css = mergedCss.map { (k, v) -> "$k:$v;" }.joinToString(separator = "")
        editorStyles.forEach { style ->
            style.css = mergedCss
            style.isBold = mergedIsBold
            style.isItalic = mergedIsItalic
        }
    }

    fun applyHtmlStyling(sentence: Sentence, tokens: List<AglToken>): String = when {
        tokens.isEmpty() -> ""
        else -> {
            val styledTokens = tokens.map { toCssStyle(it) }
            val merged = mergeStyles(styledTokens)
            val sb = StringBuilder()
            for (tok in merged) {
                val txt = sentence.textAt(tok.position, tok.length)
                val styled = applyStyle(txt, tok)
                sb.append(styled)
            }
            sb.toString()
        }
    }

    private fun toCssStyle(token: AglToken): CssToken {
        val edStyles = token.styles.mapNotNull { this.editorStyleFor(it) }
        val merged = edStyles.fold(mapOf<String, String>()) { acc, it -> acc.plus(it.css) }
        val fontStyle: Boolean? = edStyles.fold(null) { acc, it -> it.isItalic ?: acc }
        val fontWeight: Boolean? = edStyles.fold(null) { acc, it -> it.isBold ?: acc }
        return CssToken(
            css = merged,
            isBold = fontWeight,
            isItalic = fontStyle,
            position = token.position,
            length = token.length
        )
    }

    private fun mergeStyles(tokens: List<CssToken>): List<CssToken> {
        val merged = mutableListOf<CssToken>()
        var last = tokens.first()
        for (i in 1 until tokens.size) {
            val tok = tokens[i]
            when {
                tok.styleMatches(last) -> {
                    last = CssToken(
                        last.css,
                        last.isBold,
                        last.isItalic,
                        last.position,
                        tok.length + last.length
                    )
                }

                else -> {
                    merged.add(last)
                    last = tok
                }
            }
        }
        merged.add(last)
        return merged
    }

    private fun applyStyle(text: String, style: CssToken): String {
        val sb = StringBuilder()
        val cssStyle = style.css.entries.joinToString(separator = "") { (k, v) -> "$k:$v;" }
        var wrappedText = encodeForHtml(text)
        if (true == style.isBold) {
            wrappedText = "<b>$wrappedText</b>"
        }
        if (true == style.isItalic) {
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