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
import net.akehurst.language.agl.grammar.scopes.ReferenceDefinition
import net.akehurst.language.typemodel.api.PropertyCharacteristic
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.api.typeModel


object AglWorkerSerialisation {

    private var initialised = false
    private val serialiser = KSerialiserJson().also {
        it.registerKotlinStdPrimitives()
    }

    private fun initialise() {
        if (!initialised) {
            agl_editor_common_commonMain.KotlinxReflectForModule.registerUsedClasses()
            //TODO: enable kserialisation/komposite/reflect to auto add these some how!!
            initialiseApiTypes()
            initialiseTypeModel()
            initialiseStyleAsm()
            initialiseScopesAsm()
            initialiseGrammarAsm()
            initialiseMessages()
            initialiseAsmSimple()
            initialiseSPPT()
            serialiser.registry.resolveImports()
            initialised = true
        }
    }

    private fun initialiseApiTypes() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("ApiType") {
            namespace("net.akehurst.language.editor.common", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglToken") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styles", "Array", listOf("String"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "line", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "column", "Int")
                }
            }
            namespace("net.akehurst.language.api.parser", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("InputLocation") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "column", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "line", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "length", "Int")
                }
            }
            namespace("net.akehurst.language.api.processor", imports = listOf("kotlin", "kotlin.collections")) {
                enumType("LanguageIssueKind", emptyList())
                enumType("LanguageProcessorPhase", emptyList())
                dataType("LanguageIssue") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "kind", "LanguageIssueKind")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "phase", "LanguageProcessorPhase")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "location", "InputLocation")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "data", "Any")
                }
                dataType("CompletionItem") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "ruleName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                }
            }
        })
    }

    private fun initialiseTypeModel() {
        serialiser.confgureFromKompositeModel(typeModel("TypeModel") {
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("TypeModelFromGrammar") {
                    superTypes("net.akehurst.language.agl.agl.typemodel.TypeModelAbstract")
                }
            }
            namespace("net.akehurst.language.agl.agl.typemodel", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("TypeModelSimple") {
                    superTypes("TypeModelAbstract")
                }
                dataType("TypeModelAbstract") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "rules", "Map", listOf("String", "net.akehurst.language.api.typemodel.RuleType"))
                }
            }
            namespace("net.akehurst.language.api.typemodel", imports = listOf("kotlin", "kotlin.collections")) {
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
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "subtypes", "List", listOf("RuleType"))
                }
                dataType("ListSimpleType") {
                    superTypes("RuleType")

                    propertyOf(setOf(MEMBER, REFERENCE), "elementType", "RuleType")
                }
                dataType("ListSeparatedType") {
                    superTypes("RuleType")

                    propertyOf(setOf(MEMBER, REFERENCE), "itemType", "RuleType")
                    propertyOf(setOf(MEMBER, REFERENCE), "separatorType", "RuleType")
                }
                dataType("StructuredRuleType") {
                    superTypes("RuleType")

                    propertyOf(setOf(MEMBER, COMPOSITE), "property", "Map", listOf("String", "PropertyDeclaration"))
                }
                dataType("TupleType") {
                    superTypes("StructuredRuleType")
                }
                dataType("ElementType") {
                    superTypes("StructuredRuleType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "typeModel", "TypeModel")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, REFERENCE), "supertypes", "Set", listOf("ElementType"))
                    propertyOf(setOf(MEMBER, REFERENCE), "subtypes", "List", listOf("ElementType"))
                }
                dataType("PropertyDeclaration") {
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "owner", "StructuredRuleType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "type", "RuleType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isNullable", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "childIndex", "Int")
                }
            }
        })
    }

    private fun initialiseStyleAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("StyleAsm") {
            namespace("net.akehurst.language.agl.grammar.style", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleModelDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rules", "List", listOf("net.akehurst.language.api.style.AglStyleRule"))
                }
            }
            namespace("net.akehurst.language.api.style", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleRule") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "selector", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "styles", "Map", listOf("String", "AglStyle"))

                }
                dataType("AglStyle") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                }
            }
        })
    }

    private fun initialiseScopesAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("ScopesAsm") {
            namespace("net.akehurst.language.agl.grammar.scopes", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("ScopeModelAgl") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "scopes", "Map",listOf("String","ScopeDefinition"))
                    propertyOf(setOf(MEMBER, COMPOSITE), "references", "List",listOf("ReferenceDefinition"))
                }
                dataType("ScopeDefinition") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeFor", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "identifiables", "List", listOf("Identifiable"))
                }
                dataType("Identifiable") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "propertyName", "String")
                }
                dataType("ReferenceDefinition") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "inTypeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "referringPropertyName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "refersToTypeName", "List", listOf("String"))
                }
            }
        })
    }

    private fun initialiseMessages() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("Messages") {
            namespace("net.akehurst.language.api.automaton", imports = listOf(
                "kotlin", "kotlin.collections")) {
                enumType("ParseAction", emptyList())
            }
            namespace("net.akehurst.language.agl.grammar.grammar", imports = listOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.syntaxAnalyser")) {
                dataType("ContextFromGrammar") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple",listOf("String"))
                }
            }
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = listOf("kotlin", "kotlin.collections","net.akehurst.language.agl.syntaxAnalyser")) {
                dataType("ContextSimple") {
                    typeParameters("E")
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple",listOf("E"))
                }
                dataType("ContextFromTypeModel") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple",listOf("String"))
                }
            }
            namespace("net.akehurst.language.editor.common.messages", imports = listOf("kotlin", "kotlin.collections")) {
                enumType("MessageStatus", emptyList())
                dataType("MessageProcessorCreate") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "grammarStr", "String", emptyList(), true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeModelStr", "String", emptyList(), true)
                }
                dataType("MessageProcessorCreateResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                }
                dataType("MessageSyntaxAnalyserConfigure") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "configuration", "Map",listOf("String","Any"))
                }
                dataType("MessageSyntaxAnalyserConfigureResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                }
                dataType("MessageProcessRequest") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "goalRuleName", "String", emptyList(),true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "context", "Any",  emptyList(),true)
                }
                dataType("MessageParseResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List",listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeSerialised", "String", emptyList(),true)
                }
                dataType("MessageSyntaxAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", emptyList(),true)
                }
                dataType("MessageSemanticAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", emptyList(),true)
                }
                dataType("MessageParserInterruptRequest") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "reason", "String")
                }
                dataType("MessageLineTokens") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "lineTokens", "Array", listOf("Array","AglToken"))
                }
                dataType("MessageSetStyle") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "css", "String")
                }
                dataType("MessageSetStyleResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                }
                dataType("MessageCodeCompleteRequest") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "goalRuleName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                }
                dataType("MessageCodeCompleteResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "completionItems", "Array", listOf("CompletionItem"))
                }
            }
        })
    }

    private fun initialiseAsmSimple() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("AsmSimple") {
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("ScopeSimple") {
                    typeParameters("AsmElementIdType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "parent", "ScopeSimple",listOf("AsmElementIdType"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forReferenceInParent", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forTypeName", "String")

                        propertyOf(setOf(MEMBER, COMPOSITE), "scopeMap", "Map",listOf("String","ScopeSimple"))
                        propertyOf(setOf(MEMBER, COMPOSITE), "childScopes", "Map",listOf("AsmElementIdType","ScopeSimple"))
                        propertyOf(setOf(MEMBER, COMPOSITE), "items", "Map") {
                            typeArgument("String")
                            typeArgument("Map") {
                                typeArgument("String")
                                typeArgument("AsmElementIdType")
                            }
                        }
                }
            }
            namespace("net.akehurst.language.api.asm", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AsmElementPath") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                }
                dataType("AsmSimple") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootElements", "List", listOf("AsmElementSimple"))
                }
                dataType("AsmElementSimple") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asmPath", "AsmElementPath")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "asm", "AsmSimple")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeName", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "properties", "Map", listOf("String", "AsmElementProperty"))
                }
                dataType("AsmElementProperty") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "childIndex", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "Any")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isReference", "Boolean")
                }
                dataType("AsmElementReference") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "reference", "String")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "value", "AsmElementSimple")
                }
            }
        })
    }

    private fun initialiseGrammarAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("GrammarAsm") {
            namespace("net.akehurst.language.api.grammar", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("Grammar") {}
                dataType("RuleItem") {}
            }
            namespace("net.akehurst.language.agl.grammar.grammar", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglGrammarGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.scopes", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglScopesGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.style", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.format", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("AglFormatGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.grammar.asm", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("NamespaceDefault") {

                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")

                }
                dataType("GrammarReferenceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "localNamespace", "Namespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nameOrQName", " String")

                    propertyOf(setOf(MEMBER, REFERENCE), "resolved", "GrammarAbstract")
                }
                dataType("GrammarDefault") {
                    superTypes("GrammarAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "Namespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("GrammarAbstract") {
                    superTypes("net.akehurst.language.api.grammar.Grammar")

                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "Namespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "extends", "List", listOf("GrammarReferenceDefault"))
                    propertyOf(setOf(MEMBER, COMPOSITE), "grammarRule", "List", listOf("GrammarRuleDefault"))
                }
                dataType("GrammarRuleDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isOverride", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isLeaf", "Boolean")

                    propertyOf(setOf(MEMBER, REFERENCE), "grammar", "GrammarDefault")
                    propertyOf(setOf(MEMBER, COMPOSITE), "rhs", "RuleItemAbstract")
                }
                dataType("RuleItemAbstract") {
                    superTypes("net.akehurst.language.api.grammar.RuleItem")
                }
                dataType("EmptyRuleDefault") {
                    superTypes("RuleItemAbstract")
                }
                dataType("ChoiceAbstract") {
                    superTypes("RuleItemAbstract")
                }
                dataType("ChoiceLongestDefault") {
                    superTypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List", listOf("net.akehurst.language.api.grammar.RuleItem"))
                }
                dataType("ChoicePriorityDefault") {
                    superTypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List", listOf("net.akehurst.language.api.grammar.RuleItem"))
                }
                dataType("ChoiceAmbiguousDefault") {
                    superTypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List", listOf("net.akehurst.language.api.grammar.RuleItem"))
                }
                dataType("ConcatenationDefault") {
                    superTypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "items", "List", listOf("net.akehurst.language.api.grammar.RuleItem"))
                }
                dataType("ConcatenationItemAbstract") {
                    superTypes("RuleItemAbstract")
                }
                dataType("SimpleItemAbstract") {
                    superTypes("ConcatenationItemAbstract")
                }
                dataType("GroupDefault") {
                    superTypes("ConcatenationItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "groupedContent", "net.akehurst.language.api.grammar.RuleItem")
                }
                dataType("NonTerminalDefault") {
                    superTypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
//                        reference("owningRule", "Rule")
                }
                dataType("TerminalDefault") {
                    superTypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isPattern", "Boolean")
                }
                dataType("EmbeddedDefault") {
                    superTypes("RuleItemAbstract")

                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "embeddedGoalName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "embeddedGrammarReference", "GrammarReferenceDefault")
                }
                dataType("SeparatedListDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "min", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "max", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "item", "SimpleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "separator", "SimpleItem")
                }
                dataType("SimpleListDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "min", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "max", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "item", "SimpleItemAbstract")
                }
                dataType("OptionalItemDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "item", "net.akehurst.language.api.grammar.RuleItem")
                }
            }
        })
    }

    private fun initialiseSPPT() {
        serialiser.confgureFromKompositeModel(typeModel("SPPT") {
            namespace("net.akehurst.language.agl.sppt", imports = listOf("kotlin", "kotlin.collections")) {
                dataType("TreeDataComplete") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forStateSetNumber", "Int")

                    propertyOf(setOf(MEMBER, COMPOSITE), "root", "CN", emptyList(), true)
                    propertyOf(setOf(MEMBER, COMPOSITE), "initialSkip", "TreeDataComplete", listOf("CN"), true)
                    propertyOf(setOf(MEMBER, COMPOSITE), "completeChildren", "Map", emptyList(),false) {
                        typeArgument("CN")
                        typeArgument("Map") {
                            typeArgument("Int")
                            typeArgument("List") {
                                typeArgument("CN")
                            }
                        }
                    }
                }
            }
        })
    }

    fun confgureFromKompositeString(datatypeModel: String) {
        serialiser.confgureFromKompositeString(datatypeModel)
    }

    fun confgureFromKompositeModel(datatypeModel: TypeModel) {
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

