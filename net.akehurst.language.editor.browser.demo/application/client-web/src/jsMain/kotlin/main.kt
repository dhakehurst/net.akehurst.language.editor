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

import ace.IRange
import codemirror.view.EditorViewConfig
import korlibs.io.async.asyncImmediately
import korlibs.io.file.std.localVfs
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import monaco.IDisposable
import monaco.editor.IMarkerData
import monaco.editor.IStandaloneEditorConstructionOptions
import monaco.editor.IStandaloneThemeData
import monaco.editor.ITextModel
import monaco.languages.CompletionItemProvider
import monaco.languages.ILanguageExtensionPoint
import monaco.languages.TokensProvider
import net.akehurst.kotlin.html5.create
import net.akehurst.language.agl.default.TypeModelFromGrammar
import net.akehurst.language.agl.language.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.language.grammar.ContextFromGrammar
import net.akehurst.language.agl.language.grammar.ContextFromGrammarRegistry
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference
import net.akehurst.language.agl.semanticAnalyser.ContextSimple
import net.akehurst.language.api.asm.*
import net.akehurst.language.api.grammarTypeModel.GrammarTypeNamespace
import net.akehurst.language.api.language.grammar.Grammar
import net.akehurst.language.api.language.reference.CrossReferenceModel
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.EventStatus
import net.akehurst.language.editor.api.LogFunction
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.browser.ace.IAce
import net.akehurst.language.editor.browser.ace.attachToAce
import net.akehurst.language.editor.browser.agl.attachToAglEditor
import net.akehurst.language.editor.browser.codemirror.attachToCodeMirror
import net.akehurst.language.editor.browser.demo.BuildConfig
import net.akehurst.language.editor.browser.monaco.Monaco
import net.akehurst.language.editor.browser.monaco.attachToMonaco
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.common.objectJSTyped
import net.akehurst.language.editor.information.Example
import net.akehurst.language.editor.information.Examples
import net.akehurst.language.editor.information.ExternalContextLanguage
import net.akehurst.language.editor.information.examples.AglGrammar
import net.akehurst.language.editor.information.examples.AglStyle
import net.akehurst.language.editor.information.examples.BasicTutorial
import net.akehurst.language.editor.technology.gui.widgets.TabView
import net.akehurst.language.editor.technology.gui.widgets.TreeView
import net.akehurst.language.editor.technology.gui.widgets.TreeViewFunctions
import net.akehurst.language.typemodel.api.*
import org.w3c.dom.*

external var aglScriptBasePath: String = definedExternally
external var resourcesPath: String = definedExternally
val workerScriptName = "${aglScriptBasePath}/application-agl-editor-worker.js"
var demo: Demo? = null

enum class AlternativeEditors {
    ACE, MONACO, CODEMIRROR, AGL
}

object Constants {
    val initialLogLevel = LogLevel.All

    const val sentenceEditorId = "editor-sentence"
    const val grammarEditorId = "editor-grammar"
    const val referencesEditorId = "editor-references"
    const val styleEditorId = "editor-style"
    const val formatEditorId = "editor-format"

    const val sentenceLanguageId = "language-user"
    val grammarLanguageId = Agl.registry.agl.grammarLanguageIdentity
    val referencesLanguageId = Agl.registry.agl.crossReferenceLanguageIdentity
    val styleLanguageId = Agl.registry.agl.styleLanguageIdentity
    val formatLanguageId = Agl.registry.agl.formatLanguageIdentity
}

interface DemoInterface {

    val logLevel: LogLevel

    fun changeLoggingLevel(level: LogLevel)
    fun changeEditor()
    fun analyseGrammar()
}

