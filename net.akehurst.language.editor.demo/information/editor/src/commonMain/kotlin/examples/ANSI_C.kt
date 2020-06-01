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

object ANSI_C {
    val id = "ansi_C"
    val label = "ANSI C"
    val sentence = """

    """.trimIndent()
    val grammar = """
namespace test

grammar C {
    skip WS = "\s+" ;

    expression
      = primary-expression
      | postfix-expression
      | array-index-expression
      | function-call-expression
      | field-selector-expression
      | unary-expression
      | cast-expression
      | infix-expression
      | assignment-expression
      ;
      
    primary-expression = ID | literal | grouped-expression ;
    postfix-expression = expression postfix-operator ;
    postfix-operator = '--' | '++' ;
    array-index-expression = expression '[' expression ']' ;
    function-call-expression = expression '(' argument-list ')' ;
    argument-list = [ expression / ',' ]* ;
    field-selector-expression = expression '.' ID ;
    unary-expression = unary-operator expression ;
    cast-expression = '(' type-name ')' expression ;
    infix-expression = [ expression / infix-operator ]2+ ;
    assignment-expression = expression '=' expression ;
    grouped-expression = '(' expression ')' ;
    
    literal
      = BOOLEAN
      | INTEGER_SUFFIX
      | FLOAT
      ;
    

    leaf BOOLEAN = 'true' | 'false' ;
    leaf INTEGER = INTEGER INT_SUFFIX 0..2 ;
    INT_SUFFIX = "[uU]" "[lL]"? | "[uU]" "ll|LL" | "[lL]" "[uU]"? | "ll|LL" "[uU]"? ;
    INTEGER = DECIMAL | OCTAL | HEX | BINARY ;
    DECIMAL = "[1-9][0-9]*" ;
    OCTAL = "0[0-7]+" ;
    HEX = "0[xX][0-9a-fA-F]+" ;
    BINARY = "0[bB][0-1]+" ;

    leaf FLOAT = ("[0-9]*[.][0-9]+" | "[0-9]+[.]") "[eE][+-]?[0-9]+"? "[flFL]"? | "[0-9]+" "[eE][+-]?[0-9]+" "[flFL]" ;

    leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;

}
    """.trimIndent()
    val style = """

    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, style, format)


}