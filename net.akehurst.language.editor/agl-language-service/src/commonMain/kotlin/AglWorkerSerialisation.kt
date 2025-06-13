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
import net.akehurst.language.asm.simple.AglAsm
import net.akehurst.language.base.processor.AglBase
import net.akehurst.language.expressions.processor.AglExpressions
import net.akehurst.language.grammar.processor.AglGrammar
import net.akehurst.language.reference.processor.AglCrossReference
import net.akehurst.language.scope.processor.AglScope
import net.akehurst.language.style.processor.AglStyle
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.builder.typeModel
import net.akehurst.language.typemodel.processor.AglTypes

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
            agl_parser_commonMain.KotlinxReflectForModule.registerUsedClasses()
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
        val namespaces = (
                AglBase.typesModel.namespace +
                        AglGrammar.typesModel.namespace +
                        AglTypes.typesModel.namespace +
                        AglAsm.typeModel.namespace +
                        AglExpressions.typesModel.namespace +
                        AglCrossReference.typeModel.namespace +
                        AglStyle.typesModel.namespace +
                        AglScope.typeModel.namespace
                ).toSet().toList()
        println(namespaces)
        val tm = typeModel("Messages", true, namespaces) {
            namespace(
                "net.akehurst.language.editor.language.service.messages",
                listOf(
                    "net.akehurst.language.editor.api",
                    "net.akehurst.language.api.processor",
                    "std",
                    "net.akehurst.language.issues.api",
                    "net.akehurst.language.scanner.api",
                    "net.akehurst.language.sppt.api",
                    "net.akehurst.language.style.api",
                    "net.akehurst.language.editor.common"
                )
            ) {
                singleton("EditorMessage")
                data("MessageProcessorDelete") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                }
                data("MessageGrammarAmbiguityAnalysisResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("MessageGrammarAmbiguityAnalysisRequest") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                }
                data("MessageSyntaxAnalysisResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("asm", "Any", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "asm", "Any", false)
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("MessageLineTokens") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("startLine", "Integer", false)
                        parameter("lineTokens", "List", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAR, CMP, STR), "lineTokens", "List", false) {
                        typeArgument("List")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "startLine", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("MessageParseResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("treeSerialised", "String", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                    propertyOf(setOf(VAL, REF, STR), "treeSerialised", "String", false)
                }
                data("MessageSetStyle") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("styleStr", "String", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "styleStr", "String", false)
                }
                data("MessageProcessorDeleteResponse") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("MessageProcessorCreateResponse") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("scannerMatchables", "List", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "scannerMatchables", "List", false) {
                        typeArgument("Matchable")
                    }
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("MessageSemanticAnalysisResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("asm", "Any", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "asm", "Any", false)
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("AglWorkerMessageResponse") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("action", "String", false)
                    }
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                }
                data("MessageParserInterruptRequest") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("reason", "String", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "reason", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                }
                data("MessageCodeCompleteResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("offset", "Integer", false)
                        parameter("completionItems", "List", false)
                    }
                    propertyOf(setOf(VAR, CMP, STR), "completionItems", "List", false) {
                        typeArgument("CompletionItem")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "offset", "Integer", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
                data("MessageProcessorCreate") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("grammarStr", "String", false)
                        parameter("typesModelStr", "String", false)
                        parameter("transformStr", "String", false)
                        parameter("crossReferenceStr", "String", false)
                        parameter("editorOptions", "EditorOptions", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "crossReferenceStr", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "editorOptions", "EditorOptions", false)
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "grammarStr", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "transformStr", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "typesModelStr", "String", false)
                }
                data("MessageCodeCompleteRequest") {
                    typeParameters("AsmType", "ContextType")
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("text", "String", false)
                        parameter("position", "Integer", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "options", "ProcessOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(VAL, REF, STR), "position", "Integer", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "text", "String", false)
                }
                data("MessageProcessRequest") {
                    typeParameters("AsmType", "ContextType")
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("text", "String", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "options", "ProcessOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "text", "String", false)
                }
                data("MessageParseResult2") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("treeData", "TreeData", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                    propertyOf(setOf(VAL, CMP, STR), "treeData", "TreeData", false)
                }
                data("MessageSetStyleResponse") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("styleModel", "AglStyleModel", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                    propertyOf(setOf(VAL, CMP, STR), "styleModel", "AglStyleModel", false)
                }
                data("AglWorkerMessage") {

                    constructor_ {
                        parameter("action", "String", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "action", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                }
                data("MessageScanResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("requestId", "RequestIdentity", false)
                        parameter("status", "MessageResponseStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("lineTokens", "List", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(VAR, CMP, STR), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(VAR, CMP, STR), "lineTokens", "List", false) {
                        typeArgument("AglTokenDefault")
                    }
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, CMP, STR), "requestId", "RequestIdentity", false)
                    propertyOf(setOf(VAL, REF, STR), "status", "MessageResponseStatus", false)
                }
            }
            namespace("net.akehurst.language.sppt.api", listOf("std", "net.akehurst.language.parser.api")) {
                interface_("TreeData") {

                }
                interface_("SpptDataNode") {

                }
            }
            namespace("net.akehurst.language.sppt.treedata", listOf("net.akehurst.language.sppt.api", "std", "net.akehurst.language.parser.api")) {
                data("TreeDataComplete2") {
                    supertype("TreeData")
                    constructor_ {
                        parameter("forStateSetNumber", "Integer", false)
                    }
                    propertyOf(setOf(VAR, CMP, STR), "_complete", "Map", false) {
                        typeArgument("SpptDataNode")
                        typeArgument("Map")
                    }
                    propertyOf(setOf(VAR, CMP, STR), "_embeddedFor", "Map", false) {
                        typeArgument("SpptDataNode")
                        typeArgument("TreeData")
                    }
                    propertyOf(setOf(VAR, CMP, STR), "_preferred", "Map", false) {
                        typeArgument("PreferredNode")
                        typeArgument("SpptDataNode")
                    }
                    propertyOf(setOf(VAR, CMP, STR), "_skipDataAfter", "Map", false) {
                        typeArgument("SpptDataNode")
                        typeArgument("TreeData")
                    }
                    propertyOf(setOf(VAL, REF, STR), "forStateSetNumber", "Integer", false)
                    propertyOf(setOf(VAR, CMP, STR), "initialSkip", "TreeData", false)
                    propertyOf(setOf(VAR, REF, STR), "root", "SpptDataNode", false)
                }
                data("CompleteTreeDataNode") {
                    supertype("SpptDataNode")
                    constructor_ {
                        parameter("rule", "Rule", false)
                        parameter("startPosition", "Integer", false)
                        parameter("nextInputPosition", "Integer", false)
                        parameter("nextInputNoSkip", "Integer", false)
                        parameter("option", "OptionNum", false)
                        parameter("dynamicPriority", "List", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "dynamicPriority", "List", false) {
                        typeArgument("Integer")
                    }
                    propertyOf(setOf(VAL, REF, STR), "nextInputNoSkip", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "nextInputPosition", "Integer", false)
                    propertyOf(setOf(VAL, CMP, STR), "option", "OptionNum", false)
                    propertyOf(setOf(VAL, REF, STR), "rule", "Rule", false)
                    propertyOf(setOf(VAL, REF, STR), "startPosition", "Integer", false)
                }
                data("PreferredNode") {

                    constructor_ {
                        parameter("rule", "Rule", false)
                        parameter("startPosition", "Integer", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "rule", "Rule", false)
                    propertyOf(setOf(VAL, REF, STR), "startPosition", "Integer", false)
                }
            }
            namespace("net.akehurst.language.parser.api", listOf("std")) {
                value("OptionNum") {
                    constructor_ { parameter("value","Integer") }
                    propertyOf(setOf(VAL, REF, STR), "value", "Integer", false)
                }
                interface_("Rule") {

                }
                interface_("ParseOptions") {

                }
            }
            namespace("net.akehurst.language.agl.runtime.structure", listOf("net.akehurst.language.parser.api", "std")) {
                data("RuntimeRule") {
                    supertype("Rule")
                    constructor_ {
                        parameter("runtimeRuleSetNumber", "Integer", false)
                        parameter("ruleNumber", "Integer", false)
                        parameter("name", "String", false)
                        parameter("isSkip", "Boolean", false)
                        parameter("isPseudo", "Boolean", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "isPseudo", "Boolean", false)
                    propertyOf(setOf(VAL, REF, STR), "isSkip", "Boolean", false)
                    propertyOf(setOf(VAL, REF, STR), "name", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "ruleNumber", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "runtimeRuleSetNumber", "Integer", false)
                }
            }
            namespace("net.akehurst.language.sentence.api", listOf("std")) {
                data("InputLocation") {

                    constructor_ {
                        parameter("position", "Integer", false)
                        parameter("column", "Integer", false)
                        parameter("line", "Integer", false)
                        parameter("length", "Integer", false)
                        parameter("sentenceIdentity", "Any", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "column", "Integer", false)
                    propertyOf(setOf(VAR, REF, STR), "length", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "line", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "position", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "sentenceIdentity", "Any", false)
                }
            }
            namespace("net.akehurst.language.issues.api", listOf("std", "net.akehurst.language.sentence.api")) {
                enum("LanguageIssueKind", listOf("ERROR", "WARNING", "INFORMATION"))
                enum("LanguageProcessorPhase", listOf("GRAMMAR", "SCAN", "PARSE", "SYNTAX_ANALYSIS", "SEMANTIC_ANALYSIS", "INTERPRET", "GENERATE", "FORMAT", "ALL"))
                data("LanguageIssue") {

                    constructor_ {
                        parameter("kind", "LanguageIssueKind", false)
                        parameter("phase", "LanguageProcessorPhase", false)
                        parameter("location", "InputLocation", false)
                        parameter("message", "String", false)
                        parameter("data", "Any", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "data", "Any", false)
                    propertyOf(setOf(VAL, REF, STR), "kind", "LanguageIssueKind", false)
                    propertyOf(setOf(VAL, CMP, STR), "location", "InputLocation", false)
                    propertyOf(setOf(VAL, REF, STR), "message", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "phase", "LanguageProcessorPhase", false)
                }
            }
            namespace("net.akehurst.language.scanner.api", listOf("std")) {
                enum("MatchableKind", listOf("EOT", "LITERAL", "REGEX"))
                interface_("ScanOptions") {

                }
                data("Matchable") {

                    constructor_ {
                        parameter("ruleSetNumber", "Integer", false)
                        parameter("ruleNumber", "Integer", false)
                        parameter("tag", "String", false)
                        parameter("expression", "String", false)
                        parameter("kind", "MatchableKind", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "expression", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "kind", "MatchableKind", false)
                    propertyOf(setOf(VAL, REF, STR), "ruleNumber", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "ruleSetNumber", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "tag", "String", false)
                }
            }
            namespace(
                "net.akehurst.language.api.processor",
                listOf(
                    "net.akehurst.language.base.api",
                    "std",
                    "net.akehurst.language.parser.api",
                    "net.akehurst.language.scanner.api",
                    "net.akehurst.language.issues.api",
                    "net.akehurst.language.api.syntaxAnalyser"
                )
            ) {
                value("LanguageIdentity") {
                    supertype("PublicValueType")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "value", "String", false)
                }
                interface_("ProcessOptions") {
                    typeParameters("AsmType", "ContextType")

                    propertyOf(setOf(VAL, CMP, STR), "completionProvider", "CompletionProviderOptions", false) {
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "parse", "ParseOptions", false)
                    propertyOf(setOf(VAL, CMP, STR), "scan", "ScanOptions", false)
                    propertyOf(setOf(VAL, CMP, STR), "semanticAnalysis", "SemanticAnalysisOptions", false) {
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "syntaxAnalysis", "SyntaxAnalysisOptions", false) {
                        typeArgument("AsmType")
                    }
                }
                interface_("SyntaxAnalysisOptions") {
                    typeParameters("AsmType")

                }
                interface_("SemanticAnalysisOptions") {
                    typeParameters("ContextType")

                    propertyOf(setOf(VAR, CMP, STR), "context", "ContextType", false)
                }
                interface_("CompletionProviderOptions") {
                    typeParameters("ContextType")

                }
                data("CompletionItem") {

                    constructor_ {
                        parameter("kind", "CompletionItemKind", false)
                        parameter("label", "String", false)
                        parameter("text", "String", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "description", "String", false)
                    propertyOf(setOf(VAR, REF, STR), "id", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "kind", "CompletionItemKind", false)
                    propertyOf(setOf(VAL, REF, STR), "label", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "text", "String", false)
                }
            }
            namespace("net.akehurst.language.api.semanticAnalyser", listOf("std")) {
                interface_("SentenceContext") {

                }
            }
            namespace("net.akehurst.language.scanner.common", listOf("net.akehurst.language.scanner.api", "std")) {
                data("ScanOptionsDefault") {
                    supertype("ScanOptions")
                    constructor_ {
                        parameter("enabled", "Boolean", false)
                        parameter("resultsByLine", "Boolean", false)
                        parameter("startAtPosition", "Integer", false)
                        parameter("offsetPosition", "Integer", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "enabled", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "offsetPosition", "Integer", false)
                    propertyOf(setOf(VAR, REF, STR), "resultsByLine", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "startAtPosition", "Integer", false)
                }
            }
            namespace("net.akehurst.language.parser.leftcorner", listOf("net.akehurst.language.parser.api", "std")) {
                singleton("SentenceIdentityFunctionNull")
                data("ParseOptionsDefault") {
                    supertype("ParseOptions")
                    constructor_ {
                        parameter("enabled", "Boolean", false)
                        parameter("goalRuleName", "String", false)
                        parameter("sentenceIdentity", "SentenceIdentityFunction", false)
                        parameter("reportErrors", "Boolean", false)
                        parameter("reportGrammarAmbiguities", "Boolean", false)
                        parameter("cacheSkip", "Boolean", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "cacheSkip", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "enabled", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "goalRuleName", "String", false)
                    propertyOf(setOf(VAR, REF, STR), "reportErrors", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "reportGrammarAmbiguities", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "sentenceIdentity", "SentenceIdentityFunction", false)
                }
            }
            namespace(
                "net.akehurst.language.agl.processor",
                listOf(
                    "net.akehurst.language.api.processor",
                    "std",
                    "net.akehurst.language.api.syntaxAnalyser",
                    "net.akehurst.language.issues.api",
                    "net.akehurst.language.scanner.api",
                    "net.akehurst.language.parser.api"
                )
            ) {
                data("SyntaxAnalysisOptionsDefault") {
                    typeParameters("AsmType")
                    supertype("SyntaxAnalysisOptions") { ref("AsmType") }
                    constructor_ {
                        parameter("enabled", "Boolean", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "enabled", "Boolean", false)
                }
                data("SemanticAnalysisOptionsDefault") {
                    typeParameters("ContextType")
                    supertype("SemanticAnalysisOptions") { ref("ContextType") }
                    constructor_ {
                        parameter("enabled", "Boolean", false)
                        parameter("locationMap", "LocationMap", false)
                        parameter("context", "ContextType", false)
                        parameter("buildScope", "Boolean", false)
                        parameter("replaceIfItemAlreadyExistsInScope", "Boolean", false)
                        parameter("ifItemAlreadyExistsInScopeIssueKind", "LanguageIssueKind", false)
                        parameter("checkReferences", "Boolean", false)
                        parameter("resolveReferences", "Boolean", false)
                        parameter("other", "Map", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "buildScope", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "checkReferences", "Boolean", false)
                    propertyOf(setOf(VAR, CMP, STR), "context", "ContextType", false)
                    propertyOf(setOf(VAR, REF, STR), "enabled", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "ifItemAlreadyExistsInScopeIssueKind", "LanguageIssueKind", false)
                    propertyOf(setOf(VAR, CMP, STR), "locationMap", "LocationMap", false)
                    propertyOf(setOf(VAR, REF, STR), "other", "Map", false) {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                    propertyOf(setOf(VAR, REF, STR), "replaceIfItemAlreadyExistsInScope", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "resolveReferences", "Boolean", false)
                }
                data("CompletionProviderOptionsDefault") {
                    typeParameters("ContextType")
                    supertype("CompletionProviderOptions") { ref("ContextType") }
                    constructor_ {
                        parameter("context", "ContextType", false)
                        parameter("depth", "Integer", false)
                        parameter("path", "List", false)
                        parameter("showOptionalItems", "Boolean", false)
                        parameter("provideValuesForPatternTerminals", "Boolean", false)
                        parameter("other", "Map", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "context", "ContextType", false)
                    propertyOf(setOf(VAR, REF, STR), "depth", "Integer", false)
                    propertyOf(setOf(VAR, REF, STR), "other", "Map", false) {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                    propertyOf(setOf(VAR, REF, STR), "path", "List", false) {
                        typeArgument("Pair")
                    }
                    propertyOf(setOf(VAR, REF, STR), "provideValuesForPatternTerminals", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "showOptionalItems", "Boolean", false)
                }
                data("ProcessOptionsDefault") {
                    typeParameters("AsmType", "ContextType")
                    supertype("ProcessOptions") { ref("AsmType"); ref("ContextType") }
                    constructor_ {
                        parameter("scan", "ScanOptions", false)
                        parameter("parse", "ParseOptions", false)
                        parameter("syntaxAnalysis", "SyntaxAnalysisOptions", false)
                        parameter("semanticAnalysis", "SemanticAnalysisOptions", false)
                        parameter("completionProvider", "CompletionProviderOptions", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "completionProvider", "CompletionProviderOptions", false) {
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "parse", "ParseOptions", false)
                    propertyOf(setOf(VAL, CMP, STR), "scan", "ScanOptions", false)
                    propertyOf(setOf(VAL, CMP, STR), "semanticAnalysis", "SemanticAnalysisOptions", false) {
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "syntaxAnalysis", "SyntaxAnalysisOptions", false) {
                        typeArgument("AsmType")
                    }
                }
            }
            namespace("net.akehurst.language.agl.simple", listOf("net.akehurst.language.api.semanticAnalyser", "std", "net.akehurst.language.scope.asm")) {
                singleton("NULL_SENTENCE_IDENTIFIER")
                data("CreateScopedItemDefault") {
                    constructor_ {  }
                }
                data("ResolveScopedItemDefault") {
                    constructor_ {  }
                }
                data("ContextWithScope") {
                    typeParameters("ItemType", "ItemInScopeType")
                    supertype("SentenceContext")
                    constructor_ {
                        parameter("createScopedItem", "CreateScopedItem", false)
                        parameter("resolveScopedItem", "ResolveScopedItem", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "createScopedItem", "CreateScopedItem", false) {
                        typeArgument("ItemType")
                        typeArgument("ItemInScopeType")
                    }
                    propertyOf(setOf(VAL, CMP, STR), "resolveScopedItem", "ResolveScopedItem", false) {
                        typeArgument("ItemType")
                        typeArgument("ItemInScopeType")
                    }
                    propertyOf(setOf(VAR, CMP, STR), "scopeForSentence", "Map", false) {
                        typeArgument("Any")
                        typeArgument("ScopeSimple")
                    }
                }
            }
            namespace("net.akehurst.language.api.syntaxAnalyser", listOf("std")) {
                interface_("LocationMap") {

                }
            }
            namespace(
                "net.akehurst.language.agl.semanticAnalyser",
                listOf("net.akehurst.language.api.semanticAnalyser", "std", "net.akehurst.language.api.processor", "net.akehurst.language.typemodel.api")
            ) {
                data("ContextFromTypeModelReference") {
                    supertype("SentenceContext")
                    constructor_ {
                        parameter("languageDefinitionId", "LanguageIdentity", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "languageDefinitionId", "LanguageIdentity", false)
                }
                data("ContextFromTypeModel") {
                    supertype("SentenceContext")
                    constructor_ {
                        parameter("typeModel", "TypeModel", false)
                    }
                    propertyOf(setOf(VAL, CMP, STR), "typeModel", "TypeModel", false)
                }
            }
            namespace(
                "net.akehurst.language.editor.api", listOf(
                    "std",
                    "net.akehurst.language.base.api",
                    "net.akehurst.language.api.processor"
                )
            ) {
                enum("MessageResponseStatus", listOf("RECEIVED", "IGNORED", "FAILURE", "SUCCESS"))
                value("RequestIdentity") {
                    supertype("PublicValueType")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "value", "String", false)
                }
                value("EditorStyleIdentity") {
                    supertype("PublicValueType")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "value", "String", false)
                }
                interface_("EditorOptions") {

                }
                interface_("AglToken") {

                }
                data("EndPointIdentity") {

                    constructor_ {
                        parameter("editorId", "String", false)
                        parameter("sessionId", "String", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "editorId", "String", false)
                    propertyOf(setOf(VAL, REF, STR), "sessionId", "String", false)
                }
            }
            namespace("net.akehurst.language.editor.common", listOf("net.akehurst.language.editor.api", "std", "net.akehurst.language.api.processor")) {
                data("AglTokenDefault") {
                    supertype("AglToken")
                    constructor_ {
                        parameter("styles", "List", false)
                        parameter("position", "Integer", false)
                        parameter("length", "Integer", false)
                    }
                    propertyOf(setOf(VAL, REF, STR), "length", "Integer", false)
                    propertyOf(setOf(VAL, REF, STR), "position", "Integer", false)
                    propertyOf(setOf(VAR, CMP, STR), "styles", "List", false) {
                        typeArgument("EditorStyleIdentity")
                    }
                }
                data("EditorOptionsDefault") {
                    supertype("EditorOptions")
                    constructor_ {
                        parameter("scan", "Boolean", false)
                        parameter("scanLineTokens", "Boolean", false)
                        parameter("parse", "Boolean", false)
                        parameter("parseLineTokens", "Boolean", false)
                        parameter("lineTokensChunkSize", "Integer", false)
                        parameter("parseTree", "Boolean", false)
                        parameter("syntaxAnalysis", "Boolean", false)
                        parameter("syntaxAnalysisAsm", "Boolean", false)
                        parameter("semanticAnalysis", "Boolean", false)
                        parameter("semanticAnalysisAsm", "Boolean", false)
                        parameter("styleCompletionItem", "LambdaType", false)
                    }
                    propertyOf(setOf(VAR, REF, STR), "lineTokensChunkSize", "Integer", false)
                    propertyOf(setOf(VAR, REF, STR), "parse", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "parseLineTokens", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "parseTree", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "scan", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "scanLineTokens", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "semanticAnalysis", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "semanticAnalysisAsm", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "styleCompletionItem", "LambdaType", false) {
                        typeArgument("CompletionItem")
                        typeArgument("String")
                    }
                    propertyOf(setOf(VAR, REF, STR), "syntaxAnalysis", "Boolean", false)
                    propertyOf(setOf(VAR, REF, STR), "syntaxAnalysisAsm", "Boolean", false)
                }
            }
            namespace("net.akehurst.language.agl.syntaxAnalyser", imports = mutableListOf("std")) {
                data("LocationMapDefault") {
                    constructor_ {  }
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
                data("GrammarTypeNamespaceFromGrammar") {
                    supertypes("GrammarTypeNamespaceAbstract")
                    propertyOf(setOf(CON, CMP), "qualifiedName", "String")
                    propertyOf(setOf(CON, CMP), "imports", "List") { typeArgument("String") }
                }
            }*/
            namespace(
                "net.akehurst.language.agl.grammarTypeModel",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.typemodel.simple")
            )
            {
                data("GrammarTypeNamespaceSimple") {
                    supertypes("GrammarTypeNamespaceAbstract")
                    propertyOf(setOf(CON, CMP), "qualifiedName", "String")
                    propertyOf(setOf(CON, CMP), "imports", "List") { typeArgument("String") }
                }
                data("GrammarTypeNamespaceAbstract") {
                    supertypes("TypeNamespaceAbstract")
                    propertyOf(setOf(CON, CMP), "imports", "List") { typeArgument("String") }

                    propertyOf(setOf(VAR, CMP), "allRuleNameToType", "Map") {
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
                data("TypeModelSimple") {
                    supertypes("TypeModelSimpleAbstract")
                    propertyOf(setOf(CON, CMP), "name", "String")
                }
                data("TypeModelSimpleAbstract") {
                    supertypes("TypeModel")
                    propertyOf(setOf(CON, CMP), "name", "String")

                    propertyOf(setOf(VAR, CMP), "namespace", "Map") {
                        typeArgument("String")
                        typeArgument("TypeNamespace")
                    }
                    propertyOf(setOf(VAR, REF), "allNamespace", "List") { typeArgument("TypeNamespace") }
                    //propertyOf(setOf(VAR, CMP), "rules", "Map", listOf("String", "net.akehurst.language.api.typemodel.RuleType"))
                }
                data("TypeInstanceAbstract") {
                    supertypes("TypeInstance")
                }
                data("TypeInstanceSimple") {
                    supertypes("TypeInstanceAbstract")
                    //propertyOf(setOf(CON, REF), "context", "TypeDeclaration")
                    propertyOf(setOf(CON, CMP), "contextQualifiedTypeName", "String", true)
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "qualifiedOrImportedTypeName", "String")
                    propertyOf(setOf(CON, CMP), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CON, CMP), "isNullable", "Boolean")
                }
                data("TupleTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, REF), "declaration", "TupleType")
                    propertyOf(setOf(CON, CMP), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CON, CMP), "isNullable", "Boolean")
                }
                data("UnnamedSupertypeTypeInstance") {
                    supertypes("TypeInstanceAbstract")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, REF), "declaration", "UnnamedSupertypeType")
                    propertyOf(setOf(CON, CMP), "typeArguments", "List") { typeArgument("TypeInstance") }
                    propertyOf(setOf(CON, CMP), "isNullable", "Boolean")
                }
                data("TypeNamespaceAbstract") {
                    supertypes("TypeNamespace")
                    propertyOf(setOf(CON, CMP), "qualifiedName", "String")
                    propertyOf(setOf(CON, CMP), "imports", "List") { typeArgument("String") }
                    propertyOf(setOf(VAR, CMP), "ownedUnnamedSupertypeType", "List") {
                        typeArgument("UnnamedSupertypeTypeSimple")
                    }
                    propertyOf(setOf(VAR, CMP), "ownedTupleTypes", "List") {
                        typeArgument("TupleTypeSimple")
                    }
                    propertyOf(setOf(VAR, CMP), "ownedTypesByName", "Map") {
                        typeArgument("String")
                        typeArgument("TypeDeclaration")
                    }
                }
                data("TypeNamespaceSimple") {
                    supertypes("TypeNamespaceAbstract")
                    propertyOf(setOf(CON, CMP), "qualifiedName", "String")
                    propertyOf(setOf(CON, CMP), "imports", "List") { typeArgument("String") }
                }
                data("TypeDeclarationSimpleAbstract") {
                    supertypes("TypeDeclaration")
                    propertyOf(setOf(VAR, CMP), "typeParameters", "List") { typeArgument("String") }

                    propertyOf(setOf(VAR, CMP), "propertyByIndex", "Map") {
                        typeArgument("Int")
                        typeArgument("PropertyDeclaration")
                    }
                }
                data("SpecialTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "name", "String")
                }
                data("PrimitiveTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "PrimitiveType")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "name", "String")
                }
                data("EnumTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "EnumType")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "literals", "List") { typeArgument("String") }
                }
                data("UnnamedSupertypeTypeSimple") {
                    supertypes("TypeDeclarationSimpleAbstract", "UnnamedSupertypeType")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "id", "Int")
                    propertyOf(setOf(CON, CMP), "subtypes", "List") { typeArgument("TypeInstance") }
                }
                data("StructuredTypeSimpleAbstract") {
                    supertypes("TypeDeclarationSimpleAbstract", "StructuredType")
                }
                data("TupleTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "TupleType")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "id", "Int")
                }
                data("DataTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "DataType")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "name", "String")

                    propertyOf(setOf(VAR, REF), "supertypes", "List") { typeArgument("DataType") }
                    propertyOf(setOf(VAR, REF), "subtypes", "List") { typeArgument("DataType") }
                }
                data("CollectionTypeSimple") {
                    supertypes("StructuredTypeSimpleAbstract", "CollectionType")
                    propertyOf(setOf(CON, REF), "namespace", "TypeNamespace")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "typeParameters", "String")

                    propertyOf(setOf(VAR, REF), "supertypes", "List") { typeArgument("CollectionType") }
                }
                data("PropertyDeclarationPrimitive") {
                    propertyOf(setOf(CON, REF), "owner", "StructuredType")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CON, CMP), "description", "String")
                    propertyOf(setOf(CON, CMP), "index", "Int")
                }
                data("PropertyDeclarationDerived") {
                    propertyOf(setOf(CON, REF), "owner", "StructuredType")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CON, CMP), "description", "String")
                    propertyOf(setOf(CON, CMP), "expression", "String")
                    propertyOf(setOf(CON, CMP), "index", "Int")
                }
                data("PropertyDeclarationStored") {
                    propertyOf(setOf(CON, REF), "owner", "StructuredType")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "typeInstance", "TypeInstance")
                    propertyOf(setOf(CON, CMP), "characteristics", "Set") { typeArgument("PropertyCharacteristic") }
                    propertyOf(setOf(CON, CMP), "index", "Int")
                }
            }
            namespace("net.akehurst.language.typemodel.api", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("TypeModel") { }
                data("TypeNamespace") {}
                data("TypeInstance") {}
                data("TypeDeclaration") {}
                data("PrimitiveType") {
                    supertypes("TypeDeclaration")
                }
                data("EnumType") {
                    supertypes("TypeDeclaration")
                }
                data("StructuredType") {
                    supertypes("TypeDeclaration")
                }
                data("TupleType") {
                    supertypes("StructuredType")
                }
                data("DataType") {
                    supertypes("StructuredType")
                }
                data("PropertyDeclaration") {
                }
                enum("PropertyCharacteristic", listOf())
                data("UnnamedSupertypeType") {
                    supertypes("TypeDeclaration")
                }
                data("CollectionType") {
                    supertypes("TypeDeclaration")
                }
            }
        })
    }

    private fun initialiseStyleAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("StyleAsm", false) {
            namespace("net.akehurst.language.agl.language.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("AglStyleGrammar") {
                    supertypes("GrammarAbstract")
                }
            }
            namespace("net.akehurst.language.agl.language.style.asm", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("AglStyleModelDefault") {
                    propertyOf(setOf(CON, CMP), "rules", "List") { typeArgument("\"net.akehurst.language.api.style.AglStyleRule\"") }
                }
            }
            namespace("net.akehurst.language.api.style", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("AglStyleRule") {
                    propertyOf(setOf(CON, CMP), "selector", "AglStyleSelector")

                    propertyOf(setOf(VAR, CMP), "styles", "Map") {
                        typeArgument("String")
                        typeArgument("AglStyle")
                    }
                }
                data("AglStyleSelector") {
                    propertyOf(setOf(CON, CMP), "value", "String")
                    propertyOf(setOf(CON, CMP), "kind", "AglStyleSelectorKind")
                }
                enum("AglStyleSelectorKind", listOf("LITERAL", "PATTERN", "RULE_NAME"))
                data("AglStyle") {
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "value", "String")
                }
            }
        })
    }

    private fun initialiseExpressionsAsm() {
        serialiser.configureFromTypeModel(typeModel("ExpressionsAsm", false) {
            namespace("net.akehurst.language.agl.language.expressions", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("RootExpressionDefault") {
                    propertyOf(setOf(CON, CMP), "value", "String")
                }
                data("NavigationDefault") {
                    propertyOf(setOf(CON, CMP), "value", "List") { typeArgument("String") }
                }
            }
        })
    }

    private fun initialiseCrossReferencesAsm() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("CrossReferencesAsm", false) {
            namespace("net.akehurst.language.agl.language.reference", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("ReferencesGrammar") {
                    supertypes("GrammarAbstract")
                }
            }
            namespace(
                "net.akehurst.language.agl.language.reference.asm",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.language.expressions")
            ) {
                data("CrossReferenceModelDefault") {
                    propertyOf(setOf(VAR, CMP), "declarationsForNamespace", "Map") {
                        typeArgument("String")
                        typeArgument("DeclarationsForNamespaceDefault")
                    }
                }
                data("DeclarationsForNamespaceDefault") {
                    propertyOf(setOf(CON, CMP), "qualifiedName", "String")

                    propertyOf(setOf(VAR, CMP), "scopeDefinition", "Map") {
                        typeArgument("String")
                        typeArgument("ScopeDefinitionDefault")
                    }
                    propertyOf(setOf(VAR, CMP), "references", "List") { typeArgument("ReferenceDefinitionDefault") }
                }
                data("ScopeDefinitionDefault") {
                    propertyOf(setOf(CON, CMP), "scopeForTypeName", "String")

                    propertyOf(setOf(VAR, CMP), "identifiables", "List") { typeArgument("IdentifiableDefault") }
                }
                data("IdentifiableDefault") {
                    propertyOf(setOf(CON, CMP), "typeName", "String")
                    propertyOf(setOf(CON, CMP), "identifiedBy", "String")
                }
                data("ReferenceDefinitionDefault") {
                    propertyOf(setOf(CON, CMP), "inTypeName", "String")
                    propertyOf(setOf(CON, CMP), "referenceExpressionList", "List") { typeArgument("ReferenceExpressionAbstract") }
                }
                data("ReferenceExpressionAbstract") {

                }
                data("PropertyReferenceExpressionDefault") {
                    supertypes("ReferenceExpressionAbstract")
                    propertyOf(setOf(CON, CMP), "referringPropertyNavigation", "Navigation")
                    propertyOf(setOf(CON, CMP), "refersToTypeName", "List") { typeArgument("String") }
                    propertyOf(setOf(CON, CMP), "fromNavigation", "Navigation", true)
                }
                data("CollectionReferenceExpressionDefault") {
                    supertypes("ReferenceExpressionAbstract")
                    propertyOf(setOf(CON, CMP), "navigation", "Navigation")
                    propertyOf(setOf(CON, CMP), "referenceExpressionList", "List") { typeArgument("ReferenceExpression") }
                }

            }
        })
    }

    private fun initialiseMessages() {
        //classes registered with KotlinxReflect via gradle plugin
        serialiser.configureFromTypeModel(typeModel("Messages", false) {
            namespace("net.akehurst.language.agl.scanner") {
                enum("MatchableKind", emptyList())
                data("Matchable") {
                    propertyOf(setOf(CON, CMP), "tag", "String")
                    propertyOf(setOf(CON, CMP), "expression", "String")
                    propertyOf(setOf(CON, CMP), "kind", "MatchableKind")

                }
            }
            namespace("net.akehurst.language.api.automaton", imports = mutableListOf("kotlin", "kotlin.collections")) {
                enum("ParseAction", emptyList())
            }
            namespace("net.akehurst.language.editor.api") {
                value("RequestIdentity") {
                }
                enum("MessageResponseStatus", emptyList())
                data("EditorOptionsDefault") {
                    propertyOf(setOf(CON, CMP), "parse", "Boolean")
                    propertyOf(setOf(CON, CMP), "parseLineTokens", "Boolean")
                    propertyOf(setOf(CON, CMP), "lineTokensChunkSize", "Int")
                    propertyOf(setOf(CON, CMP), "parseTree", "Boolean")
                    propertyOf(setOf(CON, CMP), "syntaxAnalysis", "Boolean")
                    propertyOf(setOf(CON, CMP), "syntaxAnalysisAsm", "Boolean")
                    propertyOf(setOf(CON, CMP), "semanticAnalysis", "Boolean")
                    propertyOf(setOf(CON, CMP), "semanticAnalysisAsm", "Boolean")
                }
                data("EndPointIdentity") {
                    propertyOf(setOf(CON, CMP), "editorId", "String")
                    propertyOf(setOf(CON, CMP), "sessionId", "String")
                }
                data("AglToken") {
                    propertyOf(setOf(CON, CMP), "styles", "List") { typeArgument("String") }
                    propertyOf(setOf(CON, CMP), "position", "Int")
                    propertyOf(setOf(CON, CMP), "length", "Int")
                }
            }
            namespace("net.akehurst.language.editor.common") {
                data("AglTokenDefault") {
                    supertypes("net.akehurst.language.editor.api.AglToken")
                    propertyOf(setOf(CON, CMP), "styles", "List") { typeArgument("String") }
                    propertyOf(setOf(CON, CMP), "position", "Int")
                    propertyOf(setOf(CON, CMP), "length", "Int")
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
                data("MessageProcessorCreate") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "requestId", "RequestIdentity")
                    propertyOf(setOf(CON, CMP), "languageId", "String")
                    propertyOf(setOf(CON, CMP), "grammarStr", "String", true)
                    propertyOf(setOf(CON, CMP), "typesModelStr", "String", true)
                    propertyOf(setOf(CON, CMP), "transformStr", "String", true)
                    propertyOf(setOf(CON, CMP), "crossReferenceStr", "String", true)
                    propertyOf(setOf(CON, CMP), "editorOptions", "EditorOptionsDefault", false)
                }
                data("MessageProcessorCreateResponse") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "requestId", "RequestIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "scannerMatchables", "List") { typeArgument("Matchable") }
                }
                data("MessageProcessorDelete") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "languageId", "String")
                }
                data("MessageProcessorDeleteResponse") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                }
                data("MessageProcessRequest") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "languageId", "String")
                    propertyOf(setOf(CON, CMP), "text", "String")
                    propertyOf(setOf(CON, CMP), "options", "ProcessOptionsDefault")
                }
                data("MessageParseResult") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "treeSerialised", "String", true)
                }
