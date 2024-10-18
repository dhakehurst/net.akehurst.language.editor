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

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.editor.information.Example

object AglGrammar {
    val id = "AglGrammar"
    val label = "Agl Grammar"
    val sentence = """
namespace test

grammar Common {
    skip WHITESPACE = "\s+" ;
	skip SINGLE_LINE_COMMENT = "/\*[^*]*\*+([^*/][^*]*\*+)*/" ;
	skip MULTI_LINE_COMMENT = "//[^\n\r]*" ;

    leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;
}

grammar Test extends Common {
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

    leaf type = ID;
}
    """.trimIndent()
    val grammar = Agl.registry.agl.grammar.grammarStr!!

    val references = """
namespace net.akehurst.language.agl.AglGrammar
    identify Namespace by qualifiedName
    identify Grammar by qualifiedName
    scope Namespace {
        identify Grammar by name
    }
    scope Grammar {
        identify GrammarRule by identifier
    }
    
    references {
        in NonTerminal {
            property qualifiedName.front.join refers-to Grammar
            property qualifiedName.last refers-to GrammarRule from qualifiedName.front
        }
    }
""".trimIndent()

    val style = Agl.registry.agl.grammar.styleStr!!
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar.value, references, style.value, format,"")

}