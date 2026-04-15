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

package demo

import korlibs.io.async.asyncImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.kotlinx.logging.api.Logger
import net.akehurst.kotlinx.logging.api.logger
import net.akehurst.kotlinx.logging.common.LoggerConsole
import net.akehurst.kotlinx.logging.common.LoggingByConsole
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.semanticAnalyser.contextFromTypesDomain
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.*
import net.akehurst.language.asmTransform.asm.AsmTransformDomainDefault
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.EditorOptionsDefault
import net.akehurst.language.editor.compose.attachToComposeEditor
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import net.akehurst.language.grammar.api.GrammarDomain
import net.akehurst.language.grammar.processor.contextFromGrammar


//external var aglScriptBasePath: dynamic = definedExternally
//val workerScriptName = "${aglScriptBasePath}/application-agl-editor-worker.js"
var demo: Demo? = null

fun runDemo() {

    //createBaseDom("div#agl-demo")
    val logger = logger("Demo")

    /*
        val loggingLevel = document.querySelector("#agl-demo-logging-level")!! as HTMLSelectElement
        loggingLevel.addEventListener("change", {
            val optionStr = loggingLevel.value
            val ll = LogLevel.valueOf(optionStr)
            logger.level = ll
        })

        val editorChoice = document.querySelector("#agl-options-editor")!! as HTMLSelectElement
        editorChoice.addEventListener("change", {
            val optionStr = editorChoice.value
            val edKind = AlternativeEditors.valueOf(optionStr)
            createDemo(edKind, logger)
        })
    */
//    TabView.initialise(document)
    initialiseExamples()

    createDemo(AlternativeEditors.DUMMY, logger)

}

enum class AlternativeEditors {
    DUMMY
}

object Constants {
    const val sentenceEditorId = "editor-sentence"
    const val grammarEditorId = "editor-grammar"
    const val referencesEditorId = "editor-references"
    const val styleEditorId = "editor-style"
    const val formatEditorId = "editor-format"

    val sentenceLanguageId = LanguageIdentity("language-user")
    val grammarLanguageId = Agl.registry.agl.grammarLanguageIdentity
    val referencesLanguageId = Agl.registry.agl.crossReferenceLanguageIdentity
    val styleLanguageId = Agl.registry.agl.styleLanguageIdentity
    val formatLanguageId = Agl.registry.agl.formatLanguageIdentity

    val sentenceProcessOptions = Agl.options<Any, Any> { semanticAnalysis { context(contextAsmSimple()) } }
    val grammarProcessOptions = Agl.options<Any, Any> { semanticAnalysis { context(contextAsmSimple()) } }
    val referencesProcessOptions = Agl.options<Any, Any> { semanticAnalysis { context(contextAsmSimple()) } }
    val styleProcessOptions = Agl.options<Any, Any> { semanticAnalysis { context(contextAsmSimple()) } }
}


