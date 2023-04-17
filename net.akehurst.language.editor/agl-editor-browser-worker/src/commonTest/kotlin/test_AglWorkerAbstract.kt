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

import net.akehurst.language.agl.grammar.grammar.ContextFromGrammar
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.agl.syntaxAnalyser.SyntaxAnalyserSimple
import net.akehurst.language.agl.syntaxAnalyser.TypeModelFromGrammar
import net.akehurst.language.api.asm.AsmSimple
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.typemodel.TypeModel
import net.akehurst.language.editor.common.messages.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglWorkerAbstract {

    class TestAglWorker<AsmType : Any, ContextType : Any>() : AglWorkerAbstract<AsmType, ContextType>() {
        val sent = mutableListOf<Any>()

        override fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any>) {
            //replace stuff to make testing easier
            val rmsg = when (msg) {
                is MessageLineTokens -> MessageLineTokens(msg.languageId, msg.editorId, msg.sessionId, msg.status, msg.message, emptyList())
                else -> msg
            }
            sent.add(rmsg)
        }

        override fun serialiseParseTreeToStringJson(spptNode: SPPTNode?): String? {
            return "serialised sppt"
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
        Agl.registry.register(
            identity = languageId,
            grammarStr = null,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationDefault()
        )
    }

    @Test
    fun sentence_MessageProcessorCreate() {
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
                MessageProcessorCreateResponse(languageId, editorId, sessionId, MessageStatus.SUCCESS, "OK", emptyList())
            )
        )
    }

    @Test
    fun sentence_MessageProcessorCreate_error() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, grammarStr))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(
                    languageId, editorId, sessionId, MessageStatus.FAILURE, "Error", listOf(
                        LanguageIssue(
                            LanguageIssueKind.ERROR,
                            LanguageProcessorPhase.PARSE,
                            InputLocation(0, 1, 1, 1),
                            "^garbage",
                            setOf("'namespace'")
                        )
                    )
                )
            ), sut.sent
        )
    }

    @Test
    fun sentence_MessageProcessRequest_blank_ContextSimple_parse_failure() {
        val sut = TestAglWorker<Any, Any>()
        // -- MessageProcessorCreate -->
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

        // -- MessageProcessRequest -->
        sut.receive(
            port, MessageProcessRequest(
                languageId, editorId, sessionId,
                "S", "", ContextSimple()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageParseResult(languageId, editorId, sessionId, MessageStatus.START, "Start", emptyList(), null),
                MessageParseResult(
                    languageId, editorId, sessionId, MessageStatus.FAILURE, "Parse Failed", listOf(
                        LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.PARSE, InputLocation(0, 1, 1, 1), "^", setOf("'a'"))
                    ), null
                )
            ), sut.sent
        )
    }

    @Test
    fun grammar_MessageProcessorCreate_agl_grammar_language() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "OK", emptyList())
            ), sut.sent
        )
    }

    @Test
    fun grammar_MessageProcessRequest_user_grammar() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr))
        sut.sent.clear()

        val userGrammar = """
                namespace test
                grammar Test {
                    S = 'a' ;
                }
            """.trimIndent()

        // -- MessageSetStyle grammarLanguage -->
        sut.receive(
            port, MessageSetStyle(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId,
                ""
            )
        )
        sut.sent.clear()

        // -- MessageProcessRequest grammarLanguage -->
        sut.receive(
            port, MessageProcessRequest(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId,
                null, userGrammar, null
            )
        )
        // expect
        // <-- MessageParseResult-START --
        // <-- MessageParseResult-SUCCESS --
        // <-- MessageLineTokens --
        // <-- MessageSyntaxAnalysisResult-START --
        // <-- MessageSyntaxAnalysisResult-SUCCESS --
        // <-- MessageSemanticAnalysisResult-START --
        // <-- MessageSemanticAnalysisResult-SUCCESS --

        val grammars = (sut.sent[4] as MessageSyntaxAnalysisResult).asm as List<Grammar>

        sut.sent.forEach {
            println(it)
        }
        val expected = listOf<Any>(
            MessageParseResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageParseResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList(), "serialised sppt"
            ),
            MessageLineTokens(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList()
            ),
            MessageSyntaxAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSyntaxAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList(), grammars
            ),
            MessageSemanticAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSemanticAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList(), grammars
            )
        )

        for (i in expected.indices) {
            val exp = expected[i]
            val act = sut.sent[i]
            assertEquals(exp, act)
        }
    }

    @Test
    fun grammar_MessageProcessRequest_user_grammar_with_extends() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr))
        sut.sent.clear()

        val userGrammar = """
                namespace test
                grammar Base {
                    A = 'a' ;
                }
                grammar Test extends Base {
                    S = A ;
                }
            """.trimIndent()

        // -- MessageSetStyle grammarLanguage -->
        sut.receive(
            port, MessageSetStyle(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId,
                ""
            )
        )
        sut.sent.clear()

        // -- MessageProcessRequest grammarLanguage -->
        sut.receive(
            port, MessageProcessRequest(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId,
                null, userGrammar, null
            )
        )
        // expect
        // <-- MessageParseResult-START --
        // <-- MessageParseResult-SUCCESS --
        // <-- MessageLineTokens --
        // <-- MessageSyntaxAnalysisResult-START --
        // <-- MessageSyntaxAnalysisResult-SUCCESS --
        // <-- MessageSemanticAnalysisResult-START --
        // <-- MessageSemanticAnalysisResult-SUCCESS --

        val grammars = (sut.sent[4] as MessageSyntaxAnalysisResult).asm as List<Grammar>

        sut.sent.forEach {
            println(it)
        }
        val expected = listOf<Any>(
            MessageParseResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageParseResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList(), "serialised sppt"
            ),
            MessageLineTokens(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList()
            ),
            MessageSyntaxAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSyntaxAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList(), grammars
            ),
            MessageSemanticAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSemanticAnalysisResult(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "Success",
                emptyList(), grammars
            )
        )

        for (i in expected.indices) {
            val exp = expected[i]
            val act = sut.sent[i]
            assertEquals(exp, act)
        }
    }


    @Test
    fun style_MessageProcessorCreate_agl_style_language() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.styleLanguageIdentity, editorId, sessionId, grammarStr))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(Agl.registry.agl.styleLanguageIdentity, editorId, sessionId, MessageStatus.SUCCESS, "OK", emptyList())
            ), sut.sent
        )
    }

    @Test
    fun style_MessageProcessRequest_content_ContextFromGrammar_success() {
        val sut = TestAglWorker<Any, Any>()
        val userGrammar = """
                namespace test
                grammar Test {
                    S = 'a' ;
                }
            """.trimIndent()

        // -- MessageProcessRequest user-grammar -->
        sut.receive(
            port, MessageProcessRequest(
                Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId,
                null, userGrammar, null
            )
        )

        sut.receive(
            port, MessageProcessorCreate(
                languageId, editorId, sessionId,
                userGrammar
            )
        )

        sut.sent.clear()
        // expect
        // <-- MessageParseResult-START --
        // <-- MessageParseResult-SUCCESS --
        // <-- MessageSyntaxAnalysisResult-START --
        // <-- MessageSyntaxAnalysisResult-SUCCESS --
        // <-- MessageSemanticAnalysisResult-START --
        // <-- MessageSemanticAnalysisResult-SUCCESS --

        // -- MessageProcessRequest -->
        sut.receive(
            port, MessageProcessRequest(
                languageId, editorId, sessionId,
                "S", "", null//ContextFromGrammar(grammar)
            )
        )

        assertEquals(
            listOf<Any>(
                MessageParseResult(languageId, editorId, sessionId, MessageStatus.START, "Start", emptyList(), null),
                MessageParseResult(
                    languageId, editorId, sessionId, MessageStatus.FAILURE, "Parse Failed", listOf(
                        LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.PARSE, InputLocation(0, 1, 1, 1), "^", setOf("'a'"))
                    ), null
                )
            ), sut.sent
        )
    }


    @Test
    fun receive_MessageSyntaxAnalyserConfigure_empty_configuration() {
        //Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyser =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}

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
                emptyMap()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(languageId, editorId, sessionId, MessageStatus.SUCCESS, "OK", emptyList())
            ), sut.sent
        )
    }

    @Test
    fun receive_MessageSyntaxAnalyserConfigure_error_parse() {
        //Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}

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
                emptyMap()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(
                    languageId, editorId, sessionId, MessageStatus.FAILURE, "OK", listOf(
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
        //Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}
//TODO:...fix, test was supposed to check scope model errors
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
                emptyMap()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(
                    languageId, editorId, sessionId, MessageStatus.SUCCESS, "OK", listOf(
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
        //Agl.registry.findOrPlaceholder<AsmSimple, ContextSimple>(languageId).syntaxAnalyserResolver =  { g -> SyntaxAnalyserSimple(TypeModelFromGrammar(g))}
//TODO:...fix, test was supposed to check scope model errors
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
                emptyMap()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageSyntaxAnalyserConfigureResponse(
                    languageId, editorId, sessionId, MessageStatus.SUCCESS, "OK", listOf(
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