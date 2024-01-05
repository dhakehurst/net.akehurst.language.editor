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
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.agl.semanticAnalyser.ContextSimple
import net.akehurst.language.api.asm.Asm
import net.akehurst.language.api.asm.asmSimple
import net.akehurst.language.api.language.grammar.Grammar
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.editor.api.Interest
import net.akehurst.language.editor.common.messages.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglWorkerAbstract {

    class TestAglWorker<AsmType : Any, ContextType : Any>() : AglWorkerAbstract<AsmType, ContextType>() {
        val sent = mutableListOf<AglWorkerMessage>()

        val sentTrace:List<String> get() {
            return sent.map {
                val status = when(it) {
                    is AglWorkerMessageResponse -> it.status.toString()
                    else -> "SEND"
                }
                "${it.action} $status ${it.endPoint.languageId}"
            }
        }

        override fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any>) {
            //replace stuff to make testing easier
            val rmsg = when (msg) {
                is MessageLineTokens -> MessageLineTokens(msg.endPoint, msg.status, msg.message, emptyList())
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
                    assertEquals(exp.endPoint, act.endPoint)
                    assertEquals(exp.status, act.status)
                    assertEquals(exp.issues, act.issues)
                    assertEquals((exp.asm as Asm?)?.asString("  "), (act.asm as Asm?)?.asString("  "), "MessageSyntaxAnalysisResult")
                }

                exp is MessageSemanticAnalysisResult && act is MessageSemanticAnalysisResult -> {
                    assertEquals(exp.endPoint, act.endPoint)
                    assertEquals(exp.status, act.status)
                    assertEquals(exp.issues, act.issues)
                    assertEquals((exp.asm as Asm?)?.asString("  "), (act.asm as Asm?)?.asString("  "), "MessageSemanticAnalysisResult")
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), grammarStr, null, Interest()))

        assertEquals(
            sut.sent, listOf<Any>(
                MessageProcessorCreateResponse(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(), emptyList())
            )
        )
    }

    @Test
    fun sentence_MessageProcessorCreate_error() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), grammarStr, null, Interest()))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(
                    EndPointIdentity(languageId, editorId, sessionId),
                    MessageStatus.FAILURE, "Error", emptyList(), listOf(
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), grammarStr, null, Interest()))
        sut.sent.clear()

        // -- MessageProcessRequest -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(languageId, editorId, sessionId),
                "S", Agl.options { semanticAnalysis { context(ContextSimple()) } }
            )
        )

        assertEquals(
            listOf<Any>(
                MessageParseResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
                MessageParseResult(
                    EndPointIdentity(languageId, editorId, sessionId), MessageStatus.FAILURE, "Parse Failed", listOf(
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), grammarStr, null, Interest()))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(), emptyList())
            ), sut.sent
        )
    }

    @Test
    fun grammar_MessageProcessRequest_user_grammar() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), grammarStr, null, Interest()))
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
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId),
                ""
            )
        )
        sut.sent.clear()

        // -- MessageProcessRequest grammarLanguage -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId),
                userGrammar, Agl.options { }
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
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageParseResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), "serialised sppt"
            ),
            MessageLineTokens(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList()
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), grammars
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), grammarStr, null, Interest()))
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
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId),
                ""
            )
        )
        sut.sent.clear()

        // -- MessageProcessRequest grammarLanguage -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId),
                 userGrammar, Agl.options { semanticAnalysis { context(ContextSimple()) } }
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
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageParseResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), "serialised sppt"
            ),
            MessageLineTokens(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList()
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), grammars
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), MessageStatus.SUCCESS, "Success",
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
        val crossReferenceStr = """
            namespace test.Test {
                identify Primitive by id
                identify Datatype by id
                identify Collection by id
                references {
                    in TypeReference { property type refers-to Primitive|Datatype|Collection }
                }
            }
            """
        val grammar = Agl.registry.agl.grammar.processor!!.process(userGrammar).asm!!.first()
        val typeModel = TypeModelFromGrammar.create(grammar)
        val scopeModel = Agl.registry.agl.crossReference.processor!!.process(
            crossReferenceStr,
            Agl.options { semanticAnalysis { context(ContextFromTypeModel(typeModel)) } }
        ).asm!!
        val sentence = """
            primitive String
            datatype A {
                a : String
            }
        """.trimIndent()

        // create userGrammar
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), userGrammar, crossReferenceStr, Interest()))
        // set style for agl grammar (is this needed?)
        sut.receive(port, MessageSetStyle(EndPointIdentity(languageId, editorId, sessionId), ""))
        // process the userGrammar sentence
        sut.receive(port, MessageProcessRequest(
            EndPointIdentity(languageId, editorId, sessionId),
            sentence,
            Agl.options { semanticAnalysis { context(ContextSimple()) } }
        ))

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
        val expectedAsmSem = asmSimple(typeModel = typeModel, crossReferenceModel = scopeModel, context = ContextSimple(), true) {
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
            MessageProcessorCreateResponse(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(), emptyList()),
            MessageSetStyleResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "OK"),
            MessageParseResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
            MessageParseResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList(), "serialised sppt"),
            MessageLineTokens(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList()),
            MessageSyntaxAnalysisResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
            MessageSyntaxAnalysisResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList(), expectedAsmSyn),
            MessageSemanticAnalysisResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
            MessageSemanticAnalysisResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList(), expectedAsmSem)
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
            anything because not used for style language, it is 'built-in'
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(Agl.registry.agl.styleLanguageIdentity, editorId, sessionId), grammarStr, null, Interest()))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(
                    EndPointIdentity(Agl.registry.agl.styleLanguageIdentity, editorId, sessionId),
                    MessageStatus.SUCCESS, "OK",
                    Agl.registry.agl.style.processor!!.scanner!!.matchables, emptyList()
                )
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

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId), "garbage", null, Interest()))

        // -- MessageProcessRequest user-grammar -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId),
                 userGrammar, Agl.options {  }
            )
        )

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), userGrammar, null, Interest()))

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
                EndPointIdentity(languageId, editorId, sessionId),
                "", Agl.options { parse { goalRuleName("S") }}
            )
        )

        assertEquals(
            listOf<Any>(
                MessageParseResult(EndPointIdentity(languageId, editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
                MessageParseResult(
                    EndPointIdentity(languageId, editorId, sessionId), MessageStatus.FAILURE, "Parse Failed", listOf(
                        LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.PARSE, InputLocation(0, 1, 1, 1), "^", setOf("'a'"))
                    ), null
                )
            ), sut.sent
        )
    }

    @Test
    fun references_MessageProcessRequest_success() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            namespace test
            grammar Test {
                S = A ;
                leaf A = 'a' ;
            }
        """.trimIndent()
        val referencesStr = """
            namespace test.Test {
                identify S by a
            }
        """.trimIndent()

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), grammarStr, referencesStr, Interest()))

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(Agl.registry.agl.crossReferenceLanguageIdentity, editorId, sessionId), grammarStr, referencesStr, Interest()))
        sut.receive(port, MessageSetStyle(EndPointIdentity(Agl.registry.agl.crossReferenceLanguageIdentity, editorId, sessionId), ""))

        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(Agl.registry.agl.crossReferenceLanguageIdentity, editorId, sessionId),
                referencesStr,Agl.options { semanticAnalysis { context(ContextFromTypeModelReference(languageId)) } }
            )
        )

        val expected = listOf(
            "MessageProcessorCreateResponse SUCCESS $languageId",
            "MessageProcessorCreateResponse SUCCESS ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageSetStyleResult SUCCESS ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageParseResult START ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageLineTokens SUCCESS ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageParseResult SUCCESS ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageSyntaxAnalysisResult START ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageSyntaxAnalysisResult SUCCESS ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageSemanticAnalysisResult START ${Agl.registry.agl.crossReferenceLanguageIdentity}",
            "MessageSemanticAnalysisResult SUCCESS ${Agl.registry.agl.crossReferenceLanguageIdentity}",
        )

        assertEquals(expected.joinToString(separator = "\n"), sut.sentTrace.joinToString(separator = "\n"))

    }

}