fun main() {
    try {
        val logger = DemoLogger(Constants.initialLogLevel)

        //define this before editors are created
        // need to register this so that we can set the configuration and get the default completion-provider
        Agl.registry.register(
            identity = Constants.sentenceLanguageId,
            grammarStr = "",
            buildForDefaultGoal = false,
            aglOptions = Agl.options {
                semanticAnalysis {
                    context(ContextFromGrammarRegistry(Agl.registry))
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            configuration = Agl.configurationDefault()
        )

        val demoIf = object : DemoInterface {
            override val logLevel: LogLevel get() = logger.level
            override fun changeLoggingLevel(level: LogLevel) {
                logger.level = level
            }

            override fun changeEditor() {
                TODO("not implemented")
            }

            override fun analyseGrammar() {
                TODO("not implemented")
            }

        }
        createBaseDom("div#agl-demo", demoIf)

//        val loggingLevel = document.querySelector("#agl-demo-logging-level")!! as HTMLSelectElement
//        loggingLevel.addEventListener("change", {
//            val optionStr = loggingLevel.value
//            val ll = LogLevel.valueOf(optionStr)
//            logger.level = ll
//        })

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

fun createBaseDom(appDivSelector: String, demo: DemoInterface) {
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
                                p { content = "Ace version ${BuildConfig.versionEditorAce}, Licence BSD" }
                                p { content = "Monaco version ${BuildConfig.versionEditorMonaco}, Licence MIT" }
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
                                    class_.add("typemodel")
                                    header {
                                        h3 { content = "Type Model" }
                                    }
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
                    div {
                        article {
                            header { h3 { content = "Demo" } }
                            section {
                                class_.add("configuration")
                                div {
                                    //class_.add("agl-options-editor")
                                    label { content = "Select Logging Level: " }
                                    select {
                                        attribute.id = "agl-demo-logging-level"
                                        option(value = LogLevel.None.name, selected = demo.logLevel == LogLevel.None) { content = LogLevel.None.name }
                                        option(value = LogLevel.Fatal.name, selected = demo.logLevel == LogLevel.Fatal) { content = LogLevel.Fatal.name }
                                        option(value = LogLevel.Error.name, selected = demo.logLevel == LogLevel.Error) { content = LogLevel.Error.name }
                                        option(value = LogLevel.Warning.name, selected = demo.logLevel == LogLevel.Warning) { content = LogLevel.Warning.name }
                                        option(value = LogLevel.Information.name, selected = demo.logLevel == LogLevel.Information) { content = LogLevel.Information.name }
                                        option(value = LogLevel.Debug.name, selected = demo.logLevel == LogLevel.Debug) { content = LogLevel.Debug.name }
                                        option(value = LogLevel.Trace.name, selected = demo.logLevel == LogLevel.Trace) { content = LogLevel.Trace.name }
                                        option(value = LogLevel.All.name, selected = demo.logLevel == LogLevel.All) { content = LogLevel.All.name }
                                    }.also { select ->
                                        (select as HTMLSelectElement).addEventListener("change", {
                                            val optionStr = select.value
                                            val ll = LogLevel.valueOf(optionStr)
                                            demo.changeLoggingLevel(ll)
                                        })
                                    }
                                }
                                div {
                                    //class_.add("agl-options-editor")
                                    label { content = "Select underlying Editor Type: " }
                                    select {
                                        attribute.id = "agl-options-editor"
                                        //attribute.name = "editor-choice"
                                        option(value = AlternativeEditors.ACE.name, selected = true) { content = "Ace" }
                                        //                                option(value = AlternativeEditors.MONACO.name) { content = "Monaco" }
                                        //option(value = AlternativeEditors.CODEMIRROR.name) { content = "CodeMirror" }
                                        //                                option(value = AlternativeEditors.AGL.name, selected = true) { content = "Minimal (Agl)" }
                                    }
                                }
                            }
                        }
                        /*
                        article {
                            header { h3 { content = "Parse Options" } }
                            section {
                                div {
                                    label { content = "Goal rule name: " }
                                    this.input()
                                }
                            }
                        }
                        article {
                            header { h3 { content = "Syntax Analysis Options" } }
                            section {

                            }
                        }
                        article {
                            header { h3 { content = "Semantic Analysis Options" } }
                            section {
                                div {
                                    label { content = "Check references: " }
                                    this.checkbox()
                                }
                                div {
                                    label { content = "Resolve references: " }
                                    this.checkbox()
                                }
                            }
                        }
                         */
                    }
                }
            }
        }
    }
}

