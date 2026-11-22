/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.sppt.api.LeafData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The semantics of the AGL style DSL (net.akehurst.language 5_Documentation/dsl/style.md) as applied by the editor
 */
class test_AglStyleHandler_semantics {

    private companion object {
        val langId = LanguageIdentity("test")

        fun handler(styleStr: String): AglStyleHandlerAsHtml {
            val styleModel = Agl.registry.agl.style.processor!!.process(styleStr).let {
                assertTrue(it.allIssues.errors.isEmpty(), it.allIssues.toString())
                it.asm!!
            }
            return AglStyleHandlerAsHtml(langId).also { it.updateStyleModel(styleModel) }
        }

        fun literal(text: String, position: Int) = LeafData("'$text'", false, position, text.length, listOf("S", "'$text'"))
        fun leaf(name: String, position: Int, length: Int) = LeafData(name, true, position, length, listOf("S", name))

        fun AglStyleHandlerAsHtml.html(text: String, leaves: List<LeafData>) =
            this.applyHtmlStyling(SentenceDefault(text, 0), this.transformToTokens(leaves), emptyList())

        fun ids(vararg n: Int) = n.map { EditorStyleIdentity("agl-test$it") }
    }

    @Test
    fun selectors_literal_pattern_ruleName() {
        val sut = handler(
            """
            namespace test
            styles Test {
                'part' { foreground: blue; }
                "[0-9]+" { foreground: green; }
                NAME, NUMBER { foreground: red; }
            }
            """.trimIndent()
        )
        assertEquals(ids(1), sut.mapToEditorStyle(literal("part", 0)).map { it.identity })
        assertEquals(ids(2), sut.mapToEditorStyle(leaf("\"[0-9]+\"", 0, 1)).map { it.identity })
        assertEquals(ids(3), sut.mapToEditorStyle(leaf("NAME", 0, 1)).map { it.identity })
        assertEquals(ids(3), sut.mapToEditorStyle(leaf("NUMBER", 0, 1)).map { it.identity })
        assertEquals(listOf(EditorStyleIdentity.NO_STYLE), sut.mapToEditorStyle(literal("other", 0)).map { it.identity })
    }

    @Test
    fun nostyle_is_the_base_of_every_token() {
        val sut = handler(
            """
            namespace test
            styles Test {
                'part' { foreground: blue; }
                ${'$'}nostyle { foreground: black; font-style: italic; }
            }
            """.trimIndent()
        )
        // $nostyle first, even though it is written after 'part'
        assertEquals(ids(2, 1), sut.mapToEditorStyle(literal("part", 0)).map { it.identity })
        assertEquals(ids(2), sut.mapToEditorStyle(literal("other", 0)).map { it.identity })
        assertEquals(
            "<span style='color:blue;'><i>part</i></span><span style='color:black;'><i> </i></span>",
            sut.html("part ", listOf(literal("part", 0), LeafData("WS", true, 4, 1, listOf("WS"))))
        )
    }

    @Test
    fun keyword_is_every_word_like_literal() {
        val sut = handler(
            """
            namespace test
            styles Test {
                ${'$'}keyword { font-weight: bold; }
            }
            """.trimIndent()
        )
        assertEquals(ids(1), sut.mapToEditorStyle(literal("part", 0)).map { it.identity })
        assertEquals(ids(1), sut.mapToEditorStyle(literal("part-def", 0)).map { it.identity })
        assertEquals(listOf(EditorStyleIdentity.NO_STYLE), sut.mapToEditorStyle(literal("{", 0)).map { it.identity })
        assertEquals(listOf(EditorStyleIdentity.NO_STYLE), sut.mapToEditorStyle(literal(",", 0)).map { it.identity })
        assertEquals(listOf(EditorStyleIdentity.NO_STYLE), sut.mapToEditorStyle(leaf("NAME", 0, 4)).map { it.identity })
    }

    @Test
    fun meta_rule_matches_the_terminal_as_written_in_the_grammar() {
        val sut = handler(
            """
            namespace test
            styles Test {
                ${'$'}${'$'} "'[^a-zA-Z]+'" { foreground: gray; }
            }
            """.trimIndent()
        )
        assertEquals(ids(1), sut.mapToEditorStyle(literal("{", 0)).map { it.identity })
        assertEquals(ids(1), sut.mapToEditorStyle(literal(",", 0)).map { it.identity })
        assertEquals(listOf(EditorStyleIdentity.NO_STYLE), sut.mapToEditorStyle(literal("part", 0)).map { it.identity })
    }

    @Test
    fun rules_combine_like_css_in_the_order_written() {
        val sut = handler(
            """
            namespace test
            styles Test {
                ${'$'}keyword { font-weight: bold; foreground: blue; }
                'circle' { foreground: red; }
            }
            """.trimIndent()
        )
        assertEquals(ids(1, 2), sut.mapToEditorStyle(literal("circle", 0)).map { it.identity })
        assertEquals("<span style='color:red;'><b>circle</b></span>", sut.html("circle", listOf(literal("circle", 0))))
        assertEquals("<span style='color:blue;'><b>square</b></span>", sut.html("square", listOf(literal("square", 0))))
    }

    @Test
    fun later_rule_wins_also_when_the_earlier_rule_is_after_it_in_selector_order() {
        val sut = handler(
            """
            namespace test
            styles Test {
                'circle' { foreground: red; }
                ${'$'}keyword { foreground: blue; }
            }
            """.trimIndent()
        )
        assertEquals("<span style='color:blue;'>circle</span>", sut.html("circle", listOf(literal("circle", 0))))
    }

    @Test
    fun rule_order_with_more_than_nine_rules() {
        // the order is the rule order, not the alphabetical order of the style names (agl-test10 < agl-test2)
        val rules = (1..10).joinToString("\n") { "'r$it' { foreground: red; }" }
        val sut = handler(
            """
            namespace test
            styles Test {
                'x' { foreground: green; }
                $rules
                'x' { foreground: blue; }
            }
            """.trimIndent()
        )
        assertEquals(ids(1, 12), sut.mapToEditorStyle(literal("x", 0)).map { it.identity })
        assertEquals("<span style='color:blue;'>x</span>", sut.html("x", listOf(literal("x", 0))))
    }

    @Test
    fun extends_rules_of_the_extended_style_set_come_first() {
        val sut = handler(
            """
            namespace test
            styles A {
                'part' { foreground: red; font-weight: bold; }
            }
            styles B : A {
                'part' { foreground: green; }
            }
            """.trimIndent()
        )
        // A's rule is applied once, before B's
        assertEquals(ids(1, 2), sut.mapToEditorStyle(literal("part", 0)).map { it.identity })
        assertEquals("<span style='color:green;'><b>part</b></span>", sut.html("part", listOf(literal("part", 0))))
    }

}
