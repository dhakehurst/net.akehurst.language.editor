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

object SysML_2 {
    val id = "SysML_2"
    val label = "SysML v2"
    val sentence = """
    """.trimIndent()
    val grammar = """
namespace net.akehurst.language.example

grammar SysML_2 {
    DEFINED_BY  = ':'   | 'defined' 'by' ;
    SPECIALIZES = ':>'  | 'specializes' ;
    SUBSETS     = ':>'  | 'subsets' ;
    REFERENCES  = '::>' | 'references' ;
    REDEFINES   = ':>>' | 'redefines';
    
    NAME = "" ;
    
    Identification = ( '<' declaredShortName '>' )? declaredName? ;
    declaredShortName = NAME ;
    declaredName = NAME ;
    RelationshipBody = ';' | '{' OwnedAnnotation* '}' ;
  
    Dependency = PrefixMetadataAnnotation* 'dependency' DependencyDeclaration RelationshipBody ;
    
    DependencyDeclaration = ( Identification 'from' )? [QualifiedName / ',' ]+ 'to' [QualifiedName / ',' ]+ ;

    Annotation = QualifiedName ;
    OwnedAnnotation = AnnotatingElement ;
    AnnotatingMember = AnnotatingElement ;
    AnnotatingElement = Comment | Documentation | TextualRepresentation | MetadataFeature ;
    
    Comment = 'comment' Identification ( 'about' [Annotation / ',']+ )? REGULAR_COMMENT ;
    Documentation = 'doc' Identification REGULAR_COMMENT ;
    
    TextualRepresentation = ( 'rep' Identification )? 'language' STRING_VALUE REGULAR_COMMENT ;
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