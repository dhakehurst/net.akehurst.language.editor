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
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.asm.asmSimple
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.editor.common.AglWorkerSerialisation
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglWorkerSerialisation {

    @Test
    fun AsmSimple_toJsonDocument() {

        val asm = asmSimple {
            root("Root") {
                property("content", "Elem1") {
                    property("propString", "stringValue")
                    property("propListString", listOf("Hello", "World"))
                    property("propListElem", listOf(
                        element("Elem2") { reference("ref", "/0") },
                        element("Elem2") {},
                        element("Elem2") {}
                    ))
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
                                                            entry({primitive("propString")}) {
                                                                objectReferenceable("net.akehurst.language.api.asm.AsmElementProperty") {
                                                                    property("name", "propString")
                                                                    property("value") {
                                                                        primitiveObject("kotlin.String", "stringValue")
                                                                    }
                                                                }
                                                            }
                                                            entry({primitive("propListString")}) { listObject {
                                                                primitiveObject("kotlin.String","Hello")
                                                                primitiveObject("kotlin.String","World")
                                                            } }
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
                }
            }
        }

        assertEquals(expected.toFormattedJsonString("  ", "  "), actual.toFormattedJsonString("  ", "  "))

    }

    @Test
    fun Grammar_toJsonDocument() {
        val grammar = Agl.registry.agl.grammar.processor!!.process<List<Grammar>, Any>(
            sentence = """
                namespace test.test
                grammar Test {
                    rule1 = 'a' ;
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

}