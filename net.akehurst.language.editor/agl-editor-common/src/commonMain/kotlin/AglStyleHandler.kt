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
import net.akehurst.language.style.api.*
import net.akehurst.language.style.asm.AglStyleModelDefault

open class AglStyleHandler(
    languageId: LanguageIdentity,
    val styleNamePrefixStart: String = STYLE_PREFIX
) {

    companion object {
        const val STYLE_PREFIX = "agl"
        const val EDITOR_NO_STYLE = "nostyle"
        fun languageIdToStyleClass(cssClassPrefixStart: String, languageId: LanguageIdentity): String {
            val cssLangId = languageId.value.replace(Regex("[^a-z0-9A-Z_-]"), "_")
            return "${cssClassPrefixStart}_${cssLangId}"
        }

       fun toCss(selectors:List<String>, declarations:LinkedHashMap<String,String>): String {
            return """
            ${selectors.joinToString(separator = ", ")} {
                ${declarations.entries.joinToString(separator = "\n") { "${it.key} : ${it.value} ;" }}
            }
         """.trimIndent()
        }
    }

    private var _styleModel: AglStyleModel = AglStyleModelDefault(SimpleName(languageId.last))

    val styleModel get() = _styleModel


    // AglStyleHandler is recreated if languageId changes for the editor
    //val cssLanguageId = languageId.value.replace(Regex("[^a-z0-9A-Z_-]"), "_")
    val aglStyleClass = languageIdToStyleClass(styleNamePrefixStart, languageId)

    private var _editorStyles = mutableMapOf<String, Any>()
    private var _metaStyles = mutableMapOf<String, AglStyleMetaRule>()
    private var nextCssClassNum = 1
    private var nextMetaNum = 1
    private val cssClassPrefix: String = "${aglStyleClass}-"
    private val selectorToCssClassMap = mutableMapOf<String, String>(AglStyleModelDefault.NO_STYLE_ID to EDITOR_NO_STYLE)

    private fun mapTokenTypeToClass(tokenType: String): String? {
        val cssClass = this.selectorToCssClassMap.get(tokenType)
        return cssClass
    }

    internal fun mapToCssClasses(leaf: LeafData): List<String> {
        //val metaTagClasses = leaf.metaTags.mapNotNull { this.mapTokenTypeToClass(it) }
        val metaTagClasses =this._metaStyles.mapNotNull { (k,v) ->
            when {
                v.pattern.matches(leaf.name) -> k
                leaf.tagList.any { v.pattern.matches(it) } -> k
                else -> null
            }
        }
        val otherClasses = if (leaf.tagList.isNotEmpty()) {
            leaf.tagList.mapNotNull { this.mapTokenTypeToClass(it) }
        } else {
            listOf(this.mapTokenTypeToClass(leaf.name)).mapNotNull { it }
        }
        val classes = metaTagClasses + otherClasses
        return if (classes.isEmpty()) {
            listOf(EDITOR_NO_STYLE)
        } else {
            // The order of the styles matters, last style should take precedence
            // the agl-style names are mapped to a css compatible name starting with a prefix + a number
            // the numbers provide a way to order the styles, due to the way they are created.
            // (also need to remove duplicates)
            classes.toSet().sortedBy { it }
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
        _editorStyles.clear()
        _metaStyles.clear()
        nextMetaNum = 1
        nextCssClassNum = 1
        this.selectorToCssClassMap.clear()
        this.selectorToCssClassMap[AglStyleModelDefault.NO_STYLE_ID] = EDITOR_NO_STYLE
    }

    fun updateStyleModel(styleModel: AglStyleModel) {
        _styleModel = styleModel // TODO: should not need to store this , need to modify 'updateEditorStyles' in editor specific code!
        styleModel.allDefinitions.forEach { ss ->
            ss.rules.forEach { sr ->

                when (sr) {
                    is AglStyleTagRule -> {
                        val edStyle = convert<Any>(sr)
                        sr.selector.forEach { sel ->
                            val sn = mapSelectorToCssClass(sel.value)
                            _editorStyles[sn] = edStyle
                        }
                    }

                    is AglStyleMetaRule -> {
                        val n = "\$\$"+sr.pattern.pattern
                        val sn = mapSelectorToCssClass(n)
                        _metaStyles[sn] = sr
                    }
                }
            }
        }
    }

    fun mapSelectorToCssClass(aglSelector: String): String {
        var cssClass = this.selectorToCssClassMap[aglSelector]
        if (null == cssClass) {
            // the number help preserve the precedence ordering of the styles
            cssClass = this.cssClassPrefix + this.nextCssClassNum++
            this.selectorToCssClassMap[aglSelector] = cssClass
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