fun initialiseExamples() {
    Examples.add(BasicTutorial.example)  // first example
    CoroutineScope(SupervisorJob()).asyncImmediately {
        val resources = localVfs(resourcesPath).jail()
        console.log("Reading examples from ${resources["examples"].path}")
        val names = resources["examples"].listNames()
        console.log("Found examples: $names")
        names.forEach {
            Examples.read(resources, it)
        }
    }.invokeOnCompletion {
        val exampleSelect = document.querySelector("select#example") as HTMLElement
        Examples.add(AglStyle.example)
        Examples.add(AglGrammar.example)
        Examples.map.forEach { eg ->
            val option = document.createElement("option")
            exampleSelect.appendChild(option);
            option.setAttribute("value", eg.value.id);
            option.textContent = eg.value.label;
        }

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
        val logFunction: LogFunction = { lvl, msg, t -> logger.log(lvl, msg, t) }
        val ed = when (editorChoice) {
            AlternativeEditors.ACE -> createAce(element, logFunction)
            AlternativeEditors.MONACO -> createMonaco(element, logFunction)
            AlternativeEditors.CODEMIRROR -> createCodeMirror(element, logFunction)
            AlternativeEditors.AGL -> createAgl(element, logFunction)
        }
        Pair(element.id, ed)// (editorId)
    }

    demo = Demo(editors, logger)
    demo!!.configure()
}

fun createAce(editorElement: Element, logFunction: LogFunction): AglEditor<Any, Any> {

    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!
    val ed: ace.Editor = ace.Editor(
        ace.VirtualRenderer(editorElement, null),
        ace.Ace.createEditSession(""),
        objectJS {}
    )
    val aceOptions = objectJS {
        editor = objectJS {
            enableBasicAutocompletion = true
            enableSnippets = true
//            enableLiveAutocompletion = false
        }
        renderer = {

        }
    }
    //ed.commands.addCommand(ace.ext.Autocomplete.startCommand)
    ed.setOptions(aceOptions.editor)
    ed.renderer.setOptions(aceOptions.renderer)
    val worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))

    val ace = object : IAce {
        override fun createRange(startRow: Int, startColumn: Int, endRow: Int, endColumn: Int): IRange {
            return ace.Range(startRow, startColumn, endRow, endColumn)
        }
    }
    return Agl.attachToAce(editorElement, ed, languageId, editorId, logFunction, worker, ace)
}

fun createMonaco(editorElement: Element, logFunction: LogFunction): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!
    val editorOptions = objectJSTyped<IStandaloneEditorConstructionOptions> {
        value = ""
        wordBasedSuggestions = false
    }
    val ed = monaco.editor.create(editorElement, editorOptions, null)
    val worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))

    val monaco = object : Monaco {
        override fun defineTheme(themeName: String, themeData: IStandaloneThemeData) = monaco.editor.defineTheme(themeName, themeData)
        override fun setModelMarkers(model: ITextModel, owner: String, markers: Array<IMarkerData>) = monaco.editor.setModelMarkers(model, owner, markers)
        override fun setModelLanguage(model: ITextModel, languageId: String) = monaco.editor.setModelLanguage(model, languageId)
        override fun setTheme(themeName: String) = monaco.editor.setTheme(themeName)

        override fun register(language: ILanguageExtensionPoint) = monaco.languages.register(language)
        override fun setTokensProvider(languageId: String, provider: TokensProvider): IDisposable = monaco.languages.setTokensProvider(languageId, provider)
        override fun registerCompletionItemProvider(languageId: String, provider: CompletionItemProvider): IDisposable = monaco.languages.registerCompletionItemProvider(languageId, provider)
    }
    return Agl.attachToMonaco(editorElement, ed, languageId, editorId, logFunction, worker, monaco)
}

fun createCodeMirror(editorElement: Element, logFunction: LogFunction): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!
    val editorOptions = objectJSTyped<EditorViewConfig> {
        doc = "hello"
        //extensions= [keymap.of(defaultKeymap)],
        parent = editorElement
    }
    val ed = codemirror.view.EditorView(editorOptions)
    val worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
    return Agl.attachToCodeMirror(editorElement, ed, languageId, editorId, logFunction, worker)
}

