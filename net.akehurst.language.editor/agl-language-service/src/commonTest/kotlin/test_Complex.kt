package net.akehurst.language.editor.common

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class test_Complex {

    @Test
    fun t1() {
        val grammarStr = GrammarString("""
namespace test

grammar Test {
    skip leaf WS = "\s+" ;
    statements = (content '.')+ ;
    content
      = 'A' word
      | 'B' word
      ;

    leaf word = "[^ \t\n\x0B\f\r:.]+" ;
}            
            """)
        val sentence = "A fred. B jim."

        val proc = Agl.processorFromStringSimple(grammarStr).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.processor!!
        }

        val asm = proc.process(sentence).let {
            assertTrue(it.issues.errors.isEmpty(), it.issues.toString())
            it.asm!!
        }
        println(asm.asString())
    }

}