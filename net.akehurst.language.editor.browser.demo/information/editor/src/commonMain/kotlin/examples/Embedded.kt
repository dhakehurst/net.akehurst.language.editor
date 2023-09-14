/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object Embedded {
    val id = "Embedded"
    val label = "Embedded"
    val sentence = """
babbabbab
    """.trimIndent()
    val grammar = """
namespace net.akehurst.language.example

grammar Inner {
    S = A | SA ;
    SA = S A ;
    A = a ;
    leaf a = 'a' ;
}

grammar Outer {
   S = B | SBC ;
   SBC = S BC ;
   BC = B | C ;
   B = b Inner::S b ;
   C = c Inner::S c ;
   leaf b = 'b' ;
   leaf c = 'c' ;
}
    """.trimIndent()

    val references = """
    """.trimIndent()

    val style = """
    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, "S", sentence, grammar, references, style, format)

}