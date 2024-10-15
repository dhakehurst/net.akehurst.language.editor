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

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.agl.simple.ContextAsmSimple
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.asm.api.Asm
import net.akehurst.language.asm.builder.asmSimple
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.MessageStatus
import net.akehurst.language.editor.common.EditorOptionsDefault
import net.akehurst.language.editor.language.service.messages.*
import net.akehurst.language.grammar.api.GrammarModel
import net.akehurst.language.grammar.processor.ContextFromGrammarRegistry
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.scanner.api.MatchableKind
import net.akehurst.language.sentence.api.InputLocation
import net.akehurst.language.sppt.api.SharedPackedParseTree
import net.akehurst.language.transform.asm.TransformModelDefault
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_AglWorkerAbstract {

    class TestAglWorker<AsmType : Any, ContextType : Any>() : AglWorkerAbstract() {
        val sent = mutableListOf<AglWorkerMessage>()

        val sentTrace: List<String>
            get() {
                return sent.map {
                    val status = when (it) {
                        is AglWorkerMessageResponse -> it.status.toString()
                        else -> "SEND"
                    }
                    "${it.action} $status"
                }
            }

        override fun sendMessage(port: Any, msg: AglWorkerMessage, transferables: Array<Any>) {
            //replace stuff to make testing easier
            val rmsg = when (msg) {
                is MessageLineTokens -> MessageLineTokens(msg.endPoint, msg.status, msg.message, 0, emptyList())
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
        val languageId = LanguageIdentity("test-languageId")
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
        // Ensure Agl languages are registered
        Agl.registry.agl.grammar
        Agl.registry.unregister(languageId)
//        Agl.registry.register(
//            identity = languageId,
//            grammarStr = null,
//            aglOptions = null,
//            buildForDefaultGoal = false,
//            configuration = Agl.configurationDefault()
//        )
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, grammarStr, null, EditorOptionsDefault()))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(EndPointIdentity(editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(), listOf(
                    Matchable("'a'","a",MatchableKind.LITERAL)
                ))
            ),
            sut.sent
        )
    }

    @Test
    fun sentence_MessageProcessorCreate_error() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, grammarStr, null, EditorOptionsDefault()))

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(
                    EndPointIdentity(editorId, sessionId),
                    MessageStatus.FAILURE, "Error", listOf(
                        LanguageIssue(
                            LanguageIssueKind.ERROR,
                            LanguageProcessorPhase.PARSE,
                            InputLocation(0, 1, 1, 1),
                            "^garbage",
                            setOf("'namespace'")
                        )
                    ), emptyList()
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, grammarStr, null, EditorOptionsDefault()))
        sut.sent.clear()

        // -- MessageProcessRequest -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity( editorId, sessionId),languageId,
                "", Agl.options { semanticAnalysis { context(ContextAsmSimple()) } }
            )
        )

        assertEquals(
            listOf<Any>(
                MessageParseResult(EndPointIdentity(editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
                MessageParseResult(
                    EndPointIdentity(editorId, sessionId), MessageStatus.FAILURE, "Parse Failed", listOf(
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity( editorId, sessionId), Agl.registry.agl.grammarLanguageIdentity,grammarStr, null, EditorOptionsDefault()))

        val expectedMatchables = Agl.registry.agl.grammar.processor!!.scanner!!.matchables
        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(), expectedMatchables)
            ), sut.sent
        )
    }

    @Test
    fun grammar_MessageProcessRequest_user_grammar() {
        val sut = TestAglWorker<Any, Any>()
        val grammarStr = """
            garbage
        """.trimIndent()
        sut.receive(port, MessageProcessorCreate(EndPointIdentity( editorId, sessionId), Agl.registry.agl.grammarLanguageIdentity,grammarStr, null, EditorOptionsDefault()))
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
                EndPointIdentity( editorId, sessionId),Agl.registry.agl.grammarLanguageIdentity,
                ""
            )
        )
        sut.sent.clear()

        // -- MessageProcessRequest grammarLanguage -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(editorId, sessionId),Agl.registry.agl.grammarLanguageIdentity,
                userGrammar, Agl.options { }
            )
        )
        // expect
        // <-- MessageParseResult-START --
        // <-- MessageLineTokens --
        // <-- MessageParseResult-SUCCESS --
        // <-- MessageSyntaxAnalysisResult-START --
        // <-- MessageSyntaxAnalysisResult-SUCCESS --
        // <-- MessageSemanticAnalysisResult-START --
        // <-- MessageSemanticAnalysisResult-SUCCESS --

        val grammarModel = (sut.sent[4] as MessageSyntaxAnalysisResult).asm as GrammarModel

        sut.sent.forEach {
            println(it)
        }
        val expected = listOf<Any>(
            MessageParseResult(
                EndPointIdentity(editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageLineTokens(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                0,emptyList()
            ),
            MessageParseResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), "serialised sppt"
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), grammarModel
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), grammarModel
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
        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), Agl.registry.agl.grammarLanguageIdentity, grammarStr, null, EditorOptionsDefault()))
        sut.sent.clear()

        val userGrammar = """
                namespace test
                grammar Base {
                    A = 'a' ;
                }
                grammar Test : Base {
                    S = A ;
                }
            """.trimIndent()

        // -- MessageSetStyle grammarLanguage -->
        sut.receive(
            port, MessageSetStyle(
                EndPointIdentity(editorId, sessionId),Agl.registry.agl.grammarLanguageIdentity,
                ""
            )
        )
        sut.sent.clear()

        // -- MessageProcessRequest grammarLanguage -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(editorId, sessionId),Agl.registry.agl.grammarLanguageIdentity,
                userGrammar, Agl.options { semanticAnalysis { context(ContextFromGrammarRegistry(Agl.registry)) } }
            )
        )
        // expect
        // <-- MessageParseResult-START --
        // <-- MessageLineTokens --
        // <-- MessageParseResult-SUCCESS --
        // <-- MessageSyntaxAnalysisResult-START --
        // <-- MessageSyntaxAnalysisResult-SUCCESS --
        // <-- MessageSemanticAnalysisResult-START --
        // <-- MessageSemanticAnalysisResult-SUCCESS --

        val grammarModel = (sut.sent[4] as MessageSyntaxAnalysisResult).asm as GrammarModel

        sut.sent.forEach {
            println(it)
        }
        val expected = listOf<Any>(
            MessageParseResult(
                EndPointIdentity(editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageLineTokens(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                0, emptyList()
            ),
            MessageParseResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), "serialised sppt"
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSyntaxAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), grammarModel
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.START, "Start",
                emptyList(), null
            ),
            MessageSemanticAnalysisResult(
                EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success",
                emptyList(), grammarModel
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
            namespace test.Test
                identify Primitive by id
                identify Datatype by id
                identify Collection by id
                references {
                    in TypeReference { property type refers-to Primitive|Datatype|Collection }
                }
            """
        val styleStr = """
            namespace test
            styles Test {
            
            }
        """

        val sentence = """
            primitive String
            datatype A {
                a : String
            }
        """.trimIndent()

        // create userGrammar
        sut.receive(port, MessageProcessorCreate(EndPointIdentity( editorId, sessionId), languageId,userGrammar, crossReferenceStr, EditorOptionsDefault()))
        // set style for agl grammar (is this needed?)
        sut.receive(port, MessageSetStyle(EndPointIdentity(editorId, sessionId),languageId,  styleStr))
        // process the userGrammar sentence
        sut.receive(port, MessageProcessRequest(
            EndPointIdentity( editorId, sessionId),languageId,
            sentence,
            Agl.options { semanticAnalysis { context(ContextAsmSimple()) } }
        ))

        sut.sent.forEach {
            println(it)
        }

        val expectedGrammarModel = Agl.registry.agl.grammar.processor!!.process(userGrammar).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }
        val expectedStyleModel = Agl.registry.agl.style.processor!!.process(styleStr).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }
        val trm = TransformModelDefault.fromGrammarModel(expectedGrammarModel).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }
        val expectedTypeModel = trm.typeModel!!
        val expectedScopeModel = Agl.registry.agl.crossReference.processor!!.process(
            crossReferenceStr,
            Agl.options { semanticAnalysis { context(ContextFromTypeModel(expectedTypeModel)) } }
        ).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }

        val expectedMatchables = Agl.processorFromStringSimple(GrammarString( userGrammar)).processor!!.scanner!!.matchables
        // because LanguageService is in same mem space, no serialisation occurs,
        // hence same ASM is returned for Syntax and Semantic analysis, and the Semantic analysis resolves the references.
        // TODO: could make test better by cloning the asm !
        val expectedAsmSyn = asmSimple() {
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
        val expectedAsmSem = asmSimple(typeModel = expectedTypeModel, crossReferenceModel = expectedScopeModel, context = ContextAsmSimple(), resolveReferences = true, failIfIssues = true) {
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
            MessageProcessorCreateResponse(EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(),expectedMatchables),
            MessageSetStyleResponse(EndPointIdentity(editorId, sessionId), MessageStatus.SUCCESS, "OK", emptyList(), expectedStyleModel),
            MessageParseResult(EndPointIdentity(editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
            MessageLineTokens(EndPointIdentity(editorId, sessionId), MessageStatus.SUCCESS, "Success", 0, emptyList()),
            MessageParseResult(EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList(), "serialised sppt"),
            MessageSyntaxAnalysisResult(EndPointIdentity( editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
            // because LanguageService is in same mem space, no serialisation occurs,
            // hence same ASM is returned for Syntax and Semantic analysis, and the Semantic analysis resolves the references.
            // TODO: could make test better by cloning the asm !
            MessageSyntaxAnalysisResult(EndPointIdentity(editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList(), expectedAsmSem),// expectedAsmSyn),
            MessageSemanticAnalysisResult(EndPointIdentity( editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
            MessageSemanticAnalysisResult(EndPointIdentity( editorId, sessionId), MessageStatus.SUCCESS, "Success", emptyList(), expectedAsmSem)
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
        sut.receive(
            port, MessageProcessorCreate(
                EndPointIdentity(editorId, sessionId), Agl.registry.agl.styleLanguageIdentity,
                grammarStr, null, EditorOptionsDefault()
            )
        )

        assertEquals(
            listOf<Any>(
                MessageProcessorCreateResponse(
                    EndPointIdentity(editorId, sessionId),
                    MessageStatus.SUCCESS, "OK",
                    emptyList(), Agl.registry.agl.style.processor!!.scanner!!.matchables
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

        sut.receive(
            port, MessageProcessorCreate(
                EndPointIdentity(editorId, sessionId),
                Agl.registry.agl.grammarLanguageIdentity, "garbage", null, EditorOptionsDefault()
            )
        )

        // -- MessageProcessRequest user-grammar -->
        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(editorId, sessionId), Agl.registry.agl.grammarLanguageIdentity,
                userGrammar, Agl.options { }
            )
        )

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, userGrammar, null, EditorOptionsDefault()))

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
                EndPointIdentity(editorId, sessionId), languageId,
                "", Agl.options { parse { goalRuleName("S") } }
            )
        )

        assertEquals(
            listOf<Any>(
                MessageParseResult(EndPointIdentity(editorId, sessionId), MessageStatus.START, "Start", emptyList(), null),
                MessageParseResult(
                    EndPointIdentity(editorId, sessionId), MessageStatus.FAILURE, "Parse Failed", listOf(
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
        val styleStr = """
            namespace test
        """
        val referencesStr = """
            namespace test.Test
                identify S by a
        """.trimIndent()

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, grammarStr, referencesStr, EditorOptionsDefault()))

        sut.receive(port, MessageProcessorCreate(EndPointIdentity(editorId, sessionId), Agl.registry.agl.crossReferenceLanguageIdentity, grammarStr, referencesStr, EditorOptionsDefault()))
        sut.receive(port, MessageSetStyle(EndPointIdentity(editorId, sessionId), Agl.registry.agl.crossReferenceLanguageIdentity, styleStr))

        sut.receive(
            port, MessageProcessRequest(
                EndPointIdentity(editorId, sessionId), Agl.registry.agl.crossReferenceLanguageIdentity,
                referencesStr, Agl.options { semanticAnalysis { context(ContextFromTypeModelReference(languageId)) } }
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