/*
fun createBaseDom(appDivSelector: String) {
    val appDiv = document.querySelector(appDivSelector)!!
    while (null != appDiv.firstChild) {
        appDiv.removeChild(appDiv.firstChild!!)
    }
    //val aglEditorVersion = net.akehurst.language.editor.ace.
    appDiv.create().article {
        header {
            section {
                class_.add("agl-menubar")
                h2 { content = "Version ${BuildConfig.version}" }
                section {
                    class_.add("agl-options")
                    div {
                        class_.add("agl-options-example")
                        label { attribute.for_ = "example"; content = "Example :" }
                        select { attribute.id = "example" }
                    }
                }
                nav {
                    val about = dialog {
                        val thisDialog = this.element as HTMLDialogElement
                        on.click {
                            (it.target as HTMLDialogElement).close()
                        }
                        article {
                            header { h2 { content = "About" } }
                            section {
                                p { content = "Ace version ${net.akehurst.language.editor.agl.editor.browser.ace.BuildConfig.versionEditorAce}, Licence BSD" }
                                p { content = "Monaco version ${net.akehurst.language.editor.agl.editor.browser.monaco.BuildConfig.versionEditorMonaco}, Licence MIT" }
                                p { content = "AGL version ${Agl.version}, Licence Apache 2.0" }
                                p { content = "Kotlin version ${kotlin.KotlinVersion.CURRENT}, Licence Apache 2.0" }
                            }
                            footer {
                                button {
                                    content = "Close"
                                    on.click { thisDialog.close() }
                                }
                            }
                        }
                    }
                    a {
                        //content = "About"
                        h2 { content = "About" }
                        on.click {
                            (about as HTMLDialogElement).showModal()
                        }
                    }
                }
            }

            /* TODO:
            div {
                select { attribute.id="goalRule" }
                label { attribute.for_="goalRule"; content="Optionally choose a goal rule:" }
            }
             */
        }

        section {
            htmlElement("tabview") {
                htmlElement("tab") {
                    attribute.id = "Sentence"
                    section {
                        class_.add("sentence")
                        htmlElement("agl-editor") {
                            attribute.id = Constants.sentenceEditorId
                            attribute.set("agl-language", Constants.sentenceLanguageId)
                        }
                    }
                    section {
                        class_.add("trees")
                        htmlElement("tabview") {
                            htmlElement("tab") {
                                attribute.id = "ast"
                                htmlElement("treeview") { attribute.id = "ast" }
                            }
                            htmlElement("tab") {
                                attribute.id = "parse"
                                htmlElement("treeview") { attribute.id = "parse" }
                            }
                        }
                    }
                }
                htmlElement("tab") {
                    attribute.id = "Language"
                    section {
                        class_.add("language")
                        htmlElement("tabview") {
                            htmlElement("tab") {
                                attribute.id = "Grammar"
                                section {
                                    class_.add("grammar")
                                    htmlElement("agl-editor") {
                                        attribute.id = Constants.grammarEditorId
                                        attribute.set("agl-language", Constants.grammarLanguageId)
                                    }
                                }
                                section {
                                    class_.add("typemodel")
                                    h3 { content = "Type Model" }
                                    htmlElement("treeview") { attribute.id = "typemodel" }
                                }
                            }
                            htmlElement("tab") {
                                attribute.id = "style"
                                section {
                                    htmlElement("agl-editor") {
                                        attribute.id = Constants.styleEditorId
                                        attribute.set("agl-language", Constants.styleLanguageId)
                                    }
                                }
                            }
                            htmlElement("tab") {
                                attribute.id = "crossreferences"
                                section {
                                    htmlElement("agl-editor") {
                                        attribute.id = Constants.referencesEditorId
                                        attribute.set("agl-language", Constants.referencesLanguageId)
                                    }
                                }
                            }
                        }
                    }
                }
                htmlElement("tab") {
                    attribute.id = "Configuration"
                    section {
                        class_.add("configuration")
                        div {
                            //class_.add("agl-options-editor")
                            label { content = "Select Logging Level: " }
                            select {
                                attribute.id = "agl-demo-logging-level"
                                option(value = LogLevel.None.name, selected = false) { content = LogLevel.None.name }
                                option(value = LogLevel.Fatal.name, selected = false) { content = LogLevel.Fatal.name }
                                option(value = LogLevel.Error.name, selected = false) { content = LogLevel.Error.name }
                                option(value = LogLevel.Warning.name, selected = false) { content = LogLevel.Warning.name }
                                option(value = LogLevel.Information.name, selected = true) { content = LogLevel.Information.name }
                                option(value = LogLevel.Debug.name, selected = false) { content = LogLevel.Debug.name }
                                option(value = LogLevel.Trace.name, selected = false) { content = LogLevel.Trace.name }
                                option(value = LogLevel.All.name, selected = false) { content = LogLevel.All.name }
                            }
                        }
                        div {
                            //class_.add("agl-options-editor")
                            label { content = "Select underlying Editor Type: " }
                            select {
                                attribute.id = "agl-options-editor"
                                //attribute.name = "editor-choice"
                                option(value = AlternativeEditors.ACE.name, selected = true) { content = "Ace" }
                                option(value = AlternativeEditors.MONACO.name) { content = "Monaco" }
                                option(value = AlternativeEditors.CODEMIRROR.name) { content = "CodeMirror" }
                            }
                        }
                    }
                }
            }
        }
    }
}
*/
fun initialiseExamples() {
    CoroutineScope(SupervisorJob()).asyncImmediately {
/*        Examples.add(Datatypes.example)
        Examples.add(SQL.example)
        Examples.add(GraphvizDot.example(resourcesVfs))
        Examples.add(SText.example)
        Examples.add(Java8.example(resourcesVfs))
        Examples.add(English.example)
        Examples.add(TraceabilityQuery.example)
        Examples.add(MScript.example(resourcesVfs))
        Examples.add(Xml.example)
 */
    }.invokeOnCompletion {
//        val exampleSelect = document.querySelector("select#example") as HTMLElement
//        Examples.map.forEach { eg ->
//            val option = document.createElement("option")
//            exampleSelect.appendChild(option);
//            option.setAttribute("value", eg.value.id);
//            option.textContent = eg.value.label;
//        }
    }
}

