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
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.api.typeModel

//
// This will only work if all classes are *public* (exported for JS) and *forReflection*
// make sure the relevant packages are marked in the 'exportPublic' and 'kotlinxReflect' configurations
//
//

object AglWorkerSerialisation {

    private var initialised = false
    private val serialiser = KSerialiserJson().also {
        it.registerKotlinStdPrimitives()
    }

    private fun initialise() {
        if (!initialised) {
            agl_language_service_serialisation_commonMain.KotlinxReflectForModule.registerUsedClasses()
            //TODO: enable kserialisation/komposite/reflect to auto add these some how!!
            initialiseApiTypes()
            initialiseTypeModel()
            initialiseExpressionsAsm()
            initialiseStyleAsm()
            initialiseCrossReferencesAsm()
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
        serialiser.configureFromTypeModel(typeModel("ApiType", false) {
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
            namespace("net.akehurst.language.agl.processor", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("ScanOptionsDefault") {

                }
                dataType("ParseOptionsDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "goalRuleName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "reportErrors", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "reportGrammarAmbiguities", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "cacheSkip", "Boolean")
                }
                dataType("SyntaxAnalysisOptionsDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "active", "Boolean")
                }
                dataType("SemanticAnalysisOptionsDefault") {
                    typeParameters("AsmType","ContextType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "active", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "locationMap", "Map") {
                        typeArgument("Any")
                        typeArgument("InputLocation")
                    }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "context", "ContextType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "checkReferences", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "resolveReferences", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "other", "Map") {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                }
                dataType("CompletionProviderOptionsDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "context", "ContextType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "other", "Map") {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                }
                dataType("ProcessOptionsDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scan", "ScanOptionsDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "parse", "ParseOptionsDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "syntaxAnalysis", "SyntaxAnalysisOptionsDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "semanticAnalysis", "SemanticAnalysisOptionsDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "completionProvider", "CompletionProviderOptionsDefault")
                }
            }
        })
    }

