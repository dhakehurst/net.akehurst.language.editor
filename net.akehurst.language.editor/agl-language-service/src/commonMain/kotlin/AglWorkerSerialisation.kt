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

package net.akehurst.language.editor.language.service

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
            agl_language_service_commonMain.KotlinxReflectForModule.registerUsedClasses()
            //TODO: enable kserialisation/komposite/reflect to auto add these some how!!
            initialiseBase()
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

    /*
    api.parser -->
     */
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
                enumType("CompletionItemKind", emptyList())
                enumType("LanguageProcessorPhase", emptyList())
                dataType("LanguageIssue") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "kind", "LanguageIssueKind")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "phase", "LanguageProcessorPhase")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "location", "InputLocation")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "data", "Any")
                }
                dataType("CompletionItem") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "kind", "CompletionItemKind")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "description", "String")
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

    /*
     api.language.base --> std
     agl.language.base --> std, api.language.base
     */
    private fun initialiseBase() {
        serialiser.configureFromTypeModel(typeModel("Base", false) {
            namespace("net.akehurst.language.api.language.base", listOf("std")) {
                valueType("SimpleName") {
                    supertypes("PossiblyQualifiedName", "std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                valueType("QualifiedName") {
                    supertypes("PossiblyQualifiedName", "std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                valueType("Import") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                interfaceType("PossiblyQualifiedName") {
                    supertypes("std.Any")
                }
                interfaceType("Namespace") {
                    typeParameters("DT")
                    supertypes("Formatable", "std.Any")
                }
                interfaceType("Model") {
                    typeParameters("NT", "DT")
                    supertypes("Formatable", "std.Any")
                }
                interfaceType("Formatable") {
                    supertypes("std.Any")
                }
                interfaceType("Definition") {
                    typeParameters("DT")
                    supertypes("Formatable", "std.Any")
                }
                dataType("Indent") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                        parameter("increment", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "increment", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
            }
            namespace("net.akehurst.language.agl.language.base", listOf("net.akehurst.language.api.language.base", "std")) {
                dataType("NamespaceDefault") {
                    supertypes("NamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                    }
                }
                dataType("NamespaceAbstract") {
                    supertypes("net.akehurst.language.api.language.base.Namespace", "std.Any")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "import", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.base.Import")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                }
                dataType("ModelDefault") {
                    supertypes("ModelAbstract")
                    constructor_ {
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                        parameter("namespace", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                }
                dataType("ModelAbstract") {
                    supertypes("net.akehurst.language.api.language.base.Model", "std.Any")
                    constructor_ {
                        parameter("namespace", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "std.List", false){
                        typeArgument("NT")
                    }
                }
            }
        })
    }

    /*
    api.language.grammar --> api.language.base
    agl.language.grammar.asm -> api.language.grammar, .api.language.base, agl.language.base
     */
    private fun initialiseGrammarAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("GrammarAsm", false) {
            namespace("net.akehurst.language.api.language.grammar", listOf("std", "net.akehurst.language.api.language.base")) {
                enumType("SeparatedListKind", listOf("Flat", "Left", "Right"))
                enumType("OverrideKind", listOf("REPLACE", "APPEND_ALTERNATIVE", "SUBSTITUTION"))
                enumType("Associativity", listOf("LEFT", "RIGHT"))
                valueType("GrammarRuleName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                interfaceType("Terminal") {
                    supertypes("TangibleItem", "std.Any")
                }
                interfaceType("TangibleItem") {
                    supertypes("SimpleItem", "std.Any")
                }
                interfaceType("SimpleList") {
                    supertypes("ListOfItems", "std.Any")
                }
                interfaceType("SimpleItem") {
                    supertypes("ConcatenationItem", "std.Any")
                }
                interfaceType("SeparatedList") {
                    supertypes("ListOfItems", "std.Any")
                }
                interfaceType("RuleItem") {
                    supertypes("std.Any")
                }
                interfaceType("PreferenceRule") {
                    supertypes("GrammarItem", "std.Any")
                }
                interfaceType("PreferenceOption") {
                    supertypes("net.akehurst.language.api.language.base.Formatable", "std.Any")
                }
                interfaceType("OverrideRule") {
                    supertypes("GrammarRule", "std.Any")
                }
                interfaceType("OptionalItem") {
                    supertypes("ConcatenationItem", "std.Any")
                }
                interfaceType("NormalRule") {
                    supertypes("GrammarRule", "std.Any")
                }
                interfaceType("NonTerminal") {
                    supertypes("TangibleItem", "std.Any")
                }
                interfaceType("NodeType") {
                    supertypes("std.Any")
                }
                interfaceType("ListOfItems") {
                    supertypes("ConcatenationItem", "std.Any")
                }
                interfaceType("Group") {
                    supertypes("SimpleItem", "std.Any")
                }
                interfaceType("GrammarRule") {
                    supertypes("GrammarItem", "std.Any")
                }
                interfaceType("GrammarReference") {
                    supertypes("std.Any")
                }
                interfaceType("GrammarOption") {
                    supertypes("std.Any")
                }
                interfaceType("GrammarNamespace") {
                    supertypes("net.akehurst.language.api.language.base.Namespace", "std.Any")
                }
                interfaceType("GrammarModel") {
                    supertypes("net.akehurst.language.api.language.base.Model", "std.Any")
                }
                interfaceType("GrammarLoader") {
                    supertypes("std.Any")
                }
                interfaceType("GrammarItem") {
                    supertypes("net.akehurst.language.api.language.base.Formatable", "std.Any")
                }
                interfaceType("Grammar") {
                    supertypes("net.akehurst.language.api.language.base.Definition", "std.Any")
                }
                interfaceType("EmptyRule") {
                    supertypes("TangibleItem", "std.Any")
                }
                interfaceType("Embedded") {
                    supertypes("TangibleItem", "std.Any")
                }
                interfaceType("ConcatenationItem") {
                    supertypes("RuleItem", "std.Any")
                }
                interfaceType("Concatenation") {
                    supertypes("RuleItem", "std.Any")
                }
                interfaceType("ChoicePriority") {
                    supertypes("Choice", "std.Any")
                }
                interfaceType("ChoiceLongest") {
                    supertypes("Choice", "std.Any")
                }
                interfaceType("ChoiceAmbiguous") {
                    supertypes("Choice", "std.Any")
                }
                interfaceType("Choice") {
                    supertypes("RuleItem", "std.Any")
                }
                dataType("GrammarRuleNotFoundException") {
                    supertypes("std.Exception")
                    constructor_ {
                        parameter("message", "std.String", false)
                    }
                }
                dataType("GrammarRuleItemNotFoundException") {
                    supertypes("std.Exception")
                    constructor_ {
                        parameter("message", "std.String", false)
                    }
                }
                dataType("GrammarExeception") {
                    supertypes("std.Exception")
                    constructor_ {
                        parameter("message", "std.String", false)
                        parameter("cause", "std.Exception", true)
                    }
                }
            }
            namespace("net.akehurst.language.agl.language.grammar.asm", listOf("net.akehurst.language.api.language.grammar", "std", "net.akehurst.language.api.language.base", "net.akehurst.language.agl.language.base")) {
                dataType("TerminalDefault") {
                    supertypes("TangibleItemAbstract", "net.akehurst.language.api.language.grammar.Terminal")
                    constructor_ {
                        parameter("value", "std.String", false)
                        parameter("isPattern", "std.Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "id", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isPattern", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                dataType("TangibleItemAbstract") {
                    supertypes("SimpleItemAbstract", "net.akehurst.language.api.language.grammar.TangibleItem")
                    constructor_ {}
                }
                dataType("SimpleListDefault") {
                    supertypes("ListOfItemsAbstract", "net.akehurst.language.api.language.grammar.SimpleList")
                    constructor_ {
                        parameter("min_", "std.Integer", false)
                        parameter("max_", "std.Integer", false)
                        parameter("item", "net.akehurst.language.api.language.grammar.RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "net.akehurst.language.api.language.grammar.RuleItem", false)
                }
                dataType("SimpleItemAbstract") {
                    supertypes("ConcatenationItemAbstract", "net.akehurst.language.api.language.grammar.SimpleItem")
                    constructor_ {}
                }
                dataType("SeparatedListDefault") {
                    supertypes("ListOfItemsAbstract", "net.akehurst.language.api.language.grammar.SeparatedList")
                    constructor_ {
                        parameter("min_", "std.Integer", false)
                        parameter("max_", "std.Integer", false)
                        parameter("item", "net.akehurst.language.api.language.grammar.RuleItem", false)
                        parameter("separator", "net.akehurst.language.api.language.grammar.RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "net.akehurst.language.api.language.grammar.RuleItem", false)
                    propertyOf(setOf(READ_ONLY, STORED), "separator", "net.akehurst.language.api.language.grammar.RuleItem", false)
                }
                dataType("RuleItemAbstract") {
                    supertypes("net.akehurst.language.api.language.grammar.RuleItem", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, STORED), "index", "std.List", true){
                        typeArgument("std.Integer")
                    }
                }
                dataType("PreferenceRuleDefault") {
                    supertypes("GrammarItemAbstract", "net.akehurst.language.api.language.grammar.PreferenceRule")
                    constructor_ {
                        parameter("grammar", "net.akehurst.language.api.language.grammar.Grammar", false)
                        parameter("forItem", "net.akehurst.language.api.language.grammar.SimpleItem", false)
                        parameter("optionList", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forItem", "net.akehurst.language.api.language.grammar.SimpleItem", false)
                    propertyOf(setOf(READ_ONLY, STORED), "grammar", "net.akehurst.language.api.language.grammar.Grammar", false)
                    propertyOf(setOf(READ_ONLY, STORED), "optionList", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.PreferenceOption")
                    }
                }
                dataType("PreferenceOptionDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.PreferenceOption", "std.Any")
                    constructor_ {
                        parameter("item", "net.akehurst.language.api.language.grammar.NonTerminal", false)
                        parameter("choiceNumber", "std.Integer", false)
                        parameter("onTerminals", "std.List", false)
                        parameter("associativity", "net.akehurst.language.api.language.grammar.Associativity", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "associativity", "net.akehurst.language.api.language.grammar.Associativity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "choiceNumber", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "item", "net.akehurst.language.api.language.grammar.NonTerminal", false)
                    propertyOf(setOf(READ_ONLY, STORED), "onTerminals", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.SimpleItem")
                    }
                }
                dataType("OverrideRuleDefault") {
                    supertypes("GrammarRuleAbstract", "net.akehurst.language.api.language.grammar.OverrideRule")
                    constructor_ {
                        parameter("grammar", "net.akehurst.language.api.language.grammar.Grammar", false)
                        parameter("name", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                        parameter("isSkip", "std.Boolean", false)
                        parameter("isLeaf", "std.Boolean", false)
                        parameter("overrideKind", "net.akehurst.language.api.language.grammar.OverrideKind", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "grammar", "net.akehurst.language.api.language.grammar.Grammar", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isLeaf", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isOverride", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isSkip", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "overrideKind", "net.akehurst.language.api.language.grammar.OverrideKind", false)
                }
                dataType("OptionalItemDefault") {
                    supertypes("ConcatenationItemAbstract", "net.akehurst.language.api.language.grammar.OptionalItem")
                    constructor_ {
                        parameter("item", "net.akehurst.language.api.language.grammar.RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "net.akehurst.language.api.language.grammar.RuleItem", false)
                }
                dataType("NormalRuleDefault") {
                    supertypes("GrammarRuleAbstract", "net.akehurst.language.api.language.grammar.NormalRule")
                    constructor_ {
                        parameter("grammar", "net.akehurst.language.api.language.grammar.Grammar", false)
                        parameter("name", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                        parameter("isSkip", "std.Boolean", false)
                        parameter("isLeaf", "std.Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "grammar", "net.akehurst.language.api.language.grammar.Grammar", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isLeaf", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isOverride", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isSkip", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                }
                dataType("NonTerminalDefault") {
                    supertypes("TangibleItemAbstract", "net.akehurst.language.api.language.grammar.NonTerminal")
                    constructor_ {
                        parameter("targetGrammar", "net.akehurst.language.api.language.grammar.GrammarReference", true)
                        parameter("ruleReference", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "ruleReference", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "targetGrammar", "net.akehurst.language.api.language.grammar.GrammarReference", true)
                }
                dataType("NodeTypeDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.NodeType", "std.Any")
                    constructor_ {
                        parameter("identity", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "identity", "std.String", false)
                }
                dataType("ListOfItemsAbstract") {
                    supertypes("ConcatenationItemAbstract", "net.akehurst.language.api.language.grammar.ListOfItems")
                    constructor_ {
                        parameter("min", "std.Integer", false)
                        parameter("max", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "max", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "min", "std.Integer", false)
                }
                dataType("GroupDefault") {
                    supertypes("SimpleItemAbstract", "net.akehurst.language.api.language.grammar.Group")
                    constructor_ {
                        parameter("groupedContent", "net.akehurst.language.api.language.grammar.RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "groupedContent", "net.akehurst.language.api.language.grammar.RuleItem", false)
                }
                dataType("GrammarRuleAbstract") {
                    supertypes("GrammarItemAbstract", "net.akehurst.language.api.language.grammar.GrammarRule")
                    constructor_ {}
                }
                dataType("GrammarReferenceDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarReference", "std.Any")
                    constructor_ {
                        parameter("localNamespace", "net.akehurst.language.api.language.base.Namespace", false)
                        parameter("nameOrQName", "net.akehurst.language.api.language.base.PossiblyQualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "localNamespace", "net.akehurst.language.api.language.base.Namespace", false){
                        typeArgument("net.akehurst.language.api.language.grammar.Grammar")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "nameOrQName", "net.akehurst.language.api.language.base.PossiblyQualifiedName", false)
                    propertyOf(setOf(READ_WRITE, STORED), "resolved", "net.akehurst.language.api.language.grammar.Grammar", true)
                }
                dataType("GrammarOptionDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarOption", "std.Any")
                    constructor_ {
                        parameter("name", "std.String", false)
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                dataType("GrammarNamespaceDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarNamespace", "net.akehurst.language.agl.language.base.NamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                    }
                }
                dataType("GrammarModelDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarModel", "net.akehurst.language.agl.language.base.ModelAbstract")
                    constructor_ {
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                        parameter("namespace", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                }
                dataType("GrammarItemAbstract") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarItem", "std.Any")
                    constructor_ {}
                }
                dataType("GrammarDefaultKt") {
                    supertypes("std.Any")
                }
                dataType("GrammarDefault") {
                    supertypes("GrammarAbstract")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.api.language.grammar.GrammarNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                        parameter("options", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "options", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarOption")
                    }
                }
                dataType("GrammarAbstract") {
                    supertypes("net.akehurst.language.api.language.grammar.Grammar", "std.Any")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.api.language.grammar.GrammarNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "extends", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarReference")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "grammarRule", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarRule")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.api.language.grammar.GrammarNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "preferenceRule", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.PreferenceRule")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "selfReference", "GrammarReferenceDefault", false)
                }
                dataType("EmptyRuleDefault") {
                    supertypes("TangibleItemAbstract", "net.akehurst.language.api.language.grammar.EmptyRule")
                    constructor_ {}
                }
                dataType("EmbeddedDefault") {
                    supertypes("TangibleItemAbstract", "net.akehurst.language.api.language.grammar.Embedded")
                    constructor_ {
                        parameter("embeddedGoalName", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                        parameter("embeddedGrammarReference", "net.akehurst.language.api.language.grammar.GrammarReference", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "embeddedGoalName", "net.akehurst.language.api.language.grammar.GrammarRuleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "embeddedGrammarReference", "net.akehurst.language.api.language.grammar.GrammarReference", false)
                }
                dataType("ConcatenationItemAbstract") {
                    supertypes("RuleItemAbstract", "net.akehurst.language.api.language.grammar.ConcatenationItem")
                    constructor_ {}
                }
                dataType("ConcatenationDefault") {
                    supertypes("RuleItemAbstract", "net.akehurst.language.api.language.grammar.Concatenation")
                    constructor_ {
                        parameter("items", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "items", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoicePriorityDefault") {
                    supertypes("ChoiceAbstract", "net.akehurst.language.api.language.grammar.ChoicePriority")
                    constructor_ {
                        parameter("alternative", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoiceLongestDefault") {
                    supertypes("ChoiceAbstract", "net.akehurst.language.api.language.grammar.ChoiceLongest")
                    constructor_ {
                        parameter("alternative", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoiceAmbiguousDefault") {
                    supertypes("ChoiceAbstract", "net.akehurst.language.api.language.grammar.ChoiceAmbiguous")
                    constructor_ {
                        parameter("alternative", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoiceAbstract") {
                    supertypes("RuleItemAbstract", "net.akehurst.language.api.language.grammar.Choice")
                    constructor_ {
                        parameter("alternative", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
            }
        })
    }

    /*
    api.parser --> api.runtime
    api.sppt --> api.runtime, api.parser
    agl.sppt --> api.sppt, api.runtime
     */
    private fun initialiseSPPT() {
        val tm = typeModel("Test", true, emptyList()) {
            namespace("net.akehurst.language.api.parser", listOf("std", "net.akehurst.language.agl.api.runtime")) {
                interfaceType("RuntimeSpine") {
                    supertypes("std.Any")
                }
                interfaceType("Parser") {
                    supertypes("std.Any")
                }
                dataType("InputLocation") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("position", "std.Integer", false)
                        parameter("column", "std.Integer", false)
                        parameter("line", "std.Integer", false)
                        parameter("length", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "column", "std.Integer", false)
                    propertyOf(setOf(READ_WRITE, STORED), "length", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "line", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "position", "std.Integer", false)
                }
            }
            namespace("net.akehurst.language.api.sppt", listOf("std", "net.akehurst.language.agl.api.runtime", "net.akehurst.language.api.parser")) {
                interfaceType("TreeData") {
                    supertypes("std.Any")
                }
                interfaceType("SpptWalker") {
                    supertypes("std.Any")
                }
                interfaceType("SpptDataNodeInfo") {
                    supertypes("std.Any")
                }
                interfaceType("SpptDataNode") {
                    supertypes("std.Any")
                }
                interfaceType("SharedPackedParseTreeVisitor") {
                    typeParameters("T", "A")
                    supertypes("std.Any")
                }
                interfaceType("SharedPackedParseTree") {
                    supertypes("std.Any")
                }
                interfaceType("Sentence") {
                    supertypes("std.Any")
                }
                interfaceType("SPPTParser") {
                    supertypes("std.Any")
                }
                interfaceType("SPPTNodeIdentity") {
                    supertypes("std.Any")
                }
                interfaceType("SPPTNode") {
                    supertypes("std.Any")
                }
                interfaceType("SPPTLeaf") {
                    supertypes("SPPTNode", "std.Any")
                }
                interfaceType("SPPTBranch") {
                    supertypes("SPPTNode", "std.Any")
                }
                dataType("SPPTException") {
                    supertypes("std.Exception")
                    constructor_ {
                        parameter("message", "std.String", false)
                        parameter("cause", "std.Exception", true)
                    }
                }
                dataType("LeafData") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("name", "std.String", false)
                        parameter("isPattern", "std.Boolean", false)
                        parameter("position", "std.Integer", false)
                        parameter("length", "std.Integer", false)
                        parameter("tagList", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "isPattern", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "length", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "position", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "tagList", "std.List", false){
                        typeArgument("std.String")
                    }
                }
                dataType("ChildInfo") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("propertyIndex", "std.Integer", false)
                        parameter("index", "std.Integer", false)
                        parameter("total", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "index", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "propertyIndex", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "total", "std.Integer", false)
                }
                dataType("AltInfo") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("option", "std.Integer", false)
                        parameter("index", "std.Integer", false)
                        parameter("totalMatched", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "index", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "option", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "totalMatched", "std.Integer", false)
                }
            }
            namespace("net.akehurst.language.agl.sppt", listOf("std", "net.akehurst.language.api.sppt", "net.akehurst.language.agl.api.runtime")) {
                dataType("TreeDataCompleteKt") {
                    supertypes("std.Any")
                }
                dataType("TreeDataComplete2") {
                    supertypes("net.akehurst.language.api.sppt.TreeData", "std.Any")
                    constructor_ {
                        parameter("forStateSetNumber", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forStateSetNumber", "std.Integer", false)
                    propertyOf(setOf(READ_WRITE, STORED), "initialSkip", "net.akehurst.language.api.sppt.TreeData", true)
                    propertyOf(setOf(READ_WRITE, STORED), "root", "net.akehurst.language.api.sppt.SpptDataNode", true)
                }
                dataType("TreeDataComplete") {
                    supertypes("net.akehurst.language.api.sppt.TreeData", "std.Any")
                    constructor_ {
                        parameter("forStateSetNumber", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forStateSetNumber", "std.Integer", false)
                    propertyOf(setOf(READ_WRITE, STORED), "initialSkip", "net.akehurst.language.api.sppt.TreeData", true)
                    propertyOf(setOf(READ_WRITE, STORED), "root", "net.akehurst.language.api.sppt.SpptDataNode", true)
                }
                dataType("CompleteTreeDataNode") {
                    supertypes("net.akehurst.language.api.sppt.SpptDataNode", "std.Any")
                    constructor_ {
                        parameter("rule", "net.akehurst.language.agl.api.runtime.Rule", false)
                        parameter("startPosition", "std.Integer", false)
                        parameter("nextInputPosition", "std.Integer", false)
                        parameter("nextInputNoSkip", "std.Integer", false)
                        parameter("option", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "nextInputNoSkip", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "nextInputPosition", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "option", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "rule", "net.akehurst.language.agl.api.runtime.Rule", false)
                    propertyOf(setOf(READ_ONLY, STORED), "startPosition", "std.Integer", false)
                }
                dataType("CompleteKey") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("rule", "net.akehurst.language.agl.api.runtime.Rule", false)
                        parameter("startPosition", "std.Integer", false)
                        parameter("nextInputPosition", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "nextInputPosition", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "rule", "net.akehurst.language.agl.api.runtime.Rule", false)
                    propertyOf(setOf(READ_ONLY, STORED), "startPosition", "std.Integer", false)
                }
            }
        }

        serialiser.configureFromTypeModel(tm)
    }

    /*

     */
    private fun initialiseTypeModel() {
        val tm = typeModel("Test", true, emptyList()) {
            namespace("net.akehurst.language.typemodel.api", listOf("std", "net.akehurst.language.api.language.base", "net.akehurst.language.typemodel.simple")) {
                enumType("PropertyCharacteristic", listOf("REFERENCE", "COMPOSITE", "READ_ONLY", "READ_WRITE", "STORED", "DERIVED", "PRIMITIVE", "CONSTRUCTOR", "IDENTITY"))
                valueType("PropertyName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                valueType("ParameterName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                valueType("MethodName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "std.String", false)
                }
                interfaceType("ValueType") {
                    supertypes("StructuredType", "std.Any")
                }
                interfaceType("UnnamedSupertypeType") {
                    supertypes("TypeDeclaration", "std.Any")
                }
                interfaceType("TypeNamespace") {
                    supertypes("net.akehurst.language.api.language.base.Namespace", "std.Any")
                }
                interfaceType("TypeModel") {
                    supertypes("net.akehurst.language.api.language.base.Model", "std.Any")
                }
                interfaceType("TypeInstance") {
                    supertypes("std.Any")
                }
                interfaceType("TypeDeclaration") {
                    supertypes("net.akehurst.language.api.language.base.Definition", "std.Any")
                }
                interfaceType("TupleType") {
                    supertypes("StructuredType", "std.Any")
                }
                interfaceType("StructuredType") {
                    supertypes("TypeDeclaration", "std.Any")
                }
                interfaceType("SingletonType") {
                    supertypes("TypeDeclaration", "std.Any")
                }
                interfaceType("PropertyDeclaration") {
                    supertypes("std.Any")
                }
                interfaceType("PrimitiveType") {
                    supertypes("TypeDeclaration", "std.Any")
                }
                interfaceType("ParameterDeclaration") {
                    supertypes("std.Any")
                }
                interfaceType("MethodDeclaration") {
                    supertypes("std.Any")
                }
                interfaceType("InterfaceType") {
                    supertypes("StructuredType", "std.Any")
                }
                interfaceType("EnumType") {
                    supertypes("TypeDeclaration", "std.Any")
                }
                interfaceType("DataType") {
                    supertypes("StructuredType", "std.Any")
                }
                interfaceType("ConstructorDeclaration") {
                    supertypes("std.Any")
                }
                interfaceType("CollectionType") {
                    supertypes("StructuredType", "std.Any")
                }
                dataType("ValueTypeBuilder") {
                    supertypes("StructuredTypeBuilder")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_typeReferences", "std.List", false)
                        parameter("_name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                }
                dataType("TypeUsageReferenceBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("context", "TypeDeclaration", true)
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("type", "TypeDeclaration", false)
                        parameter("nullable", "std.Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "_namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "context", "TypeDeclaration", true)
                    propertyOf(setOf(READ_ONLY, STORED), "nullable", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "type", "TypeDeclaration", false)
                }
                dataType("TypeNamespaceBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                        parameter("imports", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                }
                dataType("TypeModelBuilderKt") {
                    supertypes("std.Any")
                }
                dataType("TypeModelBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                        parameter("resolveImports", "std.Boolean", false)
                        parameter("namespaces", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "_model", "net.akehurst.language.typemodel.simple.TypeModelSimple", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespaces", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeNamespace")
                    }
                }
                dataType("TypeArgumentBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("_context", "TypeDeclaration", true)
                        parameter("_namespace", "TypeNamespace", false)
                    }
                }
                dataType("TupleTypeBuilder") {
                    supertypes("StructuredTypeBuilder")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_typeReferences", "std.List", false)
                    }
                }
                dataType("SubtypeListBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_typeReferences", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "_namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "_subtypeList", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("StructuredTypeBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_typeReferences", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "COMPOSITE", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "CONSTRUCTOR", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "DERIVED", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "IDENTITY", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "READ_ONLY", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "READ_WRITE", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "REFERENCE", "PropertyCharacteristic", false)
                    propertyOf(setOf(READ_ONLY, STORED), "STORED", "PropertyCharacteristic", false)
                }
                dataType("InterfaceTypeBuilder") {
                    supertypes("StructuredTypeBuilder")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_typeReferences", "std.List", false)
                        parameter("_name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                }
                dataType("DataTypeBuilder") {
                    supertypes("StructuredTypeBuilder")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_typeReferences", "std.List", false)
                        parameter("_name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                }
                dataType("ConstructorBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("_namespace", "TypeNamespace", false)
                        parameter("_type", "TypeDeclaration", false)
                        parameter("_typeReferences", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "_namespace", "TypeNamespace", false)
                }
            }
            namespace("net.akehurst.language.typemodel.simple", listOf("net.akehurst.language.typemodel.api", "net.akehurst.language.api.language.base", "std", "net.akehurst.language.agl.language.base")) {
                dataType("ValueTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.ValueType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "constructors", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ConstructorDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                }
                dataType("UnnamedSupertypeTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.UnnamedSupertypeType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("id", "std.Integer", false)
                        parameter("subtypes", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "id", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "subtypes", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("UnnamedSupertypeTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("declaration", "net.akehurst.language.typemodel.api.UnnamedSupertypeType", false)
                        parameter("typeArguments", "std.List", false)
                        parameter("isNullable", "std.Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "declaration", "net.akehurst.language.typemodel.api.UnnamedSupertypeType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isNullable", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeArguments", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("TypeNamespaceSimple") {
                    supertypes("TypeNamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                        parameter("imports", "std.List", false)
                    }
                }
                dataType("TypeNamespaceAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.TypeNamespace", "net.akehurst.language.agl.language.base.NamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                        parameter("imports", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "import", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.base.Import")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "ownedTupleTypes", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TupleType")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "ownedUnnamedSupertypeType", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.UnnamedSupertypeType")
                    }
                }
                dataType("TypeModelSimpleAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.TypeModel", "std.Any")
                    constructor_ {
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "allNamespace", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeNamespace")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                }
                dataType("TypeModelSimple") {
                    supertypes("TypeModelSimpleAbstract")
                    constructor_ {
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                }
                dataType("TypeInstanceSimple") {
                    supertypes("TypeInstanceAbstract")
                    constructor_ {
                        parameter("contextQualifiedTypeName", "net.akehurst.language.api.language.base.QualifiedName", true)
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("qualifiedOrImportedTypeName", "net.akehurst.language.api.language.base.PossiblyQualifiedName", false)
                        parameter("typeArguments", "std.List", false)
                        parameter("isNullable", "std.Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "contextQualifiedTypeName", "net.akehurst.language.api.language.base.QualifiedName", true)
                    propertyOf(setOf(READ_ONLY, STORED), "isNullable", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedOrImportedTypeName", "net.akehurst.language.api.language.base.PossiblyQualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeArguments", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("TypeInstanceAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.TypeInstance", "std.Any")
                    constructor_ {}
                }
                dataType("TypeDeclarationSimpleAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.TypeDeclaration", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, STORED), "metaInfo", "std.Map", false){
                        typeArgument("std.String")
                        typeArgument("std.String")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "method", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.MethodDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "propertyByIndex", "std.Map", false){
                        typeArgument("std.Integer")
                        typeArgument("net.akehurst.language.typemodel.api.PropertyDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "supertypes", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "typeParameters", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.base.SimpleName")
                    }
                }
                dataType("TupleTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.TupleType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("id", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "id", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                }
                dataType("TupleTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("declaration", "net.akehurst.language.typemodel.api.TupleType", false)
                        parameter("typeArguments", "std.List", false)
                        parameter("isNullable", "std.Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "declaration", "net.akehurst.language.typemodel.api.TupleType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isNullable", "std.Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeArguments", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("StructuredTypeSimpleAbstract") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.StructuredType")
                    constructor_ {}
                }
                dataType("SpecialTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                }
                dataType("SingletonTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.SingletonType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                }
                dataType("SimpleTypeModelStdLib") {
                    supertypes("TypeNamespaceAbstract")
                    propertyOf(setOf(READ_ONLY, STORED), "AnyType", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Boolean", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Collection", "net.akehurst.language.typemodel.api.CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Exception", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Integer", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "List", "net.akehurst.language.typemodel.api.CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "ListSeparated", "net.akehurst.language.typemodel.api.CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Map", "net.akehurst.language.typemodel.api.CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "NothingType", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "OrderedSet", "net.akehurst.language.typemodel.api.CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Pair", "net.akehurst.language.typemodel.api.DataType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Real", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Set", "net.akehurst.language.typemodel.api.CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "String", "net.akehurst.language.typemodel.api.TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Timestamp", "net.akehurst.language.typemodel.api.TypeInstance", false)
                }
                dataType("PropertyDeclarationStored") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "net.akehurst.language.typemodel.api.StructuredType", false)
                        parameter("name", "net.akehurst.language.typemodel.api.PropertyName", false)
                        parameter("typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                        parameter("characteristics", "std.Set", false)
                        parameter("index", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "characteristics", "std.Set", false){
                        typeArgument("net.akehurst.language.typemodel.api.PropertyCharacteristic")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "index", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.typemodel.api.PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "net.akehurst.language.typemodel.api.StructuredType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                }
                dataType("PropertyDeclarationResolved") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                        parameter("name", "net.akehurst.language.typemodel.api.PropertyName", false)
                        parameter("typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                        parameter("characteristics", "std.Set", false)
                        parameter("description", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "characteristics", "std.Set", false){
                        typeArgument("net.akehurst.language.typemodel.api.PropertyCharacteristic")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.typemodel.api.PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                }
                dataType("PropertyDeclarationPrimitive") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                        parameter("name", "net.akehurst.language.typemodel.api.PropertyName", false)
                        parameter("typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                        parameter("description", "std.String", false)
                        parameter("index", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "index", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.typemodel.api.PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                }
                dataType("PropertyDeclarationDerived") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                        parameter("name", "net.akehurst.language.typemodel.api.PropertyName", false)
                        parameter("typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                        parameter("description", "std.String", false)
                        parameter("expression", "std.String", false)
                        parameter("index", "std.Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "index", "std.Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.typemodel.api.PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                }
                dataType("PropertyDeclarationAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.PropertyDeclaration", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, STORED), "metaInfo", "std.Map", false){
                        typeArgument("std.String")
                        typeArgument("std.String")
                    }
                }
                dataType("PrimitiveTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.PrimitiveType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                }
                dataType("ParameterDefinitionSimple") {
                    supertypes("net.akehurst.language.typemodel.api.ParameterDeclaration", "std.Any")
                    constructor_ {
                        parameter("name", "net.akehurst.language.typemodel.api.ParameterName", false)
                        parameter("typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                        parameter("defaultValue", "std.String", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "defaultValue", "std.String", true)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.typemodel.api.ParameterName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "net.akehurst.language.typemodel.api.TypeInstance", false)
                }
                dataType("MethodDeclarationDerived") {
                    supertypes("net.akehurst.language.typemodel.api.MethodDeclaration", "std.Any")
                    constructor_ {
                        parameter("owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                        parameter("name", "net.akehurst.language.typemodel.api.MethodName", false)
                        parameter("parameters", "std.List", false)
                        parameter("description", "std.String", false)
                        parameter("body", "std.String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "body", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "description", "std.String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.typemodel.api.MethodName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "parameters", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ParameterDeclaration")
                    }
                }
                dataType("InterfaceTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.InterfaceType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "subtypes", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("EnumTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.EnumType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                        parameter("literals", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "literals", "std.List", false){
                        typeArgument("std.String")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                }
                dataType("DataTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.DataType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "constructors", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ConstructorDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "subtypes", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("ConstructorDeclarationSimple") {
                    supertypes("net.akehurst.language.typemodel.api.ConstructorDeclaration", "std.Any")
                    constructor_ {
                        parameter("owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                        parameter("parameters", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "net.akehurst.language.typemodel.api.TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "parameters", "std.List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ParameterDeclaration")
                    }
                }
                dataType("CollectionTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.CollectionType")
                    constructor_ {
                        parameter("namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                        parameter("name", "net.akehurst.language.api.language.base.SimpleName", false)
                        parameter("typeParameters", "std.List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "net.akehurst.language.api.language.base.SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "net.akehurst.language.typemodel.api.TypeNamespace", false)
                    propertyOf(setOf(READ_WRITE, STORED), "typeParameters", "std.List", false){
                        typeArgument("net.akehurst.language.api.language.base.SimpleName")
                    }
                }
            }
            namespace("net.akehurst.language.api.grammarTypeModel", listOf("net.akehurst.language.typemodel.api", "std", "net.akehurst.language.api.language.grammar")) {
                interfaceType("GrammarTypeNamespace") {
                    supertypes("net.akehurst.language.typemodel.api.TypeNamespace", "std.Any")
                }
            }
            namespace("net.akehurst.language.agl.grammarTypeModel", listOf("net.akehurst.language.api.language.base", "std", "net.akehurst.language.typemodel.simple", "net.akehurst.language.api.grammarTypeModel", "net.akehurst.language.api.language.grammar", "net.akehurst.language.typemodel.api")) {
                dataType("GrammarTypeNamespaceSimple") {
                    supertypes("GrammarTypeNamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                        parameter("imports", "std.List", false)
                    }
                }
                dataType("GrammarTypeNamespaceAbstract") {
                    supertypes("net.akehurst.language.typemodel.simple.TypeNamespaceAbstract", "net.akehurst.language.api.grammarTypeModel.GrammarTypeNamespace")
                    constructor_ {
                        parameter("qualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                        parameter("imports", "std.List", false)
                    }
                    propertyOf(setOf(READ_WRITE, STORED), "allRuleNameToType", "std.Map", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarRuleName")
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("GrammarTypeModelBuilderKt") {
                    supertypes("std.Any")
                }
                dataType("GrammarTypeModelBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("typeModel", "net.akehurst.language.typemodel.api.TypeModel", false)
                        parameter("namespaceQualifiedName", "net.akehurst.language.api.language.base.QualifiedName", false)
                        parameter("imports", "std.List", false)
                    }
                }
            }
        }

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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "contextQualifiedTypeName", "String",true)
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
                interfaceType("TypeModel") { }
                interfaceType("TypeNamespace") {}
                interfaceType("TypeInstance") {}
                interfaceType("TypeDeclaration") {}
                interfaceType("PrimitiveType") {
                    supertypes("TypeDeclaration")
                }
                interfaceType("EnumType") {
                    supertypes("TypeDeclaration")
                }
                interfaceType("StructuredType") {
                    supertypes("TypeDeclaration")
                }
                interfaceType("TupleType") {
                    supertypes("StructuredType")
                }
                interfaceType("DataType") {
                    supertypes("StructuredType")
                }
                interfaceType("PropertyDeclaration") {
                }
                enumType("PropertyCharacteristic", listOf())
                interfaceType("UnnamedSupertypeType") {
                    supertypes("TypeDeclaration")
                }
                interfaceType("CollectionType") {
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
                "net.akehurst.language.editor.language.service.messages",
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "text", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "position", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "options", "ProcessOptionsDefault")
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

