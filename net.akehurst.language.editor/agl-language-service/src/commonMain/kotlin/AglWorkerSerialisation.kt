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
import net.akehurst.language.agl.language.typemodel.typeModel
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.simple.SimpleTypeModelStdLib

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
            initialiseAllTypemodels()
//            initialiseBase()
//            initialiseGrammarAsm()
//            initialiseSPPT()
//            initialiseTypeModel()
//
//            initialiseApiTypes()
//            initialiseExpressionsAsm()
//            initialiseStyleAsm()
//            initialiseCrossReferencesAsm()
//            initialiseMessages()
//            initialiseAsmSimple()
            serialiser.registry.resolveImports()
            initialised = true
        }
    }

    /*
     api.language.base --> std
     agl.language.base --> std, api.language.base
     */
    /*
    api.language.grammar --> api.language.base
    agl.language.grammar.asm -> api.language.grammar, .api.language.base, agl.language.base
     */
    /*
    api.parser --> api.runtime
    api.sppt --> api.runtime, api.parser
    agl.sppt --> api.sppt, api.runtime
     */
    /*
    typemodel.api --> api.language.base
    typemodel.simple --> typemodel.api, agl.language.base
    api.grammarTypeModel --> typemodel.api, api.language.grammar
    agl.grammarTypeModel -->api.grammarTypeModel, typemodel.simple,
     */
    /*
    api.language.style --> api.language.base
    agl.language.style.asm --> api.language.style, agl.language.base
     */
    /*
    api.language.expressions --> typemodel.api, api.language.base
    agl.language.expressions.asm --> api.language.expressions,
     */
    /*
    api.language.reference --> api.language.expressions
    agl.language.reference.asm --> api.language.reference, api.language.expressions
     */
    private fun initialiseAllTypemodels() {
        val tm = typeModel("Test", true, listOf(SimpleTypeModelStdLib)) {
            namespace("net.akehurst.language.api.language.base", listOf("std")) {
                valueType("SimpleName") {
                    supertypes("PossiblyQualifiedName", "std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                valueType("QualifiedName") {
                    supertypes("PossiblyQualifiedName", "std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                valueType("Import") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
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
                        parameter("value", "String", false)
                        parameter("increment", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "increment", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
            }
            namespace("net.akehurst.language.agl.language.base", listOf("net.akehurst.language.api.language.base", "std")) {
                dataType("NamespaceDefault") {
                    supertypes("NamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                    }
                }
                dataType("NamespaceAbstract") {
                    supertypes("net.akehurst.language.api.language.base.Namespace", "std.Any")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "import", "List", false){
                        typeArgument("net.akehurst.language.api.language.base.Import")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedName", "QualifiedName", false)
                }
                dataType("ModelDefault") {
                    supertypes("ModelAbstract")
                    constructor_ {
                        parameter("name", "SimpleName", false)
                        parameter("namespace", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                }
                dataType("ModelAbstract") {
                    supertypes("net.akehurst.language.api.language.base.Model", "std.Any")
                    constructor_ {
                        parameter("namespace", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "List", false){
                        typeArgument("NT")
                    }
                }
            }
            namespace("net.akehurst.language.api.language.grammar", listOf("std", "net.akehurst.language.api.language.base")) {
                enumType("SeparatedListKind", listOf("Flat", "Left", "Right"))
                enumType("OverrideKind", listOf("REPLACE", "APPEND_ALTERNATIVE", "SUBSTITUTION"))
                enumType("Associativity", listOf("LEFT", "RIGHT"))
                valueType("GrammarRuleName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
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
                        parameter("message", "String", false)
                    }
                }
                dataType("GrammarRuleItemNotFoundException") {
                    supertypes("std.Exception")
                    constructor_ {
                        parameter("message", "String", false)
                    }
                }
                dataType("GrammarExeception") {
                    supertypes("std.Exception")
                    constructor_ {
                        parameter("message", "String", false)
                        parameter("cause", "Exception", true)
                    }
                }
            }
            namespace("net.akehurst.language.agl.language.grammar.asm", listOf("net.akehurst.language.api.language.grammar", "std", "net.akehurst.language.api.language.base", "net.akehurst.language.agl.language.base")) {
                dataType("TerminalDefault") {
                    supertypes("TangibleItemAbstract", "net.akehurst.language.api.language.grammar.Terminal")
                    constructor_ {
                        parameter("value", "String", false)
                        parameter("isPattern", "Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "id", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isPattern", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                dataType("TangibleItemAbstract") {
                    supertypes("SimpleItemAbstract", "net.akehurst.language.api.language.grammar.TangibleItem")
                    constructor_ {}
                }
                dataType("SimpleListDefault") {
                    supertypes("ListOfItemsAbstract", "net.akehurst.language.api.language.grammar.SimpleList")
                    constructor_ {
                        parameter("min_", "Integer", false)
                        parameter("max_", "Integer", false)
                        parameter("item", "RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "RuleItem", false)
                }
                dataType("SimpleItemAbstract") {
                    supertypes("ConcatenationItemAbstract", "net.akehurst.language.api.language.grammar.SimpleItem")
                    constructor_ {}
                }
                dataType("SeparatedListDefault") {
                    supertypes("ListOfItemsAbstract", "net.akehurst.language.api.language.grammar.SeparatedList")
                    constructor_ {
                        parameter("min_", "Integer", false)
                        parameter("max_", "Integer", false)
                        parameter("item", "RuleItem", false)
                        parameter("separator", "RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "RuleItem", false)
                    propertyOf(setOf(READ_ONLY, STORED), "separator", "RuleItem", false)
                }
                dataType("RuleItemAbstract") {
                    supertypes("net.akehurst.language.api.language.grammar.RuleItem", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, STORED), "index", "List", true){
                        typeArgument("std.Integer")
                    }
                }
                dataType("PreferenceRuleDefault") {
                    supertypes("GrammarItemAbstract", "net.akehurst.language.api.language.grammar.PreferenceRule")
                    constructor_ {
                        parameter("grammar", "Grammar", false)
                        parameter("forItem", "SimpleItem", false)
                        parameter("optionList", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forItem", "SimpleItem", false)
                    propertyOf(setOf(READ_ONLY, STORED), "grammar", "Grammar", false)
                    propertyOf(setOf(READ_ONLY, STORED), "optionList", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.PreferenceOption")
                    }
                }
                dataType("PreferenceOptionDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.PreferenceOption", "std.Any")
                    constructor_ {
                        parameter("item", "NonTerminal", false)
                        parameter("choiceNumber", "Integer", false)
                        parameter("onTerminals", "List", false)
                        parameter("associativity", "Associativity", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "associativity", "Associativity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "choiceNumber", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "item", "NonTerminal", false)
                    propertyOf(setOf(READ_ONLY, STORED), "onTerminals", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.SimpleItem")
                    }
                }
                dataType("OverrideRuleDefault") {
                    supertypes("GrammarRuleAbstract", "net.akehurst.language.api.language.grammar.OverrideRule")
                    constructor_ {
                        parameter("grammar", "Grammar", false)
                        parameter("name", "GrammarRuleName", false)
                        parameter("isSkip", "Boolean", false)
                        parameter("isLeaf", "Boolean", false)
                        parameter("overrideKind", "OverrideKind", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "grammar", "Grammar", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isLeaf", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isOverride", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isSkip", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "GrammarRuleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "overrideKind", "OverrideKind", false)
                }
                dataType("OptionalItemDefault") {
                    supertypes("ConcatenationItemAbstract", "net.akehurst.language.api.language.grammar.OptionalItem")
                    constructor_ {
                        parameter("item", "RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "RuleItem", false)
                }
                dataType("NormalRuleDefault") {
                    supertypes("GrammarRuleAbstract", "net.akehurst.language.api.language.grammar.NormalRule")
                    constructor_ {
                        parameter("grammar", "Grammar", false)
                        parameter("name", "GrammarRuleName", false)
                        parameter("isSkip", "Boolean", false)
                        parameter("isLeaf", "Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "grammar", "Grammar", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isLeaf", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isOverride", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isSkip", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "GrammarRuleName", false)
                }
                dataType("NonTerminalDefault") {
                    supertypes("TangibleItemAbstract", "net.akehurst.language.api.language.grammar.NonTerminal")
                    constructor_ {
                        parameter("targetGrammar", "GrammarReference", true)
                        parameter("ruleReference", "GrammarRuleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "ruleReference", "GrammarRuleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "targetGrammar", "GrammarReference", true)
                }
                dataType("NodeTypeDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.NodeType", "std.Any")
                    constructor_ {
                        parameter("identity", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "identity", "String", false)
                }
                dataType("ListOfItemsAbstract") {
                    supertypes("ConcatenationItemAbstract", "net.akehurst.language.api.language.grammar.ListOfItems")
                    constructor_ {
                        parameter("min", "Integer", false)
                        parameter("max", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "max", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "min", "Integer", false)
                }
                dataType("GroupDefault") {
                    supertypes("SimpleItemAbstract", "net.akehurst.language.api.language.grammar.Group")
                    constructor_ {
                        parameter("groupedContent", "RuleItem", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "groupedContent", "RuleItem", false)
                }
                dataType("GrammarRuleAbstract") {
                    supertypes("GrammarItemAbstract", "net.akehurst.language.api.language.grammar.GrammarRule")
                    constructor_ {}
                }
                dataType("GrammarReferenceDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarReference", "std.Any")
                    constructor_ {
                        parameter("localNamespace", "Namespace", false)
                        parameter("nameOrQName", "PossiblyQualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "localNamespace", "Namespace", false){
                        typeArgument("net.akehurst.language.api.language.grammar.Grammar")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "nameOrQName", "PossiblyQualifiedName", false)
                    propertyOf(setOf(READ_WRITE, STORED), "resolved", "Grammar", true)
                }
                dataType("GrammarOptionDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarOption", "std.Any")
                    constructor_ {
                        parameter("name", "String", false)
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                dataType("GrammarNamespaceDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarNamespace", "net.akehurst.language.agl.language.base.NamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                    }
                }
                dataType("GrammarModelDefault") {
                    supertypes("net.akehurst.language.api.language.grammar.GrammarModel", "net.akehurst.language.agl.language.base.ModelAbstract")
                    constructor_ {
                        parameter("name", "SimpleName", false)
                        parameter("namespace", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
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
                        parameter("namespace", "GrammarNamespace", false)
                        parameter("name", "SimpleName", false)
                        parameter("options", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "options", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarOption")
                    }
                }
                dataType("GrammarAbstract") {
                    supertypes("net.akehurst.language.api.language.grammar.Grammar", "std.Any")
                    constructor_ {
                        parameter("namespace", "GrammarNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "extends", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarReference")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "grammarRule", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.GrammarRule")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "GrammarNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "preferenceRule", "List", false){
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
                        parameter("embeddedGoalName", "GrammarRuleName", false)
                        parameter("embeddedGrammarReference", "GrammarReference", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "embeddedGoalName", "GrammarRuleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "embeddedGrammarReference", "GrammarReference", false)
                }
                dataType("ConcatenationItemAbstract") {
                    supertypes("RuleItemAbstract", "net.akehurst.language.api.language.grammar.ConcatenationItem")
                    constructor_ {}
                }
                dataType("ConcatenationDefault") {
                    supertypes("RuleItemAbstract", "net.akehurst.language.api.language.grammar.Concatenation")
                    constructor_ {
                        parameter("items", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "items", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoicePriorityDefault") {
                    supertypes("ChoiceAbstract", "net.akehurst.language.api.language.grammar.ChoicePriority")
                    constructor_ {
                        parameter("alternative", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoiceLongestDefault") {
                    supertypes("ChoiceAbstract", "net.akehurst.language.api.language.grammar.ChoiceLongest")
                    constructor_ {
                        parameter("alternative", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoiceAmbiguousDefault") {
                    supertypes("ChoiceAbstract", "net.akehurst.language.api.language.grammar.ChoiceAmbiguous")
                    constructor_ {
                        parameter("alternative", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
                dataType("ChoiceAbstract") {
                    supertypes("RuleItemAbstract", "net.akehurst.language.api.language.grammar.Choice")
                    constructor_ {
                        parameter("alternative", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "alternative", "List", false){
                        typeArgument("net.akehurst.language.api.language.grammar.RuleItem")
                    }
                }
            }
            namespace("net.akehurst.language.typemodel.api", listOf("std", "net.akehurst.language.api.language.base")) {
                enumType("PropertyCharacteristic", listOf("REFERENCE", "COMPOSITE", "READ_ONLY", "READ_WRITE", "STORED", "DERIVED", "PRIMITIVE", "CONSTRUCTOR", "IDENTITY"))
                valueType("PropertyName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                valueType("ParameterName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                valueType("MethodName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
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
            }
            namespace("net.akehurst.language.typemodel.simple", listOf("net.akehurst.language.typemodel.api", "net.akehurst.language.api.language.base", "std", "net.akehurst.language.agl.language.base")) {
                dataType("ValueTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.ValueType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "constructors", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ConstructorDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                }
                dataType("UnnamedSupertypeTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.UnnamedSupertypeType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("id", "Integer", false)
                        parameter("subtypes", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "id", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "subtypes", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("UnnamedSupertypeTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("declaration", "UnnamedSupertypeType", false)
                        parameter("typeArguments", "List", false)
                        parameter("isNullable", "Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "declaration", "UnnamedSupertypeType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isNullable", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeArguments", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("TypeNamespaceSimple") {
                    supertypes("TypeNamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                        parameter("imports", "List", false)
                    }
                }
                dataType("TypeNamespaceAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.TypeNamespace", "net.akehurst.language.agl.language.base.NamespaceAbstract")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                        parameter("imports", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "import", "List", false){
                        typeArgument("net.akehurst.language.api.language.base.Import")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "ownedTupleTypes", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TupleType")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "ownedUnnamedSupertypeType", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.UnnamedSupertypeType")
                    }
                }
                dataType("TypeModelSimpleAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.TypeModel", "std.Any")
                    constructor_ {
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "allNamespace", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeNamespace")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                }
                dataType("TypeModelSimple") {
                    supertypes("TypeModelSimpleAbstract")
                    constructor_ {
                        parameter("name", "SimpleName", false)
                    }
                }
                dataType("TypeInstanceSimple") {
                    supertypes("TypeInstanceAbstract")
                    constructor_ {
                        parameter("contextQualifiedTypeName", "QualifiedName", true)
                        parameter("namespace", "TypeNamespace", false)
                        parameter("qualifiedOrImportedTypeName", "PossiblyQualifiedName", false)
                        parameter("typeArguments", "List", false)
                        parameter("isNullable", "Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "contextQualifiedTypeName", "QualifiedName", true)
                    propertyOf(setOf(READ_ONLY, STORED), "isNullable", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedOrImportedTypeName", "PossiblyQualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeArguments", "List", false){
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
                    propertyOf(setOf(READ_WRITE, STORED), "metaInfo", "Map", false){
                        typeArgument("std.String")
                        typeArgument("std.String")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "method", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.MethodDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "propertyByIndex", "Map", false){
                        typeArgument("std.Integer")
                        typeArgument("net.akehurst.language.typemodel.api.PropertyDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "supertypes", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "typeParameters", "List", false){
                        typeArgument("net.akehurst.language.api.language.base.SimpleName")
                    }
                }
                dataType("TupleTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.TupleType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("id", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "id", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                }
                dataType("TupleTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("declaration", "TupleType", false)
                        parameter("typeArguments", "List", false)
                        parameter("isNullable", "Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "declaration", "TupleType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "isNullable", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeArguments", "List", false){
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
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                }
                dataType("SingletonTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.SingletonType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                }
                dataType("SimpleTypeModelStdLib") {
                    supertypes("TypeNamespaceAbstract")
                    propertyOf(setOf(READ_ONLY, STORED), "AnyType", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Boolean", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Collection", "CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Exception", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Integer", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "List", "CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "ListSeparated", "CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Map", "CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "NothingType", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "OrderedSet", "CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Pair", "DataType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Real", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Set", "CollectionType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "String", "TypeInstance", false)
                    propertyOf(setOf(READ_ONLY, STORED), "Timestamp", "TypeInstance", false)
                }
                dataType("PropertyDeclarationStored") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "StructuredType", false)
                        parameter("name", "PropertyName", false)
                        parameter("typeInstance", "TypeInstance", false)
                        parameter("characteristics", "Set", false)
                        parameter("index", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "characteristics", "Set", false){
                        typeArgument("net.akehurst.language.typemodel.api.PropertyCharacteristic")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "StructuredType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "TypeInstance", false)
                }
                dataType("PropertyDeclarationResolved") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "TypeDeclaration", false)
                        parameter("name", "PropertyName", false)
                        parameter("typeInstance", "TypeInstance", false)
                        parameter("characteristics", "Set", false)
                        parameter("description", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "characteristics", "Set", false){
                        typeArgument("net.akehurst.language.typemodel.api.PropertyCharacteristic")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "TypeInstance", false)
                }
                dataType("PropertyDeclarationPrimitive") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "TypeDeclaration", false)
                        parameter("name", "PropertyName", false)
                        parameter("typeInstance", "TypeInstance", false)
                        parameter("description", "String", false)
                        parameter("index", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "TypeInstance", false)
                }
                dataType("PropertyDeclarationDerived") {
                    supertypes("PropertyDeclarationAbstract")
                    constructor_ {
                        parameter("owner", "TypeDeclaration", false)
                        parameter("name", "PropertyName", false)
                        parameter("typeInstance", "TypeInstance", false)
                        parameter("description", "String", false)
                        parameter("expression", "String", false)
                        parameter("index", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "TypeInstance", false)
                }
                dataType("PropertyDeclarationAbstract") {
                    supertypes("net.akehurst.language.typemodel.api.PropertyDeclaration", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, STORED), "metaInfo", "Map", false){
                        typeArgument("std.String")
                        typeArgument("std.String")
                    }
                }
                dataType("PrimitiveTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.PrimitiveType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                }
                dataType("ParameterDefinitionSimple") {
                    supertypes("net.akehurst.language.typemodel.api.ParameterDeclaration", "std.Any")
                    constructor_ {
                        parameter("name", "ParameterName", false)
                        parameter("typeInstance", "TypeInstance", false)
                        parameter("defaultValue", "String", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "defaultValue", "String", true)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "ParameterName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeInstance", "TypeInstance", false)
                }
                dataType("MethodDeclarationDerived") {
                    supertypes("net.akehurst.language.typemodel.api.MethodDeclaration", "std.Any")
                    constructor_ {
                        parameter("owner", "TypeDeclaration", false)
                        parameter("name", "MethodName", false)
                        parameter("parameters", "List", false)
                        parameter("description", "String", false)
                        parameter("body", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "body", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "MethodName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "parameters", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ParameterDeclaration")
                    }
                }
                dataType("InterfaceTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.InterfaceType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "subtypes", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("EnumTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "net.akehurst.language.typemodel.api.EnumType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                        parameter("literals", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "literals", "List", false){
                        typeArgument("std.String")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                }
                dataType("DataTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.DataType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "constructors", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ConstructorDeclaration")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_ONLY, STORED), "subtypes", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.TypeInstance")
                    }
                }
                dataType("ConstructorDeclarationSimple") {
                    supertypes("net.akehurst.language.typemodel.api.ConstructorDeclaration", "std.Any")
                    constructor_ {
                        parameter("owner", "TypeDeclaration", false)
                        parameter("parameters", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "owner", "TypeDeclaration", false)
                    propertyOf(setOf(READ_ONLY, STORED), "parameters", "List", false){
                        typeArgument("net.akehurst.language.typemodel.api.ParameterDeclaration")
                    }
                }
                dataType("CollectionTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "net.akehurst.language.typemodel.api.CollectionType")
                    constructor_ {
                        parameter("namespace", "TypeNamespace", false)
                        parameter("name", "SimpleName", false)
                        parameter("typeParameters", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "namespace", "TypeNamespace", false)
                    propertyOf(setOf(READ_WRITE, STORED), "typeParameters", "List", false){
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
                        parameter("qualifiedName", "QualifiedName", false)
                        parameter("imports", "List", false)
                    }
                }
                dataType("GrammarTypeNamespaceAbstract") {
                    supertypes("net.akehurst.language.typemodel.simple.TypeNamespaceAbstract", "net.akehurst.language.api.grammarTypeModel.GrammarTypeNamespace")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                        parameter("imports", "List", false)
                    }
                    propertyOf(setOf(READ_WRITE, STORED), "allRuleNameToType", "Map", false){
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
                        parameter("typeModel", "TypeModel", false)
                        parameter("namespaceQualifiedName", "QualifiedName", false)
                        parameter("imports", "List", false)
                    }
                }
            }
            namespace("net.akehurst.language.api.language.expressions", listOf("std", "net.akehurst.language.typemodel.api", "net.akehurst.language.api.language.base")) {
                interfaceType("WithExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("WhenOption") {
                    supertypes("std.Any")
                }
                interfaceType("WhenExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("RootExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("PropertyCall") {
                    supertypes("NavigationPart", "std.Any")
                }
                interfaceType("OnExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("NavigationPart") {
                    supertypes("std.Any")
                }
                interfaceType("NavigationExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("MethodCall") {
                    supertypes("NavigationPart", "std.Any")
                }
                interfaceType("LiteralExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("InfixExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("IndexOperation") {
                    supertypes("NavigationPart", "std.Any")
                }
                interfaceType("Expression") {
                    supertypes("std.Any")
                }
                interfaceType("CreateTupleExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("CreateObjectExpression") {
                    supertypes("Expression", "std.Any")
                }
                interfaceType("AssignmentStatement") {
                    supertypes("std.Any")
                }
            }
            namespace("net.akehurst.language.agl.language.expressions.asm", listOf("net.akehurst.language.api.language.expressions", "std", "net.akehurst.language.typemodel.api", "net.akehurst.language.api.language.base")) {
                dataType("WithExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.WithExpression")
                    constructor_ {
                        parameter("withContext", "Expression", false)
                        parameter("expression", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "Expression", false)
                    propertyOf(setOf(READ_ONLY, STORED), "withContext", "Expression", false)
                }
                dataType("WhenOptionSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.WhenOption", "std.Any")
                    constructor_ {
                        parameter("condition", "Expression", false)
                        parameter("expression", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "condition", "Expression", false)
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "Expression", false)
                }
                dataType("WhenExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.WhenExpression")
                    constructor_ {
                        parameter("options", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "options", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.WhenOption")
                    }
                }
                dataType("RootExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.RootExpression")
                    constructor_ {
                        parameter("name", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "String", false)
                }
                dataType("PropertyCallSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.PropertyCall", "std.Any")
                    constructor_ {
                        parameter("propertyName", "PropertyName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "propertyName", "PropertyName", false)
                }
                dataType("OnExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.OnExpression")
                    constructor_ {
                        parameter("expression", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "Expression", false)
                    propertyOf(setOf(READ_WRITE, STORED), "propertyAssignments", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.AssignmentStatement")
                    }
                }
                dataType("NavigationSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.NavigationExpression")
                    constructor_ {
                        parameter("start", "Expression", false)
                        parameter("parts", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "parts", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.NavigationPart")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "start", "Expression", false)
                }
                dataType("MethodCallSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.MethodCall", "std.Any")
                    constructor_ {
                        parameter("methodName", "MethodName", false)
                        parameter("arguments", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "arguments", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "methodName", "MethodName", false)
                }
                dataType("LiteralExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.LiteralExpression")
                    constructor_ {
                        parameter("qualifiedTypeName", "QualifiedName", false)
                        parameter("value", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "Any", false)
                }
                dataType("InfixExpressionSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.InfixExpression", "std.Any")
                    constructor_ {
                        parameter("expressions", "List", false)
                        parameter("operators", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "expressions", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "operators", "List", false){
                        typeArgument("std.String")
                    }
                }
                dataType("IndexOperationSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.IndexOperation", "std.Any")
                    constructor_ {
                        parameter("indices", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "indices", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                }
                dataType("ExpressionAbstract") {
                    supertypes("net.akehurst.language.api.language.expressions.Expression", "std.Any")
                    constructor_ {}
                }
                dataType("CreateTupleExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.CreateTupleExpression")
                    constructor_ {
                        parameter("propertyAssignments", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "propertyAssignments", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.AssignmentStatement")
                    }
                }
                dataType("CreateObjectExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.CreateObjectExpression")
                    constructor_ {
                        parameter("possiblyQualifiedTypeName", "PossiblyQualifiedName", false)
                        parameter("arguments", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "arguments", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "possiblyQualifiedTypeName", "PossiblyQualifiedName", false)
                    propertyOf(setOf(READ_WRITE, STORED), "propertyAssignments", "List", false){
                        typeArgument("net.akehurst.language.api.language.expressions.AssignmentStatement")
                    }
                }
                dataType("AssignmentStatementSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.AssignmentStatement", "std.Any")
                    constructor_ {
                        parameter("lhsPropertyName", "PropertyName", false)
                        parameter("rhs", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "lhsPropertyName", "PropertyName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "rhs", "Expression", false)
                }
            }
            namespace("net.akehurst.language.api.language.reference", listOf("std", "net.akehurst.language.api.language.base", "net.akehurst.language.api.language.expressions")) {
                interfaceType("ScopeDefinition") {
                    supertypes("std.Any")
                }
                interfaceType("ReferenceExpression") {
                    supertypes("std.Any")
                }
                interfaceType("ReferenceDefinition") {
                    supertypes("std.Any")
                }
                interfaceType("Identifiable") {
                    supertypes("std.Any")
                }
                interfaceType("DeclarationsForNamespace") {
                    supertypes("std.Any")
                }
                interfaceType("CrossReferenceModel") {
                    supertypes("std.Any")
                }
            }
            namespace("net.akehurst.language.agl.language.reference.asm", listOf("net.akehurst.language.api.language.reference", "std", "net.akehurst.language.api.language.base", "net.akehurst.language.api.language.expressions")) {
                dataType("ScopeDefinitionDefault") {
                    supertypes("net.akehurst.language.api.language.reference.ScopeDefinition", "std.Any")
                    constructor_ {
                        parameter("scopeForTypeName", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "identifiables", "List", false){
                        typeArgument("net.akehurst.language.api.language.reference.Identifiable")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "scopeForTypeName", "SimpleName", false)
                }
                dataType("ReferenceExpressionAbstract") {
                    supertypes("net.akehurst.language.api.language.reference.ReferenceExpression", "std.Any")
                    constructor_ {}
                }
                dataType("ReferenceDefinitionDefault") {
                    supertypes("net.akehurst.language.api.language.reference.ReferenceDefinition", "std.Any")
                    constructor_ {
                        parameter("inTypeName", "SimpleName", false)
                        parameter("referenceExpressionList", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "inTypeName", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "referenceExpressionList", "List", false){
                        typeArgument("net.akehurst.language.api.language.reference.ReferenceExpression")
                    }
                }
                dataType("PropertyReferenceExpressionDefault") {
                    supertypes("ReferenceExpressionAbstract")
                    constructor_ {
                        parameter("referringPropertyNavigation", "NavigationExpression", false)
                        parameter("refersToTypeName", "List", false)
                        parameter("fromNavigation", "NavigationExpression", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "fromNavigation", "NavigationExpression", true)
                    propertyOf(setOf(READ_ONLY, STORED), "referringPropertyNavigation", "NavigationExpression", false)
                    propertyOf(setOf(READ_ONLY, STORED), "refersToTypeName", "List", false){
                        typeArgument("net.akehurst.language.api.language.base.PossiblyQualifiedName")
                    }
                }
                dataType("IdentifiableDefault") {
                    supertypes("net.akehurst.language.api.language.reference.Identifiable", "std.Any")
                    constructor_ {
                        parameter("typeName", "SimpleName", false)
                        parameter("identifiedBy", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "identifiedBy", "Expression", false)
                    propertyOf(setOf(READ_ONLY, STORED), "typeName", "SimpleName", false)
                }
                dataType("DeclarationsForNamespaceDefault") {
                    supertypes("net.akehurst.language.api.language.reference.DeclarationsForNamespace", "std.Any")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                        parameter("importedNamespaces", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "importedNamespaces", "List", false){
                        typeArgument("net.akehurst.language.api.language.base.Import")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "references", "List", false){
                        typeArgument("net.akehurst.language.api.language.reference.ReferenceDefinition")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "scopeDefinition", "Map", false){
                        typeArgument("net.akehurst.language.api.language.base.SimpleName")
                        typeArgument("net.akehurst.language.api.language.reference.ScopeDefinition")
                    }
                }
                dataType("CrossReferenceModelDefault") {
                    supertypes("net.akehurst.language.api.language.reference.CrossReferenceModel", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_ONLY, STORED), "declarationsForNamespace", "Map", false){
                        typeArgument("net.akehurst.language.api.language.base.QualifiedName")
                        typeArgument("net.akehurst.language.api.language.reference.DeclarationsForNamespace")
                    }
                }
                dataType("CollectionReferenceExpressionDefault") {
                    supertypes("ReferenceExpressionAbstract")
                    constructor_ {
                        parameter("expression", "Expression", false)
                        parameter("ofType", "PossiblyQualifiedName", true)
                        parameter("referenceExpressionList", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "Expression", false)
                    propertyOf(setOf(READ_ONLY, STORED), "ofType", "PossiblyQualifiedName", true)
                    propertyOf(setOf(READ_ONLY, STORED), "referenceExpressionList", "List", false){
                        typeArgument("net.akehurst.language.agl.language.reference.asm.ReferenceExpressionAbstract")
                    }
                }
            }
            namespace("net.akehurst.language.api.scope", listOf("std", "net.akehurst.language.api.language.base", "net.akehurst.language.agl.scope")) {
                interfaceType("Scope") {
                    typeParameters("ItemType")
                    supertypes("std.Any")
                }
                dataType("ScopedItem") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("referableName", "String", false)
                        parameter("qualifiedTypeName", "QualifiedName", false)
                        parameter("item", "ItemType", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "item", "ItemType", false)
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "referableName", "String", false)
                }
            }
            namespace("net.akehurst.language.agl.scope", listOf("net.akehurst.language.api.scope", "std", "net.akehurst.language.api.language.base")) {
                dataType("ScopeSimple") {
                    supertypes("net.akehurst.language.api.scope.Scope", "std.Any")
                    constructor_ {
                        parameter("parent", "ScopeSimple", true)
                        parameter("scopeIdentityInParent", "String", false)
                        parameter("forTypeName", "QualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "childScopes", "Map", false){
                        typeArgument("std.String")
                        typeArgument("net.akehurst.language.agl.scope.ScopeSimple")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "parent", "ScopeSimple", true){
                        typeArgument("ItemType")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "scopeIdentity", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "scopeIdentityInParent", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "scopeMap", "Map", false){
                        typeArgument("ItemType")
                        typeArgument("net.akehurst.language.agl.scope.ScopeSimple")
                    }
                }
            }
            namespace("net.akehurst.language.api.asm", listOf("std", "net.akehurst.language.typemodel.api", "net.akehurst.language.api.language.reference", "net.akehurst.language.agl.scope", "net.akehurst.language.agl.asm", "net.akehurst.language.api.language.base", "net.akehurst.language.collections")) {
                valueType("PropertyValueName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                interfaceType("AsmValue") {
                    supertypes("std.Any")
                }
                interfaceType("AsmTreeWalker") {
                    supertypes("std.Any")
                }
                interfaceType("AsmStructureProperty") {
                    supertypes("std.Any")
                }
                interfaceType("AsmStructure") {
                    supertypes("AsmValue", "std.Any")
                }
                interfaceType("AsmReference") {
                    supertypes("std.Any")
                }
                interfaceType("AsmPrimitive") {
                    supertypes("AsmValue", "std.Any")
                }
                interfaceType("AsmPath") {
                    supertypes("std.Any")
                }
                interfaceType("AsmNothing") {
                    supertypes("AsmValue", "std.Any")
                }
                interfaceType("AsmListSeparated") {
                    supertypes("AsmList", "std.Any")
                }
                interfaceType("AsmList") {
                    supertypes("AsmValue", "std.Any")
                }
                interfaceType("Asm") {
                    supertypes("std.Any")
                }
                dataType("ListAsmElementSimpleBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("_typeModel", "TypeModel", false)
                        parameter("_scopeModel", "CrossReferenceModel", false)
                        parameter("_scopeMap", "Map", false)
                        parameter("_asm", "AsmSimple", false)
                        parameter("_asmPath", "AsmPath", false)
                        parameter("_parentScope", "ScopeSimple", true)
                    }
                }
                dataType("AsmSimpleBuilderKt") {
                    supertypes("std.Any")
                }
                dataType("AsmElementSimpleBuilder") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("_typeModel", "TypeModel", false)
                        parameter("_crossReferenceModel", "CrossReferenceModel", false)
                        parameter("_scopeMap", "Map", false)
                        parameter("_asm", "AsmSimple", false)
                        parameter("_asmPath", "AsmPath", false)
                        parameter("_typeName", "String", false)
                        parameter("_isRoot", "Boolean", false)
                        parameter("_parentScope", "ScopeSimple", true)
                    }
                }
            }
            namespace("net.akehurst.language.agl.asm", listOf("net.akehurst.language.api.asm", "std", "net.akehurst.language.api.language.base", "net.akehurst.language.collections")) {
                dataType("AsmValueAbstract") {
                    supertypes("net.akehurst.language.api.asm.AsmValue", "std.Any")
                    constructor_ {}
                }
                dataType("AsmStructureSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmStructure")
                    constructor_ {
                        parameter("path", "AsmPath", false)
                        parameter("qualifiedTypeName", "QualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "path", "AsmPath", false)
                    propertyOf(setOf(READ_ONLY, STORED), "property", "Map", false){
                        typeArgument("net.akehurst.language.api.asm.PropertyValueName")
                        typeArgument("net.akehurst.language.api.asm.AsmStructureProperty")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedTypeName", "QualifiedName", false)
                }
                dataType("AsmStructurePropertySimple") {
                    supertypes("net.akehurst.language.api.asm.AsmStructureProperty", "std.Any")
                    constructor_ {
                        parameter("name", "PropertyValueName", false)
                        parameter("index", "Integer", false)
                        parameter("value", "AsmValue", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "PropertyValueName", false)
                    propertyOf(setOf(READ_WRITE, STORED), "value", "AsmValue", false)
                }
                dataType("AsmSimpleKt") {
                    supertypes("std.Any")
                }
                dataType("AsmSimple") {
                    supertypes("net.akehurst.language.api.asm.Asm", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_ONLY, STORED), "elementIndex", "Map", false){
                        typeArgument("net.akehurst.language.api.asm.AsmPath")
                        typeArgument("net.akehurst.language.api.asm.AsmStructure")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "root", "List", false){
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                    }
                }
                dataType("AsmReferenceSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmReference")
                    constructor_ {
                        parameter("reference", "String", false)
                        parameter("value", "AsmStructure", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "reference", "String", false)
                    propertyOf(setOf(READ_WRITE, STORED), "value", "AsmStructure", true)
                }
                dataType("AsmPrimitiveSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmPrimitive")
                    constructor_ {
                        parameter("qualifiedTypeName", "QualifiedName", false)
                        parameter("value", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "qualifiedTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "Any", false)
                }
                dataType("AsmPathSimple") {
                    supertypes("net.akehurst.language.api.asm.AsmPath", "std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                dataType("AsmNothingSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmNothing")
                }
                dataType("AsmListSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmList")
                    constructor_ {
                        parameter("elements", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "elements", "List", false){
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                    }
                }
                dataType("AsmListSeparatedSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmListSeparated")
                    constructor_ {
                        parameter("elements", "ListSeparated", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "elements", "ListSeparated", false){
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                    }
                }
            }
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
                        parameter("position", "Integer", false)
                        parameter("column", "Integer", false)
                        parameter("line", "Integer", false)
                        parameter("length", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "column", "Integer", false)
                    propertyOf(setOf(READ_WRITE, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "line", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "position", "Integer", false)
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
                        parameter("message", "String", false)
                        parameter("cause", "Exception", true)
                    }
                }
                dataType("LeafData") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("name", "String", false)
                        parameter("isPattern", "Boolean", false)
                        parameter("position", "Integer", false)
                        parameter("length", "Integer", false)
                        parameter("tagList", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "isPattern", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "tagList", "List", false){
                        typeArgument("std.String")
                    }
                }
                dataType("ChildInfo") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("propertyIndex", "Integer", false)
                        parameter("index", "Integer", false)
                        parameter("total", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "propertyIndex", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "total", "Integer", false)
                }
                dataType("AltInfo") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("option", "Integer", false)
                        parameter("index", "Integer", false)
                        parameter("totalMatched", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "option", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "totalMatched", "Integer", false)
                }
            }
            namespace("net.akehurst.language.agl.sppt", listOf("std", "net.akehurst.language.api.sppt", "net.akehurst.language.agl.api.runtime")) {
                dataType("TreeDataCompleteKt") {
                    supertypes("std.Any")
                }
                dataType("TreeDataComplete2") {
                    supertypes("net.akehurst.language.api.sppt.TreeData", "std.Any")
                    constructor_ {
                        parameter("forStateSetNumber", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forStateSetNumber", "Integer", false)
                    propertyOf(setOf(READ_WRITE, STORED), "initialSkip", "TreeData", true)
                    propertyOf(setOf(READ_WRITE, STORED), "root", "SpptDataNode", true)
                }
                dataType("TreeDataComplete") {
                    supertypes("net.akehurst.language.api.sppt.TreeData", "std.Any")
                    constructor_ {
                        parameter("forStateSetNumber", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "forStateSetNumber", "Integer", false)
                    propertyOf(setOf(READ_WRITE, STORED), "initialSkip", "TreeData", true)
                    propertyOf(setOf(READ_WRITE, STORED), "root", "SpptDataNode", true)
                }
                dataType("CompleteTreeDataNode") {
                    supertypes("net.akehurst.language.api.sppt.SpptDataNode", "std.Any")
                    constructor_ {
                        parameter("rule", "Rule", false)
                        parameter("startPosition", "Integer", false)
                        parameter("nextInputPosition", "Integer", false)
                        parameter("nextInputNoSkip", "Integer", false)
                        parameter("option", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "nextInputNoSkip", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "nextInputPosition", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "option", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "rule", "Rule", false)
                    propertyOf(setOf(READ_ONLY, STORED), "startPosition", "Integer", false)
                }
                dataType("CompleteKey") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("rule", "Rule", false)
                        parameter("startPosition", "Integer", false)
                        parameter("nextInputPosition", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "nextInputPosition", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "rule", "Rule", false)
                    propertyOf(setOf(READ_ONLY, STORED), "startPosition", "Integer", false)
                }
            }
            namespace("net.akehurst.language.agl.api.runtime", listOf("std")) {
                interfaceType("RuleSetBuilder") {
                    supertypes("std.Any")
                }
                interfaceType("RuleSet") {
                    supertypes("std.Any")
                }
                interfaceType("Rule") {
                    supertypes("std.Any")
                }
                interfaceType("ConcatenationBuilder") {
                    supertypes("std.Any")
                }
                interfaceType("ChoiceBuilder") {
                    supertypes("std.Any")
                }
            }
            namespace("net.akehurst.language.api.language.style", listOf("net.akehurst.language.api.language.base", "std")) {
                enumType("AglStyleSelectorKind", listOf("LITERAL", "PATTERN", "RULE_NAME", "META"))
                interfaceType("StyleNamespace") {
                    supertypes("net.akehurst.language.api.language.base.Namespace", "std.Any")
                }
                interfaceType("AglStyleRule") {
                    supertypes("net.akehurst.language.api.language.base.Definition", "std.Any")
                }
                interfaceType("AglStyleModel") {
                    supertypes("net.akehurst.language.api.language.base.Model", "std.Any")
                }
                dataType("AglStyleSelector") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                        parameter("kind", "AglStyleSelectorKind", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "kind", "AglStyleSelectorKind", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
                dataType("AglStyleDeclaration") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("name", "String", false)
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "value", "String", false)
                }
            }
            namespace("net.akehurst.language.editor.language.service.messages", listOf("net.akehurst.language.editor.api", "std", "net.akehurst.language.api.processor", "net.akehurst.language.agl.scanner", "net.akehurst.language.api.sppt", "net.akehurst.language.api.language.style", "net.akehurst.language.editor.common")) {
                dataType("MessageProcessorDelete") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                }
                dataType("MessageGrammarAmbiguityAnalysisResult") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", true)
                        parameter("issues", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", true)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageGrammarAmbiguityAnalysisRequest") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                }
                dataType("MessageSyntaxAnalysisResult") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("asm", "Any", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "asm", "Any", true)
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageLineTokens") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("startLine", "Integer", false)
                        parameter("lineTokens", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "lineTokens", "List", false){
                        typeArgument("std.List")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "startLine", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageParseResult") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("treeSerialised", "String", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, STORED), "treeSerialised", "String", true)
                }
                dataType("MessageSetStyle") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("styleStr", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "styleStr", "String", false)
                }
                dataType("MessageProcessorDeleteResponse") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageProcessorCreateResponse") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("scannerMatchables", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "scannerMatchables", "List", false){
                        typeArgument("net.akehurst.language.agl.scanner.Matchable")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageSemanticAnalysisResult") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("asm", "Any", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "asm", "Any", true)
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("AglWorkerMessageResponse") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("action", "String", false)
                    }
                }
                dataType("MessageParserInterruptRequest") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("reason", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "reason", "String", false)
                }
                dataType("MessageCodeCompleteResult") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("completionItems", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "completionItems", "List", false){
                        typeArgument("net.akehurst.language.api.processor.CompletionItem")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageProcessorCreate") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("grammarStr", "String", false)
                        parameter("crossReferenceModelStr", "String", true)
                        parameter("editorOptions", "EditorOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "crossReferenceModelStr", "String", true)
                    propertyOf(setOf(READ_ONLY, STORED), "editorOptions", "EditorOptions", false)
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "grammarStr", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                }
                dataType("MessageCodeCompleteRequest") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("text", "String", false)
                        parameter("position", "Integer", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "options", "ProcessOptions", false){
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "text", "String", false)
                }
                dataType("MessageProcessRequest") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("text", "String", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "options", "ProcessOptions", false){
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "text", "String", false)
                }
                dataType("MessageParseResult2") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("treeData", "TreeData", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, STORED), "treeData", "TreeData", false)
                }
                dataType("MessageSetStyleResponse") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("styleModel", "AglStyleModel", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, STORED), "styleModel", "AglStyleModel", true)
                }
                dataType("AglWorkerMessage") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("action", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "action", "String", false)
                }
                dataType("MessageScanResult") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("lineTokens", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, STORED), "issues", "List", false){
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "lineTokens", "List", false){
                        typeArgument("net.akehurst.language.editor.common.AglTokenDefault")
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "status", "MessageStatus", false)
                }
            }
            namespace("net.akehurst.language.collections", listOf("std")) {
                interfaceType("ListSeparated") {
                    typeParameters("E", "I", "S")
                    supertypes("std.List", "std.Any")
                }
            }
            namespace("net.akehurst.language.editor.api", listOf("std")) {
                enumType("MessageStatus", listOf("START", "FAILURE", "SUCCESS"))
                interfaceType("EditorOptions") {
                    supertypes("std.Any")
                }
                interfaceType("AglToken") {
                    supertypes("std.Any")
                }
                dataType("EndPointIdentity") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("editorId", "String", false)
                        parameter("sessionId", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "editorId", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "sessionId", "String", false)
                }
            }
            namespace("net.akehurst.language.editor.common", listOf("net.akehurst.language.editor.api", "std")) {
                dataType("AglTokenDefault") {
                    supertypes("net.akehurst.language.editor.api.AglToken", "std.Any")
                    constructor_ {
                        parameter("styles", "List", false)
                        parameter("position", "Integer", false)
                        parameter("length", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, STORED), "styles", "List", false){
                        typeArgument("std.String")
                    }
                }
            }
            namespace("net.akehurst.language.agl.scanner", listOf("std")) {
                dataType("Matchable") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("tag", "String", false)
                        parameter("expression", "String", false)
                        parameter("kind", "MatchableKind", false)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "expression", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "kind", "MatchableKind", false)
                    propertyOf(setOf(READ_ONLY, STORED), "tag", "String", false)
                }
            }
            namespace("net.akehurst.language.api.processor", listOf("std", "net.akehurst.language.api.parser")) {
                interfaceType("ProcessOptions") {
                    typeParameters("AsmType", "ContextType")
                    supertypes("std.Any")
                }
                dataType("LanguageIssue") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("kind", "LanguageIssueKind", false)
                        parameter("phase", "LanguageProcessorPhase", false)
                        parameter("location", "InputLocation", true)
                        parameter("message", "String", false)
                        parameter("data", "Any", true)
                    }
                    propertyOf(setOf(READ_ONLY, STORED), "data", "Any", true)
                    propertyOf(setOf(READ_ONLY, STORED), "kind", "LanguageIssueKind", false)
                    propertyOf(setOf(READ_ONLY, STORED), "location", "InputLocation", true)
                    propertyOf(setOf(READ_ONLY, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "phase", "LanguageProcessorPhase", false)
                }
                dataType("CompletionItem") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("kind", "CompletionItemKind", false)
                        parameter("text", "String", false)
                        parameter("name", "String", false)
                    }
                    propertyOf(setOf(READ_WRITE, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "kind", "CompletionItemKind", false)
                    propertyOf(setOf(READ_ONLY, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, STORED), "text", "String", false)
                }
            }
        }
        serialiser.configureFromTypeModel(tm)
    }

    /*
        namespace net.akehurst.language.agl.grammarTypeModel
          GrammarTypeNamespaceAbstract {  allRuleNameToType }
        namespace net.akehurst.language.typemodel.simple
          TypeModelSimpleAbstract {  namespace }
          TypeInstanceSimple {  typeArguments }
          UnnamedSupertypeTypeInstance {  typeArguments  }
          TypeNamespaceAbstract { ownedUnnamedSupertypeType, ownedTupleTypes, ownedTypesByName }
          TypeDeclarationSimpleAbstract { propertyByIndex }
          UnnamedSupertypeTypeSimple { subtypes }
          PropertyDeclarationPrimitive { typeInstance }
          PropertyDeclarationDerived { typeInstance }
          PropertyDeclarationStored { typeInstance }
     */
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

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "allRuleNameToType", "Map") {
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

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "namespace", "Map") {
                        typeArgument("String")
                        typeArgument("TypeNamespace")
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE), "allNamespace", "List") { typeArgument("TypeNamespace") }
                    //propertyOf(setOf(READ_WRITE, COMPOSITE), "rules", "Map", listOf("String", "net.akehurst.language.api.typemodel.RuleType"))
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "ownedUnnamedSupertypeType", "List") {
                        typeArgument("UnnamedSupertypeTypeSimple")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "ownedTupleTypes", "List") {
                        typeArgument("TupleTypeSimple")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "ownedTypesByName", "Map") {
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "typeParameters", "List") { typeArgument("String") }

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "propertyByIndex", "Map") {
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

                    propertyOf(setOf(READ_WRITE, REFERENCE), "supertypes", "List"){ typeArgument("DataType") }
                    propertyOf(setOf(READ_WRITE, REFERENCE), "subtypes", "List"){ typeArgument("DataType") }
                }
                dataType("CollectionTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "CollectionType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeParameters", "String")

                    propertyOf(setOf(READ_WRITE, REFERENCE), "supertypes", "List"){ typeArgument("CollectionType") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rules", "List"){ typeArgument("\"net.akehurst.language.api.style.AglStyleRule\"") }
                }
            }
            namespace("net.akehurst.language.api.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("AglStyleRule") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "selector", "AglStyleSelector")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "styles", "Map") {
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "List"){ typeArgument("String") }
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "declarationsForNamespace", "Map") {
                        typeArgument("String")
                        typeArgument("DeclarationsForNamespaceDefault")
                    }
                }
                dataType("DeclarationsForNamespaceDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedName", "String")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "scopeDefinition", "Map") {
                        typeArgument("String")
                        typeArgument("ScopeDefinitionDefault")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "references", "List"){ typeArgument("ReferenceDefinitionDefault") }
                }
                dataType("ScopeDefinitionDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeForTypeName", "String")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "identifiables", "List"){ typeArgument("IdentifiableDefault") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "refersToTypeName", "List"){ typeArgument("String") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "fromNavigation", "Navigation",true)
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "grammarStr", "String",  true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "crossReferenceModelStr", "String", true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorOptions", "EditorOptionsDefault", false)
                }
                dataType("MessageProcessorCreateResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeSerialised", "String",true)
                }
//FIXME
                dataType("MessageParseResult2") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeData", "TreeDataComplete", true)
                }

                dataType("MessageSyntaxAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any",  true)
                }
                dataType("MessageSemanticAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", true)
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "styleModel", "AglStyleModelDefault", true)
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List"){ typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "completionItems", "Array"){ typeArgument("CompletionItem") }
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
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "parent", "ScopeSimple"){ typeArgument("AsmElementIdType") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeIdentityInParent", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forTypeName", "String")

                    propertyOf(setOf(READ_WRITE, REFERENCE), "scopeMap", "Map") {
                        typeArgument("AsmElementIdType") //TODO: should really mark if key is composite or reference!
                        typeArgument("ScopeSimple")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "childScopes", "Map") {
                        typeArgument("String")
                        typeArgument("ScopeSimple")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "items", "Map") {
                        typeArgument("String")
                        typeArgument("Map") {
                            typeArgument("String")
                            typeArgument("AsmElementIdType")
                        }
                    }
                }
                dataType("ContextSimple") {
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "rootScope", "ScopeSimple"){ typeArgument("E") }
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "root", "List"){ typeArgument("AsmValueAbstract") }
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
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "value", "AsmElementSimple", true)
                }
                dataType("AsmStructureSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "path", "AsmPathSimple")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "qualifiedTypeName", "String")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "property", "Map") {
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "rootScope", "ScopeSimple"){ typeArgument("String") }
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

                    propertyOf(setOf(READ_WRITE, REFERENCE), "resolved", "GrammarAbstract")
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

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "extends", "List"){ typeArgument("GrammarReferenceDefault") }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "grammarRule", "List"){ typeArgument("GrammarRuleAbstract") }
                }
                dataType("GrammarRuleAbstract")
                dataType("NormalRuleDefault") {
                    supertypes("GrammarRuleAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "grammar", "GrammarDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isLeaf", "Boolean")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "rhs", "RuleItemAbstract")
                }

                dataType("OverrideRuleDefault") {
                    supertypes("GrammarRuleAbstract")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "grammar", "GrammarDefault")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isLeaf", "Boolean")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "overrideKind", "OverrideKind")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "overridenRhs", "RuleItemAbstract")
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List"){ typeArgument("RuleItem") }
                }
                dataType("ChoicePriorityDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List"){ typeArgument("RuleItem") }
                }
                dataType("ChoiceAmbiguousDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List"){ typeArgument("RuleItem") }
                }
                dataType("ConcatenationDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "items", "List") { typeArgument("RuleItem") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "targetGrammar", "GrammarReference", true)
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

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "root", "CompleteTreeDataNode",  true)
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "initialSkip", "TreeDataComplete", true) { typeArgument("CompleteTreeDataNode") }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "completeChildren", "Map") {
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

