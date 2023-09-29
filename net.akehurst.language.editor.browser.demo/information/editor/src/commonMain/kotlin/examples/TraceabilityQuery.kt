package net.akehurst.language.editor.information.examples

import net.akehurst.language.editor.information.Example

object TraceabilityQuery {

    val id = "vistraq"
    val label = "Traceability Query Language - VisTraQ"

    val sentence = """
FOR TIMESPAN '01-Jan-2017' UNTIL '31-Dec-2017' EVERY month
 MATCH Milestone AS ms
   WHERE ms.dueDate <= now
   RETURN TABLE COLUMN Due CONTAINING COUNT(ms)
 JOIN
   MATCH Milestone AS ms
    ( LINKED * TIMES WITH Bug AS bug
      LINKED USING 1..* LINKS WITH TestResult AS bResult
    OR LINKED WITH Feature AS ft
      ( LINKED * TIMES WITH SubTask AS task
      OR LINKED WITH Requirement AS req
        ( LINKED USING 1..* LINKS WITH TestResult AS iResult
        AND
          LINKED TO CodeFile AS code
          LINKED USING 1..* LINKS WITH TestResult AS uResult
        ) ) )
    LINKED 1 TIMES TO Build AS build
    LINKED 1 TIMES TO Binary AS binary
   WHERE
     ms.released == true AND ft.status == 'done' AND task.status == 'done'
     AND iResult.value == 'pass' AND uResult.value == 'pass'
     AND build.testCoverage >= 80 AND bug.status == 'done'
     AND bResult.value == 'pass' AND build.buildDate < ms.dueDate
     AND build.version == ms.version AND binary.version == ms.version
     AND binary.publishedDate < ms.dueDate
   RETURN TABLE COLUMN Met CONTAINING COUNT(ms)
 JOIN
   RETURN TABLE COLUMN Percent CONTAINING (Met / Due)* 100
    """.trimIndent()

