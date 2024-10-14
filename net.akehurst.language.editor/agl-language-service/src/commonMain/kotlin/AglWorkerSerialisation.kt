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
import net.akehurst.language.grammar.processor.AglGrammar
import net.akehurst.language.typemodel.processor.AglTypemodel
import net.akehurst.language.expressions.processor.AglExpressions
import net.akehurst.language.reference.processor.AglCrossReference
import net.akehurst.language.style.processor.AglStyle
import net.akehurst.language.scope.processor.AglScope
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
        val namesapces = (
                AglBase.typeModel.namespace +
                        AglGrammar.typeModel.namespace +
                        AglTypemodel.typeModel.namespace +
                        AglAsm.typeModel.namespace +
                        AglExpressions.typeModel.namespace +
                        AglCrossReference.typeModel.namespace +
                        AglStyle.typeModel.namespace +
                        AglScope.typeModel.namespace
                ).toSet().toList()
        println(namesapces)
        val tm = typeModel("Messages", true, namesapces) {
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
                dataType("MessageProcessorDelete") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                }
                dataType("MessageGrammarAmbiguityAnalysisResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageGrammarAmbiguityAnalysisRequest") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                }
                dataType("MessageSyntaxAnalysisResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("asm", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "asm", "Any", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageLineTokens") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("startLine", "Integer", false)
                        parameter("lineTokens", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "lineTokens", "List", false) {
                        typeArgument("List")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "startLine", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageParseResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("treeSerialised", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "treeSerialised", "String", false)
                }
                dataType("MessageSetStyle") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("styleStr", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "styleStr", "String", false)
                }
                dataType("MessageProcessorDeleteResponse") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageProcessorCreateResponse") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("scannerMatchables", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "scannerMatchables", "List", false) {
                        typeArgument("Matchable")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageSemanticAnalysisResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("asm", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "asm", "Any", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("AglWorkerMessageResponse") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("action", "String", false)
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                }
                dataType("MessageParserInterruptRequest") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("reason", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "reason", "String", false)
                }
                dataType("MessageCodeCompleteResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("completionItems", "List", false)
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "completionItems", "List", false) {
                        typeArgument("CompletionItem")
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
                dataType("MessageProcessorCreate") {
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("grammarStr", "String", false)
                        parameter("crossReferenceModelStr", "String", false)
                        parameter("editorOptions", "EditorOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "crossReferenceModelStr", "String", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "editorOptions", "EditorOptions", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "grammarStr", "String", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                }
                dataType("MessageCodeCompleteRequest") {
                    typeParameters("AsmType", "ContextType")
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("text", "String", false)
                        parameter("position", "Integer", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "options", "ProcessOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "text", "String", false)
                }
                dataType("MessageProcessRequest") {
                    typeParameters("AsmType", "ContextType")
                    supertype("AglWorkerMessage")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("languageId", "LanguageIdentity", false)
                        parameter("text", "String", false)
                        parameter("options", "ProcessOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageId", "LanguageIdentity", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "options", "ProcessOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "text", "String", false)
                }
                dataType("MessageParseResult2") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("treeData", "TreeData", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "treeData", "TreeData", false)
                }
                dataType("MessageSetStyleResponse") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("styleModel", "AglStyleModel", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "styleModel", "AglStyleModel", false)
                }
                dataType("AglWorkerMessage") {

                    constructor_ {
                        parameter("action", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "action", "String", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                }
                dataType("MessageScanResult") {
                    supertype("AglWorkerMessageResponse")
                    constructor_ {
                        parameter("endPoint", "EndPointIdentity", false)
                        parameter("status", "MessageStatus", false)
                        parameter("message", "String", false)
                        parameter("issues", "List", false)
                        parameter("lineTokens", "List", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "endPoint", "EndPointIdentity", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "issues", "List", false) {
                        typeArgument("LanguageIssue")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "lineTokens", "List", false) {
                        typeArgument("AglTokenDefault")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "status", "MessageStatus", false)
                }
            }
            namespace("net.akehurst.language.sppt.api", listOf("std", "net.akehurst.language.parser.api")) {
                interfaceType("TreeData") {

                }
                interfaceType("SpptDataNode") {

                }
            }
            namespace("net.akehurst.language.sppt.treedata", listOf("net.akehurst.language.sppt.api", "std", "net.akehurst.language.parser.api")) {
                dataType("TreeDataComplete2") {
                    supertype("TreeData")
                    constructor_ {
                        parameter("forStateSetNumber", "Integer", false)
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "_complete", "Map", false) {
                        typeArgument("SpptDataNode")
                        typeArgument("Map") {
                            typeArgument("Integer")
                            typeArgument("List") {
                                typeArgument("SpptDataNode")
                            }
                        }
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "_embeddedFor", "Map", false) {
                        typeArgument("SpptDataNode")
                        typeArgument("TreeData")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "_preferred", "Map", false) {
                        typeArgument("PreferredNode")
                        typeArgument("SpptDataNode")
                    }
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "_skipDataAfter", "Map", false) {
                        typeArgument("SpptDataNode")
                        typeArgument("TreeData")
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "forStateSetNumber", "Integer", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "initialSkip", "TreeData", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "root", "SpptDataNode", false)
                }
                dataType("CompleteTreeDataNode") {
                    supertype("SpptDataNode")
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
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "rule", "Rule", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "startPosition", "Integer", false)
                }
                dataType("PreferredNode") {

                    constructor_ {
                        parameter("rule", "Rule", false)
                        parameter("startPosition", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "rule", "Rule", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "startPosition", "Integer", false)
                }
            }
            namespace("net.akehurst.language.parser.api", listOf("std")) {
                interfaceType("Rule") {

                }
                interfaceType("ParseOptions") {

                }
            }
            namespace("net.akehurst.language.agl.runtime.structure", listOf("net.akehurst.language.parser.api", "std")) {
                dataType("RuntimeRule") {
                    supertype("Rule")
                    constructor_ {
                        parameter("runtimeRuleSetNumber", "Integer", false)
                        parameter("ruleNumber", "Integer", false)
                        parameter("name", "String", false)
                        parameter("isSkip", "Boolean", false)
                        parameter("isPseudo", "Boolean", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "isPseudo", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "isSkip", "Boolean", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "name", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "ruleNumber", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "runtimeRuleSetNumber", "Integer", false)
                }
            }
            namespace("net.akehurst.language.sentence.api", listOf("std")) {
                dataType("InputLocation") {

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
            namespace("net.akehurst.language.issues.api", listOf("std", "net.akehurst.language.sentence.api")) {
                enumType("LanguageIssueKind", listOf("ERROR", "WARNING", "INFORMATION"))
                enumType("LanguageProcessorPhase", listOf("GRAMMAR", "SCAN", "PARSE", "SYNTAX_ANALYSIS", "SEMANTIC_ANALYSIS", "INTERPRET", "GENERATE", "FORMAT", "ALL"))
                dataType("LanguageIssue") {

                    constructor_ {
                        parameter("kind", "LanguageIssueKind", false)
                        parameter("phase", "LanguageProcessorPhase", false)
                        parameter("location", "InputLocation", false)
                        parameter("message", "String", false)
                        parameter("data", "Any", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "data", "Any", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "kind", "LanguageIssueKind", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "location", "InputLocation", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "message", "String", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "phase", "LanguageProcessorPhase", false)
                }
            }
            namespace("net.akehurst.language.scanner.api", listOf("std")) {
                enumType("MatchableKind", listOf("EOT", "LITERAL", "REGEX"))
                interfaceType("ScanOptions") {

                }
                dataType("Matchable") {

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
            namespace(
                "net.akehurst.language.api.processor",
                listOf("net.akehurst.language.base.api", "std", "net.akehurst.language.parser.api", "net.akehurst.language.scanner.api", "net.akehurst.language.sentence.api")
            ) {
                valueType("LanguageIdentity") {
                    supertype("PublicValueType")
                    constructor_ {
                        parameter("value", "String", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "value", "String", false)
                }
                interfaceType("ProcessOptions") {
                    typeParameters("AsmType", "ContextType")

                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "completionProvider", "CompletionProviderOptions", false){
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "parse", "ParseOptions", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "scan", "ScanOptions", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "semanticAnalysis", "SemanticAnalysisOptions", false){
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "syntaxAnalysis", "SyntaxAnalysisOptions", false){
                        typeArgument("AsmType")
                    }
                }
                interfaceType("SyntaxAnalysisOptions") {
                    typeParameters("AsmType")

                }
                interfaceType("SemanticAnalysisOptions") {
                    typeParameters("AsmType", "ContextType")
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "context", "ContextType", false)
                }
                interfaceType("CompletionProviderOptions") {
                    typeParameters("AsmType", "ContextType")

                }
                dataType("CompletionItem") {

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
            namespace("net.akehurst.language.api.semanticAnalyser", listOf("std")) {
                interfaceType("SentenceContext") {
                    typeParameters("E")

                }
            }
            namespace("net.akehurst.language.scanner.common", listOf("net.akehurst.language.scanner.api", "std")) {
                dataType("ScanOptionsDefault") {
                    supertype("ScanOptions")
                    constructor_ {}
                }
            }
            namespace("net.akehurst.language.parser.leftcorner", listOf("net.akehurst.language.parser.api", "std")) {
                dataType("ParseOptionsDefault") {
                    supertype("ParseOptions")
                    constructor_ {
                        parameter("goalRuleName", "String", false)
                        parameter("reportErrors", "Boolean", false)
                        parameter("reportGrammarAmbiguities", "Boolean", false)
                        parameter("cacheSkip", "Boolean", false)
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "cacheSkip", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "goalRuleName", "String", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "reportErrors", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "reportGrammarAmbiguities", "Boolean", false)
                }
            }
            namespace(
                "net.akehurst.language.agl.processor",
                listOf("net.akehurst.language.api.processor", "std", "net.akehurst.language.sentence.api", "net.akehurst.language.scanner.api", "net.akehurst.language.parser.api")
            ) {
                dataType("SyntaxAnalysisOptionsDefault") {
                    typeParameters("AsmType")
                    supertype("SyntaxAnalysisOptions") { ref("AsmType") }
                    constructor_ {
                        parameter("active", "Boolean", false)
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "active", "Boolean", false)
                }
                dataType("SemanticAnalysisOptionsDefault") {
                    typeParameters("AsmType", "ContextType")
                    supertype("SemanticAnalysisOptions") { ref("AsmType"); ref("ContextType") }
                    constructor_ {
                        parameter("active", "Boolean", false)
                        parameter("locationMap", "Map", false)
                        parameter("context", "ContextType", false)
                        parameter("checkReferences", "Boolean", false)
                        parameter("resolveReferences", "Boolean", false)
                        parameter("other", "Map", false)
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "active", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "checkReferences", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "context", "ContextType", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "locationMap", "Map", false) {
                        typeArgument("Any")
                        typeArgument("InputLocation")
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "other", "Map", false) {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "resolveReferences", "Boolean", false)
                }
                dataType("CompletionProviderOptionsDefault") {
                    typeParameters("AsmType", "ContextType")
                    supertype("CompletionProviderOptions") { ref("AsmType"); ref("ContextType") }
                    constructor_ {
                        parameter("context", "ContextType", false)
                        parameter("other", "Map", false)
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "context", "ContextType", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "other", "Map", false) {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                }
                dataType("ProcessOptionsDefault") {
                    typeParameters("AsmType", "ContextType")
                    supertype("ProcessOptions") { ref("AsmType"); ref("ContextType") }
                    constructor_ {
                        parameter("scan", "ScanOptions", false)
                        parameter("parse", "ParseOptions", false)
                        parameter("syntaxAnalysis", "SyntaxAnalysisOptions", false)
                        parameter("semanticAnalysis", "SemanticAnalysisOptions", false)
                        parameter("completionProvider", "CompletionProviderOptions", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "completionProvider", "CompletionProviderOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "parse", "ParseOptions", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "scan", "ScanOptions", false)
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "semanticAnalysis", "SemanticAnalysisOptions", false) {
                        typeArgument("AsmType")
                        typeArgument("ContextType")
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "syntaxAnalysis", "SyntaxAnalysisOptions", false) {
                        typeArgument("AsmType")
                    }
                }
            }
            namespace("net.akehurst.language.agl.simple", listOf("net.akehurst.language.asm.api", "net.akehurst.language.api.semanticAnalyser", "std", "net.akehurst.language.scope.asm")) {
                dataType("ContextAsmSimple") {
                    supertype("SentenceContext") { ref("net.akehurst.language.asm.api.AsmPath") }
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "rootScope", "ScopeSimple", false) {
                        typeArgument("AsmPath")
                    }
                }
            }
            namespace("net.akehurst.language.grammar.processor", listOf("std", "net.akehurst.language.api.semanticAnalyser", "net.akehurst.language.scope.asm")) {
                dataType("ContextFromGrammar") {
                    supertype("SentenceContext") { ref("std.String") }
                    constructor_ {}
                    propertyOf(setOf(READ_WRITE, COMPOSITE, STORED), "rootScope", "ScopeSimple", false) {
                        typeArgument("String")
                    }
                }
            }
            namespace(
                "net.akehurst.language.agl.semanticAnalyser",
                listOf("std", "net.akehurst.language.api.semanticAnalyser", "net.akehurst.language.api.processor", "net.akehurst.language.typemodel.api")
            ) {
                dataType("ContextFromTypeModelReference") {
                    supertype("SentenceContext") { ref("std.String") }
                    constructor_ {
                        parameter("languageDefinitionId", "LanguageIdentity", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "languageDefinitionId", "LanguageIdentity", false)
                }
                dataType("ContextFromTypeModel") {
                    supertype("SentenceContext") { ref("std.String") }
                    constructor_ {
                        parameter("typeModel", "TypeModel", false)
                    }
                    propertyOf(setOf(READ_ONLY, COMPOSITE, STORED), "typeModel", "TypeModel", false)
                }
            }
            namespace("net.akehurst.language.editor.api", listOf("std")) {
                enumType("MessageStatus", listOf("START", "FAILURE", "SUCCESS"))
                interfaceType("EditorOptions") {

                }
                interfaceType("AglToken") {

                }
                dataType("EndPointIdentity") {

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
                    supertype("AglToken")
                    constructor_ {
                        parameter("styles", "List", false)
                        parameter("position", "Integer", false)
                        parameter("length", "Integer", false)
                    }
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "length", "Integer", false)
                    propertyOf(setOf(READ_ONLY, REFERENCE, STORED), "position", "Integer", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "styles", "List", false) {
                        typeArgument("String")
                    }
                }
                dataType("EditorOptionsDefault") {
                    supertype("EditorOptions")
                    constructor_ {
                        parameter("parse", "Boolean", false)
                        parameter("parseLineTokens", "Boolean", false)
                        parameter("lineTokensChunkSize", "Integer", false)
                        parameter("parseTree", "Boolean", false)
                        parameter("syntaxAnalysis", "Boolean", false)
                        parameter("syntaxAnalysisAsm", "Boolean", false)
                        parameter("semanticAnalysis", "Boolean", false)
                        parameter("semanticAnalysisAsm", "Boolean", false)
                    }
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "lineTokensChunkSize", "Integer", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "parse", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "parseLineTokens", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "parseTree", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "semanticAnalysis", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "semanticAnalysisAsm", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "syntaxAnalysis", "Boolean", false)
                    propertyOf(setOf(READ_WRITE, REFERENCE, STORED), "syntaxAnalysisAsm", "Boolean", false)
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

