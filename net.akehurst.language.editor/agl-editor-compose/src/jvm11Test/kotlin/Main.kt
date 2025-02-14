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

import androidx.compose.ui.window.singleWindowApplication
import net.akehurst.kotlin.compose.editor.ComposableCodeEditor
import net.akehurst.kotlin.compose.editor.api.ComposeCodeEditor
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.LogFunction
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.aglEditorOptions
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import androidx.compose.material3.Surface
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.agl.StyleString
import kotlin.test.Test

class test_AglEditorCompose {

    private companion object {
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

        const val STYLE = """
namespace net.akehurst.language.example
styles SQL {
    ID {
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
    fun runMe() = runBlocking {

        var composeEditor = ComposableCodeEditor()

        val defr = async {
            singleWindowApplication(
                title = "Code Editor Test",
            ) {
                Surface {
                    composeEditor.content()
                }
            }
        }

        val logFunction: LogFunction = { level: LogLevel, prefix: String, message: String, t: Throwable? -> println("$level - $prefix: $message"); t?.printStackTrace() }
        val editorOptions = aglEditorOptions() {
        }
        val editorId = "test"
        val languageId = LanguageIdentity("test")
        val languageService = LanguageServiceDirectExecution(logFunction)

        delay(1000) //wait for compose to start

        val aglEditor = Agl.attachToComposeEditor<Any, Any>(languageService, languageId, editorId, editorOptions, logFunction, composeEditor!!)
        println("Attached AGL")

        aglEditor.languageDefinition.update(
           grammarStr =  GrammarString(GRAMMAR),
            typeModelStr = null,
            asmTransformStr = null,
            crossReferenceStr = null,
            styleStr = StyleString(STYLE)
        )
        aglEditor.refreshProcessor()
        aglEditor.refreshStyleHandler()

        defr.await()

    }

    fun start() {

    }
}