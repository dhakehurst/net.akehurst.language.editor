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

object SText {
    val id = "statechart-tools"
    val label = "Statechart Tools"
    val sentence = """
statechart 'AlwaysOncycle' {
  specification {
    @CycleBased(200)
    @SuperSteps(no)
    interface:
        var value:integer
        var v2 : boolean
        in event e
        var x : integer
        var y : integer
  }
  region 'main region' {
    state '§entry' { specification {  } }
    state 'StateA' {
      specification {
        entry /value = 0
        always /value +=1
        exit/ value=0
      }
    }
    state 'StateB' {
      specification {
        oncycle /value+=1
        e, always / x += 1
        always, always / y+=1
      }
    }
  }
  transitions {
    '§entry' -- { } --> 'StateA'
    'StateA' -- { [value == 5] } --> 'StateB'
    'StateB' -- { [value==5] } --> 'StateA'
  }
}
    """.trimIndent()

    val grammar = """
/**
 * Copyright (c) 2010 - 2018 committers of YAKINDU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * 	committers of YAKINDU - initial API and implementation
 *
 */
namespace org.yakindu.statecharts
 
grammar Expressions {

    skip leaf COMMENT_SINGLE_LINE = "//[^\r\n]*" ;
    skip leaf COMMENT_MULTI_LINE = "/\*[^*]*\*+(?:[^/*][^*]*\*+)*/" ;
    skip leaf WS = "\s+";

    Expression
      = PrimaryExpression
      | PostFixUnaryExpression
      | TypeCastExpression 
      | NumericalUnaryExpression
      | InfixExpression
      | ConditionalExpression
      | AssignmentExpression
      ;

    ExpressionList = [ Expression / ';' ]+ ;

    AssignmentExpression = Expression AssignmentOperator Expression ;
    NotLogicalExpression = LogicalNotOperator Expression ;
    InfixExpression = [ Expression / InfixOperator ]2+ ;
    ConditionalExpression = Expression '?' Expression ':' Expression ;
    NumericalUnaryExpression = PrefixUnaryOperator Expression ;
    PostFixUnaryExpression = Expression PostfixUnaryOperator ;
    TypeCastExpression = Expression 'as' TypeSpecifier ;
    PrimaryExpression
        = PrimitiveValueExpression
        | FeatureCall
	    | ParenthesizedExpression
	    ;

    PrimitiveValueExpression = Literal ;
    NavigationOperator = '.' | '.@' ;
    FeatureCall
        = RootElement
        | FunctionCall
        | ArrayIndexExpression
        | NavigationExpression
        ;
    RootElement = ID ;
    FunctionCall = ID ArgumentList ;
    ArrayIndexExpression = ID ArrayIndex+ ;
    NavigationExpression = [ FeatureCall / NavigationOperator ]2+ ;

    ArgumentList = '(' Arguments? ')' ;
    Arguments = [Argument / ',']+ ;
    ArrayIndex = '[' Expression ']' ;
    Argument = ArgumentId?  Expression ;
    ArgumentId = ID '=' ;

    ParenthesizedExpression = '(' Expression ')' ;

    TypeSpecifier = QID GenericTypeArguments? ;
    GenericTypeArguments = '<' TypeSpecifierList? '>' ;
    TypeSpecifierList = [TypeSpecifier / ',']+ ;

    Literal
      = BoolLiteral
      | IntLiteral
      | HexLiteral
      | BinaryLiteral
      | DoubleLiteral
      | FloatLiteral
      | StringLiteral
      | NullLiteral
      ;

    leaf BoolLiteral = BOOL;
    leaf IntLiteral = INT;
    leaf DoubleLiteral = DOUBLE;
    leaf FloatLiteral = FLOAT;
    leaf HexLiteral = HEX;
    leaf BinaryLiteral = BINARY;
    leaf StringLiteral = STRING;
    leaf NullLiteral = 'null';

    leaf InfixOperator
        =
          '<<' | '>>'
        | '<=' | '<' | '>=' | '>' | '==' | '!='
        | '+' | '-'
        | '*' | '/' | '%'
        | '&&' | '||'
        | '^' | '&'
        ;

    leaf LogicalNotOperator = '!' ;
    leaf PostfixUnaryOperator    = '++' | '--' ;
    leaf AssignmentOperator = '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' ;
    leaf PrefixUnaryOperator = '+' | '-' | '~';

    QID = [ID / '.']+ ;
    leaf ID = "[a-zA-Z_][a-zA-Z_0-9]*" ;
    leaf STRING = DQ_STRING | SQ_STRING ;

    // --- only used by leaf rules
    INT = "[0-9]+" ;
    BOOL = "true|false|yes|no" ;
    HEX  = "0(x|X)[0-9a-fA-F]+" ;
    BINARY  = "0(b|B)[01]+" ;
    DOUBLE = (INT '.' INT) ('e' ('-' | '+') INT)? ('d' | 'D')? ;
    FLOAT = (INT '.' INT) ('e' ('-' | '+') INT)? ('f' | 'F')? ;
    DQ_STRING = "\"([^\\\"]|\\\"|\\)*\"";
    SQ_STRING = "'([^'\\]|\\'|\\)*'" ; 
}

grammar Global extends Expressions {

    StatechartSpecification = Namespace? Annotation* StatechartScope* ;
    Namespace = 'namespace' FQN ;
    StatechartScope = InterfaceScope | InternalScope | ImportScope ;
    InterfaceScope = 'interface' ID? ':' ScopeDeclaration* ;
    ScopeDeclaration = Annotation* MemberDeclaration ;
    MemberDeclaration
        = VariableDeclaration
        | EventDeclaration
        | AliasDeclaration
        | OperationDeclaration
        ;
    VariableDeclaration = VariableDeclarationKind ('readonly'?) ID (':' TypeSpecifier)? ('=' Expression)? ;
    VariableDeclarationKind = 'const' | 'var' ;
    EventDeclaration = Direction? 'event' ID (':' TypeSpecifier)? ;
    AliasDeclaration = 'alias' ID ':' TypeSpecifier ;
    OperationDeclaration = 'operation' ID '(' (Parameter (',' Parameter)*)? ')' (':' TypeSpecifier)? ;

    InternalScope = 'internal' ':' (ScopeDeclaration | LocalReaction)*;
    ImportScope = 'import' ':' STRING* ;
    
    Direction = 'in' | 'out' ;
    
    Annotation = '@' QID ('(' (Expression (',' Expression)*)? ')')?;
    Parameter = ID ('...')? ':' TypeSpecifier;
    LocalReaction = ReactionTrigger ('/' ReactionEffect);
    TransitionReaction = StextTrigger? ('/' ReactionEffect)? ('#' TransitionProperty*)?;
    StextTrigger = ReactionTrigger | DefaultTrigger;
    
    ReactionTrigger = EventSpecList? Guard? ;
    EventSpecList = [EventSpec / ',']+ ;
    
    DefaultTrigger = ('default' | 'else');

    Guard = '[' GuardExpression ']' ;
    GuardExpression = Expression;
    
    ReactionEffect = (Expression | EventRaisingExpression) ( ';' (Expression | EventRaisingExpression))*;
    TransitionProperty  = EntryPointSpec | ExitPointSpec;
    EntryPointSpec = '>' ID;
    ExitPointSpec = ID '>';
    EventSpec =	RegularEventSpec | TimeEventSpec | BuiltinEventSpec;

    // Use SimpleFeatureCall for eventSpec to avoid guard ambiguity with array access
    RegularEventSpec = FeatureCall; //SimpleFeatureCall;
    SimpleFeatureCall = [SimpleElementReferenceExpression / SimpleFeatureCallNavigation ]+ ;
    SimpleFeatureCallNavigation = '.' | '.@' ;
    SimpleElementReferenceExpression = ID ArgumentList? ;
    TimeEventSpec = TimeEventType Expression TimeUnit ;
    TimeEventType =	'after' | 'every';
    BuiltinEventSpec = 'entry' | 'exit' | 'always' | 'oncycle' ;
    
    EventRaisingExpression = 'raise' FeatureCall (':' Expression)?;
    EventValueReferenceExpression = 'valueof' '(' FeatureCall ')';
    ActiveStateReferenceExpression = 'active' '(' FQN ')';

    override PrimaryExpression
        = PrimitiveValueExpression
        | FeatureCall
        | ActiveStateReferenceExpression
        | EventValueReferenceExpression
        | ParenthesizedExpression
        ;
    
    TimeUnit = 's' | 'ms' | 'us' | 'ns' ;
    FQN = [ID /'.']+;
}

grammar States extends Expressions {

    StateSpecification = StateScope;
    StateScope = (LocalReaction | SubmachineReferenceExpression)* ;
    SubmachineReferenceExpression = 'submachine' FeatureCall ;
    StatechartScope = InterfaceScope | InternalScope | ImportScope ;
    InterfaceScope = 'interface' ID? ':' ScopeDeclaration* ;
    ScopeDeclaration = Annotation* MemberDeclaration ;
    MemberDeclaration
        = VariableDeclaration
        | EventDeclaration
        | AliasDeclaration
        | OperationDeclaration
        ;
    VariableDeclaration = ('const' | 'var') ('readonly'?) ID ':'	TypeSpecifier ('=' Expression)? ;
    EventDeclaration = Direction? 'event' ID (':' TypeSpecifier)? ;
    AliasDeclaration = 'alias' ID ':' TypeSpecifier ;
    OperationDeclaration = 'operation' ID '(' (Parameter (',' Parameter)*)? ')' (':' TypeSpecifier)? ;

    InternalScope = 'internal' ':' (ScopeDeclaration | LocalReaction)*;
    ImportScope = 'import' ':' STRING* ;
    Direction = 'in' | 'out' ;
    Annotation = '@' QID ('(' (Expression (',' Expression)*)? ')')?;
    Parameter = ID ('...')? ':' TypeSpecifier;
    
    LocalReaction = ReactionTrigger ('/' ReactionEffect);
    TransitionReaction = StextTrigger? ('/' ReactionEffect)? ('#' TransitionProperty*)?;
    StextTrigger = ReactionTrigger | DefaultTrigger;
    ReactionTrigger = EventSpecList? Guard? ;
    EventSpecList = [EventSpec / ',']+ ;
    DefaultTrigger = ('default' | 'else');
    
    Guard = '[' GuardExpression ']' ;
    GuardExpression = Expression;
    ReactionEffect = (Expression | EventRaisingExpression) ( ';' (Expression | EventRaisingExpression))*;
    
    TransitionProperty  = EntryPointSpec | ExitPointSpec;
    EntryPointSpec = '>' ID;
    ExitPointSpec = ID '>';
    EventSpec =	RegularEventSpec | TimeEventSpec | BuiltinEventSpec;
    
    // Use SimpleFeatureCall for eventSpec to avoid guard ambiguity with array access
    RegularEventSpec = FeatureCall; //SimpleFeatureCall;
    SimpleFeatureCall = [SimpleElementReferenceExpression / SimpleFeatureCallNavigation ]+ ;
    SimpleFeatureCallNavigation = '.' | '.@' ;
    SimpleElementReferenceExpression = ID ArgumentList? ;
    TimeEventSpec = TimeEventType Expression TimeUnit ;
    TimeEventType =	'after' | 'every';
    BuiltinEventSpec = 'entry' | 'exit' | 'always' | 'oncycle' ;
    
    EventRaisingExpression = 'raise' FeatureCall (':' Expression)?;
    EventValueReferenceExpression = 'valueof' '(' FeatureCall ')';
    ActiveStateReferenceExpression = 'active' '(' FQN ')';
    
    override PrimaryExpression
        = PrimitiveValueExpression
        | FeatureCall
        | ActiveStateReferenceExpression
        | EventValueReferenceExpression
        | ParenthesizedExpression
        ;
    
    TimeUnit = 's' | 'ms' | 'us' | 'ns' ;
    FQN = ID ('.' ID)*;

}

grammar Transitions extends Expressions {

    TransitionSpecification = TransitionReaction ;
    TransitionReaction = StextTrigger? ('/' ReactionEffect)? ('#' TransitionProperty*)?;
    StextTrigger = ReactionTrigger | DefaultTrigger;
    
    ReactionTrigger = EventSpecList? Guard? ;
    EventSpecList = [EventSpec / ',']+ ;
    DefaultTrigger = ('default' | 'else');
    
    Guard = '[' GuardExpression ']' ;
    GuardExpression = Expression;
    
    ReactionEffect = (Expression | EventRaisingExpression) ( ';' (Expression | EventRaisingExpression))*;
    
    TransitionProperty  = EntryPointSpec | ExitPointSpec;
    EntryPointSpec = '>' ID;
    ExitPointSpec = ID '>';
    EventSpec =	RegularEventSpec | TimeEventSpec | BuiltinEventSpec;
    
    // Use SimpleFeatureCall for eventSpec to avoid guard ambiguity with array access
    RegularEventSpec = FeatureCall; //SimpleFeatureCall;
    SimpleFeatureCall = [SimpleElementReferenceExpression / SimpleFeatureCallNavigation ]+ ;
    SimpleFeatureCallNavigation = '.' | '.@' ;
    SimpleElementReferenceExpression = ID ArgumentList? ;
    TimeEventSpec = TimeEventType Expression TimeUnit ;
    TimeEventType =	'after' | 'every';
    BuiltinEventSpec = 'entry' | 'exit' | 'always' | 'oncycle' ;
    
    EventRaisingExpression = 'raise' FeatureCall (':' Expression)?;
    EventValueReferenceExpression = 'valueof' '(' FeatureCall ')';
    ActiveStateReferenceExpression = 'active' '(' FQN ')';

    override PrimaryExpression
        = PrimitiveValueExpression
        | FeatureCall
        | ActiveStateReferenceExpression
        | EventValueReferenceExpression
        | ParenthesizedExpression
        ;
    
    TimeUnit = 's' | 'ms' | 'us' | 'ns' ;
    
    FQN = ID ('.' ID)*;

}

grammar Statechart {
    skip leaf WS = "\s+" ;

    statechart = 'statechart' NAME '{' specification? regions transitions? '}' ;
    specification = 'specification' '{' statechartSpecification? '}' ;
    statechartSpecification = Global::StatechartSpecification ;
    regions = region* ;
    transitions = 'transitions' '{' transition* '}' ;
    region = 'region' NAME '{' states '}' ;
    
    states = state* ;
    state = 'state' NAME '{' stateSpec? regions '}' ;
    stateSpec = 'specification' '{' stateSpecification? '}' ;
    stateSpecification = States::StateSpecification ;
    
    transition = NAME '--' '{' transitionSpecification? '}' '-->' NAME ;            
    transitionSpecification = Transitions::TransitionSpecification ;

    leaf NAME = "'[^']+'" ;
}
    """.trimIndent()

    val references = """
    """.trimIndent()

    val style = """
${'$'}keyword {
    foreground: purple;
    font-style: bold;
}
StatechartSpecification {
    background: #f5f5f1;
}

StateSpecification {
    background: #f5f1f5;
}

TransitionSpecification {
    background: #f1f5f5;
}
    """.trimIndent()
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, references, style, format)

}