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

import net.akehurst.language.agl.language.style.asm.AglStyleModelDefault
import net.akehurst.language.api.sppt.LeafData
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.Sentence
import net.akehurst.language.api.style.AglStyleModel

class AglStyleHandler(
    languageId: String,
    val cssClassPrefixStart: String = "agl"
) {

    companion object {
        const val EDITOR_NO_STYLE = "nostyle"

        fun languageIdToStyleClass(cssClassPrefixStart: String, languageId: String): String {
            val cssLangId = languageId.replace(Regex("[^a-z0-9A-Z_-]"), "_")
            return "${cssClassPrefixStart}_${cssLangId}"
        }
    }

    // AglStyleHandler is recreated if languageId changes for the editor
    val cssLanguageId = languageId.replace(Regex("[^a-z0-9A-Z_-]"), "_")
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

    fun transformToTokens(leafs: List<LeafData>): List<AglToken> {
        return leafs.map { leaf ->
            val cssClasses = this.mapToCssClasses(leaf)
            AglToken(
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

    fun updateStyleMap(aglSelectors: List<String>) {
        aglSelectors.forEach { mapClass(it) }
    }

    fun mapClass(aglSelector: String): String {
        var cssClass = this.tokenToClassMap[aglSelector]
        if (null == cssClass) {
            cssClass = this.cssClassPrefix + this.nextCssClassNum++
            this.tokenToClassMap[aglSelector] = cssClass
        }
        return cssClass
    }
}