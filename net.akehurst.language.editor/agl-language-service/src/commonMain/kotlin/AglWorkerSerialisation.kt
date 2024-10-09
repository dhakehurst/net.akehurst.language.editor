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
import net.akehurst.language.base.processor.AglBase
import net.akehurst.language.grammar.processor.AglGrammar
import net.akehurst.language.typemodel.processor.AglTypemodel
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.builder.typeModel

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
            agl_processor_commonMain.KotlinxReflectForModule.registerUsedClasses()
            agl_editor_api_commonMain.KotlinxReflectForModule.registerUsedClasses()
            agl_editor_common_commonMain.KotlinxReflectForModule.registerUsedClasses()
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
        val namesapces = (
                AglBase.typeModel.namespace +
                        AglGrammar.typeModel.namespace +
                        AglTypemodel.typeModel.namespace
                ).toSet().toList()
        println(namesapces)
        val tm = typeModel("Test", true, namesapces) {
            namespace("net.akehurst.language.api.language.expressions", listOf("std", "net.akehurst.language.typemodel.api", "net.akehurst.language.base.api")) {
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
            namespace(
                "net.akehurst.language.agl.language.expressions.asm",
                listOf("net.akehurst.language.api.language.expressions", "std", "net.akehurst.language.typemodel.api", "net.akehurst.language.base.api")
            ) {
                dataType("WithExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.WithExpression")
                    constructor_ {
                        parameter("withContext", "Expression", false)
                        parameter("expression", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "expression", "Expression", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "withContext", "Expression", false)
                }
                dataType("WhenOptionSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.WhenOption", "std.Any")
                    constructor_ {
                        parameter("condition", "Expression", false)
                        parameter("expression", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "condition", "Expression", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "expression", "Expression", false)
                }
                dataType("WhenExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.WhenExpression")
                    constructor_ {
                        parameter("options", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "options", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.WhenOption")
                    }
                }
                dataType("RootExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.RootExpression")
                    constructor_ {
                        parameter("name", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "name", "String", false)
                }
                dataType("PropertyCallSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.PropertyCall", "std.Any")
                    constructor_ {
                        parameter("propertyName", "PropertyName", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "propertyName", "PropertyName", false)
                }
                dataType("OnExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.OnExpression")
                    constructor_ {
                        parameter("expression", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "expression", "Expression", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "propertyAssignments", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.AssignmentStatement")
                    }
                }
                dataType("NavigationSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.NavigationExpression")
                    constructor_ {
                        parameter("start", "Expression", false)
                        parameter("parts", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "parts", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.NavigationPart")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "start", "Expression", false)
                }
                dataType("MethodCallSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.MethodCall", "std.Any")
                    constructor_ {
                        parameter("methodName", "MethodName", false)
                        parameter("arguments", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "arguments", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "methodName", "MethodName", false)
                }
                dataType("LiteralExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.LiteralExpression")
                    constructor_ {
                        parameter("qualifiedTypeName", "QualifiedName", false)
                        parameter("value", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "qualifiedTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "Any", false)
                }
                dataType("InfixExpressionSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.InfixExpression", "std.Any")
                    constructor_ {
                        parameter("expressions", "List", false)
                        parameter("operators", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "expressions", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "operators", "List", false) {
                        typeArgument("std.String")
                    }
                }
                dataType("IndexOperationSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.IndexOperation", "std.Any")
                    constructor_ {
                        parameter("indices", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "indices", "List", false) {
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "propertyAssignments", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.AssignmentStatement")
                    }
                }
                dataType("CreateObjectExpressionSimple") {
                    supertypes("ExpressionAbstract", "net.akehurst.language.api.language.expressions.CreateObjectExpression")
                    constructor_ {
                        parameter("possiblyQualifiedTypeName", "PossiblyQualifiedName", false)
                        parameter("arguments", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "arguments", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.Expression")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "possiblyQualifiedTypeName", "PossiblyQualifiedName", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "propertyAssignments", "List", false) {
                        typeArgument("net.akehurst.language.api.language.expressions.AssignmentStatement")
                    }
                }
                dataType("AssignmentStatementSimple") {
                    supertypes("net.akehurst.language.api.language.expressions.AssignmentStatement", "std.Any")
                    constructor_ {
                        parameter("lhsPropertyName", "PropertyName", false)
                        parameter("rhs", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "lhsPropertyName", "PropertyName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "rhs", "Expression", false)
                }
            }
            namespace("net.akehurst.language.api.language.reference", listOf("std", "net.akehurst.language.base.api", "net.akehurst.language.api.language.expressions")) {
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
            namespace(
                "net.akehurst.language.agl.language.reference.asm",
                listOf("net.akehurst.language.api.language.reference", "std", "net.akehurst.language.base.api", "net.akehurst.language.api.language.expressions")
            ) {
                dataType("ScopeDefinitionDefault") {
                    supertypes("net.akehurst.language.api.language.reference.ScopeDefinition", "std.Any")
                    constructor_ {
                        parameter("scopeForTypeName", "SimpleName", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "identifiables", "List", false) {
                        typeArgument("net.akehurst.language.api.language.reference.Identifiable")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "scopeForTypeName", "SimpleName", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "inTypeName", "SimpleName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "referenceExpressionList", "List", false) {
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "fromNavigation", "NavigationExpression", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "referringPropertyNavigation", "NavigationExpression", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "refersToTypeName", "List", false) {
                        typeArgument("net.akehurst.language.base.api.PossiblyQualifiedName")
                    }
                }
                dataType("IdentifiableDefault") {
                    supertypes("net.akehurst.language.api.language.reference.Identifiable", "std.Any")
                    constructor_ {
                        parameter("typeName", "SimpleName", false)
                        parameter("identifiedBy", "Expression", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "identifiedBy", "Expression", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "typeName", "SimpleName", false)
                }
                dataType("DeclarationsForNamespaceDefault") {
                    supertypes("net.akehurst.language.api.language.reference.DeclarationsForNamespace", "std.Any")
                    constructor_ {
                        parameter("qualifiedName", "QualifiedName", false)
                        parameter("importedNamespaces", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "importedNamespaces", "List", false) {
                        typeArgument("net.akehurst.language.base.api.Import")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "qualifiedName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "references", "List", false) {
                        typeArgument("net.akehurst.language.api.language.reference.ReferenceDefinition")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "scopeDefinition", "Map", false) {
                        typeArgument("net.akehurst.language.base.api.SimpleName")
                        typeArgument("net.akehurst.language.api.language.reference.ScopeDefinition")
                    }
                }
                dataType("CrossReferenceModelDefault") {
                    supertypes("net.akehurst.language.api.language.reference.CrossReferenceModel", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "declarationsForNamespace", "Map", false) {
                        typeArgument("net.akehurst.language.base.api.QualifiedName")
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "expression", "Expression", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "ofType", "PossiblyQualifiedName", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "referenceExpressionList", "List", false) {
                        typeArgument("net.akehurst.language.agl.language.reference.asm.ReferenceExpressionAbstract")
                    }
                }
            }
            namespace("net.akehurst.language.api.scope", listOf("std", "net.akehurst.language.base.api", "net.akehurst.language.agl.scope")) {
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "item", "ItemType", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "qualifiedTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "referableName", "String", false)
                }
            }
            namespace("net.akehurst.language.agl.scope", listOf("net.akehurst.language.api.scope", "std", "net.akehurst.language.base.api")) {
                dataType("ScopeSimple") {
                    supertypes("net.akehurst.language.api.scope.Scope", "std.Any")
                    constructor_ {
                        parameter("parent", "ScopeSimple", true)
                        parameter("scopeIdentityInParent", "String", false)
                        parameter("forTypeName", "QualifiedName", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "childScopes", "Map", false) {
                        typeArgument("std.String")
                        typeArgument("net.akehurst.language.agl.scope.ScopeSimple")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "forTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "parent", "ScopeSimple", true) {
                        typeArgument("ItemType")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "scopeIdentity", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "scopeIdentityInParent", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "scopeMap", "Map", false) {
                        typeArgument("ItemType")
                        typeArgument("net.akehurst.language.agl.scope.ScopeSimple")
                    }
                }
            }
            namespace(
                "net.akehurst.language.api.asm",
                listOf(
                    "std",
                    "net.akehurst.language.typemodel.api",
                    "net.akehurst.language.api.language.reference",
                    "net.akehurst.language.agl.scope",
                    "net.akehurst.language.agl.asm",
                    "net.akehurst.language.base.api",
                    "net.akehurst.language.collections"
                )
            ) {
                valueType("PropertyValueName") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "String", false)
                }
                interfaceType("AsmValue") {
                    supertypes("std.Any")
                }
                interfaceType("AsmTreeWalker") {
                    supertypes("std.Any")
                }
                interfaceType("AsmStructureProperty") {
                    supertypes("std.Any")
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "name", "PropertyValueName", false)
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
            namespace("net.akehurst.language.agl.asm", listOf("net.akehurst.language.api.asm", "std", "net.akehurst.language.base.api", "net.akehurst.language.collections")) {
                singleton("AsmNothingSimple")
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "path", "AsmPath", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "property", "Map", false) {
                        typeArgument("net.akehurst.language.api.asm.PropertyValueName")
                        typeArgument("net.akehurst.language.api.asm.AsmStructureProperty")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "qualifiedTypeName", "QualifiedName", false)
                }
                dataType("AsmStructurePropertySimple") {
                    supertypes("net.akehurst.language.api.asm.AsmStructureProperty", "std.Any")
                    constructor_ {
                        parameter("name", "PropertyValueName", false)
                        parameter("index", "Integer", false)
                        parameter("value", "AsmValue", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "name", "PropertyValueName", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "value", "AsmValue", false)
                }
                dataType("AsmSimpleKt") {
                    supertypes("std.Any")
                }
                dataType("AsmSimple") {
                    supertypes("net.akehurst.language.api.asm.Asm", "std.Any")
                    constructor_ {}
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "elementIndex", "Map", false) {
                        typeArgument("net.akehurst.language.api.asm.AsmPath")
                        typeArgument("net.akehurst.language.api.asm.AsmStructure")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "root", "List", false) {
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                    }
                }
                dataType("AsmReferenceSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmReference")
                    constructor_ {
                        parameter("reference", "String", false)
                        parameter("value", "AsmStructure", true)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "reference", "String", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "value", "AsmStructure", true)
                }
                dataType("AsmPrimitiveSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmPrimitive")
                    constructor_ {
                        parameter("qualifiedTypeName", "QualifiedName", false)
                        parameter("value", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "qualifiedTypeName", "QualifiedName", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "Any", false)
                }
                dataType("AsmPathSimple") {
                    supertypes("net.akehurst.language.api.asm.AsmPath", "std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "String", false)
                }
                dataType("AsmListSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmList")
                    constructor_ {
                        parameter("elements", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "elements", "List", false) {
                        typeArgument("net.akehurst.language.api.asm.AsmValue")
                    }
                }
                dataType("AsmListSeparatedSimple") {
                    supertypes("AsmValueAbstract", "net.akehurst.language.api.asm.AsmListSeparated")
                    constructor_ {
                        parameter("elements", "ListSeparated", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "elements", "ListSeparated", false) {
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "column", "Integer", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "line", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "position", "Integer", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "isPattern", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "tagList", "List", false) {
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "propertyIndex", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "total", "Integer", false)
                }
                dataType("AltInfo") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("option", "Integer", false)
                        parameter("index", "Integer", false)
                        parameter("totalMatched", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "index", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "option", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "totalMatched", "Integer", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "forStateSetNumber", "Integer", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "initialSkip", "TreeData", true)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "root", "SpptDataNode", true)
                }
                dataType("TreeDataComplete") {
                    supertypes("net.akehurst.language.api.sppt.TreeData", "std.Any")
                    constructor_ {
                        parameter("forStateSetNumber", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "forStateSetNumber", "Integer", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "initialSkip", "TreeData", true)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "root", "SpptDataNode", true)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "nextInputNoSkip", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "nextInputPosition", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "option", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "rule", "Rule", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "startPosition", "Integer", false)
                }
                dataType("CompleteKey") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("rule", "Rule", false)
                        parameter("startPosition", "Integer", false)
                        parameter("nextInputPosition", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "nextInputPosition", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "rule", "Rule", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "startPosition", "Integer", false)
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
            namespace("net.akehurst.language.api.language.style", listOf("net.akehurst.language.base.api", "std")) {
                enumType("AglStyleSelectorKind", listOf("LITERAL", "PATTERN", "RULE_NAME", "META"))
                interfaceType("StyleNamespace") {
                    supertypes("net.akehurst.language.base.api.Namespace", "std.Any")
                }
                interfaceType("AglStyleRule") {
                    supertypes("net.akehurst.language.base.api.Definition", "std.Any")
                }
                interfaceType("AglStyleModel") {
                    supertypes("net.akehurst.language.base.api.Model", "std.Any")
                }
                dataType("AglStyleSelector") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("value", "String", false)
                        parameter("kind", "AglStyleSelectorKind", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "kind", "AglStyleSelectorKind", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "String", false)
                }
                dataType("AglStyleDeclaration") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("name", "String", false)
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "String", false)
                }
            }
            namespace(
                "net.akehurst.language.editor.language.service.messages",
                listOf(
                    "net.akehurst.language.editor.api",
                    "std",
                    "net.akehurst.language.api.processor",
                    "net.akehurst.language.agl.scanner",
                    "net.akehurst.language.api.sppt",
                    "net.akehurst.language.api.language.style",
                    "net.akehurst.language.editor.common"
                )
            ) {
                dataType("MessageProcessorDelete") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
                }
                dataType("MessageGrammarAmbiguityAnalysisResult") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", true)
                        parameter("issues", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageGrammarAmbiguityAnalysisRequest") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "asm", "Any", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "lineTokens", "List", false) {
                        typeArgument("std.List")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "startLine", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "treeSerialised", "String", true)
                }
                dataType("MessageSetStyle") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("styleStr", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "styleStr", "String", false)
                }
                dataType("MessageProcessorDeleteResponse") {
                    supertypes("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "scannerMatchables", "List", false) {
                        typeArgument("net.akehurst.language.agl.scanner.Matchable")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "asm", "Any", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "reason", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "completionItems", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.CompletionItem")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "crossReferenceModelStr", "String", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "editorOptions", "EditorOptions", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "grammarStr", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "options", "ProcessOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "text", "String", false)
                }
                dataType("MessageProcessRequest") {
                    supertypes("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "String", false)
                        parameter("text", "String", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "languageId", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "options", "ProcessOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "text", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "treeData", "TreeData", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "styleModel", "AglStyleModel", true)
                }
                dataType("AglWorkerMessage") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("action", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "action", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "issues", "List", false) {
                        typeArgument("net.akehurst.language.api.processor.LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "lineTokens", "List", false) {
                        typeArgument("net.akehurst.language.editor.common.AglTokenDefault")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "editorId", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "sessionId", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "styles", "List", false) {
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "expression", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "kind", "MatchableKind", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "tag", "String", false)
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
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "data", "Any", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "kind", "LanguageIssueKind", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "location", "InputLocation", true)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "phase", "LanguageProcessorPhase", false)
                }
                dataType("CompletionItem") {
                    supertypes("std.Any")
                    constructor_ {
                        parameter("kind", "CompletionItemKind", false)
                        parameter("text", "String", false)
                        parameter("name", "String", false)
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "description", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "kind", "CompletionItemKind", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "text", "String", false)
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "contextQualifiedTypeName", "String", true)
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

                    propertyOf(setOf(READ_WRITE, REFERENCE), "supertypes", "List") { typeArgument("DataType") }
                    propertyOf(setOf(READ_WRITE, REFERENCE), "subtypes", "List") { typeArgument("DataType") }
                }
                dataType("CollectionTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "CollectionType")
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "namespace", "TypeNamespace")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "typeParameters", "String")

                    propertyOf(setOf(READ_WRITE, REFERENCE), "supertypes", "List") { typeArgument("CollectionType") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rules", "List") { typeArgument("\"net.akehurst.language.api.style.AglStyleRule\"") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "value", "List") { typeArgument("String") }
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "references", "List") { typeArgument("ReferenceDefinitionDefault") }
                }
                dataType("ScopeDefinitionDefault") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "scopeForTypeName", "String")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "identifiables", "List") { typeArgument("IdentifiableDefault") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "refersToTypeName", "List") { typeArgument("String") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "fromNavigation", "Navigation", true)
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "grammarStr", "String", true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "crossReferenceModelStr", "String", true)
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "editorOptions", "EditorOptionsDefault", false)
                }
                dataType("MessageProcessorCreateResponse") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeSerialised", "String", true)
                }
