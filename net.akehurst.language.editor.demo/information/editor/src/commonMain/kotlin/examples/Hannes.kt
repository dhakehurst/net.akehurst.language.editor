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

object Hannes {
    val id = "hannes"
    val label = "Hannes"
    val sentence = """
program test 
begin
    var x = 1
    x+465
    34+5*8
    (4+5)*54+x
end
    """.trimIndent()
    val grammar = """
namespace test

grammar Hannes {
    skip leaf WS = "\s+" ;
    skip leaf COMMENT = "//[^\n]*(\n)" ;

    Program =
        'program' ID
        'begin'
            VariableDeclaration*
            Expression*
        'end'
    ;

    VariableDeclaration = 'var' ID '=' Expression;
    Expression = AddExpression ;
    AddExpression = [ MultiplyExpression / '+' ]+ ;
    MultiplyExpression = [ AtomicExpression / '*' ]+ ;
    AtomicExpression
        = StringLiteral
        | NumberLiteral
        | BooleanLiteral
        | VariableReference
        | GroupedExpression
        ;
    GroupedExpression = '(' Expression ')' ;
    
    VariableReference = ID;
    leaf NumberLiteral = NUMBER ;
    leaf BooleanLiteral = 'true' | 'false' ;
    leaf StringLiteral = STRING;

    leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;
    leaf NUMBER = "[0-9]+([.][0-9]+)?" ;
    leaf STRING = SINGLE_QUOTE_STRING | DOUBLE_QUOTE_STRING ;
     SINGLE_QUOTE_STRING = "'([^'\\]|\\.)*'" ;
     DOUBLE_QUOTE_STRING = "\"([^\"\\]|\\.)*\"" ;
}
    """.trimIndent()

    val references = """
identify VariableDeclaration by ID
references {
    in VariableReference property ID refers-to VariableDeclaration
}
    """.trimIndent()

    val style = """
        ${"$"}keyword {
          foreground: purple;
          font-style: bold;
        }
        ID {
          foreground: blue;
          font-style: italic;
        }
        AtomicExpression {
          foreground: darkgreen;
          font-style: bold;
        }
        VariableReference {
          foreground: green;
          background: lightblue;
        }
        '(',')','+','*' {
          foreground: purple;
          font-style: bold;
        }
    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, "unit", sentence, grammar, references, style, format)

}