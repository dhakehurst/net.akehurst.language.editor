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

object Java8 {
    val id = "Java8"
    val label = "Java 8"
    val sentence = """
        class Person {
            private String name;
            private Date dob;
            private List<Person> friends;
        }
    """.trimIndent()
    val grammar = """
/*
 * derived from [https://docs.oracle.com/javase/specs/jls/se8/html/jls-19.html]
 */
namespace net.akehurst.language.java8

grammar Base {
    skip leaf WHITESPACE = "\s+" ;
    skip leaf COMMENT_SINGLE_LINE = "//[^\n]*${'$'}" ;
    skip leaf COMMENT_MULTI_LINE = "/\*[^*]*\*+([^*/][^*]*\*+)*/" ;

    leaf IDENTIFIER = JAVA_LETTER JAVA_LETTER_OR_DIGIT* ;
    leaf JAVA_LETTER_OR_DIGIT = JAVA_LETTER | "[0-9]" ;
    leaf JAVA_LETTER= UNICODE_LETTER | '${'$'}' | '_' ;
    leaf UNICODE_LETTER = "[A-Za-z]" ; //TODO: add unicode chars !

    QualifiedName = [ IDENTIFIER / '.' ]+ ;

    TypeName = QualifiedName ;
    PackageOrTypeName = QualifiedName ;
    ExpressionName = QualifiedName ;
    MethodName = IDENTIFIER;
    PackageName = QualifiedName ;
    AmbiguousName = QualifiedName ;
}

grammar Literals {
    Literal
      = INTEGER_LITERAL
      | FLOATING_POINT_LITERAL
      | BOOLEAN_LITERAL
      | CHARACTER_LITERAL
      | STRING_LITERAL
      | NULL_LITERAL
      ;

    leaf INTEGER_LITERAL
      = HEX_NUMERAL
      | OCT_NUMERAL
      | BINARY_NUMERAL
      | DECIMAL_NUMERAL
      ;

    leaf DECIMAL_NUMERAL = "(0|[1-9]([0-9_]*[0-9])?)" INTEGER_TYPE_SUFFIX? ;
    leaf HEX_NUMERAL     = "0[xX][0-9a-fA-F]([0-9a-fA-F_]*[0-9a-fA-F])?" INTEGER_TYPE_SUFFIX? ;
    leaf OCT_NUMERAL     = "0_*[0-7]([0-7_]*[0-7])?" INTEGER_TYPE_SUFFIX? ;
    leaf BINARY_NUMERAL  = "0[bB][01]([01_]*[01])?" INTEGER_TYPE_SUFFIX? ;

    leaf INTEGER_TYPE_SUFFIX = 'l' | 'L' ;

    leaf FLOATING_POINT_LITERAL
     = "[0-9]([eE][+-]?[0-9]+)?" "[fdFD]"
     | "[0-9]*.[0-9]*" "[eE][+-]?(0|[1-9])+"? "[fdFD]"?
     ;

    leaf BOOLEAN_LITERAL   = 'true' | 'false' ;
    leaf CHARACTER_LITERAL = "'" ("[^'\r\n\\]" | ESCAPE_SEQUENCE) "'" ;
    leaf ESCAPE_SEQUENCE
        = '\\' "[btnfr\x27\\]"
        | '\\' ("[0-3]"? "[0-7]")? "[0-7]"
        | '\\' 'u'+ "[0-9a-fA-F]{4}"
        ;
    leaf STRING_LITERAL    = "\"([^\"\\]|\\.)*\"" ;
    leaf NULL_LITERAL      = 'null' ;
}

grammar Annotations extends Base, Literals {
    Annotation = NormalAnnotation | MarkerAnnotation | SingleElementAnnotation ;
    NormalAnnotation = '@' TypeName '(' ElementValuePairList ')' ;
    ElementValuePairList = [ ElementValuePair / ',' ]* ;
    ElementValuePair = IDENTIFIER '=' ElementValue ;
    //ElementValue = ConditionalExpression | ElementValueArrayInitializer | Annotation ;
    // overridden in expressions
    ElementValue = Literal | ElementValueArrayInitializer | Annotation ;
    ElementValueArrayInitializer = '{' ElementValueList ','? '}' ;
    ElementValueList = [ ElementValue / ',' ]* ;
    MarkerAnnotation = '@' TypeName ;
    SingleElementAnnotation = '@' TypeName '(' ElementValue ')' ;
}

grammar Types extends Annotations {
    Type = ReferenceType < PrimitiveType ;
    PrimitiveType = Annotation* NumericType | Annotation* 'boolean' ;
    NumericType = IntegralType | FloatingPointType ;
    IntegralType = 'byte' | 'short' | 'int' | 'long' | 'char' ;
    FloatingPointType = 'float' | 'double' ;

    ReferenceType = ClassOrInterfaceType | TypeVariable | ArrayType ;
    ClassOrInterfaceType = ClassType | InterfaceType ;
    ClassType
      = Annotation* IDENTIFIER TypeArguments?
      | ClassOrInterfaceType '.' Annotation* IDENTIFIER TypeArguments?
      ;
    InterfaceType = ClassType ;
    TypeVariable = Annotation* IDENTIFIER ;
    ArrayType = PrimitiveType Dims | ClassOrInterfaceType Dims | TypeVariable Dims ;
    Dims = (Annotation* '[' ']')+ ;

    TypeParameter = TypeParameterModifier* IDENTIFIER TypeBound? ;
    TypeParameterModifier = Annotation ;
    TypeBound = 'extends' TypeVariable | 'extends' ClassOrInterfaceType AdditionalBound? ;
    AdditionalBound = '&' InterfaceType ;

    TypeArguments = '<' TypeArgumentList '>' ;
    TypeArgumentList = [ TypeArgument / ',']+ ;
    TypeArgument = ReferenceType | Wildcard ;
    Wildcard = Annotation* '?' WildcardBounds? ;
    WildcardBounds = 'extends' ReferenceType | 'super' ReferenceType ;

    UnannType = UnannPrimitiveType | UnannReferenceType ;
    UnannPrimitiveType = NumericType | 'boolean' ;
    UnannReferenceType = UnannClassOrInterfaceType | UnannTypeVariable | UnannArrayType ;
    UnannClassOrInterfaceType = UnannClassType | UnannInterfaceType ;
    UnannClassType
      = IDENTIFIER TypeArguments?
      | UnannClassOrInterfaceType '.' Annotation* IDENTIFIER TypeArguments?
      ;
    UnannInterfaceType = UnannClassType ;
    UnannTypeVariable = IDENTIFIER ;
    UnannArrayType
      = UnannPrimitiveType Dims
      | UnannClassOrInterfaceType Dims
      | UnannTypeVariable Dims
      ;
}

grammar Expressions extends Types {

    // from Annotations
    override ElementValue = ConditionalExpression | ElementValueArrayInitializer | Annotation ;

    Primary = PrimaryNoNewArray | ArrayCreationExpression ;
    // overridden in classes
    PrimaryNoNewArray
      = Literal
      | ClassLiteral
      | 'this'
      | TypeName '.' 'this'
      | '(' Expression ')'
      | ClassInstanceCreationExpression
      | FieldAccess
      | ArrayAccess
      | MethodInvocation
      | MethodReference
      ;
    ClassLiteral
      = TypeName ('[' ']')* '.' 'class'
      | NumericType ('[' ']')* '.' 'class'
      | 'boolean' ('[' ']')* '.' 'class'
      | 'void' '.' 'class'
      ;
    ClassInstanceCreationExpression
      = UnqualifiedClassInstanceCreationExpression
      | ExpressionName '.' UnqualifiedClassInstanceCreationExpression
      | Primary '.' UnqualifiedClassInstanceCreationExpression
      ;
    UnqualifiedClassInstanceCreationExpression
      = 'new' TypeArguments? ClassOrInterfaceTypeToInstantiate '(' ArgumentList? ')' ClassBody? ;
    // overridden in Classes
    ClassBody = '{' '}' ;
    ClassOrInterfaceTypeToInstantiate = Annotation* IDENTIFIER ('.' Annotation* IDENTIFIER)* TypeArgumentsOrDiamond? ;
    TypeArgumentsOrDiamond = TypeArguments | '<>' ;
    FieldAccess
      = Primary '.' IDENTIFIER
      | 'super' '.' IDENTIFIER
      | TypeName '.' 'super' '.' IDENTIFIER
      ;
    ArrayAccess
      = ExpressionName '[' Expression ']'
      | PrimaryNoNewArray '[' Expression ']'
      ;
    MethodInvocation
      = MethodName '(' ArgumentList? ')'
      | TypeName '.' TypeArguments? IDENTIFIER '(' ArgumentList? ')'
      | ExpressionName '.' TypeArguments? IDENTIFIER '(' ArgumentList? ')'
      | Primary '.' TypeArguments? IDENTIFIER '(' ArgumentList? ')'
      | 'super' '.' TypeArguments? IDENTIFIER '(' ArgumentList? ')'
      | TypeName '.' 'super' '.' TypeArguments? IDENTIFIER '(' ArgumentList? ')'
      ;
    ArgumentList = Expression (',' Expression)* ;
    MethodReference
      = ExpressionName '::' TypeArguments? IDENTIFIER
      | ReferenceType '::' TypeArguments? IDENTIFIER
      | Primary '::' TypeArguments? IDENTIFIER
      | 'super' '::' TypeArguments? IDENTIFIER
      | TypeName '.' 'super' '::' TypeArguments? IDENTIFIER
      | ClassType '::' TypeArguments? 'new'
      | ArrayType '::' 'new'
      ;
    ArrayCreationExpression
      = 'new' PrimitiveType DimExprs Dims?
      | 'new' ClassOrInterfaceType DimExprs Dims?
      | 'new' PrimitiveType Dims ArrayInitializer
      | 'new' ClassOrInterfaceType Dims ArrayInitializer
      ;

    ArrayInitializer = '{' VariableInitializerList ','? '}' ;
    VariableInitializerList = [ VariableInitializer / ',' ]* ;
    VariableInitializer = Expression | ArrayInitializer ;

    DimExprs = DimExpr DimExpr* ;
    DimExpr = Annotation* '[' Expression ']' ;
    Expression = LambdaExpression | AssignmentExpression ;
    LambdaExpression = LambdaParameters '->' LambdaBody ;
    LambdaParameters
      = IDENTIFIER
      | '(' FormalParameterList? ')'
      | '(' InferredFormalParameterList ')'
      ;
    InferredFormalParameterList = IDENTIFIER (',' IDENTIFIER)* ;

    FormalParameterList = ReceiverParameter | FormalParameters ',' LastFormalParameter | LastFormalParameter ;
    FormalParameters
      = FormalParameter (',' FormalParameter)*
      | ReceiverParameter (',' FormalParameter)*
      ;
    FormalParameter = VariableModifier* UnannType VariableDeclaratorId ;
    VariableModifier = Annotation | 'final' ;
    LastFormalParameter
      = VariableModifier* UnannType Annotation* '...' VariableDeclaratorId
      | FormalParameter
      ;
    ReceiverParameter = Annotation* UnannType (IDENTIFIER '.')? 'this' ;
    VariableDeclaratorId = IDENTIFIER Dims? ;
    VariableDeclaratorList = [ VariableDeclarator / ',' ]+ ;
    VariableDeclarator = VariableDeclaratorId ('=' VariableInitializer)? ;

    // overridden in BlocksAndStatements
    //LambdaBody = Expression | Block ;
    LambdaBody = Expression ;
    AssignmentExpression = ConditionalExpression | Assignment ;
    Assignment = LeftHandSide AssignmentOperator Expression ;
    LeftHandSide = ExpressionName | FieldAccess | ArrayAccess ;
    AssignmentOperator = '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' ;

    ConditionalExpression
      = ConditionalOrExpression
      | ConditionalOrExpression '?' Expression ':' ConditionalExpression
      | ConditionalOrExpression '?' Expression ':' LambdaExpression
      ;
    ConditionalOrExpression
      = ConditionalAndExpression
      | ConditionalOrExpression '||' ConditionalAndExpression
      ;
    ConditionalAndExpression
      = InclusiveOrExpression
      | ConditionalAndExpression '&&' InclusiveOrExpression
      ;
    InclusiveOrExpression
      = ExclusiveOrExpression
      | InclusiveOrExpression '|' ExclusiveOrExpression
      ;
    ExclusiveOrExpression
      = AndExpression
      | ExclusiveOrExpression '^' AndExpression
      ;
    AndExpression
      = EqualityExpression
      | AndExpression '&' EqualityExpression
      ;
    EqualityExpression
      = RelationalExpression
      | EqualityExpression '==' RelationalExpression
      | EqualityExpression '!=' RelationalExpression
      ;
    RelationalExpression
      = ShiftExpression
      | RelationalExpression '<' ShiftExpression
      | RelationalExpression '>' ShiftExpression
      | RelationalExpression '<=' ShiftExpression
      | RelationalExpression '>=' ShiftExpression
      | RelationalExpression 'instanceof' ReferenceType
      ;
    ShiftExpression
      = AdditiveExpression
      | ShiftExpression '<<' AdditiveExpression
      | ShiftExpression '>>' AdditiveExpression
      | ShiftExpression '>>>' AdditiveExpression
      ;
    AdditiveExpression
      = MultiplicativeExpression
      | AdditiveExpression '+' MultiplicativeExpression
      | AdditiveExpression '-' MultiplicativeExpression
      ;
    MultiplicativeExpression
      = UnaryExpression
      | MultiplicativeExpression '*' UnaryExpression
      | MultiplicativeExpression '/' UnaryExpression
      | MultiplicativeExpression '%' UnaryExpression
      ;
    UnaryExpression
      = PreIncrementExpression
      | PreDecrementExpression
      | '+' UnaryExpression
      | '-' UnaryExpression
      | UnaryExpressionNotPlusMinus
      ;
    PreIncrementExpression = '++' UnaryExpression ;
    PreDecrementExpression = '--' UnaryExpression ;
    UnaryExpressionNotPlusMinus
      = PostfixExpression
      | '~' UnaryExpression
      | '!' UnaryExpression
      | CastExpression
      ;
    PostfixExpression
      = Primary
      | ExpressionName
      | PostIncrementExpression
      | PostDecrementExpression
      ;
    PostIncrementExpression = PostfixExpression '++' ;
    PostDecrementExpression = PostfixExpression '--' ;
    CastExpression
      = '(' PrimitiveType ')' UnaryExpression
      | '(' ReferenceType AdditionalBound* ')' UnaryExpressionNotPlusMinus
      | '(' ReferenceType AdditionalBound* ')' LambdaExpression
      ;
    ConstantExpression = Expression ;
}

grammar BlocksAndStatements extends Expressions {

    // from Expressions
    override LambdaBody = Expression | Block ;

    Block = '{' BlockStatements '}' ;
    BlockStatements = BlockStatement* ;
    // overridden in classes
    BlockStatement = LocalVariableDeclarationStatement | Statement ;
    LocalVariableDeclarationStatement = LocalVariableDeclaration ';' ;
    LocalVariableDeclaration = VariableModifier* UnannType VariableDeclaratorList ;
    Statement
      = StatementWithoutTrailingSubstatement
      | LabeledStatement
      | IfThenStatement
      | IfThenElseStatement
      | WhileStatement
      | ForStatement
      ;
    StatementNoShortIf
      = StatementWithoutTrailingSubstatement
      | LabeledStatementNoShortIf
      | IfThenElseStatementNoShortIf
      | WhileStatementNoShortIf
      | ForStatementNoShortIf
      ;
    StatementWithoutTrailingSubstatement
      = Block
      | EmptyStatement
      | ExpressionStatement
      | AssertStatement
      | SwitchStatement
      | DoStatement
      | BreakStatement
      | ContinueStatement
      | ReturnStatement
      | SynchronizedStatement
      | ThrowStatement
      | TryStatement
      ;
    EmptyStatement = ';' ;
    LabeledStatement = IDENTIFIER ':' Statement ;
    LabeledStatementNoShortIf = IDENTIFIER ':' StatementNoShortIf ;
    ExpressionStatement = StatementExpression ';' ;
    StatementExpression
      = Assignment
      | PreIncrementExpression
      | PreDecrementExpression
      | PostIncrementExpression
      | PostDecrementExpression
      | MethodInvocation
      | ClassInstanceCreationExpression
      ;
    IfThenStatement = 'if' '(' Expression ')' Statement ;
    IfThenElseStatement = 'if' '(' Expression ')' StatementNoShortIf 'else' Statement ;
    IfThenElseStatementNoShortIf = 'if' '(' Expression ')' StatementNoShortIf 'else' StatementNoShortIf ;
    AssertStatement
     = 'assert' Expression ';'
     | 'assert' Expression ':' Expression ';'
     ;
    SwitchStatement = 'switch' '(' Expression ')' SwitchBlock ;
    SwitchBlock = '{' SwitchBlockStatementGroup* SwitchLabel* '}' ;
    SwitchBlockStatementGroup = SwitchLabels BlockStatements ;
    SwitchLabels = SwitchLabel SwitchLabel* ;
    SwitchLabel
      = 'case' ConstantExpression ':'
      | 'case' EnumConstantName ':'
      | 'default' ':'
      ;
    EnumConstantName = IDENTIFIER ;
    WhileStatement = 'while' '(' Expression ')' Statement ;
    WhileStatementNoShortIf = 'while' '(' Expression ')' StatementNoShortIf ;
    DoStatement = 'do' Statement 'while' '(' Expression ')' ';' ;
    ForStatement = BasicForStatement | EnhancedForStatement ;
    ForStatementNoShortIf = BasicForStatementNoShortIf | EnhancedForStatementNoShortIf ;
    BasicForStatement = 'for' '(' ForInit? ';' Expression? ';' ForUpdate? ')' Statement ;
    BasicForStatementNoShortIf = 'for' '(' ForInit? ';' Expression? ';' ForUpdate? ')' StatementNoShortIf ;
    ForInit = StatementExpressionList | LocalVariableDeclaration ;
    ForUpdate = StatementExpressionList ;
    StatementExpressionList = StatementExpression (',' StatementExpression)* ;
    EnhancedForStatement = 'for' '(' VariableModifier* UnannType VariableDeclaratorId ':' Expression ')' Statement ;
    EnhancedForStatementNoShortIf = 'for' '(' VariableModifier* UnannType VariableDeclaratorId ':' Expression ')' StatementNoShortIf ;
    BreakStatement = 'break' IDENTIFIER? ';' ;
    ContinueStatement = 'continue' IDENTIFIER? ';' ;
    ReturnStatement = 'return' Expression? ';' ;
    ThrowStatement = 'throw' Expression ';' ;
    SynchronizedStatement = 'synchronized' '(' Expression ')' Block ;
    TryStatement
      = 'try' Block Catches
      | 'try' Block Catches? Finally
      | TryWithResourcesStatement
      ;
    Catches = CatchClause CatchClause* ;
    CatchClause = 'catch' '(' CatchFormalParameter ')' Block ;
    CatchFormalParameter = VariableModifier* CatchType VariableDeclaratorId ;
    CatchType = UnannClassType ('|' ClassType)* ;
    Finally = 'finally' Block ;
    TryWithResourcesStatement = 'try' ResourceSpecification Block Catches? Finally? ;
    ResourceSpecification = '(' ResourceList ';'? ')' ;
    ResourceList = Resource (';' Resource)* ;
    Resource = VariableModifier* UnannType VariableDeclaratorId '=' Expression ;
}

grammar Classes extends BlocksAndStatements {

    // from BlocksAndStatements
    override BlockStatement = LocalVariableDeclarationStatement | ClassDeclaration | Statement ;
    // from Expressions
    override ClassBody = '{' ClassBodyDeclaration* '}' ;

    ClassDeclaration = NormalClassDeclaration | EnumDeclaration ;
    NormalClassDeclaration = ClassModifier* 'class' IDENTIFIER TypeParameters? Superclass? Superinterfaces? ClassBody ;
    ClassModifier = Annotation | 'public' | 'protected' | 'private' | 'abstract' | 'static' | 'final' | 'strictfp' ;
    TypeParameters = '<' TypeParameterList '>' ;
    TypeParameterList = [ TypeParameter / ',' ]+ ;
    Superclass = 'extends' ClassType ;
    Superinterfaces = 'implements' InterfaceTypeList ;
    InterfaceTypeList = [ InterfaceType / ',' ]+ ;

    ClassBodyDeclaration
       = ClassMemberDeclaration
       | InstanceInitializer
       | StaticInitializer
       | ConstructorDeclaration
       ;
    // overridden in Interfaces
    ClassMemberDeclaration
       = FieldDeclaration
       | MethodDeclaration
       | ClassDeclaration
       | ';'
       ;
    FieldDeclaration = FieldModifier* UnannType VariableDeclaratorList ';' ;
    FieldModifier = Annotation | 'public' | 'protected' | 'private' | 'static' | 'final' | 'transient' | 'volatile' ;

    MethodDeclaration = MethodModifier* MethodHeader MethodBody ;
    MethodModifier
      = Annotation | 'public' | 'protected' | 'private' | 'abstract'
      | 'static' | 'final' | 'synchronized' | 'native' | 'strictfp'
      ;
    MethodHeader
      = Result MethodDeclarator Throws?
      | TypeParameters Annotation* Result MethodDeclarator Throws?
      ;
    Result = UnannType | 'void' ;
    MethodDeclarator = IDENTIFIER '(' FormalParameterList? ')' Dims? ;

    Throws = 'throws' ExceptionTypeList ;
    ExceptionTypeList = [ ExceptionType / ',' ]+ ;
    ExceptionType = ClassType | TypeVariable ;
    MethodBody = Block | ';' ;
    InstanceInitializer = Block ;
    StaticInitializer = 'static' Block ;
    ConstructorDeclaration = ConstructorModifier* ConstructorDeclarator Throws? ConstructorBody ;
    ConstructorModifier = Annotation | 'public' | 'protected' | 'private' ;
    ConstructorDeclarator = TypeParameters? SimpleTypeName '(' FormalParameterList? ')' ;
    SimpleTypeName = IDENTIFIER ;
    ConstructorBody = '{' ExplicitConstructorInvocation? BlockStatements? '}' ;
    ExplicitConstructorInvocation
      = TypeArguments? 'this' '(' ArgumentList? ')' ';'
      | TypeArguments? 'super' '(' ArgumentList? ')' ';'
      | ExpressionName '.' TypeArguments? 'super' '(' ArgumentList? ')' ';'
      | Primary '.' TypeArguments? 'super' '(' ArgumentList? ')' ';'
      ;
    EnumDeclaration = ClassModifier* 'enum' IDENTIFIER Superinterfaces? EnumBody ;
    EnumBody = '{' EnumConstantList? ','? EnumBodyDeclarations? '}' ;
    EnumConstantList = [ EnumConstant / ',' ]+ ;
    EnumConstant = EnumConstantModifier* IDENTIFIER ('(' ArgumentList? ')')? ClassBody? ;
    EnumConstantModifier = Annotation ;
    EnumBodyDeclarations = ';' ClassBodyDeclaration* ;
}

grammar Interfaces extends Classes {
    // from Classes
    override ClassMemberDeclaration
                = FieldDeclaration
                | MethodDeclaration
                | ClassDeclaration
                | InterfaceDeclaration
                | ';'
                ;

    InterfaceDeclaration = NormalInterfaceDeclaration | AnnotationTypeDeclaration ;
    NormalInterfaceDeclaration = InterfaceModifier* 'interface' IDENTIFIER TypeParameters? ExtendsInterfaces? InterfaceBody ;
    InterfaceModifier = Annotation | 'public' | 'protected' | 'private' | 'abstract' | 'static' | 'strictfp' ;
    ExtendsInterfaces = 'extends' InterfaceTypeList ;
    InterfaceBody = '{' InterfaceMemberDeclaration* '}' ;
    InterfaceMemberDeclaration
      = ConstantDeclaration
      | InterfaceMethodDeclaration
      | ClassDeclaration
      | InterfaceDeclaration
      | ';'
      ;
    ConstantDeclaration = ConstantModifier* UnannType VariableDeclaratorList ;
    ConstantModifier = Annotation | 'public' | 'static' | 'final' ;
    InterfaceMethodDeclaration = InterfaceMethodModifier* MethodHeader MethodBody ;
    InterfaceMethodModifier =  Annotation | 'public' | 'abstract' | 'default' | 'static' | 'strictfp' ;
    AnnotationTypeDeclaration = InterfaceModifier* '@' 'interface' IDENTIFIER AnnotationTypeBody ;
    AnnotationTypeBody = '{' AnnotationTypeMemberDeclaration* '}' ;
    AnnotationTypeMemberDeclaration
      = AnnotationTypeElementDeclaration
      | ConstantDeclaration
      | ClassDeclaration
      | InterfaceDeclaration
      | ';'
      ;
    AnnotationTypeElementDeclaration = AnnotationTypeElementModifier* UnannType IDENTIFIER '(' ')' Dims? DefaultValue? ';' ;
    AnnotationTypeElementModifier = Annotation | 'public' | 'abstract' ;
    DefaultValue = 'default' ElementValue ;
}

grammar Packages extends Interfaces {
    CompilationUnit = PackageDeclaration? ImportDeclaration* TypeDeclaration* ;
    PackageDeclaration = PackageModifier* 'package' [IDENTIFIER / '.']+ ';' ;
    PackageModifier = Annotation ;
    ImportDeclaration
      = SingleTypeImportDeclaration
      | TypeImportOnDemandDeclaration
      | SingleStaticImportDeclaration
      | StaticImportOnDemandDeclaration
      ;
    SingleTypeImportDeclaration = 'import' TypeName ';' ;
    TypeImportOnDemandDeclaration = 'import' PackageOrTypeName '.' '*' ';' ;
    SingleStaticImportDeclaration = 'import' 'static' TypeName '.' IDENTIFIER ';' ;
    StaticImportOnDemandDeclaration = 'import' 'static' TypeName '.' '*' ';' ;
    TypeDeclaration = ClassDeclaration | InterfaceDeclaration ;
}
    """.trimIndent()
    val style = """
        ${'$'}keyword {
            foreground: purple;
            font-style: bold;
        }
        'class' {
          foreground: purple;
          font-style: bold;
        }
        'private' {
          foreground: purple;
          font-style: bold;
        }
        IDENTIFIER {
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
    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, style, format)

}