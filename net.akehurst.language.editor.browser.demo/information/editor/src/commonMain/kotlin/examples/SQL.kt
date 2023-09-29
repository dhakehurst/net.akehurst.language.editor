package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object SQL {

    val id = "simple-sql"
    val label = "Simple SQL Queries"

    val sentence = """
CREATE TABLE table (
    col1 int,
    col2 int,
    col3 varchar(255)
);

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
    """.trimIndent()

    val references = """
identify TableDefinition by table-id
scope TableDefinition {
    identify ColumnDefinition by column-id
}
references {
    in TableRef property ref refers-to TableDefinition
}
    """.trimIndent()

    val style = """
ID {
  foreground: blue;
  font-style: italic;
}
CREATE {
  foreground: chocolate;
  font-style: bold;
}
TABLE {
  foreground: chocolate;
  font-style: bold;
}
SELECT {
  foreground: chocolate;
  font-style: bold;
}
UPDATE {
  foreground: chocolate;
  font-style: bold;
}
DELETE {
  foreground: chocolate;
  font-style: bold;
}
INSERT {
  foreground: chocolate;
  font-style: bold;
}
INTO {
  foreground: chocolate;
  font-style: bold;
}
FROM {
  foreground: chocolate;
  font-style: bold;
}
VALUES {
  foreground: chocolate;
  font-style: bold;
}
SET {
  foreground: chocolate;
  font-style: bold;
}
    """.trimIndent()

    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, references, style, format)

}