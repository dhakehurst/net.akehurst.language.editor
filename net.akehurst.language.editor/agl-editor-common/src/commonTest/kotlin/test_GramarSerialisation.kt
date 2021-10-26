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

import net.akehurst.kotlin.json.JsonDocument
import net.akehurst.kotlin.json.json
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.grammar.Grammar
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals

class test_GramarSerialisation {

    @Test
    fun toJsonDocument() {
        val grammar = Agl.registry.agl.grammar.processor!!.process<List<Grammar>,Any>(
            sentence = """
                namespace test.test
                grammar Test {
                    rule1 = 'a' ;
                }
            """.trimIndent()
        ).first!![0]

        val actual = GrammarSerialisation.toJsonDocument(grammar)

        val expected = json("serialised") {
            objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.GrammarDefault") {
                property("namespace") {
                    objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.NamespaceDefault") {
                        property("qualifiedName","test.test")
                    }
                }
                property("name","Test")
                property("rule") {
                    listObject {
                        objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.RuleDefault") {
                            property("grammar") { reference("/") }
                            property("name","rule1")
                            property("isOverride",false)
                            property("isSkip",false)
                            property("isLeaf",false)
                            property("rhs") {
                                objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.ConcatenationDefault") {
                                    property("items") {
                                        listObject {
                                            objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.TerminalDefault"){
                                                property("value","a")
                                                property("isPattern",false)
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

        assertEquals(expected.toFormattedJsonString("  ","  "),actual.toFormattedJsonString("  ","  "))
    }

    @Test
    fun deserialise() {
        val json = json("serialised") {
            objectReferenceable("net.akehurst.language.api.grammar.GrammarDefault") {
                property("namespace") {
                    objectReferenceable("net.akehurst.language.api.grammar.NamespaceDefault") {
                        property("qualifiedName","test.test")
                    }
                }
                property("name","Test")
                property("rule") {
                    listObject {
                        objectReferenceable("net.akehurst.language.api.grammar.RuleDefault") {
                            property("grammar") { reference("/") }
                            property("name","rule1")
                            property("isOverride",false)
                            property("isSkip",false)
                            property("isLeaf",false)
                            property("rhs") {
                                objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.ConcatenationDefault") {
                                    property("items") {
                                        listObject {
                                            objectReferenceable("net.akehurst.language.agl.grammar.grammar.asm.TerminalDefault"){
                                                property("value","a")
                                                property("isPattern",false)
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

        val actual = GrammarSerialisation.deserialise(json.toJsonString())
        val expected = Agl.registry.agl.grammar.processor!!.process<List<Grammar>,Any>(
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

}