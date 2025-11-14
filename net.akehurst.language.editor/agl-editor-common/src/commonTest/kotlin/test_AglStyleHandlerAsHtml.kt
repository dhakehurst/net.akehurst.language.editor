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

package net.akehurst.language.editor.common

import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ResolvedReference
import net.akehurst.language.editor.api.EditorStyleIdentity
import net.akehurst.language.sentence.api.InputLocation
import net.akehurst.language.sentence.common.SentenceDefault
import net.akehurst.language.style.builder.styleDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglStyleHandlerAsHtml {

    @Test
    fun decodeFromHtml1() {
        val encodedHtml = "&lt; Max Valve Position AND Thermostat Head's Valve Position &amp;gt;"
        val actual = AglStyleHandlerAsHtml.decodeFromHtml(encodedHtml)

        val expected = "< Max Valve Position AND Thermostat Head's Valve Position >"

        assertEquals(expected, actual)
    }

    @Test
    fun decodeFromHtml2() {
        val encodedHtml = """
            The External System definition Radiator accepts the transition
            from state Completely Closed Valve to state Partially Open Valve
            when the event Thermostat Head's Valve Position &lt; Max Valve
            Position AND Thermostat Head's Valve Position &amp;gt; Min Valve
            Position becomes true occurs.
        """.trimIndent()
        val actual = AglStyleHandlerAsHtml.decodeFromHtml(encodedHtml)

        val expected = """
            The External System definition Radiator accepts the transition
            from state Completely Closed Valve to state Partially Open Valve
            when the event Thermostat Head's Valve Position < Max Valve
            Position AND Thermostat Head's Valve Position > Min Valve
            Position becomes true occurs.
        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun encodeForHtml2() {
        val text = """
            The External System definition Radiator accepts the transition
            from state Completely Closed Valve to state Partially Open Valve
            when the event Thermostat Head's Valve Position < Max Valve
            Position AND Thermostat Head's Valve Position > Min Valve
            Position becomes true occurs.
        """.trimIndent()
        val actual = AglStyleHandlerAsHtml.encodeForHtml(text)
        val expected = """
            The External System definition Radiator accepts the transition
            from state Completely Closed Valve to state Partially Open Valve
            when the event Thermostat Head&apos;s Valve Position &lt; Max Valve
            Position AND Thermostat Head&apos;s Valve Position &gt; Min Valve
            Position becomes true occurs.
        """.trimIndent()//.replace("\n","&#13;")

        assertEquals(expected, actual)
    }

    @Test
    fun applyHtmlStyling__aaaaa_as_1_token() {
        // given
        val sentence = SentenceDefault("aaaaa", 0)
        val tokens = listOf(
            AglTokenDefault(listOf(EditorStyleIdentity("agl-test1")), 0, 5)
        )
        val styleModel = styleDomain("Test") {
            namespace("test") {
                styles("Test") {
                    tagRule("aaaaa") {
                        declaration("foreground", "red")
                    }
                }
            }
        }

        // when
        val sut = AglStyleHandlerAsHtml(LanguageIdentity("test"))
        sut.updateStyleModel(styleModel)
        val actual = sut.applyHtmlStyling(sentence, tokens, emptyList())

        // then
        val expected = "<span style='color:red;'>aaaaa</span>"
        assertEquals(expected, actual)
    }

    @Test
    fun applyHtmlStyling__aaaaa_as_2_tokens() {
        // given
        val sentence = SentenceDefault("aaaaa", 0)
        val tokens = listOf(
            AglTokenDefault(listOf(EditorStyleIdentity("agl-test1")), 0, 3),
            AglTokenDefault(listOf(EditorStyleIdentity("agl-test1")), 3, 2)
        )
        val styleModel = styleDomain("Test") {
            namespace("test") {
                styles("Test") {
                    tagRule("aaaaa") {
                        declaration("foreground", "red")
                    }
                }
            }
        }

        // when
        val sut = AglStyleHandlerAsHtml(LanguageIdentity("test"))
        sut.updateStyleModel(styleModel)
        val actual = sut.applyHtmlStyling(sentence, tokens,emptyList())

        // then
        val expected = "<span style='color:red;'>aaaaa</span>"
        assertEquals(expected, actual)
    }

    @Test
    fun applyHtmlStyling__reference() {
        // given
        val sentence = SentenceDefault("""
            target
            source
            """.trimIndent(), 0)
        val references = listOf(
            ResolvedReference(
                source = null,
                sourceLocation = InputLocation(7,1,2,6,0),
                target = null,
                targetLocation = InputLocation(0,1,1,6,0)
            )
        )
        val tokens = listOf(
            AglTokenDefault(listOf(EditorStyleIdentity("agl-test1")), 0, 6),
            AglTokenDefault(listOf(EditorStyleIdentity.NO_STYLE), 6, 1),
            AglTokenDefault(listOf(EditorStyleIdentity("agl-test2")), 7, 6)
        )
        val styleModel = styleDomain("Test") {
            namespace("test") {
                styles("Test") {
                    tagRule("target") {
                        declaration("foreground", "red")
                    }
                    tagRule("source") {
                        declaration("foreground", "green")
                    }
                }
            }
        }

        // when
        val sut = AglStyleHandlerAsHtml(LanguageIdentity("test"))
        sut.updateStyleModel(styleModel)
        val actual = sut.applyHtmlStyling(sentence, tokens,references)

        // then
        val expected = """
            <span style='color:red;'>target</span><span style=''>
            </span><span style='color:green;'>source</span>
            """.trimIndent()
        assertEquals(expected, actual)
    }

}