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

package net.akehurst.language.editor.application.client.web

import codemirror.view.EditorViewConfig
import kotlinx.browser.document
import kotlinx.browser.window
import monaco.editor.IStandaloneEditorConstructionOptions
import net.akehurst.kotlin.html5.create
import net.akehurst.language.agl.grammar.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.grammar.grammar.ContextFromGrammar
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.agl.syntaxAnalyser.TypeModelFromGrammar
import net.akehurst.language.api.analyser.ScopeModel
import net.akehurst.language.api.asm.*
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.api.grammarTypeModel.GrammarTypeModel
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.EventStatus
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.browser.ace.attachToAce
import net.akehurst.language.editor.browser.codemirror.attachToCodeMirror
import net.akehurst.language.editor.browser.monaco.attachToMonaco
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.common.objectJSTyped
import net.akehurst.language.editor.demo.BuildConfig
import net.akehurst.language.editor.information.Example
import net.akehurst.language.editor.information.Examples
import net.akehurst.language.editor.information.examples.*
import net.akehurst.language.editor.technology.gui.widgets.TabView
import net.akehurst.language.editor.technology.gui.widgets.TreeView
import net.akehurst.language.editor.technology.gui.widgets.TreeViewFunctions
import net.akehurst.language.typemodel.api.*
import org.w3c.dom.*

external var aglScriptBasePath: dynamic = definedExternally
val workerScriptName = "${aglScriptBasePath}/application-agl-editor-worker.js"
var demo: Demo? = null

enum class AlternativeEditors {
    ACE, MONACO, CODEMIRROR
}

object Constants {
    const val sentenceEditorId = "editor-sentence"
    const val grammarEditorId = "editor-grammar"
    const val referencesEditorId = "editor-references"
    const val styleEditorId = "editor-style"
    const val formatEditorId = "editor-format"

    const val sentenceLanguageId = "language-user"
    val grammarLanguageId = Agl.registry.agl.grammarLanguageIdentity
    val referencesLanguageId = Agl.registry.agl.scopesLanguageIdentity
    val styleLanguageId = Agl.registry.agl.styleLanguageIdentity
    val formatLanguageId = Agl.registry.agl.formatLanguageIdentity
}

interface DemoActions {
    fun changeLoggingLevel()
    fun changeEditor()
    fun analyseGrammar()
}

