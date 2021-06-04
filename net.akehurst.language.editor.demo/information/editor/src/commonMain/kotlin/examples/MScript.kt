package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object MScript {

    val id = "mscript"
    val label = "Matlab's Script Language"

    val sentence = """
%{
  Adapted from an example in the MathWorks Script Documentation
  https://uk.mathworks.com/help/matlab/learn_matlab/scripts.html
%}
% Create and plot a sphere with radius r.
[x,y,z] = sphere;       % Create a unit sphere.
r = 2;
surf(x*r,y*r,z*r)       % Adjust each dimension and plot.
axis(equal)             % Use the same scale for each axis. 
 
% Find the surface area and volume.
A = 4*pi*r^2;
V = (4/3)*pi*r^3;
    """.trimIndent()

    val grammar = """
namespace com.yakindu.modelviewer.parser

// This is not the complete Matlab Script language
grammar Mscript {

    script = statementList ;

    // if we treat '\n' as part of the WHITESPACE skip rule, we get ambiguity in statements
    leaf EOL = "(\r?\n)" ;
    skip leaf WHITESPACE = "[ \t\x0B\f]+" ;
    skip leaf LINE_CONTINUATION =  "[.][.][.](.*)(\r?\n)" ;
    skip COMMENT = MULTI_LINE_COMMENT | SINGLE_LINE_COMMENT ;
    leaf MULTI_LINE_COMMENT = "%[{]([^%]|(\r?\n))*%[}]" ;
    leaf SINGLE_LINE_COMMENT = "%([^{\n].*)?(?=(\r?\n))" ;


    statementList = [line / EOL]* ;
    line = statement? ';'?  ;

    statement
      = conditional
      | assignment
      | expressionStatement
      //TODO: others
      ;

    assignment = lhs '=' expression ;
    lhs = rootVariable | matrix ;
    conditional = 'if' expression 'then' statementList 'else' statementList 'end' ;

    expressionStatement = expression ;

    expression
      = rootVariable
      | literal
      | matrix
      | functionCall
      | prefixExpression
      | infixExpression
      | groupExpression
      ;

    groupExpression = '(' expression ')' ;

    functionCall = NAME '(' argumentList ')' ;
    argumentList = [ argument / ',' ]* ;
    argument = expression | COLON ;

    prefixExpression = prefixOperator expression ;
    prefixOperator = '.\'' | '.^' | '\'' | '^' | '+' | '-' | '~' ;

    infixExpression =  [ expression / infixOperator ]2+ ;
    infixOperator
        = '.*' | '*' | './' | '/' | '.\\' | '\\' | '+' | '-' | '^' // arithmetic
        | '==' | '~=' | '>' | '>=' | '<' | '<='                    // relational
        | '&' | '|' | '&&' | '||' | '~'                            // logical
        | ':'
        ;

    matrix = '['  [row / ';']*  ']' ; //strictly speaking ',' and ';' are operators in mscript for array concatination!
    row = expression (','? expression)* ;
    //row = [expression / opCmr ]+ ;
    //opCmr = ','? ;

    literal
      = BOOLEAN
      | number
      | SINGLE_QUOTE_STRING
      | DOUBLE_QUOTE_STRING
      ;

    rootVariable = NAME ;

    number = INTEGER | REAL ;

    leaf NAME = "[a-zA-Z_][a-zA-Z_0-9]*" ;

    leaf COLON               = ':' ;
    leaf BOOLEAN             = 'true' | 'false' ;
    leaf INTEGER             = "([+]|[-])?[0-9]+" ;
    leaf REAL                = "[-+]?[0-9]*[.][0-9]+([eE][-+]?[0-9]+)?" ;
    leaf SINGLE_QUOTE_STRING = "'([^'\\]|\\.)*'" ;
    leaf DOUBLE_QUOTE_STRING = "\"([^\"\\]|\\.)*\"" ;

}
    """.trimIndent()

    val style = """
NAME {
  foreground: red;
  font-style: bold;
}
SINGLE_LINE_COMMENT {
  foreground: green;
}
MULTI_LINE_COMMENT {
  foreground: grey;
}
matrix {
    foreground: blue;
}
    """.trimIndent()

    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, style, format)

}