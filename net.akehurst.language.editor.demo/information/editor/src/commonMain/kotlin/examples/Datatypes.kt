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

object Datatypes {
    val id = "datatypes"
    val label = "Datatype"
    val sentence = """
        primitive String
        //primitive Date
        collection List<E>
        class Person {
            name: String
            dob: Date
            friends: List<Person>
        }
        class class {
            prop: String
        }
    """.trimIndent()
    val grammar = """
        namespace test

        grammar Test {
            skip leaf WS = "\s+" ;
            skip leaf COMMENT = "//[^\n]*(\n)" ;
        
            unit = declaration* ;
            declaration = datatype | primitive | collection ;
            primitive = 'primitive' ID ;
            collection = 'collection' ID typeParameters? ;
            typeParameters = '<' typeParameterList '>' ;
            typeParameterList = [ID / ',']+ ;
            datatype = 'class' ID '{' property* '}' ;
            property = ID ':' typeReference ;
            typeReference = type typeArguments? ;
            typeArguments = '<' typeArgumentList '>' ;
            typeArgumentList = [typeReference / ',']+ ;
        
            leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;
            leaf type = ID;
        }
    """.trimIndent()

    val references = """
        identify unit by §nothing
        scope unit {
            identify primitive by ID
            identify datatype by ID
            identify collection by ID
        }
        references {
            in typeReference property type refers-to primitive|datatype|collection
        }
    """.trimIndent()

    val style = """
        'primitive' {
          foreground: purple;
          font-style: bold;
        }
        'collection' {
          foreground: purple;
          font-style: bold;
        }
        'class' {
          foreground: purple;
          font-style: bold;
        }
        ID {
          foreground: blue;
          font-style: italic;
        }
        '{' {
          foreground: darkgreen;
          font-style: bold;
        }
        '}' {
          foreground: darkgreen;
          font-style: bold;
        }
        property {
          background: lightgray;
        }
        typeReference {
          foreground: green;
          background: lightblue;
        }
    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, "unit", sentence, grammar, references, style, format)

}