    private fun initialiseTypeModel() {
        serialiser.configureFromTypeModel(typeModel("TypeModel", false) {
/*            namespace(
                "net.akehurst.language.agl.default",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.grammarTypeModel")
            )
            {
                dataType("GrammarTypeNamespaceFromGrammar") {
                    supertypes("GrammarTypeNamespaceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "imports", "List") { typeArgument("String") }
                }
            }*/
            namespace(
                "net.akehurst.language.agl.grammarTypeModel",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.typemodel.simple")
            )
            {
                dataType("GrammarTypeNamespaceSimple") {
                    supertypes("GrammarTypeNamespaceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "imports", "List") { typeArgument("String") }
                }
                dataType("GrammarTypeNamespaceAbstract") {
                    supertypes("TypeNamespaceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "imports", "List") { typeArgument("String") }

                    propertyOf(setOf(MEMBER, COMPOSITE), "allRuleNameToType", "Map") {
                        typeArgument("String")
                        typeArgument("TypeInstance")
                    }
                }
            }
            namespace(
                "net.akehurst.language.typemodel.simple",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.typemodel.api")
            )
            {
                singleton("SimpleTypeModelStdLib")
                dataType("TypeModelSimple") {
                    supertypes("TypeModelSimpleAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("TypeModelSimpleAbstract") {
                    supertypes("TypeModel")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "namespace", "Map") {
                        typeArgument("String")
                        typeArgument("TypeNamespace")
                    }
                    propertyOf(setOf(MEMBER, REFERENCE), "allNamespace", "List") { typeArgument("TypeNamespace") }
                    //propertyOf(setOf(MEMBER, COMPOSITE), "rules", "Map", listOf("String", "net.akehurst.language.api.typemodel.RuleType"))
                }
                dataType("TypeInstanceAbstract") {
                    supertypes("TypeInstance")
                }
                dataType("TypeInstanceSimple") {
                    supertypes("TypeInstanceAbstract")
                    //propertyOf(setOf(CONSTRUCTOR, REFERENCE), "context", "TypeDeclaration")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "contextQualifiedTypeName", "String", emptyList(), true)
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedOrImportedTypeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isNullable", "Boolean")
                }
                dataType("TupleTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "declaration", "TupleType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isNullable", "Boolean")
                }
                dataType("UnnamedSupertypeTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "declaration", "UnnamedSupertypeType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isNullable", "Boolean")
                }
                dataType("TypeNamespaceAbstract") {
                    supertypes("TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "imports", "List") { typeArgument("String") }
                    propertyOf(setOf(MEMBER, COMPOSITE), "ownedUnnamedSupertypeType", "List") {
                        typeArgument("UnnamedSupertypeTypeSimple")
                    }
                    propertyOf(setOf(MEMBER, COMPOSITE), "ownedTupleTypes", "List") {
                        typeArgument("TupleTypeSimple")
                    }
                    propertyOf(setOf(MEMBER, COMPOSITE), "ownedTypesByName", "Map") {
                        typeArgument("String")
                        typeArgument("TypeDeclaration")
                    }
                }
                dataType("TypeNamespaceSimple") {
                    supertypes("TypeNamespaceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "imports", "List") { typeArgument("String") }
                }
                dataType("TypeDeclarationSimpleAbstract") {
                    supertypes("TypeDeclaration")
                    propertyOf(setOf(MEMBER, COMPOSITE), "typeParameters", "List") { typeArgument("String") }

                    propertyOf(setOf(MEMBER, COMPOSITE), "propertyByIndex", "Map") {
                        typeArgument("Int")
                        typeArgument("PropertyDeclaration")
                    }
                }
                dataType("SpecialTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("PrimitiveTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "PrimitiveType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("EnumTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "EnumType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "literals", "List") { typeArgument("String") }
                }
                dataType("UnnamedSupertypeTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "UnnamedSupertypeType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "id", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "subtypes", "List") { typeArgument("TypeInstance") }
                }
                dataType("StructuredTypeSimpleAbstract") {
                    supertypes("TypeDeclarationSimpleAbstract", "StructuredType")
                }
                dataType("TupleTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "TupleType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "id", "Int")
                }
                dataType("DataTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "DataType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, REFERENCE), "supertypes", "List", listOf("DataType"))
                    propertyOf(setOf(MEMBER, REFERENCE), "subtypes", "List", listOf("DataType"))
                }
                dataType("CollectionTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "CollectionType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeParameters", "String")

                    propertyOf(setOf(MEMBER, REFERENCE), "supertypes", "List", listOf("CollectionType"))
                }
                dataType("PropertyDeclarationPrimitive") {
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "owner", "StructuredType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "description", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "index", "Int")
                }
                dataType("PropertyDeclarationDerived") {
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "owner", "StructuredType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "description", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "expression", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "index", "Int")
                }
                dataType("PropertyDeclarationStored") {
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "owner", "StructuredType")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "characteristics", "Set") { typeArgument("PropertyCharacteristic") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "index", "Int")
                }
            }
            namespace("net.akehurst.language.typemodel.api", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("TypeModel") { }
                dataType("TypeNamespace") {}
                dataType("TypeInstance") {}
                dataType("TypeDeclaration") {}
                dataType("PrimitiveType") {
                    supertypes("TypeDeclaration")
                }
                dataType("EnumType") {
                    supertypes("TypeDeclaration")
                }
                dataType("StructuredType") {
                    supertypes("TypeDeclaration")
                }
                dataType("TupleType") {
                    supertypes("StructuredType")
                }
                dataType("DataType") {
                    supertypes("StructuredType")
                }
                dataType("PropertyDeclaration") {
                }
                enumType("PropertyCharacteristic", listOf())
                dataType("UnnamedSupertypeType") {
                    supertypes("TypeDeclaration")
                }
                dataType("CollectionType") {
                    supertypes("TypeDeclaration")
                }
            }
        })
    }

    private fun initialiseStyleAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("StyleAsm", false) {
            namespace("net.akehurst.language.agl.language.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleGrammar") {
                    supertypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.language.style.asm", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleModelDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rules", "List", listOf("net.akehurst.language.api.style.AglStyleRule"))
                }
            }
            namespace("net.akehurst.language.api.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleRule") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "selector", "AglStyleSelector")

                    propertyOf(setOf(MEMBER, COMPOSITE), "styles", "Map") {
                        typeArgument("String")
                        typeArgument("AglStyle")
                    }
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

    private fun initialiseExpressionsAsm() {
        serialiser.configureFromTypeModel(typeModel("ExpressionsAsm", false) {
            namespace("net.akehurst.language.agl.language.expressions", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("RootExpressionDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                }
                dataType("NavigationDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "List", listOf("String"))
                }
            }
        })
    }

    private fun initialiseCrossReferencesAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("CrossReferencesAsm", false) {
            namespace("net.akehurst.language.agl.language.reference", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("ReferencesGrammar") {
                    supertypes("GrammarAbstract")
                }
            }
            namespace(
                "net.akehurst.language.agl.language.reference.asm",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.language.expressions")
            ) {
                dataType("CrossReferenceModelDefault") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "declarationsForNamespace", "Map") {
                        typeArgument("String")
                        typeArgument("DeclarationsForNamespaceDefault")
                    }
                }
                dataType("DeclarationsForNamespaceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "scopeDefinition", "Map") {
                        typeArgument("String")
                        typeArgument("ScopeDefinitionDefault")
                    }
                    propertyOf(setOf(MEMBER, COMPOSITE), "references", "List", listOf("ReferenceDefinitionDefault"))
                }
                dataType("ScopeDefinitionDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeForTypeName", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "identifiables", "List", listOf("IdentifiableDefault"))
                }
                dataType("IdentifiableDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "identifiedBy", "String")
                }
                dataType("ReferenceDefinitionDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "inTypeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "referenceExpressionList", "List") { typeArgument("ReferenceExpressionAbstract") }
                }
                dataType("ReferenceExpressionAbstract") {

                }
                dataType("PropertyReferenceExpressionDefault") {
                    supertypes("ReferenceExpressionAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "referringPropertyNavigation", "Navigation")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "refersToTypeName", "List", listOf("String"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "fromNavigation", "Navigation", emptyList(), true)
                }
                dataType("CollectionReferenceExpressionDefault") {
                    supertypes("ReferenceExpressionAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "navigation", "Navigation")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "referenceExpressionList", "List") { typeArgument("ReferenceExpression") }
                }

            }
        })
    }

    private fun initialiseMessages() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("Messages", false) {
            namespace("net.akehurst.language.agl.scanner") {
                enumType("MatchableKind", emptyList())
                dataType("Matchable") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "tag", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "expression", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "kind", "MatchableKind")

                }
            }
            namespace("net.akehurst.language.api.automaton", imports = mutableListOf("kotlin", "kotlin.collections")) {
                enumType("ParseAction", emptyList())
            }
            namespace("net.akehurst.language.editor.api") {
                dataType("EditorOptionsDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "parse", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "parseLineTokens", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "lineTokensChunkSize", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "parseTree", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "syntaxAnalysis", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "syntaxAnalysisAsm", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "semanticAnalysis", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "semanticAnalysisAsm", "Boolean")
                }
                enumType("MessageStatus", emptyList())
                dataType("EndPointIdentity") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "sessionId", "String")
                }
                dataType("AglToken") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styles", "List") { typeArgument("String") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "length", "Int")
                }
            }
            namespace("net.akehurst.language.editor.common") {
                dataType("AglTokenDefault") {
                    supertypes("net.akehurst.language.editor.api.AglToken")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styles", "List") { typeArgument("String") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "length", "Int")
                }
            }
            namespace(
                "net.akehurst.language.editor.common.messages",
                imports = mutableListOf(
                    "kotlin", "kotlin.collections",
                    "net.akehurst.language.agl.scanner",
                    "net.akehurst.language.agl.sppt",
                    "net.akehurst.language.editor.api",
                    "net.akehurst.language.agl.language.style.asm"
                )
            ) {
                dataType("MessageProcessorCreate") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "grammarStr", "String", emptyList(), true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "crossReferenceModelStr", "String", emptyList(), true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorOptions", "EditorOptionsDefault", emptyList(), false)
                }
                dataType("MessageProcessorCreateResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scannerMatchables", "List") { typeArgument("Matchable") }
                }
                dataType("MessageProcessorDelete") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                }
                dataType("MessageProcessorDeleteResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                }
                dataType("MessageProcessRequest") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "options", "ProcessOptionsDefault")
                }
                dataType("MessageParseResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeSerialised", "String", emptyList(), true)
                }
