/**
 * Copyright (C) 2025 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.compose

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.akehurst.kotlin.compose.editor.ComposableCodeEditor2
import net.akehurst.kotlin.compose.editor.ComposableCodeEditor3
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.simple.ContextAsmSimple
import net.akehurst.language.api.processor.CrossReferenceString
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.editor.common.EditorOptionsDefault
import net.akehurst.language.editor.common.aglEditorOptions
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import net.akehurst.language.grammar.processor.AglGrammar
import kotlin.test.Test

class test_AglEditorCompose {

    private companion object {
        const val INITIAL_TEXT = """SELECT col1 FROM table ;
SELECT col1, col2 FROM table ;
SELECT * FROM table ;

UPDATE table SET col1=1 ;
UPDATE table SET col1=1, col1=2, col3='hello' ;

DELETE FROM table ;

INSERT INTO table ( col1 ) VALUES ( 1 ) ;
INSERT INTO table ( col1, col2 ) VALUES ( 1, 2 ) ;
INSERT INTO table ( col1, col2, col3 ) VALUES ( 1, 2, 'hello' ) ;

CREATE TABLE table ( col1  Int, col2 Int, col3 Int ) ;            
        """

        const val GRAMMAR = """
namespace net.akehurst.language.example

grammar SQL {
    skip WS = "\s+" ;
    skip leaf COMMENT_SINGLE_LINE = "//[^\r\n]*" ;
    skip leaf COMMENT_MULTI_LINE = "/\*[^*]*\*+([^*/][^*]*\*+)*/" ;

    statementList = terminatedStatement+ ;

    terminatedStatement = statement ';' ;
    statement
        = select
        | update
        | delete
        | insert
        | tableDefinition
        ;

    select = SELECT columns FROM tableRef ;
    update = UPDATE tableRef SET columnValueList ;
    delete = DELETE FROM tableRef  ;
    insert = INSERT INTO tableRef '(' columns ')' VALUES '(' values ')' ;

    columns = [columnRefOrAny / ',']+ ;
    columnValueList = [columnValue/ ',']+ ;
    columnValue = columnRef '=' value ;

    values = [value /',']+ ;
    value
        = INTEGER
        | STRING
        ;

    tableDefinition = CREATE TABLE table-id '(' columnDefinitionList ')' ;
    columnDefinitionList = [columnDefinition / ',']+ ;
    columnDefinition = column-id datatype-ref datatype-size? ;
    datatype-size = '(' INTEGER ')' ;

    columnRefOrAny = columnAny | columnRef ;

    tableRef = REF ;
    columnRef = REF ;
    columnAny = '*' ;

    leaf table-id = ID ;
    leaf column-id = ID ;
    leaf REF = ID ;
    leaf datatype-ref = ID ;
    leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;
    leaf INTEGER = "[0-9]+" ;
    leaf STRING = "'[^']*'";

    leaf CREATE = "create|CREATE" ;
    leaf TABLE  = "table|TABLE" ;
    leaf SELECT = "select|SELECT" ;
    leaf UPDATE = "update|UPDATE" ;
    leaf DELETE = "delete|DELETE" ;
    leaf INSERT = "insert|INSERT" ;
    leaf INTO   = "into|INTO"   ;
    leaf SET    = "set|SET"   ;
    leaf FROM   = "from|FROM"   ;
    leaf VALUES = "values|VALUES"   ;
}
        """

        const val CROSS_REFERENCE = """
namespace net.akehurst.language.example.SQL
    identify TableDefinition by table-id
    scope TableDefinition {
        identify ColumnDefinition by column-id
    }
    references {
        in Select {
            property tableRef.ref refers-to TableDefinition
            forall columns of-type ColumnRef {
                property ref refers-to ColumnDefinition from tableRef.ref
            }
        }
        in Update {
            property tableRef.ref refers-to TableDefinition
            forall columnValueList {
                property columnRef.ref refers-to ColumnDefinition from tableRef.ref
            }
        }
        in Insert {
            property tableRef.ref refers-to TableDefinition
            forall columns of-type ColumnRef {
                property ref refers-to ColumnDefinition from tableRef.ref
            }
        }
    }            
        """

        const val STYLE = """