fun createDemo(editorChoice: AlternativeEditors, logger: Logger) {
    val sentenceLanguage = Agl.registry.findOrPlaceholder<Any, Any>(Constants.sentenceLanguageId)
    val grammarLanguage = Agl.registry.findOrPlaceholder<Any, Any>(Constants.grammarLanguageId)
    val styleLanguage = Agl.registry.findOrPlaceholder<Any, Any>(Constants.styleLanguageId)
    val referencesLanguage = Agl.registry.findOrPlaceholder<Any, Any>(Constants.referencesLanguageId)

    //if (null != demo) { demo!!.finalize() }
    /*
    val editorEls = document.querySelectorAll("agl-editor")
    val editors = editorEls.asList().associate { node ->
        val element = node as Element
        //delete any current children of element - existing editor if switching ace<->monaco
        while (element.childElementCount != 0) {
            element.removeChild(element.firstChild!!)
        }
        val ed = when (editorChoice) {
            AlternativeEditors.ACE -> createAce(element)
            AlternativeEditors.MONACO -> createMonaco(element)
            AlternativeEditors.CODEMIRROR -> createCodeMirror(element)
        }
        ed.logger.bind = { lvl, msg, t -> logger.log(lvl, msg, t) }
        Pair(element.id, ed)// (editorId)
    }
     */
    val logFunction: LogFunction = { lvl, prefix, t, msg -> logger.log(lvl, t) { "$prefix - $msg" } }
    val languageService = LanguageServiceDirectExecution(logFunction)
    val editors = mapOf<String, AglEditor<Any, Any>>(
        Constants.sentenceEditorId to createComposeEditor(Constants.sentenceEditorId, logFunction, languageService, sentenceLanguage, Constants.sentenceProcessOptions),
        Constants.grammarEditorId to createComposeEditor(Constants.grammarEditorId, logFunction, languageService, grammarLanguage, Constants.grammarProcessOptions),
        Constants.styleEditorId to createComposeEditor(Constants.styleEditorId, logFunction, languageService, styleLanguage, Constants.referencesProcessOptions),
        Constants.referencesEditorId to createComposeEditor(Constants.referencesEditorId, logFunction, languageService, referencesLanguage, Constants.styleProcessOptions)
    )
    demo = Demo(editors, logger)
    demo!!.configure()
}

fun createComposeEditor(editorId: String, logFunction: LogFunction, languageService: LanguageService, landDef: LanguageDefinition<Any, Any>, procOpts: ProcessOptions<Any, Any>): AglEditor<Any, Any> {
    val editorState = CodeEditorStateHolder(
        initialText = ""
    )

    return Agl.attachToComposeEditor(
        languageService = languageService,
        languageDefinition = landDef,
        processOptions = { procOpts },
        editorId = editorId,
        editorOptions = EditorOptionsDefault(),
        logFunction = logFunction,
        composeEditor = editorState
    )
}

//fun createFirepad(editorElement: Element): AglEditor<Any, Any> {
//    val id = editorElement.id
//    return Agl.attachToCodeMirror(ed, id, id, workerScriptName, true)
//}

