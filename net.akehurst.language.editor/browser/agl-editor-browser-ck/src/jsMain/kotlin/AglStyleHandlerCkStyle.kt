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

package net.akehurst.language.editor.browser.ck

import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.EditorStyle
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.editor.common.AglStyleHandlerAbstract
import net.akehurst.language.style.api.*
import kotlin.collections.List
import kotlin.collections.associate
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf

data class CkStyle(
    override val identity: EditorStyleIdentity
) : EditorStyle {
    val attribs = mutableMapOf<String,Any>()
}

class AglStyleHandlerCkStyle(
    languageId: LanguageIdentity,
) : AglStyleHandlerAbstract<CkStyle>(languageId, ) {

    override val EDITOR_NO_STYLE: CkStyle = CkStyle(EditorStyleIdentity.NO_STYLE)

    private val _edStyleNamePrefix: String = "aglCkStyle-"

    override fun createEditorStyleType(identity: EditorStyleIdentity) = CkStyle(identity)

    override fun updateEditorStyles(editorStyles: List<CkStyle>, sr: AglStyleRule) {
        val attribs = sr.declaration.values.associate { oldStyle ->
            when (oldStyle.name) {
                "foreground" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_FONT_FORE_COLOUR, oldStyle.value)
                "background" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_FONT_BACK_COLOUR, oldStyle.value)
                "text-decoration" -> when (oldStyle.value) {
                    "underline" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_UNDERLINE, true)
                    else -> Pair(oldStyle.name, oldStyle.value)
                }

                "font-style" -> when (oldStyle.value) {
                    "normal" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_ITALIC, false)
                    "italic" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_ITALIC, true)
                    else -> Pair(oldStyle.name, oldStyle.value)
                }
                "font-weight" -> when (oldStyle.value) {
                    "bold" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_BOLD, true)
                    "normal" -> Pair(CkEditorHelper.ATTRIBUTE_NAME_STYLE_BOLD, false)
                    else -> Pair(oldStyle.name, oldStyle.value)
                }
                else -> Pair(oldStyle.name, oldStyle.value)
            }
        }
        editorStyles.forEach {
            it.attribs.putAll(attribs)
        }
    }

}