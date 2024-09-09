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

import net.akehurst.language.agl.Agl
import net.akehurst.language.api.language.base.QualifiedName
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.AglToken
import kotlin.test.Test
import kotlin.test.assertEquals

internal class test_AglTokenizer {

    companion object {
        val logger = AglEditorLogger { l, m, t -> println("$l: $m") }
        const val testLangId = "testLangId"
        const val testEditorId = "testEditorId"

        val grammarStr = """
            namespace test
            grammar Test {
                skip leaf WS = "\s+" ;
                S = WORD* ;
                leaf WORD = "[a-z]+" ;  
            }
        """.trimIndent()
        val styleStr = """
            WORD { }
            WS { }
            S {}
        """.trimIndent()

        val styleMdl = Agl.registry.agl.style.processor!!.process(styleStr).asm!!

        val langDef = Agl.registry.register(
            identity = QualifiedName(testLangId),
            grammarStr = grammarStr,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationDefault()
        )

        fun test_getLineTokensByScan(lineText: String, previousLineState: AglLineState, expected: Pair<AglLineState, List<AglToken>>) {
            val agl = AglComponents<Any, Any>(QualifiedName(testLangId), testEditorId, logger)
            agl.styleHandler.updateStyleModel(styleMdl)
            agl.scannerMatchables = agl.languageDefinition.processor!!.scanner!!.matchables
            val sut = AglTokenizer<Any, Any>(agl)

            val actual = sut.getLineTokensByScan(lineText, previousLineState)

            assertEquals(expected.first.lineNumber, actual.first.lineNumber, "lineNumber")
            assertEquals(expected.first.nextLineStartPosition, actual.first.nextLineStartPosition, "nextLineStartPosition")
            assertEquals(expected.first.leftOverText, actual.first.leftOverText, "leftOverText")
            assertEquals(expected.second, actual.second, "tokens")
        }

        fun test_getLineTokensByParse(fullText: String, row: Int, state: AglLineState, expected: Pair<AglLineState, List<AglToken>>) {
            val agl = AglComponents<Any, Any>(QualifiedName(testLangId), testEditorId, logger)
            agl.styleHandler.updateStyleModel(styleMdl)
            val sut = AglTokenizer<Any, Any>(agl)
            //sut.acceptingTokens = true

            val result = agl.languageDefinition.processor!!.parse(fullText, Agl.parseOptions { })
            val tokens = result.sppt!!.tokensByLineAll().map { ln ->
                agl.styleHandler.transformToTokens(ln)
            }
            sut.receiveTokens(0, tokens)

            val lineText = fullText.split("\n")[row]
            val actual = sut.useCachedTokens(sut.tokensByLine[row]!!, lineText, state)

            assertEquals(expected.first.lineNumber, actual.first.lineNumber)
            assertEquals(expected.first.nextLineStartPosition, actual.first.nextLineStartPosition)
            assertEquals(expected.first.leftOverText, actual.first.leftOverText)
            assertEquals(expected.second, actual.second)
        }
    }

    @Test
    fun getLineTokensByScan_empty_0_0() {
        val lineText = ""
        val lineNumber = 0
        val sp = 0
        val prevState = AglLineState(lineNumber - 1, sp, "")

        val expected = Pair<AglLineState, List<AglToken>>(AglLineState(lineNumber, 1, ""), emptyList())

        test_getLineTokensByScan(lineText, prevState, expected)
    }

    @Test
    fun getLineTokensByScan_oneline_0_0() {
        val lineText = "abc def ghi"
        val lineNumber = 0
        val sp = 0
        val prevState = AglLineState(lineNumber - 1, sp, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(0, lineText.length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-1"), 0, 3),
                AglTokenDefault(listOf("agl_testLangId-2"), 3, 1),
                AglTokenDefault(listOf("agl_testLangId-1"), 4, 3),
                AglTokenDefault(listOf("agl_testLangId-2"), 7, 1),
                AglTokenDefault(listOf("agl_testLangId-1"), 8, 3),
            )
        )

        test_getLineTokensByScan(lineText, prevState, expected)
    }

    @Test
    fun getLineTokensByScan_3lines_0_0() {
        val totalText = """
            aaa bbb  ccc
            dd  eee ffff
            ghi
        """.trimIndent()
        val split = totalText.split("\n")
        val lineText = split[0]
        val lineNumber = 0
        val sp = 0
        val prevState = AglLineState(lineNumber - 1, sp, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(0, split[0].length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-1"), 0, 3),
                AglTokenDefault(listOf("agl_testLangId-2"), 3, 1),
                AglTokenDefault(listOf("agl_testLangId-1"), 4, 3),
                AglTokenDefault(listOf("agl_testLangId-2"), 7, 2),
                AglTokenDefault(listOf("agl_testLangId-1"), 9, 3),
            )
        )

        test_getLineTokensByScan(
            lineText, prevState, expected
        )
    }

