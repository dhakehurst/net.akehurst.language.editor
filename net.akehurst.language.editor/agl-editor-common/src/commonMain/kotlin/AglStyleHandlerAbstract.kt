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
import net.akehurst.language.style.asm.AglStyleDomainDefault

/**
 * Applies an AGL style model to tokens, with the semantics of the AGL style DSL
 * (net.akehurst.language 5_Documentation/dsl/style.md):
 *
 *  - selectors 'literal', "pattern" (as written in the grammar) and RULE_NAME match a token by its terminal or rule name,
 *  - `$nostyle` is the default style, the base of every token,
 *  - `$keyword` is every word-like literal, the same as the meta rule `$$ "'[a-zA-Z_][a-zA-Z0-9_-]*'"`,
 *  - a meta rule `$$ "regex"` matches every terminal whose text as written in the grammar (quotes included) matches the regex,
 *  - when several rules match a token they combine like CSS: start from `$nostyle`, then apply each matching rule
 *    in the order written; a later rule overrides only the declarations it sets,
 *  - `styles B : A` extends A, A's rules come first.
 *
 * Each rule gives one editor style, numbered in rule order; a token gets the editor styles of the rules
 * that apply to it, in the order they are to be applied (the editor merges them in that order).
 */
abstract class AglStyleHandlerAbstract<EditorStyleType : EditorStyle>(
    languageId: LanguageIdentity
) : AglStyleHandler<EditorStyleType> {

    companion object {
        const val AGL_STYLE_PREFIX = "agl"

        /** the regex of the `$keyword` selector, matched against the terminal's text as written in the grammar */
        val KEYWORD_REGEX = Regex("'[a-zA-Z_][a-zA-Z0-9_-]*'")
    }

    abstract val EDITOR_NO_STYLE: EditorStyleType

    override val styleModel get() = _styleModel

    protected val _stylePrefix = "${AGL_STYLE_PREFIX}-${languageId.value}"
    private var _styleModel: AglStyleDomain = AglStyleDomainDefault(SimpleName(languageId.last))
    private var _editorStyles = mutableMapOf<EditorStyleIdentity, EditorStyleType>()

    // rule order of each editor style
    private val _ruleOrder = mutableMapOf<EditorStyleType, Int>()

    // editor styles of the $nostyle rules, in rule order
    private val _baseStyles = mutableListOf<EditorStyleType>()

    // selector value ('literal', "pattern", RULE_NAME) -> editor styles of the rules with that selector
    private val _selectorToEditorStyles = mutableMapOf<String, MutableList<EditorStyleType>>()

    // meta rules (and $keyword): regex -> editor style
    private val _metaStyles = mutableListOf<Pair<Regex, EditorStyleType>>()
    private var _nextEditorStyleNum = 1

    abstract fun createEditorStyleType(identity: EditorStyleIdentity): EditorStyleType

    open fun updateEditorStyles(editorStyles: List<EditorStyleType>, sr: AglStyleRule) {}

    open fun updateStyleRule(sr: AglStyleRule): List<EditorStyleType> {
        // one editor style per rule, numbered in rule order
        val editorStyle = createEditorStyleType(EditorStyleIdentity(_stylePrefix + this._nextEditorStyleNum))
        _ruleOrder[editorStyle] = this._nextEditorStyleNum
        this._nextEditorStyleNum++
        _editorStyles[editorStyle.identity] = editorStyle
        when (sr) {
            is AglStyleTagRule -> sr.selector.forEach { sel -> addSelector(sel, editorStyle) }
            is AglStyleMetaRule -> _metaStyles.add(Pair(Regex(sr.pattern.value), editorStyle))
            else -> error("Unsupported")
        }
        val editorStyles = listOf(editorStyle)
        updateEditorStyles(editorStyles, sr)
        return editorStyles
    }

    private fun addSelector(sel: AglStyleSelector, editorStyle: EditorStyleType) {
        when {
            sel.value == AglStyleDomainDefault.NO_STYLE_ID.value -> _baseStyles.add(editorStyle)
            sel.value == AglStyleDomainDefault.KEYWORD_STYLE_ID.value -> _metaStyles.add(Pair(KEYWORD_REGEX, editorStyle))
            sel.kind == AglStyleSelectorKind.SPECIAL -> Unit // unknown special selector, styles nothing
            else -> _selectorToEditorStyles.getOrPut(sel.value) { mutableListOf() }.add(editorStyle)
        }
    }

    // --- AglStyleHandler ---

    override fun reset() {
        _editorStyles.clear()
        _ruleOrder.clear()
        _baseStyles.clear()
        _selectorToEditorStyles.clear()
        _metaStyles.clear()
        _nextEditorStyleNum = 1
    }

    override fun updateStyleModel(styleModel: AglStyleDomain) {
        _styleModel = styleModel // TODO: should not need to store this , need to modify 'updateEditorStyles' in editor specific code!
        // allRules gives the rules of extended style sets first,
        // a rule that is in more than one style set (via extends) is applied once, at its first position
        val done = mutableSetOf<AglStyleRule>()
        styleModel.allDefinitions.forEach { ss ->
            ss.allRules.forEach { sr ->
                if (done.none { it === sr }) {
                    done.add(sr)
                    updateStyleRule(sr)
                }
            }
        }
    }

    override fun editorStyleFor(identity: EditorStyleIdentity): EditorStyleType? =
        _editorStyles[identity] ?: if (identity == EDITOR_NO_STYLE.identity) EDITOR_NO_STYLE else null

    /**
     * the editor style of the first rule with the given selector, or a new (unstyled) editor style
     */
    override fun convert(selector: AglStyleSelector): EditorStyleType = when {
        selector.value == AglStyleDomainDefault.NO_STYLE_ID.value -> _baseStyles.firstOrNull() ?: EDITOR_NO_STYLE
        else -> _selectorToEditorStyles[selector.value]?.firstOrNull() ?: let {
            val editorStyle = createEditorStyleType(EditorStyleIdentity(_stylePrefix + this._nextEditorStyleNum))
            _ruleOrder[editorStyle] = this._nextEditorStyleNum
            this._nextEditorStyleNum++
            _editorStyles[editorStyle.identity] = editorStyle
            _selectorToEditorStyles.getOrPut(selector.value) { mutableListOf() }.add(editorStyle)
            editorStyle
        }
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

    /**
     * the editor styles to apply to the leaf, in the order to apply them:
     * the $nostyle rules, then each matching rule in the order written
     */
    fun mapToEditorStyle(leaf: LeafData): List<EditorStyleType> {
        // a leaf from the parse tree has the names of its enclosing rules and its own name as tags,
        // a leaf from the scanner has only its name
        val names = leaf.tagList.ifEmpty { listOf(leaf.name) }
        val bySelector = names.flatMap { this._selectorToEditorStyles[it] ?: emptyList() }
        // meta rules match the terminal's text as written in the grammar, which is the leaf's name
        val byMeta = this._metaStyles.mapNotNull { (regex, style) -> if (regex.matches(leaf.name)) style else null }
        val matched = (bySelector + byMeta).toSet().sortedBy { _ruleOrder[it] ?: Int.MAX_VALUE }
        val classes = (_baseStyles + matched).distinct()
        return classes.ifEmpty { listOf(EDITOR_NO_STYLE) }
    }

}
