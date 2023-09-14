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
            namespace("net.akehurst.language.editor.common", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglToken") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styles", "Array", listOf("String"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "line", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "column", "Int")
                }
            }
            namespace("net.akehurst.language.api.parser", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("InputLocation") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "column", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "line", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "length", "Int")
                }
            }
            namespace("net.akehurst.language.api.processor", imports = mutableListOf("kotlin", "kotlin.collections")) {
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
            namespace(
                "net.akehurst.language.agl.syntaxAnalyser",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.grammarTypeModel")
            )
            {
                dataType("GrammarTypeNamespaceFromGrammar") {
                    superTypes("GrammarTypeNamespaceAbstract")
                }
            }
            namespace(
                "net.akehurst.language.agl.grammarTypeModel",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.typemodel.simple")
            )
            {
                dataType("GrammarTypeNamespaceSimple") {
                    superTypes("GrammarTypeNamespaceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "importsStr", "List") { typeArgument("String") }
                }
                dataType("GrammarTypeNamespaceAbstract") {
                    superTypes("TypeNamespaceAbstract") TODO("fix this...seems to be createing rather than finding!")

                    propertyOf(setOf(MEMBER, COMPOSITE), "allRuleNameToType", "Map") {
                        typeArgument("String")
                        typeArgument("TypeInstance")
                    }
                }
            }
            namespace(
                "net.akehurst.language.typemodel.simple",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.api.typemodel")
            )
            {
                dataType("SimpleTypeModelStdLib") {
                    superTypes("TypeNamespaceAbstract")
                }
                dataType("TypeModelSimple") {
                    superTypes("TypeModelAbstract")
                    propertyOf(setOf(CONSTRUCTOR), "name", "String")
                }
                dataType("TypeModelAbstract") {
                    superTypes("TypeModel")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "namespace", "Map") {
                        typeArgument("String")
                        typeArgument("TypeNamespace")
                    }
                    propertyOf(setOf(MEMBER, REFERENCE), "allNamespace", "List") { typeArgument("TypeNamespace") }
                    //propertyOf(setOf(MEMBER, COMPOSITE), "rules", "Map", listOf("String", "net.akehurst.language.api.typemodel.RuleType"))
                }
                dataType("TypeInstanceSimple") {
                    superTypes("TypeInstance")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isNullable", "Boolean")
                }
                dataType("TypeNamespaceSimple") {
                    superTypes("TypeNamespaceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "importsStr", "List") { typeArgument("String") }
                }
                dataType("TypeNamespaceAbstract") {
                    superTypes("TypeNamespace")

                    propertyOf(setOf(MEMBER, COMPOSITE), "allTypesByName", "Map") {
                        typeArgument("String")
                        typeArgument("TypeDefinition")
                    }
                }
                dataType("TypeDefinitionAbstract") {
                    superTypes("TypeDefinition")
                    propertyOf(setOf(MEMBER, COMPOSITE), "typeParameters", "List") { typeArgument("String") }
                }
                dataType("PrimitiveTypeSimple") {
                    superTypes("TypeDefinitionSimpleAbstract", "PrimitiveType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("EnumTypeSimple") {
                    superTypes("TypeDefinitionSimpleAbstract", "EnumType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "literals", "List") { typeArgument("String") }
                }
                dataType("UnnamedSuperTypeTypeSimple") {
                    superTypes("TypeDefinitionSimpleAbstract", "UnnamedSuperTypeType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "id", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "subtypes", "List") { typeArgument("TypeInstance") }
                }
                dataType("StructuredTypeSimpleAbstract") {
                    superTypes("TypeDefinitionSimpleAbstract", "StructuredTypeSimple")
                    propertyOf(setOf(MEMBER, COMPOSITE), "properties", "Map") {
                        typeArgument("Int")
                        typeArgument("PropertyDeclaration")
                    }
                    propertyOf(setOf(MEMBER, REFERENCE), "property", "Map") {
                        typeArgument("String")
                        typeArgument("PropertyDeclaration")
                    }
                }
                dataType("TupleTypeSimple") {
                    superTypes("StructuredTypeSimpleAbstract", "TupleType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")

                }
                dataType("DataTypeSimple") {
                    superTypes("StructuredTypeSimpleAbstract", "DataType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, REFERENCE), "supertypes", "List", listOf("DataType"))
                    propertyOf(setOf(MEMBER, REFERENCE), "subtypes", "List", listOf("DataType"))
                }
                dataType("CollectionTypeSimple") {
                    superTypes("StructuredTypeSimpleAbstract", "CollectionType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeParameters", "String")

                    propertyOf(setOf(MEMBER, REFERENCE), "supertypes", "List", listOf("CollectionType"))
                }
                dataType("PropertyDeclarationSimple") {
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "owner", "StructuredType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "characteristics", "Set") { typeArgument("PropertyCharacteristic") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "index", "Int")
                }
                enumType("PropertyCharacteristic", listOf())
            }
            namespace("net.akehurst.language.api.typemodel", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("TypeModel") { }
                dataType("TypeNamespace") {}
                dataType("TypeInstance") {}
                dataType("TypeDefinition") {}
                dataType("PrimitiveType") {
                    superTypes("TypeDefinition")
                }
                dataType("EnumType") {
                    superTypes("TypeDefinition")
                }
                dataType("StructuredType") {
                    superTypes("TypeDefinition")
                }
                dataType("TupleType") {
                    superTypes("StructuredRuleType")
                }
                dataType("DataType") {
                    superTypes("StructuredRuleType")
                }
                dataType("PropertyDeclaration") {
                }
                enumType("PropertyCharacteristic", listOf())
                dataType("UnnamedSuperTypeType") {
                    superTypes("TypeDefinition")
                }
                dataType("CollectionType") {
                    superTypes("TypeDefinition")
                }
            }
        })
    }

    private fun initialiseStyleAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.confgureFromKompositeModel(typeModel("StyleAsm") {
            namespace("net.akehurst.language.agl.grammar.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleModelDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rules", "List", listOf("net.akehurst.language.api.style.AglStyleRule"))
                }
            }
            namespace("net.akehurst.language.api.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleRule") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "selector", "AglStyleSelector")

                    propertyOf(setOf(MEMBER, COMPOSITE), "styles", "Map", listOf("String", "AglStyle"))
                }
                dataType("AglStyleSelector") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "kind", "AglStyleSelectorKind")
                }
                enumType("AglStyleSelectorKind", listOf("LITERAL", "PATTERN", "RULE_NAME"))
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
            namespace("net.akehurst.language.agl.grammar.scopes", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("ScopeModelAgl") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "scopes", "Map", listOf("String", "ScopeDefinition"))
                    propertyOf(setOf(MEMBER, COMPOSITE), "references", "List", listOf("ReferenceDefinition"))
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
            namespace(
                "net.akehurst.language.api.automaton", imports = mutableListOf(
                    "kotlin", "kotlin.collections"
                )
            ) {
                enumType("ParseAction", emptyList())
            }
            namespace("net.akehurst.language.agl.grammar.grammar", imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.syntaxAnalyser")) {
                dataType("ContextFromGrammar") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple", listOf("String"))
                }
            }
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.syntaxAnalyser")) {
                dataType("ContextSimple") {
                    typeParameters("E")
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple", listOf("E"))
                }
                dataType("ContextFromTypeModel") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple", listOf("String"))
                }
            }
            namespace("net.akehurst.language.editor.common.messages", imports = mutableListOf("kotlin", "kotlin.collections")) {
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "configuration", "Map", listOf("String", "Any"))
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "goalRuleName", "String", emptyList(), true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "context", "Any", emptyList(), true)
                }
                dataType("MessageParseResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeSerialised", "String", emptyList(), true)
                }
                dataType("MessageSyntaxAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", emptyList(), true)
                }
                dataType("MessageSemanticAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", emptyList(), true)
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "lineTokens", "Array", listOf("Array", "AglToken"))
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
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("ScopeSimple") {
                    typeParameters("AsmElementIdType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "parent", "ScopeSimple", listOf("AsmElementIdType"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forReferenceInParent", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forTypeName", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "scopeMap", "Map", listOf("String", "ScopeSimple"))
                    propertyOf(setOf(MEMBER, COMPOSITE), "childScopes", "Map", listOf("AsmElementIdType", "ScopeSimple"))
                    propertyOf(setOf(MEMBER, COMPOSITE), "items", "Map") {
                        typeArgument("String")
                        typeArgument("Map") {
                            typeArgument("String")
                            typeArgument("AsmElementIdType")
                        }
                    }
                }
            }
            namespace("net.akehurst.language.api.asm", imports = mutableListOf("kotlin", "kotlin.collections")) {
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
            namespace("net.akehurst.language.api.grammar", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("Grammar") {}
                dataType("RuleItem") {}
            }
            namespace("net.akehurst.language.agl.grammar.grammar", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglGrammarGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.scopes", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglScopesGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.format", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglFormatGrammar") {
                    superTypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.grammar.grammar.asm", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("NamespaceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                }
                dataType("GrammarReferenceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "localNamespace", "NamespaceDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nameOrQName", " String")

                    propertyOf(setOf(MEMBER, REFERENCE), "resolved", "GrammarAbstract")
                }
                dataType("GrammarDefault") {
                    superTypes("GrammarAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "NamespaceDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("GrammarAbstract") {
                    superTypes("net.akehurst.language.api.grammar.Grammar")

                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "NamespaceDefault")
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
            namespace("net.akehurst.language.agl.sppt", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("TreeDataComplete") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forStateSetNumber", "Int")

                    propertyOf(setOf(MEMBER, COMPOSITE), "root", "CN", emptyList(), true)
                    propertyOf(setOf(MEMBER, COMPOSITE), "initialSkip", "TreeDataComplete", listOf("CN"), true)
                    propertyOf(setOf(MEMBER, COMPOSITE), "completeChildren", "Map", emptyList(), false) {
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

