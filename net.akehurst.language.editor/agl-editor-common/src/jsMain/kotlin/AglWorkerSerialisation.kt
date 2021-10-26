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

package net.akehurst.language.editor.common

import net.akehurst.kotlin.kserialisation.json.KSerialiserJson
import net.akehurst.kotlinx.reflect.ModuleRegistry
import net.akehurst.language.agl.grammar.grammar.asm.*
import net.akehurst.language.api.asm.AsmElementProperty
import net.akehurst.language.api.asm.AsmElementReference
import net.akehurst.language.api.asm.AsmElementSimple
import net.akehurst.language.api.asm.AsmSimple
import net.akehurst.language.editor.common.serialisation.GrammarSerialisation

object AglWorkerSerialisation {

    private var initialised = false
    private val serialiser = KSerialiserJson().also {
        it.registerKotlinStdPrimitives()
    }

    private fun initialise() {
        if (!initialised) {
            //TODO: enable kserialisation/komposite/reflect to auto add these some how!!
            initialiseGrammarAsm()
            initialised = true
        }
    }
    private fun initialiseMessages() {
        serialiser.confgureDatatypeModel(
            """
            namespace net.akehurst.language.editor.common {
                datatype AglWorkerMessage {
                    val languageId : String
                    val editorId : String
                    val sessionId: String
                }
                datatype MessageProcessorCreate : AglWorkerMessage {
                    val grammarStr: String
                }
                datatype MessageSyntaxAnalyserConfigure {
                    val configuration: Any
                }
                datatype MessageProcessorCreateResponse {
                    val success: Boolean
                    val message: String
                }
                datatype MessageProcessRequest {
                    val goalRuleName: String?
                    val text: String
                    val context: Any?
                }
            }
        """.trimIndent()
        )
        //TODO: more datatypemodel
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageProcessorCreate", MessageProcessorCreate::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageSyntaxAnalyserConfigure", MessageSyntaxAnalyserConfigure::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageProcessorCreateResponse", MessageProcessorCreateResponse::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageProcessRequest", MessageProcessRequest::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageParseResult", MessageParseResult::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageSyntaxAnalysisResult", MessageSyntaxAnalysisResult::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageSemanticAnalysisResult", MessageSemanticAnalysisResult::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageParserInterruptRequest", MessageParserInterruptRequest::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageLineTokens", MessageLineTokens::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageSetStyle", MessageSetStyle::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageSetStyleResult", MessageSetStyleResult::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageCodeCompleteRequest", MessageCodeCompleteRequest::class)
        ModuleRegistry.registerClass("net.akehurst.language.editor.common.MessageCodeCompleteResult", MessageCodeCompleteResult::class)
    }
    private fun initialiseAsmSimple() {
        serialiser.confgureDatatypeModel(
            """
            namespace net.akehurst.language.api.asm {
                datatype AsmSimple {
                    car rootElements: List<AsmElementSimple>
                }
                datatype AsmElementSimple {

                }
                datatype AsmElementProperty {

                }
                datatype AsmElementReference {
                
                }
            }
        """.trimIndent()
        )
        ModuleRegistry.registerClass("net.akehurst.language.api.asm.AsmSimple", AsmSimple::class)
        ModuleRegistry.registerClass("net.akehurst.language.api.asm.AsmSimple", AsmElementSimple::class)
        ModuleRegistry.registerClass("net.akehurst.language.api.asm.AsmSimple", AsmElementProperty::class)
        ModuleRegistry.registerClass("net.akehurst.language.api.asm.AsmSimple", AsmElementReference::class)
    }
    private fun initialiseGrammarAsm() {
        serialiser.confgureDatatypeModel(
            """
            namespace net.akehurst.language.agl.grammar.grammar.asm {
                datatype NamespaceDefault {
                    cal qualifiedName: String
                }
                datatype GrammarDefault {
                    cal namespace : Namespace
                    cal name : String
                    car rule : List<RuleItemAbstract>
                }
                datatype RuleDefault {
                    val grammar: GrammarDefault
                    cal name: String
                    cal isOverride: Boolean
                    cal isSkip: Boolean
                    cal isLeaf: Boolean
                    car rhs: RuleItemAbstract
                }
                datatype RuleItemAbstract { }
                datatype EmptyRuleDefault : RuleItemAbstract { }
                datatype ChoiceAbstract : RuleItemAbstract {
                    cal alternative: List<ConcatenationDefault>
                }
                datatype ChoiceLongestDefault : ChoiceAbstract { }
                datatype ChoicePriorityDefault : ChoiceAbstract { }
                datatype ConcatenationDefault : RuleItemAbstract {
                    cal items: List<ConcatenationItem>
                }
                datatype ConcatenationItemAbstract : RuleItemAbstract { }
                datatype SimpleItemAbstract : ConcatenationItemAbstract { }
                datatype GroupDefault : SimpleItemAbstract {
                    val choice : ChoiceAbstract
                }
                datatype NonTerminalDefault : RuleItemAbstract {
                    val name: String
                    val owningGrammar: Grammar
                    val embedded: Boolean
                }
                datatype TerminalDefault : RuleItemAbstract {
                    val value: String
                    val isPattern: Boolean
                }
                datatype SeparatedListDefault : RuleItemAbstract {
                    val min: Int
                    val max: Int
                    val item: SimpleItemAbstract
                    val separator: SimpleItem,
                    val associativity: SeparatedListKind
                }
                datatype SimpleListDefault : RuleItemAbstract {
                    val min: Int
                    val max: Int
                    val item: SimpleItemAbstract
                }
            }
        """.trimIndent()
        )
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.NamespaceDefault", NamespaceDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.GrammarDefault", GrammarDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.RuleDefault", RuleDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.ConcatenationDefault", ConcatenationDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.ChoiceLongestDefault", ChoiceLongestDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.ChoicePriorityDefault", ChoicePriorityDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.EmptyRuleDefault", EmptyRuleDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.NonTerminalDefault", NonTerminalDefault::class)
        ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.TerminalDefault", TerminalDefault::class)
    }

    fun configure(datatypeModel: String) {
        serialiser.confgureDatatypeModel(datatypeModel)
    }

    fun serialise(obj: Any): String {
        return serialiser.toJson(obj, obj).toJsonString()
    }

    fun <T : Any> deserialise(jsonString: String): T? {
        return serialiser.toData<T>(jsonString)
    }

}