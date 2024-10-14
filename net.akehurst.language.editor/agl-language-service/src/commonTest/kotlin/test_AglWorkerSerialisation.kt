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

package net.akehurst.language.editor.common

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.simple.ContextAsmSimple
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.asm.api.Asm
import net.akehurst.language.asm.builder.asmSimple
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.MessageStatus
import net.akehurst.language.editor.language.service.AglWorkerSerialisation
import net.akehurst.language.editor.language.service.messages.*
import net.akehurst.language.grammar.api.Grammar
import net.akehurst.language.grammar.asm.asGrammarModel
import net.akehurst.language.grammar.processor.ContextFromGrammar
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.scanner.api.MatchableKind
import net.akehurst.language.sentence.api.InputLocation
import net.akehurst.language.sppt.treedata.TreeDataComplete2
import net.akehurst.language.transform.asm.TransformModelDefault
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.asm.SimpleTypeModelStdLib
import net.akehurst.language.typemodel.builder.typeModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_AglWorkerSerialisation {

    companion object {
        val languageId = LanguageIdentity("test-languageId")
        val editorId = "test-editorId"
        val sessionId = "test-sessionId"

        fun <T : Any> test(input: T, checkMatch: (expected: T, actual: T) -> Unit = { expected, actual -> assertEquals(expected, actual) }) {
            val json = AglWorkerSerialisation.toJsonDocument(input)
            val jsonStr = json.toStringJson()
            println(jsonStr)
            assertTrue(jsonStr.length > 10)
            val output: T = AglWorkerSerialisation.deserialise(jsonStr)

            checkMatch(input, output)
        }
    }

    @BeforeTest
    fun before() {
        //AglWorkerSerialisation.check()
    }

    @Test
    fun Grammar_serialise_deserialise() {
        val input: Grammar = Agl.registry.agl.grammar.processor!!.process(
            sentence = """
                namespace test.test
                grammar Test {
                    S = as | bs | cs ;
                    as = a* ;
                    bs = [b/',']+ ;
                    cs = 'c' | cs 'c' ;
                    leaf a = 'a' 'a' ;
                    leaf b = 'b' 'b' ;
                }
            """.trimIndent()
        ).asm!!.primary!!

        test(input.namespace){ expected, actual ->
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun TypeModel_serialise_deserialise() {
        val input: TypeModel = typeModel("test", true, listOf(SimpleTypeModelStdLib)) {
            namespace("test", listOf("std")) {
                primitiveType("APrimType")
                enumType("AnEnumType", listOf("A", "B", "C"))
                collectionType("ACollType", listOf("E"))
                dataType("ADatType")
                dataType("BDatType") {
                    supertypes("ADatType")
                    propertyPrimitiveType("propPrim", "String", false, 0)
                    propertyListTypeOf("propList", "String", false, 1)
                    propertyUnnamedSuperType("propUnnamedType", false, 2) {
                        typeRef("String")
                        typeRef("ADatType")
                    }
                    propertyTupleType("tt", false, 3) {
                        typeRef("a", "String", false)
                    }
                }
            }
        }

        test(input) { expected, actual ->
            actual.resolveImports()
            println(actual.asString())
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun GrammarTypeModel_serialise_deserialise() {
        val grammar = Agl.registry.agl.grammar.processor!!.process(
            sentence = """
                namespace test
                grammar Test {
                    S = as | bs | cs ;
                    as = a* ;
                    bs = [b/',']+ ;
                    cs = 'c' | cs 'c' ;
                    leaf a = 'a' 'a' ;
                    leaf b = 'b' 'b' ;
                }
            """.trimIndent()
        ).asm!!
        val input: TypeModel = TransformModelDefault.fromGrammarModel(grammar).asm!!.typeModel!!

        test(input) { expected, actual ->
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun Asm_serialise_deserialise() {
        val input: Asm = asmSimple() {
            element("Root") {
                propertyElementExplicitType("content", "Elem1") {
                    propertyString("propString", "stringValue")
                    propertyListOfString("propListString", listOf("Hello", "World"))
                    propertyListOfElement("propListElem") {
                        element("Elem2") {
                            propertyString("id", "e1")
                        }
                        element("Elem2") {
                            propertyString("id", "e2")
                        }
                        element("Elem2") {
                            propertyString("id", "e3")
                        }
                    }
                }
            }
        }

        test(input) { expected, actual ->
            println(actual.asString())
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun CrossReferences_serialise_deserialise() {
        val result = Agl.registry.agl.crossReference.processor!!.process(
            sentence = """
                namespace ns
                    identify Elem2 by id
                    references {
                        in Elem2 { property ref refers-to Elem2 }
                    }
            """.trimIndent()
        )
        assertTrue(result.issues.errors.isEmpty(), result.issues.toString())
        val input= result.asm!!

        test(input) { expected, actual ->
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun ContextSimple_serialise_deserialise() {
        val input = contextAsmSimple {
            scope("a1", "A", "/a1")
            item("b1", "B", "/a1/b1")
            scopedItem("c1", "C", "/c1") {
                scope("a1", "A", "/c1/a1")
                item("b1", "B", "/c1/a1/b1")
                scopedItem("c1", "C", "/c1/c1") {}
            }
        }

        test(input) { expected, actual ->
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun Asm_With_CrossReferences_serialise_deserialise() {
        val result = Agl.registry.agl.crossReference.processor!!.process(
            sentence = """
                namespace ns
                    identify Elem2 by id
                    references {
                        in Elem2 { property ref refers-to Elem2 }
                    }
            """.trimIndent()
        )
        assertTrue(result.issues.errors.isEmpty(), result.issues.toString())
        val tm = typeModel("Test", true) {
            namespace("ns") {
                dataType("Root")
                dataType("Elem2") {
                    propertyPrimitiveType("id", "String", false, 0)
                    propertyPrimitiveType("ref", "String", false, 1)
                }
            }
        }
        val context = ContextAsmSimple()
        val input: Asm = asmSimple(typeModel = tm, crossReferenceModel = result.asm!!, context = context) {
            element("Root") {
                propertyElementExplicitType("content", "Elem1") {
                    propertyString("propString", "stringValue")
                    propertyListOfString("propListString", listOf("Hello", "World"))
                    propertyListOfElement("propListElem") {
                        element("Elem2") {
                            propertyString("id", "e1")
                            reference("ref", "e2")
                        }
                        element("Elem2") {
                            propertyString("id", "e2")
                            reference("ref", "e3")
                        }
                        element("Elem2") {
                            propertyString("id", "e3")
                            reference("ref", "e1")
                        }
                    }
                }
            }
        }

        test(input) { expected, actual ->
            assertEquals(expected.asString(), actual.asString())
        }
    }


    // --- MessageProcessorCreate ---
    @Test
    fun MessageProcessorCreate_com_null() {
        val input = MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, "", null, EditorOptionsDefault())

        test(input)
    }

    @Test
    fun MessageProcessorCreate_com_blank() {
        val input = MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, "", null, EditorOptionsDefault())

        test(input)
    }

    @Test
    fun MessageProcessorCreate_com_grammar() {
        val input = MessageProcessorCreate(EndPointIdentity(editorId, sessionId), languageId, "namespace test grammar Test { rule1 = 'a' ; }", null, EditorOptionsDefault())

        test(input)
    }

    // --- MessageProcessorCreateResponse ---
    @Test
    fun MessageProcessorCreateResponse_com_Start() {
        val input = MessageProcessorCreateResponse(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.START, "Start", emptyList(), emptyList()
        )

        test(input) { expected, actual ->
            assertEquals(expected.endPoint, actual.endPoint)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
            assertEquals(expected.issues, actual.issues)
        }
    }

    @Test
    fun MessageProcessorCreateResponse_com_Error() {
        val input = MessageProcessorCreateResponse(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.FAILURE, "Error",
            listOf(
                LanguageIssue(
                    LanguageIssueKind.ERROR,
                    LanguageProcessorPhase.PARSE,
                    InputLocation(0, 1, 1, 1),
                    "error",
                    emptySet<String>()
                )
            ),
            emptyList(),
        )

        test(input) { expected, actual ->
            assertEquals(expected.endPoint, actual.endPoint)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
            assertEquals(expected.issues, actual.issues)
        }
    }

    @Test
    fun MessageProcessorCreateResponse_com_Success() {
        val expected = MessageProcessorCreateResponse(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.SUCCESS, "OK", emptyList(), listOf(
                Matchable("tag", "expr", MatchableKind.LITERAL)
            )
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessorCreateResponse>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.status, actual.status)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
    }

    // --- MessageProcessRequest ---
    @Test
    fun MessageProcessRequest_com_empty_ContextSimple() {
        val context = ContextAsmSimple()
        val expected = MessageProcessRequest(
            EndPointIdentity(editorId, sessionId), languageId,
            "",
            Agl.options {
                parse { goalRuleName("rule1") }
                semanticAnalysis {
                    context(context)
                }
            },
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        println(jsonStr)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<Any, ContextAsmSimple>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.options.parse.goalRuleName, actual.options.parse.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.options.semanticAnalysis.context, actual.options.semanticAnalysis.context)
    }

    @Test
    fun MessageProcessRequest_com_empty_ContextFromGrammar() {
        val grammarStr = """
            namespace test
            grammar Test {
              S = 'a' ;
            }
        """
        val grammar = Agl.registry.agl.grammar.processor!!.process(grammarStr).asm!!
        val context = ContextFromGrammar.createContextFrom(grammar)
        val expected = MessageProcessRequest(
            EndPointIdentity(editorId, sessionId), languageId,
            "",
            Agl.options {
                parse { goalRuleName("rule1") }
                semanticAnalysis {
                    context(context)
                }
            },
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<Any, ContextFromGrammar>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.options.parse.goalRuleName, actual.options.parse.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.options.semanticAnalysis.context as ContextFromGrammar, actual.options.semanticAnalysis.context as ContextFromGrammar)
    }

    @Test
    fun MessageProcessRequest_com_empty_ContextFromTypeModel() {
        val grammarStr = """
            namespace test
            grammar Test {
              S = 'a' ;
            }
        """
        val proc = Agl.processorFromStringSimple(GrammarString(grammarStr)).processor!!
        val context = ContextFromTypeModel(proc.typeModel)
        val expected = MessageProcessRequest(
            EndPointIdentity(editorId, sessionId), languageId,
            "",
            Agl.options {
                parse { goalRuleName("rule1") }
                semanticAnalysis {
                    context(context)
                }
            },
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        println(jsonStr)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<Any, ContextFromTypeModel>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.options.parse.goalRuleName, actual.options.parse.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.options.semanticAnalysis.context as ContextFromTypeModel, actual.options.semanticAnalysis.context as ContextFromTypeModel)
        //TODO: check typemodels match ?
    }

    @Test
    fun MessageProcessRequest_com_user_grammar_with_extends() {
        val userGrammar = """
                namespace test
                grammar Base {
                    A = 'a' ;
                }
                grammar Test extends Base {
                    S = A ;
                }
            """.trimIndent()
        val expected = MessageProcessRequest(
            EndPointIdentity(editorId, sessionId), Agl.registry.agl.grammarLanguageIdentity,
            userGrammar,
            Agl.options { }
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<Any, ContextAsmSimple>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.options.parse.goalRuleName, actual.options.parse.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.options.semanticAnalysis.context, actual.options.semanticAnalysis.context)
    }

    // --- MessageLineTokens ---

    @Test
    fun MessageLineTokens_empty() {
        val expected = MessageLineTokens(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.SUCCESS,
            "",
            0,
            emptyList()
        )
        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageLineTokens>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.status, actual.status)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.lineTokens, actual.lineTokens)
    }

    @Test
    fun MessageLineTokens_lines() {
        val expected = MessageLineTokens(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.SUCCESS,
            "",
            0,
            listOf(
                listOf(
                    AglTokenDefault(listOf("xxx"), 0, 3),
                    AglTokenDefault(listOf("eol"), 3, 1)
                ),
                listOf(
                    AglTokenDefault(listOf("xx"), 4, 2)
                )
            )
        )
        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageLineTokens>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.status, actual.status)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.lineTokens, actual.lineTokens)
    }

    // --- MessageParseResult ---

    @Test
    fun test_TreeDataComplete_empty() {
        val treeData = TreeDataComplete2(0)

        val expected = MessageParseResult2(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.START,
            "Start",
            emptyList(),
            treeData
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageParseResult2>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.treeData, actual.treeData)
    }

    @Test
    fun test_TreeDataComplete_a() {
        val p = Agl.processorFromStringSimple(
            GrammarString(
                """
            namespace test
            grammar Test {
                S = 'a' ;
            }
        """.trimIndent()
            )
        ).processor!!
        val treeData = p.parse("a").sppt?.treeData!!

        val expected = MessageParseResult2(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.START,
            "Start",
            emptyList(),
            treeData
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        println(jsonStr)
        val actual = AglWorkerSerialisation.deserialise<MessageParseResult2>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.treeData, actual.treeData)
        assertTrue(expected.treeData.matches(actual.treeData))
        assertTrue(actual.treeData.matches(expected.treeData))
    }

    @Test
    fun MessageParseResult_com_Start() {
        val expected = MessageParseResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.START,
            "Start",
            emptyList(),
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageParseResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.treeSerialised, actual.treeSerialised)
    }

    @Test
    fun MessageParseResult_com_Error() {
        val expected = MessageParseResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.FAILURE,
            "Error",
            listOf(
                LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.PARSE, InputLocation(0, 1, 1, 1), "error", emptySet<String>())
            ),
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageParseResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.treeSerialised, actual.treeSerialised)
    }

    @Test
    fun MessageParseResult_com_OK() {
        val expected = MessageParseResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.SUCCESS,
            "OK",
            emptyList(),
            "serialised-tree"
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageParseResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.treeSerialised, actual.treeSerialised)
    }

    // --- MessageSyntaxAnalysisResult ---
    @Test
    fun MessageSyntaxAnalysisResult_com_Start() {
        val expected = MessageSyntaxAnalysisResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.START,
            "Start",
            emptyList(),
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalysisResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.asm, actual.asm)
    }

    @Test
    fun MessageSyntaxAnalysisResult_com_Error() {
        val expected = MessageSyntaxAnalysisResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.FAILURE,
            "Error",
            listOf(
                LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.SYNTAX_ANALYSIS, InputLocation(0, 1, 1, 1), "error", null)
            ),
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalysisResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.asm, actual.asm)
    }

    @Test
    fun MessageSyntaxAnalysisResult_com_OK() {
        val grammarStr = """
            namespace test
            grammar Test {
              S = 'a' ;
            }
        """
        val proc = Agl.processorFromStringSimple(GrammarString(grammarStr)).processor!!
        val expected = MessageSyntaxAnalysisResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.SUCCESS,
            "OK",
            emptyList(),
            proc.grammar!!.asGrammarModel()
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        println(jsonStr)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalysisResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
        assertEquals(expected.asm, actual.asm)
    }

    // --- MessageSemanticAnalysisResult ---
    @Test
    fun MessageSemanticAnalysisResult_com_Start() {
        val expected = MessageSemanticAnalysisResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.START,
            "Start",
            emptyList(),
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSemanticAnalysisResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
    }

    @Test
    fun MessageSemanticAnalysisResult_com_Error() {
        val expected = MessageSemanticAnalysisResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.FAILURE,
            "Error",
            listOf(
                LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.SEMANTIC_ANALYSIS, InputLocation(0, 1, 1, 1), "error")
            ),
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSemanticAnalysisResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
    }

    @Test
    fun MessageSemanticAnalysisResult_com_OK_with_reference() {
        val expected = MessageSemanticAnalysisResult(
            EndPointIdentity(editorId, sessionId),
            MessageStatus.SUCCESS,
            "OK",
            emptyList(),
            asmSimple() {
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
                                        propertyNothing("typeArguments")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSemanticAnalysisResult>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
    }

    @Test
    fun MessageProcessRequest_com_ContextFromTypeModel() {
        val result = Agl.registry.agl.crossReference.processor!!.process(
            sentence = """
                namespace test
                    identify Elem by id
                    identify ScopedElem by id
                    scope ScopedElem {
                        identify Elem by id
                    }
                    references {
                        in Elem property ref refers-to Elem
                    }
            """.trimIndent()
        )
        val grammar = Agl.registry.agl.grammar.processor!!.process(
            sentence = """
                namespace test
                grammar Test {
                   S = 'a' ;
                }
            """
        ).asm!!
        val typeModel = TransformModelDefault.fromGrammarModel(grammar).asm!!.typeModel!!
        val context = ContextFromTypeModel(typeModel)

        val expected = MessageProcessRequest(
            EndPointIdentity(editorId, sessionId), languageId,
            "Start",
            Agl.options {
                parse { goalRuleName("rule1") }
                semanticAnalysis {
                    context(context)
                }
            }
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        println(jsonStr)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<Any, ContextAsmSimple>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.options.parse.goalRuleName, actual.options.parse.goalRuleName)
        assertEquals(expected.text, actual.text)
//TODO
        //assertEquals((expected.context as ContextFromTypeModel).typeModel.findOrNull("e1", "Elem"), (actual.context as ContextFromTypeModel).rootScope.findOrNull("e1", "Elem"))

    }


}