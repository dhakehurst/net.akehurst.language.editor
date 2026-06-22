/**
 * Copyright (C) 2025 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.EditorStyle
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.editor.common.AglStyleHandlerAbstract
import net.akehurst.language.style.api.AglStyleRule

data class ComposeStyle(
    override val identity: EditorStyleIdentity
) : EditorStyle {
    var composeStyle = SpanStyle()
}

class AglStyleHandlerComposeStyle(
    languageId: LanguageIdentity,
) : AglStyleHandlerAbstract<ComposeStyle>(languageId) {

    override val EDITOR_NO_STYLE: ComposeStyle = ComposeStyle(EditorStyleIdentity.NO_STYLE)

    override fun createEditorStyleType(identity: EditorStyleIdentity) = ComposeStyle(identity)

    override fun updateEditorStyles(editorStyles: List<ComposeStyle>, sr: AglStyleRule) {
        var foreground = Color.Black
        var background = Color.Transparent
        var fontWeight = FontWeight.Normal
        var fontStyle = FontStyle.Normal
        var textDecoration = TextDecoration.None
        sr.declaration.values.forEach { oldStyle ->
            when (oldStyle.name) {
                "foreground" -> foreground = oldStyle.value.toComposeColor()
                "background" -> background = oldStyle.value.toComposeColor()
                "text-decoration" -> when (oldStyle.value) {
                    "underline" -> textDecoration = TextDecoration.Underline
                    else -> Unit
                }

                "font-style" -> when (oldStyle.value) {
                    "normal" -> fontStyle = FontStyle.Normal
                    "italic" -> fontStyle = FontStyle.Italic
                    else -> Unit
                }

                "font-weight" -> when (oldStyle.value) {
                    "bold" -> fontWeight = FontWeight.Bold
                    "normal" -> fontWeight = FontWeight.Normal
                    else -> Unit
                }

                else -> Unit
            }
        }
        editorStyles.forEach {
            it.composeStyle = SpanStyle(
                color = foreground,
                background = background,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                textDecoration = textDecoration,
            )
        }
    }

    fun String.toComposeColor(alpha: Int = 0xFF, defaultColor: Color = Color.Black): Color {
        return when {
            this.startsWith("#") -> {
                val digits = this.removePrefix("#")
                val num = when (digits.length) {
                    8 -> digits.toLong(16)
                    else -> digits.toLong(16) or 0x00000000FF000000
                }
                Color(num)
            }

            else -> {
                val lower = this.lowercase()
                CssColours.NAME_to_HEX[lower]?.let {
                    val num = it.toLong()
                    Color(num)
                } ?: defaultColor
            }
        }
    }
}
