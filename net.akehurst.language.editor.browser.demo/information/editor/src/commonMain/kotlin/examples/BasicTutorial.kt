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

object BasicTutorial {
    val id = "BasicTutorial"
    val label = "Basic Tutorial"
    val sentence = """
// Use this editor to enter a sentence in the defined language

// The result of parsing the sentence, according to the given grammar, forms the Parse-Tree.

target Julian
target George

Hello World !
Hello Julian !
Hello Ann !
Hello George
    """.trimIndent()
    val grammar = """
// use this editor to enter the grammar for your language

// each set of grammars must be defined in a namespace
// one namespace is given for the whole file/grammar-definition
namespace net.akehurst.language.example

// multiple grammars can be defined,
// the last grammar defined is the 'target grammar' - see other examples that illustrate this

grammar BasicTutorial {

    // the default goal/start rule can be defined explicitly,
    // if not defined, the first non skip rule in the last grammar definition is used.  
    @defaultGoalRule document

    // skip rules define text that can appear at any point, such as whitespace and comments
    skip leaf WS = "\s+";
    skip leaf COMMENT = "//[^\r\n]*" ;
    
    // the default goal is a (possibly empty) list of target definitions,
    // followed by a list of (at least one) greeting(s).
    document = targetDefList* greeting+ 
	greeting = hello greetingTargetList '!' ;

    // a greeting target is a list of one or more target references separated by commas (',').
    greetingTargetList = [targetRefOrWorld / ',']+ ;
    
    targetDefList = targetDef ;
    targetDef = 'target' name ;
    leaf hello = 'Hello' ;
    targetRefOrWorld = targetRef | 'World' ;
    targetRef = NAME ;
    leaf NAME = "[a-zA-Z]+" ;
}
""".trimIndent()

    val references = """
// use this editor to enter the scopes and references for your language

// There is a type-model associated with each language, with a namespace for each grammar.
// The type-model is automatically deduced from the grammar rules.
// Instances of the types from the type-model are created as a result of syntax-analysis phase,
// this set of instances forms the Abstract-Syntax-Model/Tree (ASM).
// Scopes and references for a language are defined in the context of the type-model,
// they are resolved as part of the semantic analysis phase, modifying the ASM as a result.

namespace net.akehurst.language.example.BasicTutorial {

    // target definitions are identified by their name
    identify TargetDef by name
    
    references {
        // in every greeting, each target, that is a reference, refers to a target definition
        in Greeting {
            forall greetingTargetList of-type TargetRef {
                property targetRefOrWorld refers-to TargetDef
            }
        }
    }
}
""".trimIndent()

    val style = """
// use this editor to enter the styles, syntax highlighting rules, for your language
// the name of any terminal or non-terminal from the grammar can be used

targetDef {
    background: yellow;
}
targetRef {
  foreground: red;
  font-style: italic;  
}
${"$"}keyword {
  foreground: blue;
  font-style: bold;
}
'hello' {
  foreground: green;
  font-style: bold;
}
""".trimIndent()

    val format = """
// use this editor to enter the scopes and references for your language

""".trimIndent()

    val example = Example(id, label, sentence, grammar, references, style, format)

}