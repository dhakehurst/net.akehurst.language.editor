package net.akehurst.language.editor.information

import net.akehurst.language.agl.asm.AsmPathSimple
import net.akehurst.language.agl.grammarTypeModel.grammarTypeModel
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.semanticAnalyser.ContextSimple
import net.akehurst.language.agl.semanticAnalyser.contextSimple
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_ExternalContextLanguage {

    private companion object {

        fun testProcessor(sentence:String, expected:ContextSimple) {
            val result = ExternalContextLanguage.processor.process(sentence)
            assertTrue(result.issues.errors.isEmpty(), result.issues.toString())
            assertEquals(result.asm!!.asString(), expected.asString())
        }
    }

    @Test
    fun check_grammar() {
        val result = Agl.registry.agl.grammar.processor!!.process(ExternalContextLanguage.grammarStr)
        assertTrue(result.issues.errors.isEmpty(), result.issues.toString())
    }

    @Test
    fun check_typeModel() {
        val result = Agl.processorFromStringDefault(ExternalContextLanguage.grammarStr)
        assertTrue(result.issues.errors.isEmpty(), result.issues.toString())

        val expected = grammarTypeModel("demo.ExternalContext", "ExternalContext", "") {
            stringTypeFor("NAME")
            stringTypeFor("STRING")
            dataType("rootScope", "RootScope") {
                propertyListTypeOf("item", "Item", false, 0)
            }
            dataType("item", "Item") {
                propertyPrimitiveType("referableName", "ReferableName", false, 0)
                propertyPrimitiveType("typeReference", "TypeReference", false, 1)
                propertyPrimitiveType("scope", "Scope", true, 2)
            }
            dataType("scope", "Scope") {
                propertyListTypeOf("item", "Item", false, 0)
            }
            dataType("typeReference", "TypeReference") {
                propertyListTypeOf("qualifiedName", "String", false, 0)
            }
            dataType("referableName", "ReferableName") {
                propertyPrimitiveType("string", "String",false,0)
            }
            dataType("qualifiedName", "QualifiedName") {
                propertyListTypeOf("name", "String", false, 0)
            }
        }

        assertEquals(expected.asString(), result.processor!!.typeModel.asString())
    }

    @Test
    fun one_item() {
        val sentence = "'it': external.Type"

        val expected = contextSimple {
            item("it","external.Type", AsmPathSimple.EXTERNAL.value)
        }

        testProcessor(sentence, expected)
    }

    @Test
    fun multiple_items() {
        val sentence = """
            'it': external.Type
            'it2': external.Type
            'it3': external.Type
        """.trimIndent()

        val expected = contextSimple {
            item("it","external.Type", AsmPathSimple.EXTERNAL.value)
            item("it2","external.Type", AsmPathSimple.EXTERNAL.value)
            item("it3","external.Type", AsmPathSimple.EXTERNAL.value)
        }

        testProcessor(sentence, expected)
    }
}