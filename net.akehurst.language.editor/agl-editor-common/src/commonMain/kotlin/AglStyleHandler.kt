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
import net.akehurst.language.sppt.api.LeafData
import net.akehurst.language.style.api.AglStyleModel
import net.akehurst.language.style.api.AglStyleRule
import net.akehurst.language.style.asm.AglStyleModelDefault

open class AglStyleHandler(
    languageId: LanguageIdentity,
    val cssClassPrefixStart: String = "agl"
) {

    companion object {
        const val EDITOR_NO_STYLE = "nostyle"
        fun languageIdToStyleClass(cssClassPrefixStart: String, languageId: LanguageIdentity): String {
            val cssLangId = languageId.value.replace(Regex("[^a-z0-9A-Z_-]"), "_")
            return "${cssClassPrefixStart}_${cssLangId}"
        }
    }

    private var _styleModel: AglStyleModel = AglStyleModelDefault(SimpleName( languageId.last),emptyList())

    val styleModel get() = _styleModel

    private var _editorStyles = mutableMapOf<String, Any>()

    // AglStyleHandler is recreated if languageId changes for the editor
    //val cssLanguageId = languageId.value.replace(Regex("[^a-z0-9A-Z_-]"), "_")
    val aglStyleClass = languageIdToStyleClass(cssClassPrefixStart, languageId)

    private var nextCssClassNum = 1
    private val cssClassPrefix: String = "${aglStyleClass}-"
    private val tokenToClassMap = mutableMapOf<String, String>(
        AglStyleModelDefault.NO_STYLE_ID to EDITOR_NO_STYLE
    )

    private fun mapTokenTypeToClass(tokenType: String): String? {
        val cssClass = this.tokenToClassMap.get(tokenType)
        return cssClass
    }

    private fun mapToCssClasses(leaf: LeafData): List<String> {
        val metaTagClasses = leaf.metaTags.mapNotNull { this.mapTokenTypeToClass(it) }
        val otherClasses = if (!leaf.tagList.isEmpty()) {
            leaf.tagList.mapNotNull { this.mapTokenTypeToClass(it) }
        } else {
            listOf(this.mapTokenTypeToClass(leaf.name)).mapNotNull { it }
        }
        val classes = metaTagClasses + otherClasses
        return if (classes.isEmpty()) {
            listOf(EDITOR_NO_STYLE)
        } else {
            classes.toSet().toList()
        }
    }

    fun transformToTokens(leafs: List<LeafData>): List<AglTokenDefault> {
        return leafs.map { leaf ->
            val cssClasses = this.mapToCssClasses(leaf)
            AglTokenDefault(
                cssClasses.toSet().toList(),
                leaf.position,
                leaf.length
            )
        }
    }

    fun reset() {
        this.tokenToClassMap.clear()
        nextCssClassNum = 1
        this.tokenToClassMap[AglStyleModelDefault.NO_STYLE_ID] = EDITOR_NO_STYLE
    }

    fun updateStyleModel(styleModel: AglStyleModel) {
        _styleModel = styleModel // TODO: should not need to store this , need to modify 'updateEditorStyles' in editor specific code!
        styleModel.allDefinitions.forEach { ss ->
            ss.rules.forEach { sr ->
                val edStyle = convert<Any>(sr)
                sr.selector.forEach { sel ->
                    val sn = mapClass(sel.value)
                    _editorStyles[sn] = edStyle
                }
            }
        }
    }

    fun mapClass(aglSelector: String): String {
        var cssClass = this.tokenToClassMap[aglSelector]
        if (null == cssClass) {
            cssClass = this.cssClassPrefix + this.nextCssClassNum++
            this.tokenToClassMap[aglSelector] = cssClass
        }
        return cssClass
    }

    open fun <EditorStyleType : Any> convert(rule: AglStyleRule): EditorStyleType {
        return rule as EditorStyleType
    }

    fun <EditorStyleType : Any> editorStyleFor(styleName: String): EditorStyleType? {
        return _editorStyles[styleName] as EditorStyleType?
    }
}