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

import net.akehurst.language.agl.default.TypeModelFromGrammar
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.api.asm.AsmSimple
import net.akehurst.language.api.asm.asmSimple
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.sppt.SharedPackedParseTree
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

        override fun serialiseParseTreeToStringJson(sentence: String, sppt: SharedPackedParseTree?): String? {
            return "serialised sppt"
        }

        fun receive(port: Any, msg: AglWorkerMessage) {
            super.receiveAglWorkerMessage(port, msg)
        }
    }

    class TestPort {

    }

    private companion object {
        val aglGrammarId = Agl.registry.agl.grammarLanguageIdentity
        val languageId = "test-languageId"
        val editorId = "test-editorId"
        val sessionId = "test-sessionId"
        val port = TestPort()

        fun checkEquals(exp: AglWorkerMessage, act: AglWorkerMessage) {
            when {
                exp is MessageSyntaxAnalysisResult && act is MessageSyntaxAnalysisResult -> {
                    assertEquals(exp.languageId, act.languageId)
                    assertEquals(exp.editorId, act.editorId)
                    assertEquals(exp.sessionId, act.sessionId)
                    assertEquals(exp.status, act.status)
                    assertEquals(exp.issues, act.issues)
                    assertEquals((exp.asm as AsmSimple?)?.asString("  "), (act.asm as AsmSimple?)?.asString("  "),"MessageSyntaxAnalysisResult")
                }

                exp is MessageSemanticAnalysisResult && act is MessageSemanticAnalysisResult -> {
                    assertEquals(exp.languageId, act.languageId)
                    assertEquals(exp.editorId, act.editorId)
                    assertEquals(exp.sessionId, act.sessionId)
                    assertEquals(exp.status, act.status)
                    assertEquals(exp.issues, act.issues)
                    assertEquals((exp.asm as AsmSimple?)?.asString("  "), (act.asm as AsmSimple?)?.asString("  "),"MessageSemanticAnalysisResult")
                }

                else -> assertEquals(exp, act)
            }
        }
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
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, grammarStr, null))

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
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, grammarStr, null))

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
        val grammarStr = """
                namespace test
                grammar Test {
                    S = 'a' ;
                }
            """.trimIndent()
        // -- MessageProcessorCreate -->
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, grammarStr, null))
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
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr, null))

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
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr, null))
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
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId, grammarStr, null))
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
    fun grammar_MessageProcessRequest_user_grammar_with_reference() {
        val sut = TestAglWorker<Any, Any>()
        val userGrammar = """
            namespace test
            
            grammar Test {
                skip leaf WS = "\s+" ;
                skip leaf COMMENT = "//[^\n]*(\n)" ;
            
                unit = declaration* ;
                declaration = datatype | primitive | collection ;
                primitive = 'primitive' ID ;
                collection = 'collection' ID typeParameters? ;
                typeParameters = '<' typeParameterList '>' ;
                typeParameterList = [ID / ',']+ ;
                datatype = 'datatype' ID '{' property* '}' ;
                property = ID ':' typeReference ;
                typeReference = type typeArguments? ;
                typeArguments = '<' typeArgumentList '>' ;
                typeArgumentList = [typeReference / ',']+ ;
            
                leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;
                leaf type = ID;
            }
        """
        val scopeStr = """
                identify Unit by §nothing
                scope Unit {
                    identify Primitive by id
                    identify Datatype by id
                    identify Collection by id
                }
                references {
                    in TypeReference property type refers-to Primitive|Datatype|Collection
                }
            """
        val grammar = Agl.registry.agl.grammar.processor!!.process(userGrammar).asm!!.first()
        val scopeModel = Agl.registry.agl.scopes.processor!!.process(
            scopeStr,
            Agl.options { semanticAnalysis { context(ContextFromTypeModel(grammar.qualifiedName, TypeModelFromGrammar.create(grammar))) } }
        ).asm!!
        val sentence = """
            primitive String
            datatype A {
                a : String
            }
        """.trimIndent()

        // create userGrammar
        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, userGrammar, scopeStr))
        // set style for agl grammar (is this needed?)
        sut.receive(port, MessageSetStyle(languageId, editorId, sessionId, ""))
        // process the userGrammar sentence
        sut.receive(port, MessageProcessRequest(languageId, editorId, sessionId, "unit", sentence, ContextSimple()))

        sut.sent.forEach {
            println(it)
        }
        val expectedAsmSyn = asmSimple {
            element("Unit") {
                propertyListOfElement("declaration") {
                    element("Primitive") {
                        propertyString("id", "String")
                    }
                    element("Datatype") {
                        propertyString("id", "A")
                        propertyListOfElement("property") {
                            element("Property") {
                                propertyString("id", "a")
                                propertyElementExplicitType("typeReference", "TypeReference") {
                                    reference("type", "String")
                                    propertyString("typeArguments", null)
                                }
                            }
                        }
                    }
                }
            }
        }
        val expectedAsmSem = asmSimple(scopeModel, ContextSimple(), true) {
            element("Unit") {
                propertyListOfElement("declaration") {
                    element("Primitive") {
                        propertyString("id", "String")
                    }
                    element("Datatype") {
                        propertyString("id", "A")
                        propertyListOfElement("property") {
                            element("Property") {
                                propertyString("id", "a")
                                propertyElementExplicitType("typeReference", "TypeReference") {
                                    reference("type", "String")
                                    propertyString("typeArguments", null)
                                }
                            }
                        }
                    }
                }
            }
        }
        val expected = listOf<Any>(
            MessageProcessorCreateResponse(languageId, editorId, sessionId, MessageStatus.SUCCESS, "OK", emptyList()),
            MessageSetStyleResult(languageId, editorId, sessionId, MessageStatus.SUCCESS, "OK"),
            MessageParseResult(languageId, editorId, sessionId, MessageStatus.START, "Start", emptyList(), null),
            MessageParseResult(languageId, editorId, sessionId, MessageStatus.SUCCESS, "Success", emptyList(), "serialised sppt"),
            MessageLineTokens(languageId, editorId, sessionId, MessageStatus.SUCCESS, "Success", emptyList()),
            MessageSyntaxAnalysisResult(languageId, editorId, sessionId, MessageStatus.START, "Start", emptyList(), null),
            MessageSyntaxAnalysisResult(languageId, editorId, sessionId, MessageStatus.SUCCESS, "Success", emptyList(), expectedAsmSyn),
            MessageSemanticAnalysisResult(languageId, editorId, sessionId, MessageStatus.START, "Start", emptyList(), null),
            MessageSemanticAnalysisResult(languageId, editorId, sessionId, MessageStatus.SUCCESS, "Success", emptyList(), expectedAsmSem)
        )

        for (i in expected.indices) {
            val exp = expected[i]
            val act = sut.sent[i]
            checkEquals(exp as AglWorkerMessage, act as AglWorkerMessage)
        }
    }

    @Test
    fun style_MessageProcessorCreate_agl_style_language() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(Agl.registry.agl.styleLanguageIdentity, editorId, sessionId, grammarStr, null))

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

        sut.receive(port, MessageProcessorCreate(languageId, editorId, sessionId, userGrammar, null))

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


}