fun main() {
    try {
        createBaseDom("div#agl-demo")
        val logger = DemoLogger(LogLevel.Information)

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

        TabView.initialise(document)
        initialiseExamples()

        createDemo(AlternativeEditors.ACE, logger)
    } catch (t: Throwable) {
        console.error(t)
    }
}

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
                            thisDialog.close()
                        }
                        article {
                            header { h2 { content = "About" } }
                            section {
                                p { content = "Ace version ${net.akehurst.language.editor.agl.editor.browser.ace.BuildConfig.versionEditorAce}, Licence BSD" }
                                p { content = "Monaco version ${net.akehurst.language.editor.agl.editor.browser.monaco.BuildConfig.versionEditorMonaco}, Licence MIT" }
                                p { content = "AGL version ${Agl.version}, Licence Apache 2.0" }
                                p { content = "Kotlin version ${kotlin.KotlinVersion.CURRENT}, Licence Apache 2.0" }
                                h2 { content = "Description" }
                                p {
                                    content = """
                                    Functions best using Chrome.
                                    This demo is a work in progress.
                                    Many features are still experimental.
                                    For ambiguous grammars, only one option is displayed in the ParseTree/AST.
                                    Feel free to modify any of the grammars, they are all stored in the browser and will reset when you reload the page.
                                    If something stops working, simply reload the page, and please let me know.
                                """.trimMargin()
                                }
                                p { content = "The 'Sentence' tab is for entering sentences in a language. The 'Language' tab is for defining the language." }
                                a {
                                    attribute.set("href", "https://levelup.gitconnected.com/a-kotlin-multi-platform-parser-usable-from-a-jvm-or-javascript-59e870832a79")
                                    this.content = "More documentation about the grammar syntax. (The author's personal blog post.)"
                                }
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
                                attribute.id = "ParseTree"
                                htmlElement("treeview") { attribute.id = "parse" }
                            }
                            htmlElement("tab") {
                                attribute.id = "AST"
                                htmlElement("treeview") { attribute.id = "ast" }
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
                                article {
                                /*  TODO:
                                       header {
                                        button {
                                            attribute.id = "buttonAnalyseGrammar"
                                            content="Analyse"
                                        }
                                        p { content="WARNING: this can take a long while" }
                                    }
                                    */
                                    section {
                                        class_.add("typemodel")
                                        h3 { content = "Type Model" }
                                        htmlElement("treeview") { attribute.id = "typemodel" }
                                    }
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
                                //option(value = AlternativeEditors.MONACO.name) { content = "Monaco" }
                                //option(value = AlternativeEditors.CODEMIRROR.name) { content = "CodeMirror" }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun initialiseExamples() {
    val exampleSelect = document.querySelector("select#example") as HTMLElement
    Examples.add(Datatypes.example)
    Examples.add(SQL.example)
    Examples.add(GraphvizDot.example)
    Examples.add(SText.example)
    Examples.add(Java8.example)
    //Examples.add(English.example)
    Examples.add(TraceabilityQuery.example)
    Examples.add(MScript.example)
    Examples.add(Xml.example)
    Examples.add(AglStyle.example)
    Examples.add(AglGrammar.example)
    Examples.add(SSS.example)
    Examples.add(BSc.example)
    Examples.add(Embedded.example)

    Examples.map.forEach { eg ->
        val option = document.createElement("option")
        exampleSelect.appendChild(option);
        option.setAttribute("value", eg.value.id);
        option.textContent = eg.value.label;
    }
}

fun createDemo(editorChoice: AlternativeEditors, logger: DemoLogger) {
    if (null != demo) {
        demo!!.finalize()
    }
    // kill shared worker to start
    val w = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
    w.port.close()

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

    demo = Demo(editors, logger)
    demo!!.configure()
}

fun createAce(editorElement: Element): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!
    val ed: ace.Editor = ace.Editor(
        ace.VirtualRenderer(editorElement, null),
        ace.Ace.createEditSession(""),
        objectJS { } //options are set later in init_
    )
    val aceOptions = objectJS {
        editor = objectJS {
            enableBasicAutocompletion = true
            enableSnippets = true
            enableLiveAutocompletion = false
        }
        renderer = {

        }
    }
    ed.setOptions(aceOptions.editor)
    ed.renderer.setOptions(aceOptions.renderer)
    return Agl.attachToAce(editorElement, ed, languageId, editorId, workerScriptName, true)
}

fun createMonaco(editorElement: Element): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!
    val editorOptions = objectJSTyped<IStandaloneEditorConstructionOptions> {
        value = ""
        wordBasedSuggestions = false
    }
    val ed = monaco.editor.create(editorElement, editorOptions, null)
    return Agl.attachToMonaco(editorElement, ed, languageId, editorId, workerScriptName, true)
}

fun createCodeMirror(editorElement: Element): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!
    val editorOptions = objectJSTyped<EditorViewConfig> {
        doc = "hello"
        //extensions= [keymap.of(defaultKeymap)],
        parent = editorElement
    }
    val ed = codemirror.view.EditorView(editorOptions)
    return Agl.attachToCodeMirror(editorElement, ed, languageId, editorId, workerScriptName, true)
}

//fun createFirepad(editorElement: Element): AglEditor<Any, Any> {
//    val id = editorElement.id
//    return Agl.attachToCodeMirror(ed, id, id, workerScriptName, true)
//}