//FIXME
                dataType("MessageParseResult2") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "treeData", "TreeDataComplete", true)
                }

                dataType("MessageSyntaxAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "asm", "Any", true)
                }
                dataType("MessageSemanticAnalysisResult") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "status", "MessageStatus")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "message", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "completionItems", "Array") { typeArgument("CompletionItem") }
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
                    propertyOf(setOf(CONSTRUCTOR, REFERENCE), "parent", "ScopeSimple") { typeArgument("AsmElementIdType") }
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "rootScope", "ScopeSimple") { typeArgument("E") }
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "root", "List") { typeArgument("AsmValueAbstract") }
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
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "rootScope", "ScopeSimple") { typeArgument("String") }
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

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "extends", "List") { typeArgument("GrammarReferenceDefault") }
                    propertyOf(setOf(READ_WRITE, COMPOSITE), "grammarRule", "List") { typeArgument("GrammarRuleAbstract") }
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
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List") { typeArgument("RuleItem") }
                }
                dataType("ChoicePriorityDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List") { typeArgument("RuleItem") }
                }
                dataType("ChoiceAmbiguousDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "alternative", "List") { typeArgument("RuleItem") }
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
            namespace("net.akehurst.language.agl.runtime.structure", imports = mutableListOf("kotlin", "kotlin.collections")) {
                dataType("RuntimeRule") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "runtimeRuleSetNumber", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "ruleNumber", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "name", "String")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "isSkip", "Boolean")
                }
            }
            namespace("net.akehurst.language.agl.sppt", imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.runtime.structure")) {
                dataType("CompleteTreeDataNode") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "rule", "RuntimeRule")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "startPosition", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nextInputPosition", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "nextInputNoSkip", "Int")
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "option", "Int")
                }
                dataType("TreeDataComplete2") {
                    propertyOf(setOf(CONSTRUCTOR, COMPOSITE), "forStateSetNumber", "Int")

                    propertyOf(setOf(READ_WRITE, COMPOSITE), "root", "CompleteTreeDataNode", true)
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
                    typeParameters("AsmType", "ContextType")
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