    val grammar = """
namespace com.itemis.typedgraph.query

grammar Query {

    skip WHITE_SPACE = "\s+" ;
	skip SINGLE_LINE_COMMENT = "/\*[^*]*\*+([^*/][^*]*\*+)*/" ;
	skip MULTI_LINE_COMMENT = "//[^\n\r]*" ;

    query = singleQuery | compositeQuery ;
    singleQuery = timespanDefinition? querySource? returnDefinition? ;
    compositeQuery = query compositionOperator query ;
    leaf compositionOperator = 'UNION' | 'JOIN' ;

    timespanDefinition = 'FOR' timeDef nameDefinition? ;
	timeDef =  time | timespan ;
	time = 'TIME' timePoint ;
	timePoint = 'start' | 'now' | timestamp ;
	timestamp = SINGLE_QUOTE_STRING ;
	timespan = 'TIMESPAN' timeRange 'EVERY' period ;
	timeRange = 'all' | timePoint 'UNTIL' timePoint ;
	leaf period = 'second' | 'minute' | 'hour' | 'day' | 'week' | 'month' | 'year' ;


	querySource = pathQuery | storedQueryReference ;
	storedQueryReference = 'FROM' STORED_QUERY_ID ;
    pathQuery = 'MATCH' pathExpression whereClause? ;
    pathExpression =  nodeSelector linkedNodeSelectorPath? ;

	nodeSelector = nodeTypeReferenceExpression nameDefinition? ;
    nodeTypeReferenceExpression
        = nodeTypeReference
        < NONE_NODE_TYPE
        < ANY_NODE_TYPE
        < negatedNodeTypeReferenceExpression
        < nodeTypeReferenceGroup
        ;
    negatedNodeTypeReferenceExpression = 'NOT' nodeTypeReferenceExpression ;
    nodeTypeReferenceGroup = '(' [nodeTypeReferenceExpression / 'OR' ]+ ')' ;

    linkedNodeSelector = linkSelectorExpression ;
    linkedNodeSelectorNegated = linkSelectorNegated ;
	linkSelectorExpression = linkSelector | linkSelectorGroupedPath ;
	linkSelector = 'LINKED' links? multiplicity? via? ('TO'|'FROM'|'WITH') nodeSelector ;

	links = 'USING' range 'LINKS' ;
	multiplicity = range 'TIMES' ;
    range = POSITIVE_INT '..' UNLIMITED_POSITIVE_INT | UNLIMITED_POSITIVE_INT ;

	via = 'VIA'  linkTypeReferenceExpression ;
	linkSelectorNegated = 'NOT' linkSelectorGroupedPath ;
	linkSelectorGroupedPath = '(' linkSelectorGroupedItem ')' ;
	linkSelectorGroupedItem = linkedNodeSelectorPath | linkSelectorOperator ;
	//linkSelectorGroupedOperator = '(' linkSelectorOperator ')';
	linkSelectorOperator = linkedNodeSelectorPath logicalOperator linkedNodeSelectorPath ;

	linkedNodeSelectorPath = linkedNodeSelectorPathNormal | linkedNodeSelectorPathNegated ;
	linkedNodeSelectorPathNormal = linkedNodeSelector+ ;
	linkedNodeSelectorPathNegated = linkedNodeSelector*  linkedNodeSelectorNegated ;

    linkTypeReferenceExpression = ANY_LINK_TYPE | linkTypeReference | negatedLinkTypeReferenceExpression | linkTypeReferenceGroup ;
    negatedLinkTypeReferenceExpression = 'NOT' linkTypeReferenceExpression ;
    linkTypeReferenceGroup = '(' [linkTypeReferenceExpression / 'OR' ]+ ')' ;

	whereClause = 'WHERE' expression ;

	returnDefinition = returnExpression | returnTable | returnSelect | returnGraph ;

	returnExpression = 'RETURN' aggregateFunctionCallOrExpression ;

	returnTable = 'RETURN' 'TABLE' columnDefinition+ orderBy? ;
	columnDefinition = 'COLUMN' NAME 'CONTAINING' aggregateFunctionCallOrExpression whereClause? ;

    returnSelect = 'RETURN' 'SELECT' selectList;
    selectList = [selectItem / ',']* ;
    selectItem = NAME | propertyReference ;
    propertyReference = NAME '.' NAME ;

	returnGraph = 'RETURN' 'GRAPH' subgraphConstruction ;
	subgraphConstruction = nodeConstruction linkedSubGraphConstruction? whereConstructionClause?;
	nodeConstruction = nodeTypeConstructionExpression '{' nodeIdentityExpressionConstruction nodePropertyAssignmentExpressionList? '}' nameDefinition?;
	nodeTypeConstructionExpression = expression < nodeTypeReference ;
    nodeIdentityExpressionConstruction = expression ;
    nodePropertyAssignmentExpressionList = '|' nodePropertyAssignmentExpression+ ;
    nodePropertyAssignmentExpression = NAME ':=' expression ;

	linkedSubGraphConstruction = linkedNodeConstructionExpression+ ;
	linkedNodeConstructionExpression = linkedNodeConstructionPath | linkedNodeConstructionGroup ;
	linkedNodeConstructionPath = linkedNodeConstruction+ ;
	linkedNodeConstruction = 'LINKED' 'VIA' linkTypeReference ('TO'|'FROM') nodeConstruction ;
	linkedNodeConstructionGroup = '(' linkedNodeConstructionGroupItem ')' ;
	linkedNodeConstructionGroupItem = linkedNodeConstructionPath |  linkedNodeConstructionGroupItemOperator ;
	linkedNodeConstructionGroupItemOperator =  linkedNodeConstructionExpression 'AND' linkedNodeConstructionExpression ;

	whereConstructionClause = 'WHERE' constructionExpression ;

	constructionExpression = andConstructionExpression
	                       < propertyAssignment
                           ;

	propertyAssignment = propertyCall ':=' expression ;
	andConstructionExpression = constructionExpression 'AND' constructionExpression ;

	aggregateFunctionCallOrExpression = aggregateFunctionCall | expression ;
	aggregateFunctionCall = aggregateFunctionName '(' expression ')' ;
    expression
		= root
		| literalValue
		| propertyCall
		| methodCall
		| infixFunction
		| conditionalExpression
    	| groupExpression
        ;

    nameDefinition = 'AS' NAME ;

    groupExpression = '(' expression ')'  ;
	root = NAME < 'start' < 'now' ;
	propertyCall = expression '.' NAME ;
	methodCall = expression '.' NAME '(' argList ')';
	argList = [expression / ',']* ;
	infixFunction = [expression / operator]2+ ;
    operator
        = logicalOperator
        | arithmeticOperator
        | comparisonOperator
        ;
    leaf arithmeticOperator =  '/' | '*' | '+' | '-' ;
    leaf comparisonOperator = '==' | '!=' | '<=' | '>=' | '<' | '>'  ;
    leaf logicalOperator = 'AND' | 'OR' | 'XOR' ;

    conditionalExpression = expression '?' expression ':' expression ;

	orderBy = 'ORDER' 'BY' [columnOrder / ',']+ ;
	columnOrder = NAME ('ASCENDING' | 'DESCENDING')? ;

	leaf ANY_NODE_TYPE = 'any' ;
	leaf NONE_NODE_TYPE = 'none' ;
	nodeTypeReference = NAME ;
	leaf ANY_LINK_TYPE = 'any' ;
	linkTypeReference = NAME ;
	aggregateFunctionName = NAME ;
	leaf STORED_QUERY_ID = "([a-zA-Z_][a-zA-Z0-9_]*)([.][a-zA-Z_][a-zA-Z0-9_]*)?" ; // project metricDef OR metricSet.metricDef
	leaf NAME = "[a-zA-Z_][a-zA-Z0-9_]*" ;
	leaf POSITIVE_INT = "[0-9]+" ;
	leaf UNLIMITED_POSITIVE_INT = "[0-9]+" | '*' ;

    literalValue = BOOLEAN < SINGLE_QUOTE_STRING < INTEGER < REAL < NULL ;
    leaf BOOLEAN = 'true' | 'false' ;
    leaf SINGLE_QUOTE_STRING = "'([^']|\\')*'" ;
    leaf INTEGER = "[0-9]+" ;
    leaf REAL = "[0-9]+[.][0-9]+" ;
    leaf NULL = 'null' ;
}
    """.trimIndent()

    val references = """
    """.trimIndent()

    val style = """
${'$'}keyword {
    foreground: green;
    font-style: bold;
}
compositionOperator {
    foreground: green;
    font-style: bold;
}
logicalOperator {
    foreground: green;
    font-style: bold;
}
NAME {
  foreground: blue;
  font-style: italic;
}
SINGLE_QUOTE_STRING {
    foreground: purple;
}
range {
    foreground: orange;
}
timestamp {
  foreground: red;
  font-style: italic;
}
    """.trimIndent()

    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, references, style, format)

}