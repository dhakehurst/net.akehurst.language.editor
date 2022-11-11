/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.worker

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.agl.syntaxAnalyser.SyntaxAnalyserSimple
import net.akehurst.language.agl.syntaxAnalyser.TypeModelFromGrammar
import net.akehurst.language.api.asm.AsmSimple
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.typeModel.TypeModel
import net.akehurst.language.editor.common.messages.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglWorkerAbstract {

    class TestAglWorker<AsmType : Any, ContextType : Any>() : AglWorkerAbstract<AsmType, ContextType>() {
        val sent = mutableListOf<Any>()

        override fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any>) {
            sent.add(msg)
        }

        override fun serialiseParseTreeToStringJson(spptNode: SPPTNode?): String? {
            TODO("not implemented")
        }

        fun receive(port: Any, msg: AglWorkerMessage) {
            super.receiveAglWorkerMessage(port, msg)
        }
    }

    class TestPort {

    }

    companion object {
        val languageId = "test-languageId"
        val editorId = "test-editorId"
        val sessionId = "test-sessionId"
        val port = TestPort()
    }

    @BeforeTest
    fun before() {
        Agl.registry.unregister(languageId)
        Agl.registry.register<Any, Any>(
            languageId,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null
        )
    }

    @Test
    fun receive_MessageProcessorCreate() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            namespace test
            grammar Test {
                S = 'a' ;
            }
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, grammarStr))

        assertEquals(
            sut.sent, listOf<Any>(
                MessageProcessorCreateResponse(languageId, editorId, sessionId, true, "OK")
            )
        )
    }

    @Test
    fun receive_MessageProcessorCreate_error() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, grammarStr))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(
                    languageId, editorId, sessionId, false, "Unable to parse grammarDefinitionStr:\n" +
                            " at line: 1 column: 1 expected one of: ['namespace']"
                )
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageProcessorCreate_agl_grammar_langauge() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, true, "OK")
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageSyntaxAnalyserConfigure_no_SyntaxAnalyser() {
        val sut = TestAglWorker<Any, Any>()
        sut.receive(
            port, MessageProcessorCreate(
                languageId, editorId, sessionId,
                """
                namespace test
                grammar Test {
                    S = 'a' ;
                }
            """.trimIndent()
            )
        )
        sut.sent.clear()

        sut.receive(
            port, MessageSyntaxAnalyserConfigure(
                languageId, editorId, sessionId,
                ""
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(languageId, editorId, sessionId, true, "OK", emptyList())
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageSyntaxAnalyserConfigure_empty_configuration() {
        Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}

        val sut = TestAglWorker<Any, Any>()
        sut.receive(
            port, MessageProcessorCreate(
                languageId, editorId, sessionId,
                """
                namespace test
                grammar Test {
                    S = 'a' ;
                }
            """.trimIndent()
            )
        )
        sut.sent.clear()

        sut.receive(
            port, MessageSyntaxAnalyserConfigure(
                languageId, editorId, sessionId,
                ""
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(languageId, editorId, sessionId, true, "OK", emptyList())
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageSyntaxAnalyserConfigure_error_parse() {
        Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}

        val sut = TestAglWorker<Any, Any>()
        sut.receive(
            port, MessageProcessorCreate(
                languageId, editorId, sessionId,
                """
                namespace test
                grammar Test {
                    S = 'a' ;
                }
            """.trimIndent()
            )
        )
        sut.sent.clear()

        sut.receive(
            port, MessageSyntaxAnalyserConfigure(
                languageId, editorId, sessionId,
                "garbage"
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(
                    languageId, editorId, sessionId, true, "OK", listOf(
                        LanguageIssue(
                            LanguageIssueKind.ERROR,
                            LanguageProcessorPhase.PARSE,
                            InputLocation(0, 1, 1, 1),
                            "^garbage",
                            setOf("'identify'", "'scope'", "'references'", "<EOT>")
                        )
                    )
                )
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageSyntaxAnalyserConfigure_error_syntaxAnalysis_1() {
        Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}

        val sut = TestAglWorker<Any, Any>()
        sut.receive(
            port, MessageProcessorCreate(
                languageId, editorId, sessionId,
                """
                namespace test
                grammar Test {
                    S = a ;
                    leaf a = 'a' ;
                }
            """.trimIndent()
            )
        )
        sut.sent.clear()

        sut.receive(
            port, MessageSyntaxAnalyserConfigure(
                languageId, editorId, sessionId,
                """
                identify X by b
            """.trimIndent()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(
                    languageId, editorId, sessionId, true, "OK", listOf(
                        LanguageIssue(
                            LanguageIssueKind.ERROR,
                            LanguageProcessorPhase.SYNTAX_ANALYSIS,
                            InputLocation(9, 10, 1, 2),
                            "In root scope 'X' not found as identifiable type"
                        )
                    )
                )
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageSyntaxAnalyserConfigure_error_syntaxAnalysis_2() {
        Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}

        val sut = TestAglWorker<Any, Any>()
        sut.receive(
            port, MessageProcessorCreate(
                languageId, editorId, sessionId,
                """
                namespace test
                grammar Test {
                    S = a ;
                    leaf a = 'a' ;
                }
            """.trimIndent()
            )
        )
        sut.sent.clear()

        sut.receive(
            port, MessageSyntaxAnalyserConfigure(
                languageId, editorId, sessionId,
                """
                identify S by b
            """.trimIndent()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(
                    languageId, editorId, sessionId, true, "OK", listOf(
                        LanguageIssue(
                            LanguageIssueKind.ERROR,
                            LanguageProcessorPhase.SYNTAX_ANALYSIS,
                            InputLocation(14, 15, 1, 1),
                            "In root scope 'b' not found for identifying property of 'S'"
                        )
                    )
                )
            ), sut.sent
        )
    }


}