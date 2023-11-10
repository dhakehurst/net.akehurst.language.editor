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
package net.akehurst.language.editor.common.serialisation

import net.akehurst.kotlin.json.json
import net.akehurst.language.agl.asm.AsmPathSimple
import net.akehurst.language.agl.default.TypeModelFromGrammar
import net.akehurst.language.agl.grammarTypeModel.grammarTypeModel
import net.akehurst.language.agl.language.grammar.ContextFromGrammar
import net.akehurst.language.agl.language.reference.CrossReferenceModelDefault
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextSimple
import net.akehurst.language.agl.semanticAnalyser.ScopeSimple
import net.akehurst.language.agl.semanticAnalyser.contextSimple
import net.akehurst.language.agl.sppt.TreeDataComplete
import net.akehurst.language.api.asm.Asm
import net.akehurst.language.api.asm.AsmPath
import net.akehurst.language.api.asm.asmSimple
import net.akehurst.language.api.language.grammar.Grammar
import net.akehurst.language.api.language.reference.Scope
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.sppt.SpptDataNode
import net.akehurst.language.editor.common.AglWorkerSerialisation
import net.akehurst.language.editor.common.messages.*
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.api.typeModel
import net.akehurst.language.typemodel.simple.SimpleTypeModelStdLib
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_AglWorkerSerialisation {

    companion object {
        val languageId = "test-languageId"
        val editorId = "test-editorId"
        val sessionId = "test-sessionId"

        fun <T : Any> test(input: T, checkMatch: (expected: T, actual: T) -> Unit = { expected, actual -> assertEquals(expected, actual) }) {
            val json = AglWorkerSerialisation.toJsonDocument(input)
            val jsonStr = json.toStringJson()
            assertTrue(jsonStr.length > 10)
            val output: T = AglWorkerSerialisation.deserialise(jsonStr)

            checkMatch(input, output)
        }
    }

    @BeforeTest
    fun before() {
        AglWorkerSerialisation.check()
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
        ).asm!![0]

        test(input)
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
                        primitiveRef("String")
                        elementRef("ADatType")
                    }
                    propertyTupleType("tt", false, 3) {
                        propertyPrimitiveType("a", "String", false, 0)
                    }
                }
            }
        }

        test(input) { expected, actual ->
            actual.resolveImports()
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun Asm_serialise_deserialise() {
        val result = Agl.registry.agl.crossReference.processor!!.process(
            sentence = """
                namespace test {
                    identify Root by §nothing
                    scope Root {
                        identify Elem2 by id
                    }
                    references {
                        in Elem2 { property ref refers-to Elem2 }
                    }
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
        val context = ContextSimple()
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

    @Test
    fun GrammarTypeModel_serialise_deserialise() {
        val grammar = Agl.registry.agl.grammar.processor!!.process(
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
        ).asm!![0]
        val input: TypeModel = TypeModelFromGrammar.create(grammar)

        test(input) { expected, actual ->
            assertEquals(expected.asString(), actual.asString())
        }
    }

    @Test
    fun ContextSimple_serialise_deserialise() {
        val input = contextSimple {
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

    // --- MessageProcessorCreate ---
    @Test
    fun MessageProcessorCreate_com_null() {
        val input = MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), "", null)

        test(input)
    }

    @Test
    fun MessageProcessorCreate_com_blank() {
        val input = MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), "", null)

        test(input)
    }

    @Test
    fun MessageProcessorCreate_com_grammar() {
        val input = MessageProcessorCreate(EndPointIdentity(languageId, editorId, sessionId), "namespace test grammar Test { rule1 = 'a' ; }", null)

        test(input)
    }

    // --- MessageProcessorCreateResponse ---
    @Test
    fun MessageProcessorCreateResponse_com_Start() {
        val input = MessageProcessorCreateResponse(
            EndPointIdentity(languageId, editorId, sessionId),
            MessageStatus.START, "Start", emptyList(),emptyList()
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
            EndPointIdentity(languageId, editorId, sessionId),
            MessageStatus.FAILURE, "Error", emptyList(), listOf(
                LanguageIssue(
                    LanguageIssueKind.ERROR,
                    LanguageProcessorPhase.PARSE,
                    InputLocation(0, 1, 1, 1),
                    "error",
                    emptySet<String>()
                )
            )
        )

        test(input) { expected, actual ->
            assertEquals(expected.endPoint, actual.endPoint)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
            assertEquals(expected.issues, actual.issues)
        }
    }

    @Test
    fun MessageProcessorCreateResponse_com_OK() {
        val expected = MessageProcessorCreateResponse(
            EndPointIdentity(languageId, editorId, sessionId),
            MessageStatus.SUCCESS, "OK", emptyList(),emptyList()
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessorCreateResponse>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.status, actual.status)
        assertEquals(expected.message, actual.message)
        assertEquals(expected.issues, actual.issues)
    }

    // ---  ---
    @Test
    fun MessageSyntaxAnalyserConfigure_com() {
        val expected = MessageSyntaxAnalyserConfigure(
            EndPointIdentity(languageId, editorId, sessionId),
            emptyMap(),//"scope XX { identify y by z } references { }"
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalyserConfigure>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.configuration, actual.configuration)
    }

    @Test
    fun MessageSyntaxAnalyserConfigureResponse_com() {
        val expected = MessageSyntaxAnalyserConfigureResponse(
            EndPointIdentity(languageId, editorId, sessionId),
            MessageStatus.FAILURE,
            "Error",
            listOf(
                LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.SYNTAX_ANALYSIS, InputLocation(0, 0, 0, 0), "Error Message")
            )
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalyserConfigureResponse>(jsonStr)

        assertEquals(expected, actual)
    }

    // --- MessageProcessRequest ---
    @Test
    fun MessageProcessRequest_com_empty_ContextSimple() {
        val context = ContextSimple()
        val expected = MessageProcessRequest(
            EndPointIdentity(languageId, editorId, sessionId),
            "rule1",
            "",
            context
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<ContextSimple>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.context, actual.context)
    }

    @Test
    fun MessageProcessRequest_com_empty_ContextFromGrammar() {
        val grammarStr = """
            namespace test
            grammar Test {
              S = 'a' ;
            }
        """
        val grammar = Agl.registry.agl.grammar.processor!!.process(grammarStr).asm!!.first()
        val context = ContextFromGrammar.createContextFrom(listOf(grammar))
        val expected = MessageProcessRequest(
            EndPointIdentity(languageId, editorId, sessionId),
            "rule1",
            "",
            context
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<ContextFromGrammar>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.context as ContextFromGrammar, actual.context as ContextFromGrammar)
    }

    @Test
    fun MessageProcessRequest_com_empty_ContextFromTypeModel() {
        val grammarStr = """
            namespace test
            grammar Test {
              S = 'a' ;
            }
        """
        val proc = Agl.processorFromStringDefault(grammarStr).processor!!
        val context = ContextFromTypeModel(proc.typeModel)
        val expected = MessageProcessRequest(
            EndPointIdentity(languageId, editorId, sessionId),
            "rule1",
            "",
            context
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<ContextFromTypeModel>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.context as ContextFromTypeModel, actual.context as ContextFromTypeModel)
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
            EndPointIdentity(Agl.registry.agl.grammarLanguageIdentity, editorId, sessionId),
            null,
            userGrammar,
            null
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<ContextSimple>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.context, actual.context)
    }


    // --- MessageParseResult ---

    @Test
    fun test_TreeDataComplete() {
        val sut = TreeDataComplete<SpptDataNode>(0)

        TODO()
    }

    @Test
    fun MessageParseResult_com_Start() {
        val expected = MessageParseResult(
            EndPointIdentity(languageId, editorId, sessionId),
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
            EndPointIdentity(languageId, editorId, sessionId),
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
            EndPointIdentity(languageId, editorId, sessionId),
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
            EndPointIdentity(languageId, editorId, sessionId),
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
            EndPointIdentity(languageId, editorId, sessionId),
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
        val proc = Agl.processorFromStringDefault(grammarStr).processor!!
        val expected = MessageSyntaxAnalysisResult(
            EndPointIdentity(languageId, editorId, sessionId),
            MessageStatus.SUCCESS,
            "OK",
            emptyList(),
            listOf(proc.grammar!!)
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
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
            EndPointIdentity(languageId, editorId, sessionId),
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
            EndPointIdentity(languageId, editorId, sessionId),
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
            EndPointIdentity(languageId, editorId, sessionId),
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
                                        propertyNull("typeArguments")
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
        ).asm!!.first()
        val context = ContextFromTypeModel(TypeModelFromGrammar.create(grammar))

        val expected = MessageProcessRequest(
            EndPointIdentity(languageId, editorId, sessionId),
            "rule1",
            "Start",
            context
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest<ContextSimple>>(jsonStr)

        assertEquals(expected.endPoint, actual.endPoint)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)
//TODO
        assertEquals((expected.context as ContextFromTypeModel).rootScope.findOrNull("e1", "Elem"), (actual.context as ContextFromTypeModel).rootScope.findOrNull("e1", "Elem"))

    }


}