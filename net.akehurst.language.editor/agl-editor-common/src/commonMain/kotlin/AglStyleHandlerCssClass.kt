/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
import net.akehurst.language.editor.api.EditorStyle
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.style.api.AglStyleRule
import kotlin.jvm.JvmInline

//TODO: seems to be a kotlin bug with nested value classes - found in updateStyleRule
// return from convert and use as index to map
data class CssClassStyle(override val identity: EditorStyleIdentity) : EditorStyle {
    val cssClassName: String get() = identity.value.replace(Regex("[^a-z0-9A-Z_-]"), "_")
}

open class AglStyleHandlerCssClass(
    languageId: LanguageIdentity,
) : AglStyleHandlerAbstract<CssClassStyle>(languageId, CssClassStyle(EditorStyleIdentity.NO_STYLE)) {

    companion object {
        fun toCss(selectors: List<CssClassStyle>, declarations: LinkedHashMap<String, String>): String {
            return """
            ${selectors.joinToString(separator = ", ") { it.cssClassName }} {
                ${declarations.entries.joinToString(separator = "\n") { "${it.key} : ${it.value} ;" }}
            }
         """.trimIndent()
        }
    }

    val languageCssClassStyle = CssClassStyle(EditorStyleIdentity(".$_stylePrefix"))

    private var _mappedCss = ""

    fun stylesToCss(): String {
        return _mappedCss
    }

    override fun createEditorStyleType(identity: EditorStyleIdentity): CssClassStyle = CssClassStyle(identity)

    override fun updateStyleRule(sr: AglStyleRule): List<CssClassStyle>  {
        val editorStyles = super.updateStyleRule(sr)
        val cssClasses = listOf(languageCssClassStyle) + editorStyles
        val declarations = LinkedHashMap(sr.declaration.values.associate { oldStyle ->
            when (oldStyle.name) {
                "foreground" -> Pair("color", oldStyle.value)
                "background" -> Pair("background-color", oldStyle.value)
                "font-style" -> when (oldStyle.value) {
                    "bold" -> Pair("font-weight", oldStyle.value)
                    "italic" -> Pair("font-style", oldStyle.value)
                    else -> Pair(oldStyle.name, oldStyle.value)
                }

                else -> Pair(oldStyle.name, oldStyle.value)
            }
        })
        _mappedCss = _mappedCss + "\n" + AglStyleHandlerCssClass.toCss(cssClasses, declarations)
        return editorStyles
    }

}