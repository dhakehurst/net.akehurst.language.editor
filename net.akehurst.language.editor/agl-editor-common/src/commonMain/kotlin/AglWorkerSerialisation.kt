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
            initialiseGrammarAsm()
            initialiseMessages()
            initialiseAsmSimple()
            initialised = true
        }
    }

    private fun initialiseMessages() {
        serialiser.confgureDatatypeModel(
            """
            namespace net.akehurst.language.editor.common.messages {
                datatype MessageProcessorCreate {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val grammarStr: String
                }
                datatype MessageSyntaxAnalyserConfigure {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val configuration: Any
                }
                datatype MessageProcessorCreateResponse {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                }
                datatype MessageProcessRequest {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val goalRuleName: String?
                    composite-val text: String
                    composite-val context: Any?
                }
                datatype MessageParseResult {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val tree: Any?
                    composite-val issues: Array<LanguageIssue>
                }
                datatype MessageSyntaxAnalysisResult {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val asm: Any?
                    composite-val issues: Array<LanguageIssue>
                }
                datatype MessageSemanticAnalysisResult {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val issues: Array<LanguageIssue>
                }
                datatype MessageParserInterruptRequest {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val reason: String
                }
                datatype MessageLineTokens {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val lineTokens: Array<Array<AglToken>>
                }
                datatype MessageSetStyle {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val css: String
                }
                datatype MessageSetStyleResult {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                }
                datatype MessageCodeCompleteRequest {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val goalRuleName: String?
                    composite-val text: String
                    composite-val position: Int
                }
                datatype MessageCodeCompleteResult {
                    composite-val languageId : String
                    composite-val editorId : String
                    composite-val sessionId: String
                    composite-val success: Boolean
                    composite-val message: String
                    composite-val completionItems: Array<Pair<String, String>>?
                }
            }
            namespace net.akehurst.language.agl.syntaxAnalyser {
                datatype ContextSimple {
                    composite-val scope: ScopeSimple<AsmElementSimple>
                }
            }
        """.trimIndent()
        )
        //classes registered with KotlinxReflect via gradle plugin
    }

    private fun initialiseAsmSimple() {
        //TODO: add type arg to ScopeSimple<E>
        serialiser.confgureDatatypeModel(
            """
            namespace net.akehurst.language.api.asm {
                datatype ScopeSimple {
                    reference-val parent: ScopeSimple<E>?
                    composite-val forTypeName:String
                    composite-var childScopes:Map<String,ScopeSimple<E>>
                    composite-var items:Map<String,Map<String,E>>
                }
                datatype AsmSimple {
                    composite-var rootElements: List<AsmElementSimple>
                }
                datatype AsmElementSimple {
                    composite-val id: Int
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
        //classes registered with KotlinxReflect via gradle plugin
    }

    private fun initialiseGrammarAsm() {
        serialiser.confgureDatatypeModel(
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
                    composite-val owningGrammar: Grammar
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
                    composite-val associativity: SeparatedListKind
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

    fun configure(datatypeModel: String) {
        serialiser.confgureDatatypeModel(datatypeModel)
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