package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object SQL {

    val id = "simple-sql"
    val label = "Simple SQL Queries"

    val sentence = """
SELECT col1 FROM table ;
SELECT col1, col2 FROM table ;
SELECT * FROM table ;

UPDATE table SET col1=1 ;
UPDATE table SET col1=1, col1=2, col3='hello' ;

DELETE FROM table ;

INSERT INTO table ( col1 ) VALUES ( 1 ) ;
INSERT INTO table ( col1, col2 ) VALUES ( 1, 2 ) ;
INSERT INTO table ( col1, col2, col3 ) VALUES ( 1, 2, 'hello' ) ;
    """.trimIndent()

    val grammar = """
namespace net.akehurst.language

grammar SQL {
    skip WS = "\s+" ;

    statementList = terminated-statement+ ;

    terminated-statement = statement ';' ;
    statement
        = select
        | update
        | delete
        | insert
        ;

    select = SELECT columns FROM table-id ;
    update = UPDATE table-id SET column-values ;
    delete = DELETE FROM table-id  ;
    insert = INSERT INTO table-id '(' columns ')' VALUES '(' values ')' ;


    columns = [column-id / ',']+ ;
    column-values = [column-value/ ',']+ ;
    column-value = column-id '=' value ;

    values = [value /',']+ ;
    value
        = INTEGER
        | STRING
        ;

    table-id = ID ;
    column-id = '*' | ID ;

    leaf ID = "[A-Za-z_][A-Za-z0-9_]*" ;
    leaf INTEGER = "[0-9]+" ;
    leaf STRING = "'[^']*'";

    leaf SELECT = "select|SELECT" ;
    leaf UPDATE = "update|UPDATE" ;
    leaf DELETE = "delete|DELETE" ;
    leaf INSERT = "insert|INSERT" ;
    leaf INTO   = "into|INTO"   ;
    leaf SET    = "set|SET"   ;
    leaf FROM   = "from|FROM"   ;
    leaf VALUES = "values|VALUES"   ;
}
    """.trimIndent()

    val style = """
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

    val example = Example(id, label, sentence, grammar, style, format)

}