//FIXME
                data("MessageParseResult2") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "treeData", "TreeDataComplete", true)
                }

                data("MessageSyntaxAnalysisResult") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "asm", "Any", true)
                }
                data("MessageSemanticAnalysisResult") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "asm", "Any", true)
                }
                data("MessageParserInterruptRequest") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "languageId", "String")
                    propertyOf(setOf(CON, CMP), "reason", "String")
                }
                data("MessageLineTokens") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "startLine", "Int")
                    propertyOf(setOf(CON, CMP), "lineTokens", "List") {
                        typeArgument("List") {
                            typeArgument("AglToken")
                        }
                    }
                }
                data("MessageSetStyle") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "languageId", "String")
                    propertyOf(setOf(CON, CMP), "styleStr", "String")
                }
                data("MessageSetStyleResponse") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "styleModel", "AglStyleModelDefault", true)
                }
                data("MessageCodeCompleteRequest") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "languageId", "String")
                    propertyOf(setOf(CON, CMP), "text", "String")
                    propertyOf(setOf(CON, CMP), "position", "Int")
                    propertyOf(setOf(CON, CMP), "options", "ProcessOptionsDefault")
                }
                data("MessageCodeCompleteResult") {
                    propertyOf(setOf(CON, CMP), "endPoint", "EndPointIdentity")
                    propertyOf(setOf(CON, CMP), "status", "MessageResponseStatus")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "issues", "List") { typeArgument("LanguageIssue") }
                    propertyOf(setOf(CON, CMP), "completionItems", "Array") { typeArgument("CompletionItem") }
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
                data("ScopeSimple") {
                    typeParameters("AsmElementIdType")
                    propertyOf(setOf(CON, REF), "parent", "ScopeSimple") { typeArgument("AsmElementIdType") }
                    propertyOf(setOf(CON, CMP), "scopeIdentityInParent", "String")
                    propertyOf(setOf(CON, CMP), "forTypeName", "String")

                    propertyOf(setOf(VAR, REF), "scopeMap", "Map") {
                        typeArgument("AsmElementIdType") //TODO: should really mark if key is composite or reference!
                        typeArgument("ScopeSimple")
                    }
                    propertyOf(setOf(VAR, CMP), "childScopes", "Map") {
                        typeArgument("String")
                        typeArgument("ScopeSimple")
                    }
                    propertyOf(setOf(VAR, CMP), "items", "Map") {
                        typeArgument("String")
                        typeArgument("Map") {
                            typeArgument("String")
                            typeArgument("AsmElementIdType")
                        }
                    }
                }
                data("ContextSimple") {
                    propertyOf(setOf(VAR, CMP), "rootScope", "ScopeSimple") { typeArgument("E") }
                }
                data("ContextFromTypeModelReference") {
                    propertyOf(setOf(CON, CMP), "languageDefinitionId", "String")
                }
                data("ContextFromTypeModel") {
                    propertyOf(setOf(CON, CMP), "typeModel", "net.akehurst.language.typemodel.api.TypeModel")
                }
            }
            namespace("net.akehurst.language.agl.asm", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("AsmPathSimple") {
                    propertyOf(setOf(CON, CMP), "value", "String")
                }
                data("AsmSimple") {
                    propertyOf(setOf(VAR, CMP), "root", "List") { typeArgument("AsmValueAbstract") }
                }
                data("AsmValueAbstract")
                data("AsmNothingSimple") {
                    supertypes("AsmValueAbstract")
                }
                data("AsmPrimitiveSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CON, CMP), "qualifiedTypeName", "String")
                    propertyOf(setOf(CON, CMP), "value", "Any")
                }
                data("AsmReferenceSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CON, CMP), "reference", "String")
                    propertyOf(setOf(CON, REF), "value", "AsmElementSimple", true)
                }
                data("AsmStructureSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CON, CMP), "path", "AsmPathSimple")
                    propertyOf(setOf(CON, CMP), "qualifiedTypeName", "String")

                    propertyOf(setOf(VAR, CMP), "property", "Map") {
                        typeArgument("String")
                        typeArgument("AsmStructurePropertySimple")
                    }
                }
                data("AsmStructurePropertySimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, REF), "index", "Int")
                    propertyOf(setOf(CON, CMP), "value", "Any")
                }
                data("AsmListSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CON, CMP), "elements", "AsmValueAbstract")
                }
                data("AsmListSeparatedSimple") {
                    supertypes("AsmValueAbstract")
                    propertyOf(setOf(CON, CMP), "elements", "AsmValueAbstract")
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
                data("Grammar") {}
                data("RuleItem") {}
                enum("OverrideKind", listOf())
            }
            namespace(
                "net.akehurst.language.agl.language.grammar",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.semanticAnalyser")
            ) {
                data("AglGrammarGrammar") {
                    supertypes("GrammarAbstract")
                }
//                data("ContextFromGrammar") {
//                    propertyOf(setOf(VAR, CMP), "rootScope", "ScopeSimple") { typeArgument("String") }
//                }
            }
            namespace("net.akehurst.language.agl.language.format", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("AglFormatGrammar") {
                    supertypes("GrammarAbstract")
                }
            }
            namespace(
                "net.akehurst.language.agl.language.grammar.asm",
                imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.api.language.grammar")
            ) {
                data("NamespaceDefault") {
                    propertyOf(setOf(CON, CMP), "qualifiedName", "String")
                }
                data("GrammarReferenceDefault") {
                    propertyOf(setOf(CON, CMP), "localNamespace", "NamespaceDefault")
                    propertyOf(setOf(CON, CMP), "nameOrQName", " String")

                    propertyOf(setOf(VAR, REF), "resolved", "GrammarAbstract")
                }
                data("GrammarDefault") {
                    supertypes("GrammarAbstract")
                    propertyOf(setOf(CON, CMP), "namespace", "NamespaceDefault")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "options", "List") { typeArgument("GrammarOptionDefault") }
                }
                data("GrammarOptionDefault") {
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "value", "String")
                }
                data("GrammarAbstract") {
                    supertypes("Grammar")

                    propertyOf(setOf(CON, CMP), "namespace", "NamespaceDefault")
                    propertyOf(setOf(CON, CMP), "name", "String")

                    propertyOf(setOf(VAR, CMP), "extends", "List") { typeArgument("GrammarReferenceDefault") }
                    propertyOf(setOf(VAR, CMP), "grammarRule", "List") { typeArgument("GrammarRuleAbstract") }
                }
                data("GrammarRuleAbstract")
                data("NormalRuleDefault") {
                    supertypes("GrammarRuleAbstract")
                    propertyOf(setOf(CON, REF), "grammar", "GrammarDefault")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "isSkip", "Boolean")
                    propertyOf(setOf(CON, CMP), "isLeaf", "Boolean")

                    propertyOf(setOf(VAR, CMP), "rhs", "RuleItemAbstract")
                }

                data("OverrideRuleDefault") {
                    supertypes("GrammarRuleAbstract")
                    propertyOf(setOf(CON, REF), "grammar", "GrammarDefault")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "isSkip", "Boolean")
                    propertyOf(setOf(CON, CMP), "isLeaf", "Boolean")
                    propertyOf(setOf(CON, CMP), "overrideKind", "OverrideKind")

                    propertyOf(setOf(VAR, CMP), "overridenRhs", "RuleItemAbstract")
                }
                data("RuleItemAbstract") {
                    supertypes("RuleItem")
                }
                data("EmptyRuleDefault") {
                    supertypes("RuleItemAbstract")
                }
                data("ChoiceAbstract") {
                    supertypes("RuleItemAbstract")
                }
                data("ChoiceLongestDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CON, CMP), "alternative", "List") { typeArgument("RuleItem") }
                }
                data("ChoicePriorityDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CON, CMP), "alternative", "List") { typeArgument("RuleItem") }
                }
                data("ChoiceAmbiguousDefault") {
                    supertypes("ChoiceAbstract")
                    propertyOf(setOf(CON, CMP), "alternative", "List") { typeArgument("RuleItem") }
                }
                data("ConcatenationDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CON, CMP), "items", "List") { typeArgument("RuleItem") }
                }
                data("ConcatenationItemAbstract") {
                    supertypes("RuleItemAbstract")
                }
                data("SimpleItemAbstract") {
                    supertypes("ConcatenationItemAbstract")
                }
                data("GroupDefault") {
                    supertypes("ConcatenationItemAbstract")
                    propertyOf(setOf(CON, CMP), "groupedContent", "RuleItem")
                }
                data("NonTerminalDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CON, CMP), "targetGrammar", "GrammarReference", true)
                    propertyOf(setOf(CON, CMP), "name", "String")
                }
                data("TerminalDefault") {
                    supertypes("RuleItemAbstract")
                    propertyOf(setOf(CON, CMP), "value", "String")
                    propertyOf(setOf(CON, CMP), "isPattern", "Boolean")
                }
                data("EmbeddedDefault") {
                    supertypes("RuleItemAbstract")

                    propertyOf(setOf(CON, CMP), "embeddedGoalName", "String")
                    propertyOf(setOf(CON, CMP), "embeddedGrammarReference", "GrammarReferenceDefault")
                }
                data("SeparatedListDefault") {
                    propertyOf(setOf(CON, CMP), "min", "Int")
                    propertyOf(setOf(CON, CMP), "max", "Int")
                    propertyOf(setOf(CON, CMP), "item", "SimpleItemAbstract")
                    propertyOf(setOf(CON, CMP), "separator", "SimpleItem")
                }
                data("SimpleListDefault") {
                    propertyOf(setOf(CON, CMP), "min", "Int")
                    propertyOf(setOf(CON, CMP), "max", "Int")
                    propertyOf(setOf(CON, CMP), "item", "SimpleItemAbstract")
                }
                data("OptionalItemDefault") {
                    propertyOf(setOf(CON, CMP), "item", "RuleItem")
                }
            }
        })
    }

    private fun initialiseSPPT() {
        serialiser.configureFromTypeModel(typeModel("SPPT", false) {
            namespace("net.akehurst.language.agl.runtime.structure", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("RuntimeRule") {
                    propertyOf(setOf(CON, CMP), "runtimeRuleSetNumber", "Int")
                    propertyOf(setOf(CON, CMP), "ruleNumber", "Int")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(CON, CMP), "isSkip", "Boolean")
                }
            }
            namespace("net.akehurst.language.agl.sppt", imports = mutableListOf("kotlin", "kotlin.collections", "net.akehurst.language.agl.runtime.structure")) {
                data("CompleteTreeDataNode") {
                    propertyOf(setOf(CON, CMP), "rule", "RuntimeRule")
                    propertyOf(setOf(CON, CMP), "startPosition", "Int")
                    propertyOf(setOf(CON, CMP), "nextInputPosition", "Int")
                    propertyOf(setOf(CON, CMP), "nextInputNoSkip", "Int")
                    propertyOf(setOf(CON, CMP), "option", "Int")
                }
                data("TreeDataComplete2") {
                    propertyOf(setOf(CON, CMP), "forStateSetNumber", "Int")

                    propertyOf(setOf(VAR, CMP), "root", "CompleteTreeDataNode", true)
                    propertyOf(setOf(VAR, CMP), "initialSkip", "TreeDataComplete", true) { typeArgument("CompleteTreeDataNode") }
                    propertyOf(setOf(VAR, CMP), "completeChildren", "Map") {
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
                data("InputLocation") {
                    propertyOf(setOf(CON, CMP), "position", "Int")
                    propertyOf(setOf(CON, CMP), "column", "Int")
                    propertyOf(setOf(CON, CMP), "line", "Int")
                    propertyOf(setOf(CON, CMP), "length", "Int")
                }
            }
            namespace("net.akehurst.language.api.processor", imports = mutableListOf("kotlin", "kotlin.collections")) {
                enum("LanguageIssueKind", emptyList())
                enum("CompletionItemKind", emptyList())
                enum("LanguageProcessorPhase", emptyList())
                data("LanguageIssue") {
                    propertyOf(setOf(CON, CMP), "kind", "LanguageIssueKind")
                    propertyOf(setOf(CON, CMP), "phase", "LanguageProcessorPhase")
                    propertyOf(setOf(CON, CMP), "location", "InputLocation")
                    propertyOf(setOf(CON, CMP), "message", "String")
                    propertyOf(setOf(CON, CMP), "data", "Any")
                }
                data("CompletionItem") {
                    propertyOf(setOf(CON, CMP), "kind", "CompletionItemKind")
                    propertyOf(setOf(CON, CMP), "text", "String")
                    propertyOf(setOf(CON, CMP), "name", "String")
                    propertyOf(setOf(VAR, CMP), "description", "String")
                }
            }
            namespace("net.akehurst.language.agl.processor", imports = mutableListOf("kotlin", "kotlin.collections")) {
                data("ScanOptionsDefault") {

                }
                data("ParseOptionsDefault") {
                    propertyOf(setOf(CON, CMP), "goalRuleName", "String")
                    propertyOf(setOf(CON, CMP), "reportErrors", "Boolean")
                    propertyOf(setOf(CON, CMP), "reportGrammarAmbiguities", "Boolean")
                    propertyOf(setOf(CON, CMP), "cacheSkip", "Boolean")
                }
                data("SyntaxAnalysisOptionsDefault") {
                    propertyOf(setOf(CON, CMP), "active", "Boolean")
                }
                data("SemanticAnalysisOptionsDefault") {
                    typeParameters("AsmType", "ContextType")
                    propertyOf(setOf(CON, CMP), "active", "Boolean")
                    propertyOf(setOf(CON, CMP), "locationMap", "Map") {
                        typeArgument("Any")
                        typeArgument("InputLocation")
                    }
                    propertyOf(setOf(CON, CMP), "context", "ContextType")
                    propertyOf(setOf(CON, CMP), "checkReferences", "Boolean")
                    propertyOf(setOf(CON, CMP), "resolveReferences", "Boolean")
                    propertyOf(setOf(CON, CMP), "other", "Map") {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                }
                data("CompletionProviderOptionsDefault") {
                    propertyOf(setOf(CON, CMP), "context", "ContextType")
                    propertyOf(setOf(CON, CMP), "other", "Map") {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                }
                data("ProcessOptionsDefault") {
                    propertyOf(setOf(CON, CMP), "scan", "ScanOptionsDefault")
                    propertyOf(setOf(CON, CMP), "parse", "ParseOptionsDefault")
                    propertyOf(setOf(CON, CMP), "syntaxAnalysis", "SyntaxAnalysisOptionsDefault")
                    propertyOf(setOf(CON, CMP), "semanticAnalysis", "SemanticAnalysisOptionsDefault")
                    propertyOf(setOf(CON, CMP), "completionProvider", "CompletionProviderOptionsDefault")
                }
            }
        })
    }

    fun check() {
        val issues = serialiser.registry.checkPublicAndReflectable()
        check(issues.isEmpty()) { issues.joinToString(separator = "\n") }
    }

    //fun configureFromKompositeString(datatypeModel: String) {
    //    serialiser.configureFromKompositeString(datatypeModel)
    //}

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