fun createAgl(editorElement: Element, logFunction: LogFunction): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = editorElement.getAttribute("agl-language")!!

    val worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
    return Agl.attachToAglEditor(editorElement, languageId, editorId, logFunction, worker)
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
    val sentenceEditor = editors[Constants.sentenceEditorId]!! as AglEditor<Asm, ContextSimple>
    val grammarEditor = editors[Constants.grammarEditorId]!!
    val styleEditor = editors[Constants.styleEditorId]!! as AglEditor<AglStyleModel, ContextFromGrammar>
    val referencesEditor = editors[Constants.referencesEditorId]!! as AglEditor<CrossReferenceModel, ContextFromTypeModelReference>
    //val formatEditor = editors["language-format"]!!

    fun configure() {
        this.connectEditors()
        this.connectTrees()
        this.configExampleSelector()
    }

    private fun connectEditors() {
        //ids should already be set when dom and editors are created
        grammarEditor.languageIdentity = Constants.grammarLanguageId
        grammarEditor.options.semanticAnalysis.context = null // ensure this is null, so that Worker uses default of ContextFromGrammarRegistry
        styleEditor.languageIdentity = Constants.styleLanguageId
        referencesEditor.languageIdentity = Constants.referencesLanguageId
        //Agl.registry.unregister(Constants.sentenceLanguageId)
        sentenceEditor.languageIdentity = Constants.sentenceLanguageId
        grammarEditor.editorSpecificStyleStr = Agl.registry.agl.grammar.styleStr
        styleEditor.editorSpecificStyleStr = Agl.registry.agl.style.styleStr
        referencesEditor.editorSpecificStyleStr = Agl.registry.agl.crossReference.styleStr

        grammarEditor.onSemanticAnalysis { event ->
            when (event.status) {
                EventStatus.START -> Unit

                EventStatus.FAILURE -> {
                    styleEditor.options.semanticAnalysis.context?.clear()
                    //referencesEditor.sentenceContext?.clear()
                    logger.logError(grammarEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.grammarStr = ""
                }

                EventStatus.SUCCESS -> {
                    logger.logDebug("Send grammarStr Semantic Analysis success")
                    val grammars = event.asm as List<Grammar>? ?: error("should always be a List<Grammar> if success")
                    styleEditor.options.semanticAnalysis.context = ContextFromGrammar.createContextFrom(grammars)
                    referencesEditor.options.semanticAnalysis.context = ContextFromTypeModelReference(sentenceEditor.languageIdentity)
                    try {
                        if (doUpdate) {
                            logger.logDebug("Send set sentenceEditor grammarStr")
                            sentenceEditor.languageDefinition.grammarStr = grammarEditor.text
                        }
                    } catch (t: Throwable) {
                        logger.log(LogLevel.Error, grammarEditor.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.grammarStr = ""
                    }
                }
            }

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
                            logger.logDebug("Setting cross-reference model for sentenceEditor")
                            sentenceEditor.languageDefinition.crossReferenceModelStr = referencesEditor.text
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
                    is TypeModel -> "model ${it.name}"
                    is GrammarTypeNamespace -> "namespace ${it.qualifiedName}"
                    is TypeNamespace -> "namespace ${it.qualifiedName}"
                    is Pair<String, TypeInstance> -> {
                        val type = it.second.declaration
                        val ruleName = it.first
                        when (type) {
                            is DataType -> when {
                                type.supertypes.isEmpty() -> "$ruleName : ${type.signature(type.namespace)}"
                                else -> "$ruleName : ${type.signature(type.namespace)} -> ${type.supertypes.joinToString { it.signature(type.namespace, 0) }}"
                            }

                            else -> "$ruleName : ${type.signature(it.second.namespace)}"
                        }
                    }

                    is Map.Entry<String, TypeDeclaration> -> {
                        val type = it.value
                        val typeName = it.key
                        when (type) {
                            is DataType -> when {
                                type.supertypes.isEmpty() -> "$typeName : ${type.signature(type.namespace)}"
                                else -> "$typeName : ${type.signature(type.namespace)} -> ${type.supertypes.joinToString { it.signature(type.namespace, 0) }}"
                            }

                            else -> "$typeName : ${type.signature(type.namespace)}"
                        }
                    }

                    is PropertyDeclaration -> "${it.name} : ${it.typeInstance.signature(it.owner.namespace, 0)}"
                    else -> error("Internal Error: type ${it::class.simpleName} not handled")
                }
            },
            hasChildren = {
                when (it) {
                    is String -> false
                    is List<*> -> true
                    is TypeModel -> it.namespace.isNotEmpty()
                    is GrammarTypeNamespace -> it.allTypesByRuleName.isNotEmpty()
                    is TypeNamespace -> it.ownedTypesByName.isNotEmpty()
                    is Pair<String, TypeInstance> -> {
                        val type = it.second.declaration
                        when (type) {
                            is TupleType -> type.property.isNotEmpty()
                            is DataType -> type.property.isNotEmpty()
                            else -> false
                        }
                    }

                    is Map.Entry<String, TypeDeclaration> -> when (it.value) {
                        is StructuredType -> (it.value as StructuredType).property.isNotEmpty()
                        else -> false
                    }

                    is PropertyDeclaration -> false
                    else -> error("Internal Error: type ${it::class.simpleName} not handled")
                }
            },
            children = {
                when (it) {
                    is String -> emptyArray<Any>()
                    is List<*> -> it.toArray()
                    is TypeModel -> it.allNamespace.toTypedArray()
                    is GrammarTypeNamespace -> it.allTypesByRuleName.toTypedArray()
                    is TypeNamespace -> it.ownedTypesByName.entries.toTypedArray()
                    is Pair<String, TypeInstance> -> {
                        val type = it.second.declaration
                        when (type) {
                            is StructuredType -> type.property.toTypedArray()
                            else -> emptyArray<Any>()
                        }
                    }

                    is Map.Entry<String, TypeDeclaration> -> when (it.value) {
                        is StructuredType -> (it.value as StructuredType).property.toTypedArray()
                        else -> emptyArray<Any>()
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
                        TypeModelFromGrammar.create(it)
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
                when (it) {
                    is Array<*> -> ": Array"
                    is List<*> -> ": List"
                    is Set<*> -> ": Set"
                    is AsmNothing -> "Nothing"
                    is AsmPrimitive -> "${it.value}"
                    is AsmList -> ": List"
                    is AsmListSeparated -> ": ListSeparated"
                    is AsmStructure -> ": " + it.typeName
                    is AsmStructureProperty -> {
                        val v = it.value
                        when (v) {
                            is AsmNothing -> "${it.name} = Nothing"
                            is AsmPrimitive -> "${it.name} = '${v.value}'"
                            is AsmList -> "${it.name} : List"
                            is AsmListSeparated -> "${it.name} : ListSeparated"
                            is AsmStructure -> "${it.name} : ${v.typeName}"
                            is AsmReference -> when (v.value) {
                                null -> "${it.name} = &'${v.reference}' - <unresolved reference>"
                                else -> "${it.name} = &'${v.reference}' : ${v.value?.typeName} - ${v.value?.path?.value}"
                            }
                            //it.name == "'${v}'" -> "${it.name}"
                            else -> "${it.name} = ${v}"
                        }
                    }

                    else -> it.toString()
                }
            },
            hasChildren = {
                when (it) {
                    is Array<*> -> true
                    is Collection<*> -> true
                    is AsmList -> true
                    is AsmListSeparated -> true
                    is AsmStructure -> it.property.isNotEmpty()
                    is AsmStructureProperty -> {
                        when (it.value) {
                            is AsmList -> true
                            is AsmListSeparated -> true
                            is AsmStructure -> true
                            else -> false
                        }
                    }

                    else -> false
                }
            },
            children = {
                when (it) {
                    is AsmList -> it.elements.toTypedArray()
                    is AsmListSeparated -> it.elements.toTypedArray()
                    is AsmStructure -> it.property.values.toTypedArray()
                    is AsmStructureProperty -> {
                        when (val v = it.value) {
                            is Array<*> -> v
                            is Collection<*> -> v.toTypedArray()
                            is AsmList -> v.elements.toTypedArray()
                            is AsmListSeparated -> v.elements.toTypedArray()
                            is AsmStructure -> v.property.values.toTypedArray()
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
                        is Asm -> trees["ast"]!!.setRoots((event.asm as Asm).root)
                        else -> trees["ast"]!!.setRoots(listOf("<Unknown>"))
                    }
                }

                EventStatus.SUCCESS -> {
                    loading(null, false)
                    when (event.asm) {
                        is Asm -> trees["ast"]!!.setRoots((event.asm as Asm).root)
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
                        is Asm -> trees["ast"]!!.setRoots((event.asm as Asm).root)
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
        val eg = BasicTutorial.example// Examples.map["BasicTutorial"]!!
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
        sentenceEditor.options.semanticAnalysis.context = ExternalContextLanguage.processor.process(eg.context).asm
        logger.log(LogLevel.Trace, "Update sentenceEditor with grammar, refs, style", null)
        sentenceEditor.languageDefinition.update(grammarEditor.text, referencesEditor.text, styleEditor.text)
        sentenceEditor.text = eg.sentence
        sentenceEditor.doUpdate = true
        this.doUpdate = true
        logger.log(LogLevel.Information, "Finished setting example", null)
    }

    fun finalize() {
        editors.values.forEach {
            it.destroy()
        }
    }
}


