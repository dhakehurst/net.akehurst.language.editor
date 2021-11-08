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
import net.akehurst.kotlin.komposite.processor.komposite
import net.akehurst.kotlin.kserialisation.json.KSerialiserJson


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
            initialised = true
        }
    }

    //TODO: define the Komposite defs below using a builder rather than parsed string
// to improve performance
    private fun initialiseApiTypes() {
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
        //classes registered with KotlinxReflect via gradle plugin
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
        //classes registered with KotlinxReflect via gradle plugin
    }

    private fun initialiseMessages() {
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
                    composite-val treeSerialised: String?
                    composite-val issues: List<LanguageIssue>
                }
                datatype MessageSyntaxAnalysisResult {
                    composite-val languageId : String  composite-val editorId : String  composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val asm: Any?
                    composite-val issues: List<LanguageIssue>
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
        )
        //classes registered with KotlinxReflect via gradle plugin
    }

    private fun initialiseAsmSimple() {
        //TODO: add type arg to ScopeSimple<E>
        /*
        serialiser.confgureFromKompositeString(
            """
            namespace net.akehurst.language.api.asm {
                datatype AsmElementPath {
                    composite-val value:String
                }
                datatype ScopeSimple {
                    reference-val parent: ScopeSimple<E>?
                    composite-val forReferenceInParent:String
                    composite-val forTypeName:String
                    
                    composite-var childScopes:Map<String,ScopeSimple<E>>
                    composite-var items:Map<String,Map<String,E>>
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
                        composite("properties","Map") {
                            typeArgument("String")
                            typeArgument("AsmElementProperty")
                        }
                    }
                }
                dataType("AsmElementProperty") {
                    constructorArguments {
                        composite("name", "String")
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
                dataType("ScopeSimple") {
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
        })
    }

    private fun initialiseGrammarAsm() {
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
                datatype ChoiceAbstract : RuleItemAbstract {
                    composite-val alternative: List<ConcatenationDefault>
                }
                datatype ChoiceLongestDefault : ChoiceAbstract { }
                datatype ChoicePriorityDefault : ChoiceAbstract { }
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
        //classes registered with KotlinxReflect via gradle plugin
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

    fun configure(datatypeModel: String) {
        serialiser.confgureFromKompositeString(datatypeModel)
    }

    // provided to make testing better
    internal fun toJsonDocument(obj: Any): JsonDocument {
        if (this.initialised.not()) this.initialise()
        return serialiser.toJson(obj, obj)
    }

    fun serialise(obj: Any): String {
        return toJsonDocument(obj).toJsonString()
    }

    fun <T : Any> deserialise(jsonString: String): T {
        if (this.initialised.not()) this.initialise()
        return serialiser.toData<T>(jsonString)
    }

}