class Demo(
    val editors: Map<String, AglEditor<*, *>>,
    val logger: DemoLogger
) {
    var doUpdate = true
    val trees = TreeView.initialise(document)

    val exampleSelect = document.querySelector("select#example") as HTMLElement
    val sentenceEditor = editors[Constants.sentenceEditorId]!! as AglEditor<AsmSimple, ContextSimple>
    val grammarEditor = editors[Constants.grammarEditorId]!!
    val styleEditor = editors[Constants.styleEditorId]!! as AglEditor<AglStyleModel, ContextFromGrammar>
    val referencesEditor = editors[Constants.referencesEditorId]!! as AglEditor<ScopeModel, ContextFromTypeModel>
    //val formatEditor = editors["language-format"]!!

    fun configure() {
        this.connectEditors()
        this.connectTrees()
        this.configExampleSelector()
    }

    private fun connectEditors() {
        grammarEditor.languageIdentity = Agl.registry.agl.grammarLanguageIdentity
        styleEditor.languageIdentity = Agl.registry.agl.styleLanguageIdentity
        referencesEditor.languageIdentity = Agl.registry.agl.scopesLanguageIdentity
        Agl.registry.unregister(Constants.sentenceLanguageId)
        sentenceEditor.languageIdentity = Agl.registry.register(
            identity = Constants.sentenceLanguageId,
            grammarStr = "",
            buildForDefaultGoal = false,
            aglOptions = Agl.options {
                semanticAnalysis {
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            configuration = Agl.configurationDefault()
        ).identity

        grammarEditor.editorSpecificStyleStr = Agl.registry.agl.grammar.styleStr
        styleEditor.editorSpecificStyleStr = Agl.registry.agl.style.styleStr
        referencesEditor.editorSpecificStyleStr = Agl.registry.agl.scopes.styleStr

        val styleContext = ContextFromGrammar()
        val scopeContext = ContextFromTypeModel()

        grammarEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit

                EventStatus.FAILURE -> {
                    styleContext.clear()
                    scopeContext.clear()

                    logger.logError(grammarEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.grammarStr = ""
                }

                EventStatus.SUCCESS -> {
                    styleContext.clear()
                    scopeContext.clear()
                    val grammars = event.asm as List<Grammar>? ?: error("should always be a List<Grammar> if success")
                    styleContext.createScopeFrom(grammars)
                    scopeContext.createScopeFrom(TypeModelFromGrammar.createFrom(grammars.first()))
                    try {
                        logger.logDebug(" Grammar parse success")
                        if (doUpdate) {
                            logger.logDebug("resetting sentence processor")
                            sentenceEditor.languageDefinition.grammarStr = grammarEditor.text
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, grammarEditor.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.grammarStr = ""
                    }
                }
            }
            referencesEditor.sentenceContext = scopeContext
            styleEditor.sentenceContext = styleContext
        }
        styleEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.FAILURE -> {
                    logger.logError(styleEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.styleStr = ""
                }

                EventStatus.SUCCESS -> {
                    try {
                        logger.logDebug("Style parse success")
                        if (doUpdate) {
                            logger.logDebug("resetting sentence style")
                            sentenceEditor.languageDefinition.styleStr = styleEditor.text
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, styleEditor.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.styleStr = ""
                    }
                }
            }
        }
        referencesEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit
                EventStatus.FAILURE -> {
                    logger.logError(referencesEditor.editorId + ": " + event.message)
                }

                EventStatus.SUCCESS -> {
                    try {
                        //sentenceScopeModel = event.asm as ScopeModel?
                        logger.logDebug("CrossReferences SyntaxAnalysis success")
                        if (doUpdate) {
                            logger.logDebug("resetting scopes and references")
                            sentenceEditor.languageDefinition.scopeModelStr = referencesEditor.text
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, referencesEditor.editorId + ": " + t.message, t)
                    }
                }
            }
        }
    }

    private fun loading(parse: Boolean?, ast: Boolean?) {
        if (null != parse) trees["parse"]!!.loading = parse
        if (null != ast) trees["ast"]!!.loading = ast
    }

    private fun connectTrees() {
        trees["typemodel"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = { root, it ->
                when (it) {
                    is String -> it
                    is List<*> -> "List"
                    is GrammarTypeModel -> "model ${it.name}"
                    is Pair<String, TypeUsage> -> {
                        val type = it.second.type
                        val ruleName = it.first
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
                    is GrammarTypeModel -> it.allTypesByRuleName.isNotEmpty()
                    is Pair<String, TypeUsage> -> {
                        val type = it.second.type
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
                    is GrammarTypeModel -> it.allTypesByRuleName.toTypedArray()
                    is Pair<String, TypeUsage> -> {
                        val type = it.second.type
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
                    loading(false, null)
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
                    logger.logError(event.message)
                    loading(null, false)
                    when (event.asm) {
                        is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                        else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    }
                }

                EventStatus.SUCCESS -> {
                    loading(null, false)
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
                    logger.logError(event.message)
                    loading(null, false)
                    //when(event.asm) {
                    //    is AsmSimple -> trees["ast"]!!.setRoots((event.asm as AsmSimple).rootElements)
                    //    else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    //}
                }

                EventStatus.SUCCESS -> {
                    loading(null, false)
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
            // delay setting stuff so that 'loading' is processed first
            val egName = js("event.target.value") as String
            val eg = Examples[egName]
//            window.setTimeout({ setExample(eg) }, 100)
            setExample(eg)
        })

        // select initial example
        loading(true, true)
        val eg = Datatypes.example
        (exampleSelect as HTMLSelectElement).value = eg.id
        setExample(eg)
    }

    fun setExample(eg: Example) {
        this.doUpdate = false
        grammarEditor.text = eg.grammar
        styleEditor.text = eg.style
        referencesEditor.text = eg.references
        //formatEditor.text = eg.format
        sentenceEditor.doUpdate = false
        sentenceEditor.sentenceContext = ContextSimple()
        sentenceEditor.languageDefinition.update(grammarEditor.text, referencesEditor.text, styleEditor.text)
        sentenceEditor.text = eg.sentence
        sentenceEditor.doUpdate = true
        this.doUpdate = true
    }

    fun finalize() {
        editors.values.forEach {
            it.destroy()
        }
    }
}


