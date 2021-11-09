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
import net.akehurst.language.agl.grammar.scopes.ScopeModel
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.api.asm.AsmSimple
import net.akehurst.language.api.asm.asmSimple
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.editor.common.AglWorkerSerialisation
import net.akehurst.language.editor.common.messages.*
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglWorkerSerialisation {

    companion object {
        val languageId = "test-languageId"
        val editorId = "test-editorId"
        val sessionId = "test-sessionId"
    }

    @Test
    fun AsmSimple_toJsonDocument() {
        val (scopeModel, issues) = Agl.registry.agl.scopes.processor!!.process<ScopeModel, ContextSimple>(
            sentence = """
                scope Root {
                    identify Elem2 by id
                }
                references {
                    in Elem2 property ref refers-to Elem2
                }
            """.trimIndent()
        )
        val context = ContextSimple()
        val asm = asmSimple(scopeModel!!, context) {
            root("Root") {
                propertyElement("content", "Elem1") {
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

        val actual = AglWorkerSerialisation.toJsonDocument(asm)
        val expected = json("serialised") {
            objectReferenceable("net.akehurst.language.api.asm.AsmSimple") {
                property("rootElements") {
                    listObject {
                        objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                            property("id") { primitiveObject("kotlin.Int", 0) }
                            property("asm") { reference("/") }
                            property("typeName", "Root")
                            property("properties") {
                                mapObject {
                                    entry({ primitive("content") }) {
                                        objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                            property("name", "content")
                                            property("value") {
                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                    property("id") { primitiveObject("kotlin.Int", 1) }
                                                    property("asm") { reference("/") }
                                                    property("typeName", "Elem1")
                                                    property("properties") {
                                                        mapObject {
                                                            entry({ primitive("propString") }) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propString")
                                                                    property("value", "stringValue")
                                                                    property("isReference", false)
                                                                }
                                                            }
                                                            entry({ primitive("propListString") }) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propListString")
                                                                    property("value") {
                                                                        listObject {
                                                                            primitive("Hello")
                                                                            primitive("World")
                                                                        }
                                                                    }
                                                                    property("isReference", false)
                                                                }
                                                            }
                                                            entry({ primitive("propListElem") }) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propListElem")
                                                                    property("value") {
                                                                        listObject {
                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                                                property("id") { primitiveObject("kotlin.Int", 2) }
                                                                                property("asm") { reference("/") }
                                                                                property("typeName", "Elem2")
                                                                                property("properties") {
                                                                                    mapObject {
                                                                                        entry({ primitive("id") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "id")
                                                                                                property("value", "e1")
                                                                                                property("isReference", false)
                                                                                            }
                                                                                        }
                                                                                        entry({ primitive("ref") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "ref")
                                                                                                property("value") {
                                                                                                    objectReferenceable("net.akehurst.language.api.asm.AsmElementReference") {
                                                                                                        property("reference", "e2")
                                                                                                        property("value") { reference("/rootElements/\$elements/0/properties/\$entries/0/\$value/value/properties/\$entries/2/\$value/value/\$elements/1") }
                                                                                                    }
                                                                                                }
                                                                                                property("isReference", true)
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                                                property("id") { primitiveObject("kotlin.Int", 3) }
                                                                                property("asm") { reference("/") }
                                                                                property("typeName", "Elem2")
                                                                                property("properties") {
                                                                                    mapObject {
                                                                                        entry({ primitive("id") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "id")
                                                                                                property("value", "e2")
                                                                                                property("isReference", false)
                                                                                            }
                                                                                        }
                                                                                        entry({ primitive("ref") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "ref")
                                                                                                property("value") {
                                                                                                    objectReferenceable("net.akehurst.language.api.asm.AsmElementReference") {
                                                                                                        property("reference", "e3")
                                                                                                        property("value") { reference("/rootElements/\$elements/0/properties/\$entries/0/\$value/value/properties/\$entries/2/\$value/value/\$elements/2") }
                                                                                                    }
                                                                                                }
                                                                                                property("isReference", true)
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                                                property("id") { primitiveObject("kotlin.Int", 4) }
                                                                                property("asm") { reference("/") }
                                                                                property("typeName", "Elem2")
                                                                                property("properties") {
                                                                                    mapObject {
                                                                                        entry({ primitive("id") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "id")
                                                                                                property("value", "e3")
                                                                                                property("isReference", false)
                                                                                            }
                                                                                        }
                                                                                        entry({ primitive("ref") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "ref")
                                                                                                property("value") {
                                                                                                    objectReferenceable("net.akehurst.language.api.asm.AsmElementReference") {
                                                                                                        property("reference", "e1")
                                                                                                        property("value") { reference("/rootElements/\$elements/0/properties/\$entries/0/\$value/value/properties/\$entries/2/\$value/value/\$elements/0") }
                                                                                                    }
                                                                                                }
                                                                                                property("isReference", true)
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    property("isReference", false)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            property("isReference", false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        assertEquals(expected.toFormattedJsonString("  ", "  "), actual.toFormattedJsonString("  ", "  "))

    }

    @Test
    fun AsmSimple_deserialise() {
        val json = json("serialised") {
            objectReferenceable("net.akehurst.language.api.asm.AsmSimple") {
                property("rootElements") {
                    listObject {
                        objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                            property("id") { primitiveObject("kotlin.Int", 0) }
                            property("asm") { reference("/") }
                            property("typeName", "Root")
                            property("properties") {
                                mapObject {
                                    entry({ primitive("content") }) {
                                        objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                            property("name", "content")
                                            property("value") {
                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                    property("id") { primitiveObject("kotlin.Int", 1) }
                                                    property("asm") { reference("/") }
                                                    property("typeName", "Elem1")
                                                    property("properties") {
                                                        mapObject {
                                                            entry({ primitive("propString") }) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propString")
                                                                    property("value", "stringValue")
                                                                    property("isReference", false)
                                                                }
                                                            }
                                                            entry({ primitive("propListString") }) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propListString")
                                                                    property("value") {
                                                                        listObject {
                                                                            primitive("Hello")
                                                                            primitive("World")
                                                                        }
                                                                    }
                                                                    property("isReference", false)
                                                                }
                                                            }
                                                            entry({ primitive("propListElem") }) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propListElem")
                                                                    property("value") {
                                                                        listObject {
                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                                                property("id") { primitiveObject("kotlin.Int", 2) }
                                                                                property("asm") { reference("/") }
                                                                                property("typeName", "Elem2")
                                                                                property("properties") {
                                                                                    mapObject {
                                                                                        entry({ primitive("id") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "id")
                                                                                                property("value", "e1")
                                                                                                property("isReference", false)
                                                                                            }
                                                                                        }
                                                                                        entry({ primitive("ref") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "ref")
                                                                                                property("value") {
                                                                                                    objectReferenceable("net.akehurst.language.api.asm.AsmElementReference") {
                                                                                                        property("reference", "e2")
                                                                                                        property("value") { reference("/rootElements/\$elements/0/properties/\$entries/0/\$value/value/properties/\$entries/2/\$value/value/\$elements/1") }
                                                                                                    }
                                                                                                }
                                                                                                property("isReference", true)
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                                                property("id") { primitiveObject("kotlin.Int", 3) }
                                                                                property("asm") { reference("/") }
                                                                                property("typeName", "Elem2")
                                                                                property("properties") {
                                                                                    mapObject {
                                                                                        entry({ primitive("id") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "id")
                                                                                                property("value", "e2")
                                                                                                property("isReference", false)
                                                                                            }
                                                                                        }
                                                                                        entry({ primitive("ref") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "ref")
                                                                                                property("value") {
                                                                                                    objectReferenceable("net.akehurst.language.api.asm.AsmElementReference") {
                                                                                                        property("reference", "e3")
                                                                                                        property("value") { reference("/rootElements/\$elements/0/properties/\$entries/0/\$value/value/properties/\$entries/2/\$value/value/\$elements/2") }
                                                                                                    }
                                                                                                }
                                                                                                property("isReference", true)
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementSimple") {
                                                                                property("id") { primitiveObject("kotlin.Int", 4) }
                                                                                property("asm") { reference("/") }
                                                                                property("typeName", "Elem2")
                                                                                property("properties") {
                                                                                    mapObject {
                                                                                        entry({ primitive("id") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "id")
                                                                                                property("value", "e3")
                                                                                                property("isReference", false)
                                                                                            }
                                                                                        }
                                                                                        entry({ primitive("ref") }) {
                                                                                            objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                                                property("name", "ref")
                                                                                                property("value") {
                                                                                                    objectReferenceable("net.akehurst.language.api.asm.AsmElementReference") {
                                                                                                        property("reference", "e1")
                                                                                                        property("value") { reference("/rootElements/\$elements/0/properties/\$entries/0/\$value/value/properties/\$entries/2/\$value/value/\$elements/0") }
                                                                                                    }
                                                                                                }
                                                                                                property("isReference", true)
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    property("isReference", false)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            property("isReference", false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val actual = AglWorkerSerialisation.deserialise<AsmSimple>(json.toJsonString())

        val (scopeModel, issues) = Agl.registry.agl.scopes.processor!!.process<ScopeModel, ContextSimple>(
            sentence = """
                scope Root {
                    identify Elem2 by id
                }
                references {
                    in Elem2 property ref refers-to Elem2
                }
            """.trimIndent()
        )
        val context = ContextSimple()
        val expected = asmSimple(scopeModel!!, context) {
            root("Root") {
                propertyElement("content", "Elem1") {
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

        assertEquals(expected.asString("  ", ""), actual.asString("  ", ""))
    }

    @Test
    fun Grammar_toJsonDocument() {
        val grammar = Agl.registry.agl.grammar.processor!!.process<List<Grammar>, Any>(
            sentence = """
                namespace test.test
                grammar Test {
                    S = as | bs | cs ;
                    as = a* ;
                    bs = [b/',']+ ;
                    cs = 'c' | c 'c' ;
                    leaf a = 'a' 'a' ;
                    leaf b = 'b' 'b' ;
                }
            """.trimIndent()
        ).first!![0]

        val actual = AglWorkerSerialisation.toJsonDocument(grammar)

        val expected = json("serialised") {
            objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.GrammarDefault") {
                property("namespace") {
                    objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.NamespaceDefault") {
                        property("qualifiedName", "test.test")
                    }
                }
                property("name", "Test")
                property("rule") {
                    listObject {
                        objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.RuleDefault") {
                            property("grammar") { reference("/") }
                            property("name", "rule1")
                            property("isOverride", false)
                            property("isSkip", false)
                            property("isLeaf", false)
                            property("rhs") {
                                objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.ConcatenationDefault") {
                                    property("items") {
                                        listObject {
                                            objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.TerminalDefault") {
                                                property("value", "a")
                                                property("isPattern", false)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
//TODO: complete json
        assertEquals(expected.toFormattedJsonString("  ", "  "), actual.toFormattedJsonString("  ", "  "))
    }

    @Test
    fun Grammar_deserialise() {
        val json = json("serialised") {
            objectReferenceable("net.akehurst.language.api.grammar.GrammarDefault") {
                property("namespace") {
                    objectReferenceable("net.akehurst.language.api.grammar.NamespaceDefault") {
                        property("qualifiedName", "test.test")
                    }
                }
                property("name", "Test")
                property("rule") {
                    listObject {
                        objectReferenceable("net.akehurst.language.api.grammar.RuleDefault") {
                            property("grammar") { reference("/") }
                            property("name", "rule1")
                            property("isOverride", false)
                            property("isSkip", false)
                            property("isLeaf", false)
                            property("rhs") {
                                objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.ConcatenationDefault") {
                                    property("items") {
                                        listObject {
                                            objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.TerminalDefault") {
                                                property("value", "a")
                                                property("isPattern", false)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val actual = AglWorkerSerialisation.deserialise<Grammar>(json.toJsonString())
        val expected = Agl.registry.agl.grammar.processor!!.process<List<Grammar>, Any>(
            sentence = """
                namespace test.test
                grammar Test {
                    rule1 = 'a' ;
                }
            """.trimIndent()
        ).first!![0]

        assertEquals(expected.namespace, actual.namespace)
        assertEquals(expected.name, actual.name)
        assertEquals(expected.rule.size, actual.rule.size)
        expected.rule.forEachIndexed { i, e ->
            val a = actual.rule[i]
            assertEquals(e.name, a.name)
            //TODO
        }
    }

    @Test
    fun MessageProcessorCreate_com() {
        val expected = MessageProcessorCreate(
            languageId, editorId, sessionId,
            "namespace test grammar Test { rule1 = 'a' ; }"
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessorCreate>(jsonStr)

        assertEquals(expected, actual)
    }

    @Test
    fun MessageProcessorCreateResponse_com() {
        val expected = MessageProcessorCreateResponse(
            languageId, editorId, sessionId,
            false, "Start"
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessorCreateResponse>(jsonStr)

        assertEquals(expected.languageId, actual.languageId)
        assertEquals(expected.editorId, actual.editorId)
        assertEquals(expected.sessionId, actual.sessionId)
        assertEquals(expected.success, actual.success)
        assertEquals(expected.message, actual.message)
    }


    @Test
    fun MessageSyntaxAnalyserConfigure_com() {
        val expected = MessageSyntaxAnalyserConfigure(
            languageId, editorId, sessionId,
            "scope XX { identify y by z } references { }"
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalyserConfigure>(jsonStr)

        assertEquals(expected.languageId, actual.languageId)
        assertEquals(expected.editorId, actual.editorId)
        assertEquals(expected.sessionId, actual.sessionId)
        assertEquals(expected.configuration, actual.configuration)
    }

    @Test
    fun MessageSyntaxAnalyserConfigureResponse_com() {
        val expected = MessageSyntaxAnalyserConfigureResponse(
            languageId, editorId, sessionId,
            false,
            "Error",
            listOf(
                LanguageIssue(LanguageIssueKind.ERROR, LanguageProcessorPhase.SYNTAX_ANALYSIS, InputLocation(0, 0, 0, 0), "Error Message")
            )
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageSyntaxAnalyserConfigureResponse>(jsonStr)

        assertEquals(expected, actual)
    }


    @Test
    fun MessageProcessRequest_com_empty_context() {
        val (scopeModel, issues) = Agl.registry.agl.scopes.processor!!.process<ScopeModel, ContextSimple>(
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
        val context = ContextSimple()
        val expected = MessageProcessRequest(
            "testLang", "tesEditor", "testSession",
            "rule1",
            "Start",
            context
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest>(jsonStr)

        assertEquals(expected.languageId, actual.languageId)
        assertEquals(expected.editorId, actual.editorId)
        assertEquals(expected.sessionId, actual.sessionId)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)

        assertEquals((expected.context as ContextSimple).rootScope.findOrNull("e1", "Elem"), (actual.context as ContextSimple).rootScope.findOrNull("e1", "Elem"))
    }

    @Test
    fun MessageProcessRequest_com() {
        val (scopeModel, issues) = Agl.registry.agl.scopes.processor!!.process<ScopeModel, ContextSimple>(
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
        val context = ContextSimple()
        val asm = asmSimple(scopeModel!!, context) {
            root("Elem") {
                propertyString("id", "e1")
                propertyElement("s1","ScopedElem") {
                    propertyString("id", "se1")
                }
            }
        }
        val expected = MessageProcessRequest(
            "testLang", "tesEditor", "testSession",
            "rule1",
            "Start",
            context
        )

        val jsonStr = AglWorkerSerialisation.serialise(expected)
        val actual = AglWorkerSerialisation.deserialise<MessageProcessRequest>(jsonStr)

        assertEquals(expected.languageId, actual.languageId)
        assertEquals(expected.editorId, actual.editorId)
        assertEquals(expected.sessionId, actual.sessionId)
        assertEquals(expected.goalRuleName, actual.goalRuleName)
        assertEquals(expected.text, actual.text)

        assertEquals((expected.context as ContextSimple).rootScope.findOrNull("e1", "Elem"), (actual.context as ContextSimple).rootScope.findOrNull("e1", "Elem"))
    }
}