class Demo(
    val editors: Map<String, AglEditor<*, *>>,
    val logger: Logger
) {
    //val trees = TreeView.initialise(document)

    //val exampleSelect = document.querySelector("select#example") as HTMLElement
    val sentenceEditor = editors[Constants.sentenceEditorId]!! as AglEditor<Any, Any>
    val grammarEditor = editors[Constants.grammarEditorId]!!
    val styleEditor = editors[Constants.styleEditorId]!! as AglEditor<Any, Any>
    val referencesEditor = editors[Constants.referencesEditorId]!! as AglEditor<Any, Any>
    //val formatEditor = editors["language-format"]!!

    fun configure() {
        this.connectEditors()
        //this.connectTrees()
        //this.configExampleSelector()
    }

    private fun connectEditors() {
        grammarEditor.editorSpecificStyleStr = Agl.registry.agl.grammar.styleString
        styleEditor.editorSpecificStyleStr = Agl.registry.agl.style.styleString
        referencesEditor.editorSpecificStyleStr = Agl.registry.agl.crossReference.styleString

        grammarEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.IGNORED -> Unit
                EventStatus.FAILURE -> {
                    referencesEditor.processOptions().semanticAnalysis.context = null
                    styleEditor.processOptions().semanticAnalysis.context = null
                    logger.logError { grammarEditor.endPointIdentity.editorId + ": " + event.message }
                    sentenceEditor.languageDefinition.update(grammarString = GrammarString(""))
                }

                EventStatus.SUCCESS -> {
                    val grammars = event.asm as GrammarDomain? ?: error("should always be a List<Grammar> if success")
                    val styleContext = contextFromGrammar(grammars)
                    val trm = AsmTransformDomainDefault.fromGrammarDomain(grammars).let {
                        it.asm!!
                    }
                    val scopeContext = contextFromTypesDomain(trm.typesDomain!!)
                    referencesEditor.processOptions().semanticAnalysis.context = scopeContext
                    styleEditor.processOptions().semanticAnalysis.context = styleContext
                    try {
                        logger.logDebug { "Debug: Grammar parse success, resetting sentence processor" }
                        sentenceEditor.languageDefinition.update(grammarString = GrammarString(grammarEditor.text))
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, t) { grammarEditor.endPointIdentity.editorId + ": " + t.message }
                        sentenceEditor.languageDefinition.update(grammarString = GrammarString(""))
                    }
                }
            }

        }

        styleEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.IGNORED -> Unit
                EventStatus.FAILURE -> {
                    logger.logError { styleEditor.endPointIdentity.editorId + ": " + event.message }
                    sentenceEditor.languageDefinition.update(styleString = StyleString(""))
                }

                EventStatus.SUCCESS -> {
                    try {
                        logger.logDebug { "Debug: Style parse success, resetting sentence style" }
                        sentenceEditor.languageDefinition.update(styleString = StyleString(styleEditor.text))
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, t) { styleEditor.endPointIdentity.editorId + ": " + t.message }
                        sentenceEditor.languageDefinition.update(styleString = StyleString(""))
                    }
                }
            }
        }
        referencesEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.IGNORED -> Unit
                EventStatus.FAILURE -> {
                    logger.logError { referencesEditor.endPointIdentity.editorId + ": " + event.message }
                    sentenceEditor.languageDefinition.update(crossReferenceString = CrossReferenceString(""))
                }

                EventStatus.SUCCESS -> {
                    try {
                        //sentenceScopeModel = event.asm as ScopeModel?
                        logger.logDebug { "Debug: CrossReferences SyntaxAnalysis success, resetting scopes and references" }
                        sentenceEditor.languageDefinition.update(crossReferenceString = CrossReferenceString(referencesEditor.text))
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, t) { referencesEditor.endPointIdentity.editorId + ": " + t.message }
                        sentenceEditor.languageDefinition.update(crossReferenceString = CrossReferenceString(""))
                    }
                }
            }
        }
    }

    private fun loading(parse: Boolean, ast: Boolean) {
        //     trees["parse"]!!.loading = parse
        //    trees["ast"]!!.loading = ast
    }
    /*
        private fun connectTrees() {
            trees["typemodel"]!!.treeFunctions = TreeViewFunctions<dynamic>(
                label = { root, it ->
                    when (it) {
                        is String -> it
                        is List<*> -> "List"
                        is TypeModel -> "model ${it.name}"
                        is Map.Entry<String, TypeUsage> -> {
                            val ruleName = it.key
                            val type = it.value.type
                            when (type) {
                                is ElementType -> when {
                                    type.supertypes.isEmpty() -> "$ruleName : ${type.signature(type.typeModel)}"
                                    else -> "$ruleName : ${type.signature(type.typeModel)} -> ${type.supertypes.joinToString { it.signature(type.typeModel) }}"
                                }

                                else -> "$ruleName : ${type.signature(root as TypeModel?)}"
                            }

                        }

                        is PropertyDeclaration -> "${it.name} : ${it.typeUse.signature(root as TypeModel, 0)}"
                        else -> error("Internal Error: type ${it::class.simpleName} not handled")
                    }
                },
                hasChildren = {
                    when (it) {
                        is String -> false
                        is List<*> -> true
                        is TypeModel -> it.allRuleNameToType.isNotEmpty()
                        is Map.Entry<String, TypeUsage> -> {
                            val type = it.value.type
                            when (type) {
                                is AnyType -> false
                                is ListSimpleType -> false
                                is ListSeparatedType -> false
                                is NothingType -> false
                                is PrimitiveType -> false
                                is UnnamedSuperTypeType -> false
                                is StructuredRuleType -> when (type) {
                                    is TupleType -> type.property.isNotEmpty()
                                    is ElementType -> type.property.isNotEmpty()
                                }
                            }
                        }

                        is PropertyDeclaration -> false
                        else -> error("Internal Error: type ${it::class.simpleName} not handled")
                    }
                },
                children = {
                    when (it) {
                        is String -> emptyArray<Any>()
                        is List<*> -> it.toArray()
                        is TypeModel -> it.allRuleNameToType.entries.toTypedArray()
                        is Map.Entry<String, TypeUsage> -> {
                            val type = it.value.type
                            when (type) {
                                is AnyType -> emptyArray<Any>()
                                is ListSimpleType -> emptyArray<Any>()
                                is ListSeparatedType -> emptyArray<Any>()
                                is NothingType -> emptyArray<Any>()
                                is PrimitiveType -> emptyArray<Any>()
                                is UnnamedSuperTypeType -> emptyArray<Any>()
                                is StructuredRuleType -> when (type) {
                                    is TupleType -> type.property.values.toTypedArray()
                                    is ElementType -> type.property.values.toTypedArray()
                                }
                            }
                        }

                        is PropertyDeclaration -> emptyArray<Any>()
                        else -> error("Internal Error: type ${it::class.simpleName} not handled")
                    }
                }
            )
            grammarEditor.onSemanticAnalysis { event ->
                when (event.status) {
                    EventStatus.START -> trees["typemodel"]!!.loading = true
                    EventStatus.FAILURE -> trees["typemodel"]!!.loading = false
                    EventStatus.SUCCESS -> {
                        val typemodels = (event.asm as List<Grammar>).map {
                            TypeModelFromGrammar.createFrom(it)
                        }
                        trees["typemodel"]!!.loading = false
                        trees["typemodel"]!!.setRoots(typemodels)
                    }
                }
            }

            trees["parse"]!!.treeFunctions = TreeViewFunctions<dynamic>(
                label = { root, it ->
                    when (it.isBranch) {
                        false -> "${it.name} = ${it.nonSkipMatchedText}"
                        true -> it.name
                        else -> error("error")
                    }
                },
                hasChildren = { it.isBranch },
                children = { it.children }
            )

            sentenceEditor.onParse { event ->
                when (event.status) {
                    EventStatus.START -> loading(true, true)
                    EventStatus.FAILURE -> loading(false, false)
                    EventStatus.SUCCESS -> {
                        trees["parse"]!!.loading = false
                        trees["parse"]!!.setRoots(event.tree?.let { listOf(it) } ?: emptyList())
                    }
                }
            }

            trees["ast"]!!.treeFunctions = TreeViewFunctions<dynamic>(
                label = { root, it ->
                    when {
                        it is Array<*> -> ": Array"
                        it is List<*> -> ": List"
                        it is Set<*> -> ": Set"
                        it is AsmElementSimple -> ": " + it.typeName
                        it is AsmElementProperty -> {
                            val v = it.value
                            when {
                                null == v -> "${it.name} = null"
                                v is Array<*> -> "${it.name} : Array"
                                v is List<*> -> "${it.name} : List"
                                v is Set<*> -> "${it.name} : Set"
                                v is AsmElementSimple -> "${it.name} : ${v.typeName}"
                                v is AsmElementReference -> when (v.value) {
                                    null -> "&'${v.reference}' = <unresolved reference>"
                                    else -> "&'${v.reference}' = ${v.value?.asmPath?.value} : ${v.value?.typeName}"
                                }

                                it.name == "'${v}'" -> "${it.name}"
                                v is String -> "${it.name} = '${v}'"
                                else -> "${it.name} = ${v}"
                            }
                        }

                        else -> it.toString()
                    }
                },
                hasChildren = {
                    when {
                        it is Array<*> -> true
                        it is Collection<*> -> true
                        it is AsmElementSimple -> it.properties.size != 0
                        it is AsmElementProperty -> {
                            val v = it.value
                            when {
                                null == v -> false
                                v is Array<*> -> true
                                v is Collection<*> -> true
                                v is AsmElementSimple -> true
                                else -> false
                            }
                        }

                        else -> false
                    }
                },
                children = {
                    when {
                        it is Array<*> -> it
                        it is Collection<*> -> it.toTypedArray()
                        it is AsmElementSimple -> it.properties.values.toTypedArray()
                        it is AsmElementProperty -> {
                            val v = it.value
                            when {
                                v is Array<*> -> v
                                v is Collection<*> -> v.toTypedArray()
                                v is AsmElementSimple -> v.properties.values.toTypedArray()
                                else -> emptyArray<dynamic>()
                            }
                        }

                        else -> emptyArray<dynamic>()
                    }
                }
            )

            sentenceEditor.onSyntaxAnalysis { event ->
                when (event.status) {
                    EventStatus.START -> {
                        //trees["ast"]!!.loading = true
                    }

                    EventStatus.FAILURE -> {//Failure
                        console.error(event.message)
                        trees["ast"]!!.loading = false
                        when (event.asm) {
                            is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                            else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                        }
                    }

                    EventStatus.SUCCESS -> {
                        trees["ast"]!!.loading = false
                        when (event.asm) {
                            is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                            else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                        }
                    }
                }
            }

            sentenceEditor.onSemanticAnalysis { event ->
                when (event.status) {
                    EventStatus.START -> {
                        //trees["ast"]!!.loading = true
                    }

                    EventStatus.FAILURE -> {//Failure
                        console.error(event.message)
                        trees["ast"]!!.loading = false
                        //when(event.asm) {
                        //    is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                        //    else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                        //}
                    }

                    EventStatus.SUCCESS -> {
                        trees["ast"]!!.loading = false
                        when (event.asm) {
                            is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                            else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                        }
                    }
                }
            }
        }

        private fun configExampleSelector() {
            exampleSelect.addEventListener("change", { _ ->
                loading(true, true)
                val egName = js("event.target.value") as String
                val eg = Examples[egName]
                grammarEditor.text = eg.grammar
                styleEditor.text = eg.style
                referencesEditor.text = eg.references
                //formatEditor.text = eg.format
                sentenceEditor.text = ""
                sentenceEditor.sentenceContext = ContextSimple()
                sentenceEditor.languageDefinition.styleStr = styleEditor.text
                sentenceEditor.languageDefinition.scopeModelStr = referencesEditor.text
                sentenceEditor.languageDefinition.grammarStr = grammarEditor.text
                sentenceEditor.text = eg.sentence
            })

            // select initial example
            loading(true, true)
            val eg = Datatypes.example
            (exampleSelect as HTMLSelectElement).value = eg.id
            grammarEditor.text = eg.grammar
            styleEditor.text = eg.style
            referencesEditor.text = eg.references
            //formatEditor.text = eg.format
            sentenceEditor.sentenceContext = ContextSimple()
            sentenceEditor.languageDefinition.styleStr = styleEditor.text
            sentenceEditor.languageDefinition.scopeModelStr = referencesEditor.text
            sentenceEditor.languageDefinition.grammarStr = grammarEditor.text
            sentenceEditor.text = eg.sentence
        }
    */
}


