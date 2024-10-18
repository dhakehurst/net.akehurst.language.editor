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

import ResizeObserver
import ace.IRange
import korlibs.io.async.asyncImmediately
import korlibs.io.file.std.localVfs
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import monaco.IDisposable
import monaco.IPosition
import monaco.editor.IMarkerData
import monaco.editor.IStandaloneEditorConstructionOptions
import monaco.editor.IStandaloneThemeData
import monaco.editor.ITextModel
import monaco.languages.CompletionItemProvider
import monaco.languages.ILanguageExtensionPoint
import monaco.languages.TokensProvider
import net.akehurst.kotlin.compose.editor.ComposeCodeEditorJs
import net.akehurst.kotlin.html5.elCreate
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.browser.ace.IAce
import net.akehurst.language.editor.browser.ace.attachToAce
import net.akehurst.language.editor.browser.agl.attachToAglEditor
import net.akehurst.language.editor.browser.ck.attachToCk
import net.akehurst.language.editor.browser.codemirror.attachToCodeMirror
import net.akehurst.language.editor.browser.demo.BuildConfig
import net.akehurst.language.editor.browser.monaco.Monaco
import net.akehurst.language.editor.browser.monaco.attachToMonaco
import net.akehurst.language.editor.common.ConsoleLogger
import net.akehurst.language.editor.common.compose.attachToComposeEditor
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.common.objectJSTyped
import net.akehurst.language.editor.information.Examples
import net.akehurst.language.editor.information.examples.AglGrammar
import net.akehurst.language.editor.information.examples.AglStyle
import net.akehurst.language.editor.information.examples.BasicTutorial
import net.akehurst.language.editor.language.service.AglLanguageServiceByWorker
import net.akehurst.language.editor.technology.gui.widgets.TabView
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.grammar.processor.ContextFromGrammarRegistry
import org.w3c.dom.*

external fun import(moduleName:String, options:dynamic = definedExternally  ):dynamic

external var aglScriptBasePath: String = definedExternally
external var resourcesPath: String = definedExternally
val workerScriptName = "${aglScriptBasePath}/application-agl-editor-worker.js"
var demo: Demo? = null

enum class EditorKind {
    ACE, MONACO, CK, CODEMIRROR, AGL, COMPOSE
}

object Constants {
    val initialLogLevel = LogLevel.All
    val initialEditorKind = EditorKind.ACE

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
}

interface DemoInterface {

    val logLevel: LogLevel

    fun changeLoggingLevel(level: LogLevel)
    fun changeEditor()
    fun analyseGrammar()
}

