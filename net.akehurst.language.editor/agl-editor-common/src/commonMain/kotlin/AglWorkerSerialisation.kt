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
            initialiseTypeModel()
            initialiseStyleAsm()
            initialiseScopesAsm()
            initialiseGrammarAsm()
            initialiseMessages()
            initialiseAsmSimple()
            initialiseSPPT()
            initialised = true
        }
    }

    private fun initialiseApiTypes() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.editor.common") {
                dataType("AglToken") {
                    constructorArguments {
                        composite("styles", "Array") { typeArgument("String") }
                        composite("value", "String")
                        composite("line", "Int")
                        composite("column", "Int")
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
                        composite("ruleName", "String")
                        composite("text", "String")
                    }
                }
            }
        })
    }

    private fun initialiseTypeModel() {
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.agl.syntaxAnalyser") {
                dataType("TypeModelFromGrammar") {
                    superTypes("net.akehurst.language.agl.agl.typemodel.TypeModelAbstract")
                }
            }
            namespace("net.akehurst.language.agl.agl.typemodel") {
                dataType("TypeModelSimple") {
                    superTypes("TypeModelAbstract")
                }
                dataType("TypeModelAbstract") {
                    constructorArguments {
                        composite("namespace", "String")
                        composite("name", "String")
                    }
                    mutableProperties {
                        composite("rules", "Map") {
                            typeArgument("String")
                            typeArgument("net.akehurst.language.api.typemodel.RuleType")
                        }
                    }
                }
            }
            namespace("net.akehurst.language.api.typemodel") {
                dataType("TypeModel") {}
                dataType("RuleType") {}
                dataType("StringType") {
                    superTypes("RuleType")
                }
                dataType("AnyType") {
                    superTypes("RuleType")
                }
                dataType("NothingType") {
                    superTypes("RuleType")
                }
                dataType("UnnamedSuperTypeType") {
                    superTypes("RuleType")
                    constructorArguments {
                        reference("subtypes", "List") {
                            typeArgument("RuleType")
                        }
                    }
                }
                dataType("ListSimpleType") {
                    superTypes("RuleType")
                    mutableProperties {
                        reference("elementType", "RuleType")
                    }
                }
                dataType("ListSeparatedType") {
                    superTypes("RuleType")
                    mutableProperties {
                        reference("itemType", "RuleType")
                        reference("separatorType", "RuleType")
                    }
                }
                dataType("StructuredRuleType") {
                    superTypes("RuleType")
                    mutableProperties {
                        composite("property", "Map") {
                            typeArgument("String")
                            typeArgument("PropertyDeclaration")
                        }
                    }
                }
                dataType("TupleType") {
                    superTypes("StructuredRuleType")
                }
                dataType("ElementType") {
                    superTypes("StructuredRuleType")
                    constructorArguments {
                        reference("typeModel", "TypeModel")
                        composite("name", "String")
                    }
                    mutableProperties {
                        reference("supertypes", "Set", false) {
                            typeArgument("ElementType")
                        }
                        reference("subtypes", "List", false) {
                            typeArgument("ElementType")
                        }
                    }
                }
                dataType("PropertyDeclaration") {
                    constructorArguments {
                        reference("owner", "StructuredRuleType")
                        composite("name", "String")
                        composite("type", "RuleType")
                        composite("isNullable", "Boolean")
                        composite("childIndex", "Int")
                    }
                }
            }
        })
    }

    private fun initialiseStyleAsm() {
        //classes registered with KotlinxReflect via gradle plugin
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
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.api.automaton") {
                enumType("ParseAction")
            }
            namespace("net.akehurst.language.agl.grammar.grammar") {
                dataType("ContextFromGrammar") {
                    mutableProperties {
                        composite("rootScope", "ScopeSimple") {
                            typeArgument("String")
                        }
                    }
                }
            }
            namespace("net.akehurst.language.agl.syntaxAnalyser") {
                dataType("ContextSimple") {
                    typeParameters("E")
                    mutableProperties {
                        composite("rootScope", "ScopeSimple") {
                            typeArgument("E")
                        }
                    }
                }
                dataType("ContextFromTypeModel") {
                    mutableProperties {
                        composite("rootScope", "ScopeSimple") {
                            typeArgument("String")
                        }
                    }
                }
            }
            namespace("net.akehurst.language.editor.common.messages") {
                enumType("MessageStatus")
                dataType("MessageProcessorCreate") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("grammarStr", "String", true)
                        composite("scopeModelStr", "String", true)
                    }
                }
                dataType("MessageProcessorCreateResponse") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("issues", "List") { typeArgument("LanguageIssue") }
                    }
                }
                dataType("MessageSyntaxAnalyserConfigure") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("configuration", "Map") {
                            typeArgument("String")
                            typeArgument("Any")
                        }
                    }
                }
                dataType("MessageSyntaxAnalyserConfigureResponse") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("issues", "List") { typeArgument("LanguageIssue") }
                    }
                }
                dataType("MessageProcessRequest") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("goalRuleName", "String", true)
                        composite("text", "String")
                        composite("context", "Any", true)
                    }
                }
                dataType("MessageParseResult") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("issues", "List") { typeArgument("LanguageIssue") }
                        composite("treeSerialised", "String", true)
                    }
                }
                dataType("MessageSyntaxAnalysisResult") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("issues", "List") { typeArgument("LanguageIssue") }
                        composite("asm", "Any", true)
                    }
                }
                dataType("MessageSemanticAnalysisResult") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("issues", "List") { typeArgument("LanguageIssue") }
                        composite("asm", "Any", true)
                    }
                }
                dataType("MessageParserInterruptRequest") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("reason", "String")
                    }
                }
                dataType("MessageLineTokens") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("lineTokens", "Array") { typeArgument("Array") { typeArgument("AglToken") } }
                    }
                }
                dataType("MessageSetStyle") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("css", "String")
                    }
                }
                dataType("MessageSetStyleResult") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                    }
                }
                dataType("MessageCodeCompleteRequest") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("goalRuleName", "String")
                        composite("text", "String")
                        composite("position", "Int")
                    }
                }
                dataType("MessageCodeCompleteResult") {
                    constructorArguments {
                        composite("languageId", "String")
                        composite("editorId", "String")
                        composite("sessionId", "String")
                        composite("status", "MessageStatus")
                        composite("message", "String")
                        composite("completionItems", "Array") { typeArgument("CompletionItem") }
                    }
                }
            }
        })
    }

    private fun initialiseAsmSimple() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.agl.syntaxAnalyser") {
                dataType("ScopeSimple") {
                    typeParameters("AsmElementIdType")
                    constructorArguments {
                        reference("parent", "ScopeSimple") { typeArgument("AsmElementIdType") }
                        composite("forReferenceInParent", "String")
                        composite("forTypeName", "String")
                    }
                    mutableProperties {
                        composite("scopeMap", "Map") {
                            typeArgument("String")
                            typeArgument("ScopeSimple")
                        }
                        composite("childScopes", "Map") {
                            typeArgument("AsmElementIdType")
                            typeArgument("ScopeSimple")
                        }
                        composite("items", "Map") {
                            typeArgument("String")
                            typeArgument("Map") {
                                typeArgument("String")
                                typeArgument("AsmElementIdType")
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
                        reference("childIndex", "Int")
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
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(komposite {
            namespace("net.akehurst.language.api.grammar") {
                dataType("Grammar") {}
            }
            namespace("net.akehurst.language.agl.grammar.grammar") {
                dataType("AglGrammarGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.scopes") {
                dataType("AglScopesGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.style") {
                dataType("AglStyleGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.format") {
                dataType("AglFormatGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.grammar.asm") {
                dataType("NamespaceDefault") {
                    constructorArguments {
                        composite("qualifiedName", "String")
                    }
                }
                dataType("GrammarReferenceDefault") {
                    constructorArguments {
                        composite("localNamespace", "Namespace")
                        composite("nameOrQName", " String")
                    }
                    mutableProperties {
                        reference("resolved", "GrammarAbstract")
                    }
                }
                dataType("GrammarDefault") {
                    superTypes("GrammarAbstract")
                    constructorArguments {
                        composite("namespace", "Namespace")
                        composite("name", "String")
                    }
                }
                dataType("GrammarAbstract") {
                    superTypes("net.akehurst.language.api.grammar.Grammar")
                    constructorArguments {
                        composite("namespace", "Namespace")
                        composite("name", "String")
                    }
                    mutableProperties {
                        composite("extends", "List") {
                            typeArgument("GrammarReferenceDefault")
                        }
                        composite("grammarRule", "List") {
                            typeArgument("GrammarRuleDefault")
                        }
                    }
                }
                dataType("GrammarRuleDefault") {
                    constructorArguments {
                        reference("grammar", "GrammarDefault")
                        composite("name", "String")
                        composite("isOverride", "Boolean")
                        composite("isSkip", "Boolean")
                        composite("isLeaf", "Boolean")
                    }
                    mutableProperties {
                        composite("rhs", "RuleItemAbstract")
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
                        composite("alternative", "List") {
                            typeArgument("ConcatenationDefault")
                        }
                    }
                }
                dataType("ChoicePriorityDefault") {
                    superTypes("ChoiceAbstract")
                    constructorArguments {
                        composite("alternative", "List") {
                            typeArgument("ConcatenationDefault")
                        }
                    }
                }
                dataType("ConcatenationDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("items", "List") {
                            typeArgument("ConcatenationItemAbstract")
                        }
                    }
                }
                dataType("ConcatenationItemAbstract") {
                    superTypes("RuleItemAbstract")
                }
                dataType("SimpleItemAbstract") {
                    superTypes("ConcatenationItemAbstract")
                }
                dataType("GroupDefault") {
                    superTypes("ConcatenationItemAbstract")
                    constructorArguments {
                        composite("choice", "ChoiceAbstract")
                    }
                }
                dataType("NonTerminalDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("name", "String")
                        reference("owningRule", "Rule")
                    }
                }
                dataType("TerminalDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("value", "String")
                        composite("isPattern", "Boolean")
                    }
                }
                dataType("EmbeddedDefault") {
                    superTypes("RuleItemAbstract")
                    constructorArguments {
                        composite("embeddedGoalName", "String")
                        composite("embeddedGrammarReference", "GrammarReferenceDefault")
                    }
                }
                dataType("SeparatedListDefault") {
                    constructorArguments {
                        composite("min", "Int")
                        composite("max", "Int")
                        composite("item", "SimpleItemAbstract")
                        composite("separator", "SimpleItem")
                    }
                }
                dataType("SimpleListDefault") {
                    constructorArguments {
                        composite("min", "Int")
                        composite("max", "Int")
                        composite("item", "SimpleItemAbstract")
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

