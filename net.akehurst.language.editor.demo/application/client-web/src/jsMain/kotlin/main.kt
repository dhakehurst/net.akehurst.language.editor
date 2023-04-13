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
import monaco.editor.IStandaloneEditorConstructionOptions
import net.akehurst.kotlin.html5.create
import net.akehurst.language.agl.grammar.grammar.ContextFromGrammar
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.api.analyser.ScopeModel
import net.akehurst.language.api.asm.*
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.api.processor.SentenceContext
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.browser.ace.attachToAce
import net.akehurst.language.editor.browser.codemirror.attachToCodeMirror
import net.akehurst.language.editor.browser.monaco.attachToMonaco
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.common.objectJSTyped
import net.akehurst.language.editor.demo.BuildConfig
import net.akehurst.language.editor.information.Examples
import net.akehurst.language.editor.information.examples.*
import net.akehurst.language.editor.technology.gui.widgets.TabView
import net.akehurst.language.editor.technology.gui.widgets.TreeView
import net.akehurst.language.editor.technology.gui.widgets.TreeViewFunctions
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

fun main() {

    createBaseDom("div#agl-demo")
    val logger = DemoLogger()

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
                                attribute.id = "grammar"
                                section {
                                    htmlElement("agl-editor") {
                                        attribute.id = Constants.grammarEditorId
                                        attribute.set("agl-language", Constants.grammarLanguageId)
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

fun initialiseExamples() {
    val exampleSelect = document.querySelector("select#example") as HTMLElement
    Examples.add(Datatypes.example)
    Examples.add(SQL.example)
    Examples.add(GraphvizDot.example)
    Examples.add(SText.example)
    Examples.add(Java8.example)
    Examples.add(English.example)
    Examples.add(TraceabilityQuery.example)
    Examples.add(MScript.example)
    Examples.add(Xml.example)

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
        ed.logger.bind =  { lvl, msg, t -> logger.log(lvl,msg,t)}
        Pair(element.id, ed)// (editorId)
    }

    demo = Demo(editors)
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
    val editors: Map<String, AglEditor<*, *>>
) {
    val trees = TreeView.initialise(document)

    val exampleSelect = document.querySelector("select#example") as HTMLElement
    val sentenceEditor = editors[Constants.sentenceEditorId]!! as AglEditor<AsmSimple, ContextSimple>
    val grammarEditor = editors[Constants.grammarEditorId]!!
    val styleEditor = editors[Constants.styleEditorId]!! as AglEditor<AglStyleModel, ContextFromGrammar>
    val referencesEditor = editors[Constants.referencesEditorId]!! as AglEditor<ScopeModel, ContextFromGrammar>
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
            grammarStr = null,
            buildForDefaultGoal = false,
            aglOptions = Agl.options {
                semanticAnalysis {
                    active(false)
                }
            },
            configuration = Agl.configurationDefault()
        ).identity

        grammarEditor.editorSpecificStyleStr = Agl.registry.agl.grammar.styleStr
        styleEditor.editorSpecificStyleStr = Agl.registry.agl.style.styleStr
        referencesEditor.editorSpecificStyleStr = Agl.registry.agl.scopes.styleStr

        val grammarAsContext = ContextFromGrammar()
        var sentenceScopeModel: ScopeModel? = null

        grammarEditor.onParse { event ->
            when {
                event.isStart -> Unit
                event.success -> {
                    try {
                        console.asDynamic().debug("Debug: Grammar parse success, resetting sentence processor")
                        sentenceEditor.languageDefinition.grammarStr = grammarEditor.text
                    } catch (t: Throwable) {
                        console.error(grammarEditor.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.grammarStr = null
                    }
                }

                event.failure -> {
                    console.error(grammarEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.grammarStr = null
                }

                else -> {
                }
            }
        }
        grammarEditor.onSyntaxAnalysis { event ->
            when {
                event.isStart -> Unit
                event.success -> {
                    val grammar = event.asm as List<Grammar>? ?: error("should always be a List<Grammar> if success")
                    //grammarContext = ContextFromGrammar(grammar.last()) //TODO: multiple grammars
                    grammarAsContext.createScopeFrom(grammar.last())
                    //grammar.forEach { g ->
                    //    g.allResolvedGrammarRule.forEach { r ->
                    //        grammarAsContext?.rootScope?.addToScope(r.name, "Rule", AsmElementPath("${g.name}/rules/${r.name}"))
                    //    }
                    //    g.allResolvedTerminal.forEach { term ->
                    //        val ref = if (term.isPattern) "\"${term.value}\"" else "'${term.value}'"
                    //        grammarAsContext?.rootScope?.addToScope(ref, "Rule", AsmElementPath("${g.name}/terminals/${ref}"))
                    //    }
                    //}
                }

                event.failure -> grammarAsContext.clear()
            }
            referencesEditor.sentenceContext = grammarAsContext
            styleEditor.sentenceContext = grammarAsContext
        }

        styleEditor.onSyntaxAnalysis { event ->
            when {
                event.isStart -> Unit
                event.success -> {
                    try {
                        console.asDynamic().debug("Debug: Style parse success, resetting sentence style")
                        sentenceEditor.languageDefinition.styleStr = styleEditor.text
                    } catch (t: Throwable) {
                        console.error(styleEditor.editorId + ": " + t.message, t)
                        sentenceEditor.languageDefinition.styleStr = ""
                    }
                }

                event.failure -> {
                    console.error(styleEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.styleStr = ""
                }

                else -> Unit
            }
        }
        referencesEditor.onSyntaxAnalysis { event ->
            when {
                event.isStart -> Unit
                event.success -> {
                    try {
                        sentenceScopeModel = event.asm as ScopeModel
                        console.asDynamic().debug("Debug: CrossReferences SyntaxAnalysis success, resetting scopes and references")
                        sentenceEditor.languageDefinition.scopeModelStr = referencesEditor.text
                    } catch (t: Throwable) {
                        console.error(referencesEditor.editorId + ": " + t.message, t)
                    }
                }

                event.failure -> {
                    console.error(referencesEditor.editorId + ": " + event.message)
                }

                else -> {
                }
            }
        }
    }

    private fun loading(parse: Boolean, ast: Boolean) {
        trees["parse"]!!.loading = parse
        trees["ast"]!!.loading = ast
    }

    private fun connectTrees() {
        trees["parse"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = {
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
            when {
                event.success -> {
                    trees["parse"]!!.loading = false
                    trees["parse"]!!.root = event.tree
                }

                event.isStart -> loading(true, true)
                else -> loading(false, false)
            }
        }

        trees["ast"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = {
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
                            v is AsmElementReference -> "& ${v.reference} : ${v.value?.typeName}"
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
                    it is Array<*> -> (it as Array<*>)
                    it is Collection<*> -> (it as Collection<*>).toTypedArray()
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
            when {
                event.isStart -> {
                    //trees["ast"]!!.loading = true
                }

                event.success -> {
                    trees["ast"]!!.loading = false
                    trees["ast"]!!.root = if (event.asm is AsmSimple) {
                        val rts = (event.asm as AsmSimple).rootElements
                        when {
                            rts.size == 0 -> "<Empty>"
                            rts.size == 1 -> rts[0]
                            else -> rts
                        }
                    } else {
                        "<Unknown>"
                    }
                }

                else -> {//Failure
                    console.error(event.message)
                    trees["ast"]!!.loading = false
                    trees["ast"]!!.root = event.asm
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
            sentenceEditor.sentenceContext = ContextSimple()
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
        sentenceEditor.languageDefinition.grammarStr = grammarEditor.text
        sentenceEditor.languageDefinition.styleStr = styleEditor.text
        sentenceEditor.languageDefinition.scopeModelStr = referencesEditor.text
        sentenceEditor.text = eg.sentence
    }

    fun finalize() {
        editors.values.forEach {
            it.finalize()
        }
    }
}