//FIXME
                dataType("MessageParseResult2") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeData", "TreeDataComplete", emptyList(), true)
                }

                dataType("MessageSyntaxAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", emptyList(), true)
                }
                dataType("MessageSemanticAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", emptyList(), true)
                }
                dataType("MessageParserInterruptRequest") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "reason", "String")
                }
                dataType("MessageLineTokens") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "startLine", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "lineTokens", "List") {
                        typeArgument("List") {
                            typeArgument("AglToken")
                        }
                    }
                }
                dataType("MessageSetStyle") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styleStr", "String")
                }
                dataType("MessageSetStyleResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styleModel", "AglStyleModelDefault", emptyList(), true)
                }
                dataType("MessageCodeCompleteRequest") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageId", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "goalRuleName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                }
                dataType("MessageCodeCompleteResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List", listOf("LanguageIssue"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "completionItems", "Array", listOf("CompletionItem"))
                }
            }
        })
    }

    private fun initialiseAsmSimple() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("AsmSimple", false) {
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = mutableListOf("kotlin", "kotlin.collections")) {
            }
            namespace(
                "net.akehurst.language.agl.semanticAnalyser",
                imports = mutableListOf("kotlin", "kotlin.collections")
            ) {
                dataType("ScopeSimple") {
                    typeParameters("AsmElementIdType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "parent", "ScopeSimple", listOf("AsmElementIdType"))
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeIdentityInParent", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forTypeName", "String")

                    propertyOf(setOf(MEMBER, REFERENCE), "scopeMap", "Map") {
                        typeArgument("AsmElementIdType") //TODO: should really mark if key is composite or reference!
                        typeArgument("ScopeSimple")
                    }
                    propertyOf(setOf(MEMBER, COMPOSITE), "childScopes", "Map") {
                        typeArgument("String")
                        typeArgument("ScopeSimple")
                    }
                    propertyOf(setOf(MEMBER, COMPOSITE), "items", "Map") {
                        typeArgument("String")
                        typeArgument("Map") {
                            typeArgument("String")
                            typeArgument("AsmElementIdType")
                        }
                    }
                }
                dataType("ContextSimple") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple", listOf("E"))
                }
                dataType("ContextFromTypeModelReference") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "languageDefinitionId", "String")
                }
                dataType("ContextFromTypeModel") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeModel", "net.akehurst.language.typemodel.api.TypeModel")
                }
            }
            namespace("net.akehurst.language.agl.asm", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AsmPathSimple") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                }
                dataType("AsmSimple") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "root", "List", listOf("AsmValueAbstract"))
                }
                dataType("AsmValueAbstract")
                dataType("AsmNothingSimple") {
                    supertypes("AsmValueAbstract")
                }
                dataType("AsmPrimitiveSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedTypeName", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "Any")
                }
                dataType("AsmReferenceSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "reference", "String")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "value", "AsmElementSimple", emptyList(), true)
                }
                dataType("AsmStructureSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "path", "AsmPathSimple")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedTypeName", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "property", "Map") {
                        typeArgument("String")
                        typeArgument("AsmStructurePropertySimple")
                    }
                }
                dataType("AsmStructurePropertySimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "index", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "Any")
                }
                dataType("AsmListSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "elements", "AsmValueAbstract")
                }
                dataType("AsmListSeparatedSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "elements", "AsmValueAbstract")
                }
            }
        })
    }

    private fun initialiseGrammarAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("GrammarAsm", false) {
            namespace(
                "net.akehurst.language.api.language.grammar",
                imports = mutableListOf("kotlin", "kotlin.collections")
            ) {
                dataType("Grammar") {}
                dataType("RuleItem") {}
                enumType("OverrideKind", listOf())
            }
            namespace(
                "net.akehurst.language.agl.language.grammar",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.semanticAnalyser")
            ) {
                dataType("AglGrammarGrammar") {
                    supertypes("GrammarAbstract")
                }
                dataType("ContextFromGrammar") {
                    propertyOf(setOf(MEMBER, COMPOSITE), "rootScope", "ScopeSimple", listOf("String"))
                }
            }
            namespace("net.akehurst.language.agl.language.format", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglFormatGrammar") {
                    supertypes("GrammarAbstract")
                }
            }
            namespace(
                "net.akehurst.language.agl.language.grammar.asm",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.api.language.grammar")
            ) {
                dataType("NamespaceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")
                }
                dataType("GrammarReferenceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "localNamespace", "NamespaceDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nameOrQName", " String")

                    propertyOf(setOf(MEMBER, REFERENCE), "resolved", "GrammarAbstract")
                }
                dataType("GrammarDefault") {
                    supertypes("GrammarAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "NamespaceDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "options", "List") { typeArgument("GrammarOptionDefault") }
                }
                dataType("GrammarOptionDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                }
                dataType("GrammarAbstract") {
                    supertypes("Grammar")

                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "namespace", "NamespaceDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")

                    propertyOf(setOf(MEMBER, COMPOSITE), "extends", "List", listOf("GrammarReferenceDefault"))
                    propertyOf(setOf(MEMBER, COMPOSITE), "grammarRule", "List", listOf("GrammarRuleAbstract"))
                }
                dataType("GrammarRuleAbstract")
                dataType("NormalRuleDefault") {
                    supertypes("GrammarRuleAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "grammar", "GrammarDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isLeaf", "Boolean")

                    propertyOf(setOf(MEMBER, COMPOSITE), "rhs", "RuleItemAbstract")
                }

                dataType("OverrideRuleDefault") {
                    supertypes("GrammarRuleAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "grammar", "GrammarDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isLeaf", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "overrideKind", "OverrideKind")

                    propertyOf(setOf(MEMBER, COMPOSITE), "overridenRhs", "RuleItemAbstract")
                }
                dataType("RuleItemAbstract") {
                    supertypes("RuleItem")
                }
                dataType("EmptyRuleDefault") {
                    supertypes("RuleItemAbstract")
                }
                dataType("ChoiceAbstract") {
                    supertypes("RuleItemAbstract")
                }
                dataType("ChoiceLongestDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List", listOf("RuleItem"))
                }
                dataType("ChoicePriorityDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List", listOf("RuleItem"))
                }
                dataType("ChoiceAmbiguousDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List", listOf("RuleItem"))
                }
                dataType("ConcatenationDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "items", "List", listOf("RuleItem"))
                }
                dataType("ConcatenationItemAbstract") {
                    supertypes("RuleItemAbstract")
                }
                dataType("SimpleItemAbstract") {
                    supertypes("ConcatenationItemAbstract")
                }
                dataType("GroupDefault") {
                    supertypes("ConcatenationItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "groupedContent", "RuleItem")
                }
                dataType("NonTerminalDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "targetGrammar", "GrammarReference", emptyList(), true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                }
                dataType("TerminalDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isPattern", "Boolean")
                }
                dataType("EmbeddedDefault") {
                    supertypes("RuleItemAbstract")

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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "item", "RuleItem")
                }
            }
        })
    }

    private fun initialiseSPPT() {
        serialiser.configureFromTypeModel(typeModel("SPPT", false) {
            namespace("net.akehurst.language.agl.runtime.structure",imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("RuntimeRule") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "runtimeRuleSetNumber", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "ruleNumber", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                }
            }
            namespace("net.akehurst.language.agl.sppt", imports = mutableListOf("kotlin", "kotlin.collections","net.akehurst.language.agl.runtime.structure")) {
                dataType("CompleteTreeDataNode") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rule", "RuntimeRule")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "startPosition", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nextInputPosition", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nextInputNoSkip", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "option", "Int")
                }
                dataType("TreeDataComplete2") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forStateSetNumber", "Int")

                    propertyOf(setOf(MEMBER, COMPOSITE), "root", "CompleteTreeDataNode", emptyList(), true)
                    propertyOf(setOf(MEMBER, COMPOSITE), "initialSkip", "TreeDataComplete", listOf("CompleteTreeDataNode"), true)
                    propertyOf(setOf(MEMBER, COMPOSITE), "completeChildren", "Map") {
                        typeArgument("CN")
                        typeArgument("Map") {
                            typeArgument("Int")
                            typeArgument("List") {
                                typeArgument("CompleteTreeDataNode")
                            }
                        }
                    }
                }
            }
        })
    }

    fun check() {
        val issues = serialiser.registry.checkPublicAndReflectable()
        check(issues.isEmpty()) { issues.joinToString(separator = "\n") }
    }

    fun configureFromKompositeString(datatypeModel: String) {
        serialiser.configureFromKompositeString(datatypeModel)
    }

    fun configureFromTypeModel(datatypeModel: TypeModel) {
        serialiser.configureFromTypeModel(datatypeModel)
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

