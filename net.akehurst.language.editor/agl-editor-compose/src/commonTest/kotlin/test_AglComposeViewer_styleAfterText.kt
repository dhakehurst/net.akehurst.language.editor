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

package net.akehurst.language.editor.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.test.runTest
import net.akehurst.kotlin.compose.viewer.CodeViewerStateHolder
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.common.aglEditorOptions
import net.akehurst.language.editor.language.service.LanguageServiceByCoroutine
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The style of a viewer can be set after its text,
 * tokens styled from the parse tree (not only those styled from the scanner) must then be styled.
 */
class test_AglComposeViewer_styleAfterText {

    private companion object {
        val GRAMMAR = """
            namespace test
            grammar Parts {
                skip leaf WS = "\s+" ;
                unit = part* ;
                part = 'part' NAME ';' ;
                NAME = ID ;
                leaf ID = "[a-zA-Z_][a-zA-Z_0-9]*" ;
            }
        """.trimIndent()

        // NAME is not a leaf, so a token is styled by the NAME rule only via the parse tree
        fun style(nameColour: String) = """
            namespace test
            styles Parts {
                NAME { foreground: $nameColour; font-weight: bold; }
            }
        """.trimIndent()

        const val TEXT = "part abc ;"
        const val NAME_START = 5
        const val NAME_FINISH = 8

        val RED = Color(0xFFFF0000)
        val BLUE = Color(0xFF0000FF)

        val logFunction: LogFunction = { level, prefix, t, message -> println("$level - $prefix: ${message()}"); t?.printStackTrace() }

        var nextLanguageNumber = 0
    }

    private fun createViewer(languageService: LanguageService, viewerState: CodeViewerStateHolder): AglEditor<*, *> {
        val languageDefinition = Agl.languageDefinitionFromStringSimple(
            identity = LanguageIdentity("test.styleAfterText${nextLanguageNumber++}"),
            grammarDefinitionStr = GrammarString(GRAMMAR)
        )
        return Agl.attachToComposeViewer(
            languageService,
            languageDefinition,
            { Agl.options { semanticAnalysis { enabled(false) } } },
            "viewer-styleAfterText",
            aglEditorOptions { },
            logFunction,
            viewerState
        )
    }

    private fun CodeViewerStateHolder.styleOfName(): SpanStyle {
        val segment = this.lineStyles[0]?.firstOrNull { it.start == NAME_START && it.finish == NAME_FINISH }
        assertNotNull(segment, "No token for the NAME 'abc', line styles are: ${this.lineStyles[0]?.map { "${it.start}-${it.finish}" }}")
        return segment.style
    }

    /** before a style is set the NAME has no style (there may be no tokens at all) */
    private fun assertNameNotStyled(viewerState: CodeViewerStateHolder) {
        val segment = viewerState.lineStyles[0]?.firstOrNull { it.start == NAME_START && it.finish == NAME_FINISH }
        assertEquals(Color.Unspecified, segment?.style?.color ?: Color.Unspecified)
    }

    private fun assertNameStyled(viewerState: CodeViewerStateHolder, colour: Color) {
        val style = viewerState.styleOfName()
        assertEquals(colour, style.color)
        assertEquals(FontWeight.Bold, style.fontWeight)
    }

    @Test
    fun directExecution_text_then_style() {
        val viewerState = CodeViewerStateHolder(defaultTextStyle = SpanStyle(color = Color.Black))
        val viewer = createViewer(LanguageServiceDirectExecution(logFunction), viewerState)

        viewer.text = TEXT
        // no style yet, so NAME is not styled
        assertNameNotStyled(viewerState)

        viewer.editorSpecificStyleStr = StyleString(style("red"))
        assertNameStyled(viewerState, RED)
    }

    @Test
    fun directExecution_style_then_text() {
        val viewerState = CodeViewerStateHolder(defaultTextStyle = SpanStyle(color = Color.Black))
        val viewer = createViewer(LanguageServiceDirectExecution(logFunction), viewerState)

        viewer.editorSpecificStyleStr = StyleString(style("red"))
        viewer.text = TEXT
        assertNameStyled(viewerState, RED)
    }

    @Test
    fun directExecution_text_then_style_changed_repeatedly() {
        val viewerState = CodeViewerStateHolder(defaultTextStyle = SpanStyle(color = Color.Black))
        val viewer = createViewer(LanguageServiceDirectExecution(logFunction), viewerState)

        viewer.text = TEXT
        viewer.editorSpecificStyleStr = StyleString(style("red"))
        assertNameStyled(viewerState, RED)
        viewer.editorSpecificStyleStr = StyleString(style("blue"))
        assertNameStyled(viewerState, BLUE)
        viewer.editorSpecificStyleStr = StyleString(style("red"))
        assertNameStyled(viewerState, RED)
    }

    @Test
    fun byCoroutine_text_then_style() = runTest {
        val viewerState = CodeViewerStateHolder(defaultTextStyle = SpanStyle(color = Color.Black))
        val viewer = createViewer(LanguageServiceByCoroutine(this, logFunction), viewerState)

        viewer.text = TEXT
        testScheduler.advanceUntilIdle()
        assertNameNotStyled(viewerState)

        viewer.editorSpecificStyleStr = StyleString(style("red"))
        testScheduler.advanceUntilIdle()
        assertNameStyled(viewerState, RED)
    }

    @Test
    fun byCoroutine_text_then_style_changed_repeatedly() = runTest {
        val viewerState = CodeViewerStateHolder(defaultTextStyle = SpanStyle(color = Color.Black))
        val viewer = createViewer(LanguageServiceByCoroutine(this, logFunction), viewerState)

        viewer.text = TEXT
        testScheduler.advanceUntilIdle()
        viewer.editorSpecificStyleStr = StyleString(style("red"))
        testScheduler.advanceUntilIdle()
        assertNameStyled(viewerState, RED)
        viewer.editorSpecificStyleStr = StyleString(style("blue"))
        testScheduler.advanceUntilIdle()
        assertNameStyled(viewerState, BLUE)
    }
}
