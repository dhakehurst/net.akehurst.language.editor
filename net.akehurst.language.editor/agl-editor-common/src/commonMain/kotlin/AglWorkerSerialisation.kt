/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.common

import net.akehurst.kotlin.json.JsonDocument
import net.akehurst.kotlin.komposite.api.DatatypeModel
import net.akehurst.kotlin.komposite.processor.komposite
import net.akehurst.kotlin.kserialisation.json.KSerialiserJson
import net.akehurst.language.api.style.AglStyleModel


object AglWorkerSerialisation {

    private var initialised = false
    private val serialiser = KSerialiserJson().also {
        it.registerKotlinStdPrimitives()
    }

    private fun initialise() {
        if (!initialised) {
            agl_editor_common.KotlinxReflectForModule.registerUsedClasses()
            //TODO: enable kserialisation/komposite/reflect to auto add these some how!!
            initialiseApiTypes()
            initialiseStyleAsm()
            initialiseScopesAsm()
            initialiseGrammarAsm()
            initialiseMessages()
            initialiseAsmSimple()
            initialiseSPPT()
            initialised = true
        }
    }

    //TODO: define the Komposite defs below using a builder rather than parsed string
// to improve performance
    private fun initialiseApiTypes() {
        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.editor.common {
                datatype AglToken {
                    composite-val styles: Array<String>
                    composite-val value: String
                    composite-val line: Int
                    composite-val column: Int
                }
            }
            namespace net.akehurst.language.api.parser {
                datatype InputLocation{
                    composite-val position:Int
                    composite-val column: Int
                    composite-val line: Int
                    composite-val length: Int
                }
            }
            namespace net.akehurst.language.api.processor {
                enum LanguageIssueKind
                enum LanguageProcessorPhase
                datatype LanguageIssue {
                    composite-val kind: LanguageIssueKind
                    composite-val phase: LanguageProcessorPhase
                    composite-val location: InputLocation?
                    composite-val message: String
                    composite-val data: Any?
                }
            }
            """.trimIndent()
        )
         */
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.editor.common") {
                dataType("AglToken") {
                    constructorArguments {
                        composite("styles","Array") { typeArgument("String") }
                        composite("value","String")
                        composite("line","Int")
                        composite("column","Int")
                    }
                }
            }
            namespace("net.akehurst.language.api.parser") {
                dataType("InputLocation") {
                    constructorArguments {
                        composite("position", "Int")
                        composite("column", "Int")
                        composite("line", "Int")
                        composite("length", "Int")
                    }
                }
            }
            namespace("net.akehurst.language.api.processor") {
                enumType("LanguageIssueKind")
                enumType("LanguageProcessorPhase")
                dataType("LanguageIssue") {
                    constructorArguments {
                        composite("kind", "LanguageIssueKind")
                        composite("phase", "LanguageProcessorPhase")
                        composite("location", "InputLocation")
                        composite("message", "String")
                        composite("data", "Any")
                    }
                }
                dataType("CompletionItem") {
                    constructorArguments {
                        composite("ruleName","String")
                        composite("text","String")
                    }
                }
            }
        })
    }

    private fun initialiseStyleAsm() {

        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.api.style {
                datatype AglStyleRule {
                    composite-val selector: String
                    composite-var styles: Map<String,AglStyle>
                }
                datatype AglStyle {
                    composite-val name: String
                    composite-val value: String
                }
            }
            """.trimIndent()
        )
         */
        //classes registered with KotlinxReflect via gradle plugin
        // use DslBuilder rather than KompositeProcessor as it is faster
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.agl.grammar.style") {
                dataType("AglStyleModelDefault") {
                    constructorArguments {
                        composite("rules", "List") {
                            typeArgument("net.akehurst.language.api.style.AglStyleRule")
                        }
                    }
                }
            }
            namespace("net.akehurst.language.api.style") {
                dataType("AglStyleRule") {
                    constructorArguments {
                        composite("selector", "String")
                    }
                    mutableProperties {
                        composite("styles", "Map") {
                            typeArgument("String")
                            typeArgument("AglStyle")
                        }
                    }
                }
                dataType("AglStyle") {
                    constructorArguments {
                        composite("name", "String")
                        composite("value", "String")
                    }
                }
            }
        })
    }

    private fun initialiseScopesAsm() {

        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.agl.grammar.scopes {
                datatype ScopeModel {
                    composite-var scopes: Map<String,ScopeDefinition>
                    composite-var references: List<ReferenceDefinition>
                }
                datatype ScopeDefinition {
                    composite-val scopeFor: String
                    composite-var identifiables: List<Identifiable>
                }
                datatype Identifiable {
                    composite-val typeName: String
                    composite-val propertyName: String
                }
                datatype ReferenceDefinition {
                    composite-val inTypeName: String
                    composite-val referringPropertyName: String
                    composite-val refersToTypeName: List<String>
                }
            }
            """.trimIndent()
        )
         */
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.agl.grammar.scopes") {
                dataType("ScopeModelAgl") {
                    mutableProperties {
                        composite("scopes", "Map") {
                            typeArgument("String")
                            typeArgument("ScopeDefinition")
                        }
                        composite("references", "List") {
                            typeArgument("ReferenceDefinition")
                        }
                    }
                }
                dataType("ScopeDefinition") {
                    constructorArguments {
                        composite("scopeFor", "String")
                    }
                    mutableProperties {
                        composite("identifiables", "List") { typeArgument("Identifiable") }
                    }
                }
                dataType("Identifiable") {
                    constructorArguments {
                        composite("typeName", "String")
                        composite("propertyName", "String")
                    }
                }
                dataType("ReferenceDefinition") {
                    constructorArguments {
                        composite("inTypeName", "String")
                        composite("referringPropertyName", "String")
                        composite("refersToTypeName", "List") { typeArgument("String") }
                    }
                }
            }
        })
    }

    private fun initialiseMessages() {
        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.editor.common.messages {
                datatype MessageProcessorCreate {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val grammarStr: String
                }
                datatype MessageProcessorCreateResponse {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                }
                datatype MessageSyntaxAnalyserConfigure {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val configuration: Any
                }
                datatype MessageSyntaxAnalyserConfigureResponse {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val issues: List<LanguageIssue>
                }                
                datatype MessageProcessRequest {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val goalRuleName: String?
                    composite-val text: String
                    composite-val context: Any?
                }
                datatype MessageParseResult {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val issues: List<LanguageIssue>
                    composite-val treeSerialised: String?
                }
                datatype MessageSyntaxAnalysisResult {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val issues: List<LanguageIssue>
                    composite-val asm: Any?
                }
                datatype MessageSemanticAnalysisResult {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val issues: ArListray<LanguageIssue>
                }
                datatype MessageParserInterruptRequest {
                     composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val reason: String
                }
                datatype MessageLineTokens {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val lineTokens: Array<Array<AglToken>>
                }
                datatype MessageSetStyle {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val css: String
                }
                datatype MessageSetStyleResult {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                }
                datatype MessageCodeCompleteRequest {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val goalRuleName: String?
                    composite-val text: String
                    composite-val position: Int
                }
                datatype MessageCodeCompleteResult {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val completionItems: Array<Pair<String, String>>?
                }
            }
            namespace net.akehurst.language.agl.syntaxAnalyser {
                datatype ContextSimple {
                    composite-var rootScope: ScopeSimple<AsmElementPath>
                }
            }
        """.trimIndent()
        )*/
        //classes registered with KotlinxReflect via gradle plugin
        // use DslBuilder rather than KompositeProcessor as it is faster
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.api.automaton") {
                enumType("ParseAction")
            }
            namespace("net.akehurst.language.agl.syntaxAnalyser") {
                dataType("ContextSimple") {
                    typeParameters("E")
                    mutableProperties {
                        composite("rootScope","ScopeSimple") { typeArgument("E") }
                    }
                }
            }
            namespace("net.akehurst.language.editor.common.messages") {
                dataType("MessageProcessorCreate") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("grammarStr","String")
                    }
                }
                dataType("MessageProcessorCreateResponse") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("issues","List") { typeArgument("LanguageIssue") }
                    }
                }
                dataType("MessageSyntaxAnalyserConfigure") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("configuration","Map") {
                            typeArgument("String")
                            typeArgument("Any")
                        }
                    }
                }
                dataType("MessageSyntaxAnalyserConfigureResponse") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("issues","List") { typeArgument("LanguageIssue") }
                    }
                }
                dataType("MessageProcessRequest") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("goalRuleName","String",true)
                        composite("text","String")
                        composite("context","Any",true)
                    }
                }
                dataType("MessageParseResult") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("issues","List") { typeArgument("LanguageIssue") }
                        composite("treeSerialised", "String",true)
                    }
                }
                dataType("MessageSyntaxAnalysisResult") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("issues","List") { typeArgument("LanguageIssue") }
                        composite("asm", "Any",true)
                    }
                }
                dataType("MessageSemanticAnalysisResult") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("issues","List") { typeArgument("LanguageIssue") }
                    }
                }
                dataType("MessageParserInterruptRequest") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("reason","String")
                    }
                }
                dataType("MessageLineTokens") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("lineTokens","Array") { typeArgument("Array"){typeArgument("AglToken")} }
                    }
                }
                dataType("MessageSetStyle") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("css","String")
                    }
                }
                dataType("MessageSetStyleResult") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                    }
                }
                dataType("MessageCodeCompleteRequest") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("goalRuleName","String")
                        composite("text","String")
                        composite("position","Int")
                    }
                }
                dataType("MessageCodeCompleteResult") {
                    constructorArguments {
                        composite("languageId","String")
                        composite("editorId","String")
                        composite("sessionId","String")
                        composite("success","Boolean")
                        composite("message","String")
                        composite("completionItems","Array") { typeArgument("CompletionItem") }
                    }
                }
            }
        })
    }

    private fun initialiseAsmSimple() {
        //TODO: add type arg to ScopeSimple<E>
        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.agl.syntaxAnalyser {
                datatype ScopeSimple {
                    reference-val parent: ScopeSimple<E>?
                    composite-val forReferenceInParent:String
                    composite-val forTypeName:String

                    composite-var childScopes:Map<String,ScopeSimple<E>>
                    composite-var items:Map<String,Map<String,E>>
                }
            }
            namespace net.akehurst.language.api.asm {
                datatype AsmElementPath {
                    composite-val value:String
                }
                datatype AsmSimple {
                    composite-var rootElements: List<AsmElementSimple>
                }
                datatype AsmElementSimple {
                    composite-val asmPath: AsmElementPath
                    reference-val asm: AsmSimple
                    composite-val typeName: String
                    
                    composite-var properties: Map<String, AsmElementProperty>
                }
                datatype AsmElementProperty {
                    composite-val name: String
                    composite-val value: Any?
                    composite-val isReference: Boolean
                }
                datatype AsmElementReference {
                    composite-val reference: String
                    reference-val value: AsmElementSimple?
                }
            }
        """.trimIndent()
        )
         */
        //classes registered with KotlinxReflect via gradle plugin
        // use DslBuilder rather than String as it is faster
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.agl.syntaxAnalyser") {
                dataType("ScopeSimple") {
                    typeParameters("E")
                    constructorArguments {
                        reference("parent", "ScopeSimple") { typeArgument("E") }
                        composite("forReferenceInParent", "String")
                        composite("forTypeName", "String")
                    }
                    mutableProperties {
                        composite("childScopes", "Map") {
                            typeArgument("String")
                            typeArgument("ScopeSimple")
                        }
                        composite("items", "Map") {
                            typeArgument("String")
                            typeArgument("Map") {
                                typeArgument("String")
                                typeArgument("E")
                            }
                        }
                    }
                }
            }
            namespace("net.akehurst.language.api.asm") {
                dataType("AsmElementPath") {
                    constructorArguments {
                        composite("value", "String")
                    }
                }
                dataType("AsmSimple") {
                    mutableProperties {
                        composite("rootElements", "List") {
                            typeArgument("AsmElementSimple")
                        }
                    }
                }
                dataType("AsmElementSimple") {
                    constructorArguments {
                        composite("asmPath", "AsmElementPath")
                        reference("asm", "AsmSimple")
                        composite("typeName", "String")
                    }
                    mutableProperties {
                        composite("properties", "Map") {
                            typeArgument("String")
                            typeArgument("AsmElementProperty")
                        }
                    }
                }
                dataType("AsmElementProperty") {
                    constructorArguments {
                        composite("name", "String")
                        composite("declaration", "net.akehurst.language.api.typeModel.PropertyDeclaration")
                        composite("value", "Any")
                        composite("isReference", "Boolean")
                    }
                }
                dataType("AsmElementReference") {
                    constructorArguments {
                        composite("reference", "String")
                        reference("value", "AsmElementSimple")
                    }
                }
            }
        })
    }

    private fun initialiseGrammarAsm() {
        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.agl.grammar.grammar.asm {
                datatype NamespaceDefault {
                    composite-val qualifiedName: String
                }
                datatype GrammarDefault {
                    composite-val namespace : Namespace
                    composite-val name : String
                    composite-var rule : List<RuleItemAbstract>
                }
                datatype RuleDefault {
                    reference-val grammar: GrammarDefault
                    composite-val name: String
                    composite-val isOverride: Boolean
                    composite-val isSkip: Boolean
                    composite-val isLeaf: Boolean
                    composite-var rhs: RuleItemAbstract
                }
                datatype RuleItemAbstract { }
                datatype EmptyRuleDefault : RuleItemAbstract { }
                datatype ChoiceAbstract : RuleItemAbstract {}
                datatype ChoiceLongestDefault : ChoiceAbstract {
                    composite-val alternative: List<ConcatenationDefault>
                }
                datatype ChoicePriorityDefault : ChoiceAbstract { 
                    composite-val alternative: List<ConcatenationDefault>
                }
                datatype ConcatenationDefault : RuleItemAbstract {
                    composite-val items: List<ConcatenationItem>
                }
                datatype ConcatenationItemAbstract : RuleItemAbstract { }
                datatype SimpleItemAbstract : ConcatenationItemAbstract { }
                datatype GroupDefault : SimpleItemAbstract {
                    composite-val choice : ChoiceAbstract
                }
                datatype NonTerminalDefault : RuleItemAbstract {
                    composite-val name: String
                    reference-val owningGrammar: Grammar
                    composite-val embedded: Boolean
                }
                datatype TerminalDefault : RuleItemAbstract {
                    composite-val value: String
                    composite-val isPattern: Boolean
                }
                datatype SeparatedListDefault : RuleItemAbstract {
                    composite-val min: Int
                    composite-val max: Int
                    composite-val item: SimpleItemAbstract
                    composite-val separator: SimpleItem
                }
                datatype SimpleListDefault : RuleItemAbstract {
                    composite-val min: Int
                    composite-val max: Int
                    composite-val item: SimpleItemAbstract
                }
            }
        """.trimIndent()
        )
        */
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.agl.grammar.grammar.asm") {
                dataType("NamespaceDefault") {
                    constructorArguments {
                        composite("qualifiedName","String")
                    }
                }
                dataType("GrammarDefault") {
                    constructorArguments {
                        composite("namespace","Namespace")
                        composite("name","String")
                    }
                    mutableProperties {
                        composite("grammarRule","List") {
                            typeArgument("GrammarRuleDefault")
                        }
                    }
                }
                dataType("GrammarRuleDefault") {
                    constructorArguments {
                        reference("grammar","GrammarDefault")
                        composite("name","String")
                        composite("isOverride","Boolean")
                        composite("isSkip","Boolean")
                        composite("isLeaf","Boolean")
                    }
                    mutableProperties {
                        composite("rhs","RuleItemAbstract")
                    }
                }
                dataType("RuleItemAbstract") {}
                dataType("EmptyRuleDefault") {
                    superTypes("RuleItemAbstract")
                }
                dataType("ChoiceAbstract") {
                    superTypes("RuleItemAbstract")
                }
                dataType("ChoiceLongestDefault") {
                    superTypes("ChoiceAbstract")
                    constructorArguments {
                        composite("alternative","List") {
                            typeArgument("ConcatenationDefault")
                        }
                    }
                }
                dataType("ChoicePriorityDefault") {
                    superTypes("ChoiceAbstract")
                    constructorArguments {
                        composite("alternative","List") {
                            typeArgument("ConcatenationDefault")
                        }
                    }
                }
                dataType("ConcatenationDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("items","List") {
                            typeArgument("ConcatenationItemAbstract")
                        }
                    }
                }
                dataType("ConcatenationItemAbstract")  {
                    superTypes("RuleItemAbstract")
                }
                dataType("SimpleItemAbstract") {
                    superTypes("ConcatenationItemAbstract")
                }
                dataType("GroupDefault") {
                    superTypes("ConcatenationItemAbstract")
                    constructorArguments {
                        composite("choice","ChoiceAbstract")
                    }
                }
                dataType("NonTerminalDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("name","String")
                        reference("owningRule","Rule")
                    }
                }
                dataType("TerminalDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("value","String")
                        composite("isPattern","Boolean")
                    }
                }
                dataType("EmbeddedDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("embeddedGoalName","String")
                        reference("embeddedGrammar","Grammar")
                    }
                }
                dataType("SeparatedListDefault") {
                    constructorArguments {
                        composite("min","Int")
                        composite("max","Int")
                        composite("item","SimpleItemAbstract")
                        composite("separator","SimpleItem")
                    }
                }
                dataType("SimpleListDefault") {
                    constructorArguments {
                        composite("min","Int")
                        composite("max","Int")
                        composite("item","SimpleItemAbstract")
                    }
                }
            }
        })
    }

    private fun initialiseSPPT() {
        //Cannot currently do this, serialising the SPPT implementation classes is too complex
        /*
        serialiser.confgureDatatypeModel(
            """
            namespace net.akehurst.language.agl.sppt {
                SharedPackedParseTreeDefault {
                    composite-val root: SPPTNode,
                    composite-val seasons: Int,
                    composite-val maxNumHeads: Int
                }
            }
            """.trimIndent())
         */
        //classes registered with KotlinxReflect via gradle plugin
    }

    fun confgureFromKompositeString(datatypeModel: String) {
        serialiser.confgureFromKompositeString(datatypeModel)
    }
    fun confgureFromKompositeModel(datatypeModel: DatatypeModel) {
        serialiser.confgureFromKompositeModel(datatypeModel)
    }
    // provided to make testing better
    internal fun toJsonDocument(obj: Any): JsonDocument {
        if (this.initialised.not()) this.initialise()
        return serialiser.toJson(obj, obj)
    }

    fun serialise(obj: Any): String {
        return toJsonDocument(obj).toStringJson()
    }

    fun <T : Any> deserialise(jsonString: String): T {
        if (this.initialised.not()) this.initialise()
        return serialiser.toData<T>(jsonString)
    }

}