suspend fun main() {
    try {
        val logger = ConsoleLogger(Constants.initialLogLevel)

        //define this before editors are created
        // need to register this so that we can set the configuration and get the default completion-provider
        Agl.registry.register(
            identity = Constants.sentenceLanguageId,
            grammarStr = GrammarString(""),
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
            override val logLevel: LogLevel get() = logger.outputLevel
            override fun changeLoggingLevel(level: LogLevel) {
                logger.outputLevel = level
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
        editorChoice.value = Constants.initialEditorKind.name
        editorChoice.addEventListener("change", {
            val optionStr = editorChoice.value
            val edKind = EditorKind.valueOf(optionStr)
            CoroutineScope(SupervisorJob()).asyncImmediately {
                createDemo(edKind, logger)
            }.invokeOnCompletion {
                console.info("Reset Demo to use '$optionStr'")
            }
        })

        TabView.initialise(document)
        initialiseExamples()

        createDemo(Constants.initialEditorKind, logger)
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
    appDiv.elCreate().article {
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
                            attribute.set("agl-language", Constants.sentenceLanguageId.value)
                        }
                    }
                    section {
                        class_.add("trees")
                        htmlElement("tabview") { //TODO: use from lib
                            htmlElement("tab") {
                                attribute.id = "ParseTree"
                                htmlElement("treeview") { attribute.id = "parse" } //TODO: use from lib
                            }
                            htmlElement("tab") {
                                attribute.id = "AST"
                                htmlElement("treeview") { attribute.id = "ast" } //TODO: use from lib
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
                                        attribute.set("agl-language", Constants.grammarLanguageId.value)
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
                                        attribute.set("agl-language", Constants.styleLanguageId.value)
                                    }
                                }
                            }
                            htmlElement("tab") {
                                attribute.id = "crossreferences"
                                section {
                                    htmlElement("agl-editor") {
                                        attribute.id = Constants.referencesEditorId
                                        attribute.set("agl-language", Constants.referencesLanguageId.value)
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
                                        option(value = EditorKind.CK.name) { content = "CK-Editor" }
                                        option(value = EditorKind.ACE.name) { content = "Ace" }
                                        option(value = EditorKind.MONACO.name) { content = "Monaco" }
                                        option(value = EditorKind.CODEMIRROR.name) { content = "CodeMirror" }
                                        //option(value = AlternativeEditors.AGL.name, selected = true) { content = "Minimal (Agl)" }
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

suspend fun createDemo(editorChoice: EditorKind, logger: ConsoleLogger) {
    if (null != demo) {
        demo!!.finalize()
    }
    // kill shared worker to start
    val w = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
    w.port.close()
    val worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
    val logFunction: LogFunction = { lvl, msg, t -> logger.log(lvl, msg, t) }
    val languageService = AglLanguageServiceByWorker(worker, AglEditorLogger(logFunction))

    val editorEls = document.querySelectorAll("agl-editor")
    val editors = editorEls.asList().associate { node ->
        val element = node as Element
        //delete any current children of element - existing editor if switching ace<->monaco
        while (element.childElementCount != 0) {
            element.removeChild(element.firstChild!!)
        }
        val ed = when (editorChoice) {
            EditorKind.ACE -> createAce(element, logFunction, languageService)
            EditorKind.MONACO -> createMonaco(element, logFunction, languageService)
            EditorKind.CK -> createCk(element, logFunction, languageService)
            EditorKind.CODEMIRROR -> createCodeMirror(element, logFunction, languageService)
            EditorKind.AGL -> createAgl(element, logFunction, languageService)
            EditorKind.COMPOSE -> createCompose(element, logFunction, languageService)
        }
        Pair(element.id, ed)// (editorId)
    }

    demo = Demo(editors, logger)
    demo!!.configure()
}

fun createAce(editorElement: Element, logFunction: LogFunction, languageService: LanguageService): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = LanguageIdentity(editorElement.getAttribute("agl-language")!!)

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

    // make sure ace editor gets resized
    val resizeObserver = ResizeObserver { entries ->
        entries.forEach { entry ->
            if (entry.target == editorElement) {
               ed.resize(true)
            }
        }
    }
    resizeObserver.observe(editorElement)

    val ace = object : IAce {
        override fun createRange(startRow: Int, startColumn: Int, endRow: Int, endColumn: Int): IRange {
            return ace.Range(startRow, startColumn, endRow, endColumn)
        }
    }
    return Agl.attachToAce(
        languageService = languageService,
        containerElement = editorElement,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        aceEditor = ed,
        ace = ace
    )
}

fun createMonaco(editorElement: Element, logFunction: LogFunction, languageService: LanguageService): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = LanguageIdentity(editorElement.getAttribute("agl-language")!!)

    val editorOptions = objectJSTyped<IStandaloneEditorConstructionOptions> {
        value = ""
        wordBasedSuggestions = "off"
    }
    val ed = monaco.editor.create(editorElement, editorOptions, null)
    // ensure editor is resized
    val resizeObserver = ResizeObserver { entries ->
        entries.forEach { entry ->
            if (entry.target == editorElement) {
                ed.layout()
            }
        }
    }
    resizeObserver.observe(editorElement)

    val monaco = object : Monaco {
        override fun defineTheme(themeName: String, themeData: IStandaloneThemeData) = monaco.editor.defineTheme(themeName, themeData)
        override fun setModelMarkers(model: ITextModel, owner: String, markers: Array<IMarkerData>) = monaco.editor.setModelMarkers(model, owner, markers)
        override fun setModelLanguage(model: ITextModel, languageId: String) = monaco.editor.setModelLanguage(model, languageId)
        override fun setTheme(themeName: String) = monaco.editor.setTheme(themeName)

        override fun register(language: ILanguageExtensionPoint) = monaco.languages.register(language)
        override fun setTokensProvider(languageId: String, provider: TokensProvider): IDisposable = monaco.languages.setTokensProvider(languageId, provider)
        override fun registerCompletionItemProvider(languageId: String, provider: CompletionItemProvider): IDisposable = monaco.languages.registerCompletionItemProvider(languageId, provider)
        override fun createCompletionItem(position: IPosition, aglCompletionItem: CompletionItem): monaco.languages.CompletionItem {
            return object : monaco.languages.CompletionItem {
                override val label: String = "${aglCompletionItem.text} (${aglCompletionItem.name})"
                override val insertText: String = aglCompletionItem.text
                override val kind = monaco.languages.CompletionItemKind.Text
                override val range: monaco.IRange = objectJSTyped<monaco.IRange> {
                    startColumn = position.column
                    endColumn = position.column + aglCompletionItem.text.length //TODO: may not be same line
                    startLineNumber = position.lineNumber
                    endLineNumber = position.lineNumber //TODO: may not be same line
                }
            }
        }
    }
    return Agl.attachToMonaco(
        languageService = languageService,
        containerElement = editorElement,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        monacoEditor = ed,
        monaco = monaco
    )
}

suspend fun createCk(editorElement: Element, logFunction: LogFunction, languageService: LanguageService): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = LanguageIdentity(editorElement.getAttribute("agl-language")!!)

    // CK creats it editor next to the element passed in.
    // so create this dummy div so that the editor is under the editorElement
    val innerDiv = document.createElement("div") as HTMLDivElement
    editorElement.appendChild(innerDiv)
    // resize CK to fit editorElement
    val resizeObserver = ResizeObserver { entries ->
        entries.forEach { entry ->
            if (entry.target == editorElement) {
                val toolbarHeight = editorElement.querySelector(".ck-editor__top")!!.clientHeight
                val h = editorElement.parentElement!!.clientHeight - toolbarHeight
                val el = editorElement.querySelector(".ck-editor__editable_inline") as HTMLDivElement
                el.style.height = "${h}px"
            }
        }
    }
    resizeObserver.observe(editorElement)

    import("ckeditor5/ckeditor5.css")
    val toolbarItems =  arrayOf(
            "undo", "redo", "|", "bold", "italic", "|",
            "fontSize", "fontFamily", "fontColor", "fontBackgroundColor"
        )
    val plgs = arrayOf(ck.Essentials, ck.Bold, ck.Italic, ck.Font, ck.Paragraph)//, ck.Mention)
    val config = objectJS {
        plugins = plgs
        toolbar = objectJSTyped<ck.CkConfigToolbar> { items = toolbarItems }
//        mention = objectJSTyped {
//            feeds = arrayOf<dynamic>(
//                objectJSTyped {
//                    marker = "'"
//                    feed = ::getAutocompleteItems
//                }
//            )
//        }
    }
    var ed:ck.Editor? = null
    val edPromise = ck.ClassicEditor.create(innerDiv, config).then { ed = it }
    edPromise.await()
    println("CK editor '$editorId' created")

    return Agl.attachToCk(
        languageService = languageService,
        containerElement = editorElement,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        ckEditor = ed!!
    )
}


fun createCodeMirror(editorElement: Element, logFunction: LogFunction, languageService: LanguageService): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = LanguageIdentity(editorElement.getAttribute("agl-language")!!)
    val editorOptions = objectJSTyped<codemirror.view.EditorViewConfig> {
        doc = ""
        extensions = arrayOf(
            codemirror.extensions.view.keymap.of(codemirror.extensions.commands.defaultKeymap),
            codemirror.extensions.view.lineNumbers(),
            codemirror.extensions.view.highlightActiveLine(),
            codemirror.extensions.view.highlightActiveLineGutter(),
            codemirror.extensions.commands.history(),
        )
        parent = editorElement
    }
    val ed = codemirror.extensions.view.EditorView(editorOptions)
    return Agl.attachToCodeMirror(
        languageService = languageService,
        containerElement = editorElement,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        cmEditor = ed,
        codemirror = codemirror.CodeMirror
    )
}

fun createAgl(editorElement: Element, logFunction: LogFunction, languageService: LanguageService): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = LanguageIdentity(editorElement.getAttribute("agl-language")!!)
    return Agl.attachToAglEditor(
        languageService = languageService,
        containerElement = editorElement,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
    )
}

fun createCompose(editorElement: Element, logFunction: LogFunction, languageService: LanguageService): AglEditor<Any, Any> {
    val editorId = editorElement.id
    val languageId = LanguageIdentity(editorElement.getAttribute("agl-language")!!)

    val editor = ComposeCodeEditorJs(
        editorElement = editorElement,
        initialText = "",
    )

    return Agl.attachToComposeEditor(
        languageService = languageService,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        composeEditor = editor
    )
}

//fun createFirepad(editorElement: Element): AglEditor<Any, Any> {
//    val id = editorElement.id
//    return Agl.attachToCodeMirror(ed, id, id, workerScriptName, true)
//}




