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

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.editor.api.AglEditorLogger
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

        val langDef = Agl.registry.register(
            identity = testLangId,
            grammarStr = grammarStr,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationDefault()
        )

        fun test(lineText: String, row: Int, state: AglLineState, expected: AglLineState) {
            val agl = AglComponents<Any, Any>(testLangId, testEditorId, logger)
            val sut = AglTokenizer<Any, Any>(agl)

            val actual = sut.getLineTokensByScan(lineText, state, row)

            assertEquals(expected.lineNumber, actual.lineNumber)
            assertEquals(expected.nextLineStartPosition, actual.nextLineStartPosition)
            assertEquals(expected.leftOverText, actual.leftOverText)
            assertEquals(expected.tokens, actual.tokens)
        }
    }

    @Test
    fun getLineTokensByScan_empty_0_0() {
        val lineText = ""
        val row = 0
        val sp = 0
        val state = AglLineState(row, sp, "", emptyList())

        val expected = AglLineState(0, 1, "", emptyList())

        test(lineText, row, state, expected)
    }

    @Test
    fun getLineTokensByScan_oneline_0_0() {
        val lineText = "abc def ghi"
        val row = 0
        val sp = 0
        val state = AglLineState(row, sp, "", emptyList())

        val expected = AglLineState(0, lineText.length+1, "", listOf(
            AglToken(listOf("agl_testLangId-1"),0,3),
            AglToken(listOf("agl_testLangId-2"),3,1),
            AglToken(listOf("agl_testLangId-1"),4,3),
            AglToken(listOf("agl_testLangId-2"),7,1),
            AglToken(listOf("agl_testLangId-1"),8,3),
        ))

        test(lineText, row, state, expected)
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
        val row = 0
        val sp = 0
        val state = AglLineState(row, sp, "", emptyList())

        val expected = AglLineState(0, split[0].length+1, "", listOf(
            AglToken(listOf("agl_testLangId-1"),0,3),
            AglToken(listOf("agl_testLangId-2"),3,1),
            AglToken(listOf("agl_testLangId-1"),4,3),
            AglToken(listOf("agl_testLangId-2"),7,2),
            AglToken(listOf("agl_testLangId-1"),9,3),
        ))

        test(lineText, row, state, expected)
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
        val state = AglLineState(row, split[0].length+1, "", emptyList())

        val expected = AglLineState(1, state.nextLineStartPosition+split[1].length+1, "", listOf(
            AglToken(listOf("agl_testLangId-1"),state.nextLineStartPosition+0,2),
            AglToken(listOf("agl_testLangId-2"),state.nextLineStartPosition+2,2),
            AglToken(listOf("agl_testLangId-1"),state.nextLineStartPosition+4,3),
            AglToken(listOf("agl_testLangId-2"),state.nextLineStartPosition+7,1),
            AglToken(listOf("agl_testLangId-1"),state.nextLineStartPosition+8,4),
        ))

        test(lineText, row, state, expected)
    }
}