namespace net.akehurst.language.example
styles SQL {
    ID,table-id,column-id {
      foreground: red;
      font-style: normal;
    }
    REF {
      foreground: blue;
      font-style: italic;
    }    
    CREATE,TABLE,SELECT,UPDATE,DELETE,INSERT,INTO,FROM,VALUES,SET {
      foreground: chocolate;
      font-style: bold;
    }
}
        """
    }

    @Test
    fun run_AglComposeTextEditor() = runBlocking {
        val aglEditor = AglComposeTextEditor()
        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    aglEditor.content()
                }
            }
        }
        defr.await()
    }

    @Test
    fun run_AglComposeTextEditor_SQL() = runBlocking {
        val aglEditor = AglComposeTextEditor(
            initialText = INITIAL_TEXT,
            grammarString = GrammarString(GRAMMAR),
            styleString = StyleString(STYLE)
        )
        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    aglEditor.content()
                }
            }
        }
        defr.await()
    }

    @Test
    fun run_ComposableCodeEditor2() = runBlocking {

        var composeEditor = ComposableCodeEditor2()

        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    composeEditor.content()
                }
            }
        }

        val logFunction: LogFunction = { level, prefix, t, message -> println("$level - $prefix: ${message()}"); t?.printStackTrace() }
        val editorOptions = aglEditorOptions() {
        }
        val editorId = "test"
        val languageId = LanguageIdentity("test")
        val languageDefinition = Agl.registry.findOrPlaceholder(
            languageId,
            aglOptions = Agl.options { },
            configuration = Agl.configurationSimple()
        )
        val languageService = LanguageServiceDirectExecution(logFunction)

        delay(1000) //wait for compose to start

        val aglEditor = Agl.attachToComposeEditor(
            languageService, languageDefinition,
            { Agl.options { semanticAnalysis { context(ContextAsmSimple()) } } },
            editorId, editorOptions, logFunction, composeEditor!!
        )
        println("Attached AGL")

        defr.await()

    }

    @Test
    fun run_ComposableCodeEditor2b() = runBlocking {

        var composeEditor = ComposableCodeEditor3(
            initialText = INITIAL_TEXT,
        )

        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    composeEditor.content(autocompleteModifier = Modifier.widthIn(100.dp, 400.dp).heightIn(30.dp, 300.dp))
                }
            }
        }

        val logFunction: LogFunction = { level, prefix, t, message -> println("$level - $prefix: ${message()}"); t?.printStackTrace() }
        val editorOptions = EditorOptionsDefault()
        val editorId = "test"
        val languageId = LanguageIdentity("test")
        val languageDefinition = Agl.languageDefinitionFromStringSimple(
            languageId,
            grammarDefinitionStr = GrammarString(GRAMMAR),
            referenceStr = CrossReferenceString(CROSS_REFERENCE),
            styleStr = StyleString(STYLE)
        )
        val languageService = LanguageServiceDirectExecution(logFunction)

        delay(1000) //wait for compose to start

        val aglEditor = Agl.attachToComposeEditor(
            languageService, languageDefinition,
            { Agl.options { semanticAnalysis { context(ContextAsmSimple()) } } },
            editorId, editorOptions, logFunction, composeEditor!!
        )

        println("Attached AGL")

        defr.await()

    }

    @Test
    fun run_ComposableCodeEditor2c() = runBlocking {

        var composeEditor = ComposableCodeEditor2(
            initialText = """
                namespace test
                grammar Test {
                  S = 'a' ;
                }
            """.trimIndent(),
        )

        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    composeEditor.content(autocompleteModifier = Modifier.width(400.dp).height(300.dp))
                }
            }
        }

        val logFunction: LogFunction = { level, prefix, t, message -> println("$level - $prefix: ${message()}"); t?.printStackTrace() }
        val editorOptions = aglEditorOptions() {
        }
        val editorId = "test"
        val languageId = Agl.registry.agl.grammarLanguageIdentity
        val languageDefinition = Agl.languageDefinitionFromString<Any, Any>(
            languageId,
            grammarDefinitionStr = GrammarString(GRAMMAR),
            styleStr = StyleString(STYLE)
        )
        val languageService = LanguageServiceDirectExecution(logFunction)

        delay(1000) //wait for compose to start

        val aglEditor = Agl.attachToComposeEditor(
            languageService, languageDefinition,
            { Agl.options { semanticAnalysis { context(ContextAsmSimple()) } } },
            editorId,
            editorOptions, logFunction, composeEditor!!
        )
        println("Attached AGL")

        aglEditor.updateLanguageDefinitionWith(
            grammarStr = GrammarString(AglGrammar.grammarString),
            styleStr = StyleString(AglGrammar.styleString)
        )

        defr.await()

    }

}