    @Test
    fun getLineTokensByScan_3lines_1_0() {
        val totalText = """
            aaa bbb  ccc
            dd  eee ffff
            ghi
        """.trimIndent()
        val split = totalText.split("\n")
        val lineText = split[1]
        val row = 1
        val sp = 0
        val state = AglLineState(row, split[0].length + 1, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(1, state.nextLineStartPosition + split[1].length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-1"), state.nextLineStartPosition + 0, 2),
                AglTokenDefault(listOf("agl_testLangId-2"), state.nextLineStartPosition + 2, 2),
                AglTokenDefault(listOf("agl_testLangId-1"), state.nextLineStartPosition + 4, 3),
                AglTokenDefault(listOf("agl_testLangId-2"), state.nextLineStartPosition + 7, 1),
                AglTokenDefault(listOf("agl_testLangId-1"), state.nextLineStartPosition + 8, 4),
            )
        )

        test_getLineTokensByScan(lineText, state, expected)
    }

    @Test
    fun getLineTokensByScan_3lines_2_0() {
        val totalText = """
            aaa bbb  ccc
            dd  eee ffff
            ghi
        """.trimIndent()
        val split = totalText.split("\n")
        val lineText = split[2]
        val row = 2
        val sp = 0
        val state = AglLineState(row, split[0].length + 1 + split[1].length + 1, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(2, state.nextLineStartPosition + split[2].length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-1"), state.nextLineStartPosition + 0, 3),
            )
        )

        test_getLineTokensByScan(lineText, state, expected)
    }

    @Test
    fun getLineTokensByParse_empty_0_0() {
        val lineText = ""
        val row = 0
        val sp = 0
        val state = AglLineState(row, sp, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(0, 1, ""),
            emptyList()
        )

        test_getLineTokensByParse(lineText, row, state, expected)
    }

    @Test
    fun getLineTokensByParse_oneline_0_0() {
        val lineText = "abc def ghi"
        val row = 0
        val sp = 0
        val state = AglLineState(row, sp, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(0, lineText.length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), 0, 3),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), 3, 1),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), 4, 3),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), 7, 1),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), 8, 3),
            )
        )

        test_getLineTokensByParse(lineText, row, state, expected)
    }

    @Test
    fun getLineTokensByParse_3lines_0_0() {
        val totalText = """
            aaa bbb  ccc
            dd  eee ffff
            ghi
        """.trimIndent()
        val split = totalText.split("\n")
        val row = 0
        val sp = 0
        val state = AglLineState(row, sp, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(0, split[0].length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), 0, 3),  //aaa
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), 3, 1),  // .
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), 4, 3),  // bbb
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), 7, 2),  // ..
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), 9, 3),  // ccc
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), 12, 1), // EOL
            )
        )

        test_getLineTokensByParse(totalText, row, state, expected)
    }

    @Test
    fun getLineTokensByParse_3lines_1_0() {
        val totalText = """
            aaa bbb  ccc
            dd  eee ffff
            ghi
        """.trimIndent()
        val split = totalText.split("\n")
        val row = 1
        val sp = 0
        val state = AglLineState(row, split[0].length + 1, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(1, state.nextLineStartPosition + split[1].length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), state.nextLineStartPosition + 0, 2),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), state.nextLineStartPosition + 2, 2),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), state.nextLineStartPosition + 4, 3),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), state.nextLineStartPosition + 7, 1),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), state.nextLineStartPosition + 8, 4),
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-2"), state.nextLineStartPosition + 12, 1),
            )
        )

        test_getLineTokensByParse(totalText, row, state, expected)
    }

    @Test
    fun getLineTokensByParse_3lines_2_0() {
        val totalText = """
            aaa bbb  ccc
            dd  eee ffff
            ghi
        """.trimIndent()
        val split = totalText.split("\n")
        val row = 2
        val sp = 0
        val state = AglLineState(row, split[0].length + 1 + split[1].length + 1, "")

        val expected = Pair<AglLineState, List<AglToken>>(
            AglLineState(2, state.nextLineStartPosition + split[2].length + 1, ""),
            listOf(
                AglTokenDefault(listOf("agl_testLangId-3", "agl_testLangId-1"), state.nextLineStartPosition + 0, 3),
            )
        )

        test_getLineTokensByParse(totalText, row, state, expected)
    }
}