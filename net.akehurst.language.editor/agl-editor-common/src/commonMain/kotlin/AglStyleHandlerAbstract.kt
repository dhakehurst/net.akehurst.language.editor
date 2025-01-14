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
import net.akehurst.language.base.api.SimpleName
import net.akehurst.language.editor.api.AglStyleHandler
import net.akehurst.language.editor.api.EditorStyle
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.sppt.api.LeafData
import net.akehurst.language.style.api.*
import net.akehurst.language.style.asm.AglStyleModelDefault

abstract class AglStyleHandlerAbstract<EditorStyleType : EditorStyle>(
    languageId: LanguageIdentity,
    val EDITOR_NO_STYLE: EditorStyleType
) : AglStyleHandler<EditorStyleType> {

    companion object {
        const val AGL_STYLE_PREFIX = "agl"
    }

    override val styleModel get() = _styleModel

    protected val _stylePrefix = "${AGL_STYLE_PREFIX}-${languageId.value}"
    private var _styleModel: AglStyleModel = AglStyleModelDefault(SimpleName(languageId.last))
    private var _editorStyles = mutableMapOf<EditorStyleIdentity, EditorStyleType>()
    private var _metaStyles = mutableMapOf<EditorStyleType, AglStyleMetaRule>()
    private val _selectorToEditorStyle = mutableMapOf<String, EditorStyleType>(AglStyleModelDefault.NO_STYLE_ID.value to EDITOR_NO_STYLE)
    private var _nextEditorStyleNum = 1

    abstract fun createEditorStyleType(identity: EditorStyleIdentity):EditorStyleType

    open fun updateEditorStyles(editorStyles: List<EditorStyleType>, sr: AglStyleRule) {}

    open fun updateStyleRule(sr:AglStyleRule): List<EditorStyleType> {
        val editorStyles = when (sr) {
            is AglStyleTagRule -> {
                sr.selector.map { sel ->
                    val sn = convert(sel)
                    _editorStyles[sn.identity] = sn
                    sn
                }
            }

            is AglStyleMetaRule -> {
                val n = AglStyleSelector("\$\$" + sr.pattern.pattern, AglStyleSelectorKind.SPECIAL)
                val sn = convert(n)
                _metaStyles[sn] = sr
                _editorStyles[sn.identity] = sn
                listOf(sn)
            }

            else -> error("Unsupported")
        }
        updateEditorStyles(editorStyles, sr)
        return editorStyles
    }

    // --- AglStyleHandler ---

    override fun reset() {
        _editorStyles.clear()
        _metaStyles.clear()
        _nextEditorStyleNum = 1
        this._selectorToEditorStyle.clear()
        this._selectorToEditorStyle[AglStyleModelDefault.NO_STYLE_ID.value] = EDITOR_NO_STYLE
    }

    override fun updateStyleModel(styleModel: AglStyleModel) {
        _styleModel = styleModel // TODO: should not need to store this , need to modify 'updateEditorStyles' in editor specific code!
        styleModel.allDefinitions.forEach { ss ->
            ss.rules.forEach { sr ->
                updateStyleRule(sr)
            }
        }
    }

    override fun editorStyleFor(identity: EditorStyleIdentity): EditorStyleType? =
        _editorStyles[identity]

    override fun convert(selector: AglStyleSelector): EditorStyleType {
        var editorStyle = this._selectorToEditorStyle[selector.value]
        if (null == editorStyle) {
            // the number help preserve the precedence ordering of the styles
            editorStyle = createEditorStyleType(EditorStyleIdentity(_stylePrefix + this._nextEditorStyleNum++))
            this._selectorToEditorStyle[selector.value] = editorStyle
        }
        return editorStyle
    }

    override fun transformToTokens(leafs: List<LeafData>): List<AglTokenDefault> {
        return leafs.map { leaf ->
            val edStyles = this.mapToEditorStyle(leaf)
            AglTokenDefault(
                edStyles.map { it.identity },
                leaf.position,
                leaf.length
            )
        }
    }

    private fun mapToEditorStyle(leaf: LeafData): List<EditorStyleType> {
        //val metaTagClasses = leaf.metaTags.mapNotNull { this.mapTokenTypeToClass(it) }
        val metaTagClasses = this._metaStyles.mapNotNull { (k, v) ->
            when {
                v.pattern.matches(leaf.name) -> k
                leaf.tagList.any { v.pattern.matches(it) } -> k
                else -> null
            }
        }

        val kind = when {
            leaf.isPattern -> AglStyleSelectorKind.PATTERN
            else -> AglStyleSelectorKind.LITERAL
        }
        val otherClasses = if (leaf.tagList.isNotEmpty()) {
            leaf.tagList.mapNotNull {
                //val sel = AglStyleSelector(it, kind)
                this._selectorToEditorStyle[it]
            }
        } else {
            listOf(this._selectorToEditorStyle[AglStyleSelector(leaf.name, AglStyleSelectorKind.RULE_NAME).value]).filterNotNull()
        }
        val classes = metaTagClasses + otherClasses
        return if (classes.isEmpty()) {
            listOf(EDITOR_NO_STYLE)
        } else {
            // The order of the styles matters, last style should take precedence
            // the agl-style names are mapped to a css compatible name starting with a prefix + a number
            // the numbers provide a way to order the styles, due to the way they are created.
            // (also need to remove duplicates)
            classes.toSet().sortedBy { it.identity.value }
        }
    }

}