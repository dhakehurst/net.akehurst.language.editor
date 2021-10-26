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

import kotlinx.browser.document
import net.akehurst.kotlin.html5.create
import net.akehurst.language.agl.grammar.grammar.ContextFromGrammar
import net.akehurst.language.agl.grammar.scopes.ScopeModel
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.ContextSimple
import net.akehurst.language.agl.syntaxAnalyser.SyntaxAnalyserSimple
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.language.editor.ace.AglEditorAce
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.common.objectJS

import net.akehurst.language.editor.demo.BuildConfig
import net.akehurst.language.editor.information.Examples
import net.akehurst.language.editor.information.examples.*
import net.akehurst.language.editor.monaco.AglEditorMonaco

import net.akehurst.language.editor.technology.gui.widgets.TabView
import net.akehurst.language.editor.technology.gui.widgets.TreeView
import net.akehurst.language.editor.technology.gui.widgets.TreeViewFunctions
import org.w3c.dom.*

external var aglScriptBasePath: dynamic = definedExternally
val workerScriptName = "${aglScriptBasePath}/application-agl-editor-worker.js"
var demo: Demo? = null

fun main() {

    createBaseDom("div#agl-demo")

    val editorChoiceAce = document.querySelector("#editor-choice-ace")!!
    val editorChoiceMonaco = document.querySelector("#editor-choice-monaco")!!

    TabView.initialise(document)
    initialiseExamples()

    createDemo(true)

    editorChoiceAce.addEventListener("click", {
        createDemo(true)
    })
    editorChoiceMonaco.addEventListener("click", {
        createDemo(false)
    })

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
                nav {
                    val about = dialog {
                        val thisDialog = this.element as HTMLDialogElement
                        on.click {
                            (it.target as HTMLDialogElement).close()
                        }
                        article {
                            header { h2 { content = "About" } }
                            section {
                                p { content = "Ace version 1.4.12, Licence BSD" }
                                p { content = "Monaco version 0.20.0, Licence MIT" }
                                p { content = "AGL version ${Agl.version}, Licence Apache 2.0" }
                                p { content = "Kotlin version 1.5.31, Licence Apache 2.0" }
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
                        content = "About"
                        on.click {
                            (about as HTMLDialogElement).showModal()
                        }
                    }
                }
            }
            section {
                class_.add("agl-options")
                div {
                    class_.add("agl-options-editor")
                    label { content = "Select underlying Editor Type: " }
                    radio {
                        attribute.id = "editor-choice-ace"
                        attribute.name = "editor-choice"
                        attribute.value = "ace"
                        attribute.checked = "checked"
                    }
                    label { attribute.for_ = "editor-choice-ace"; content = "Ace" }
                    radio {
                        attribute.id = "editor-choice-monaco"
                        attribute.name = "editor-choice"
                        attribute.value = "monaco"
                    }
                    label { attribute.for_ = "editor-choice-monaco"; content = "Monaco" }
                }
                div {
                    class_.add("agl-options-example")
                    label { attribute.for_ = "example"; content = "Please choose an example :" }
                    select { attribute.id = "example" }
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
                        htmlElement("agl-editor") { attribute.id = "sentence-text" }
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
                                    htmlElement("agl-editor") { attribute.id = "language-grammar" }
                                }
                            }
                            htmlElement("tab") {
                                attribute.id = "style"
                                section {
                                    htmlElement("agl-editor") { attribute.id = "language-style" }
                                }
                            }
                            htmlElement("tab") {
                                attribute.id = "crossreferences"
                                section {
                                    htmlElement("agl-editor") { attribute.id = "language-references" }
                                }
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

    Examples.map.forEach { eg ->
        val option = document.createElement("option")
        exampleSelect.appendChild(option);
        option.setAttribute("value", eg.value.id);
        option.textContent = eg.value.label;
    }
}

fun createDemo(isAce: Boolean) {
    if (null != demo) {
        demo!!.finalize()
    }
    val editorEls = document.querySelectorAll("agl-editor")
    val aceOptions = objectJS {
        editor = objectJS {
            enableBasicAutocompletion = true
            enableSnippets = true
            enableLiveAutocompletion = false
        }
    }
    val editors = editorEls.asList().associate { node ->
        val element = node as Element
        //delete any current children of element - existing editor if switching ace<->monaco
        while (element.childElementCount != 0) {
            element.removeChild(element.firstChild!!)
        }
        val id = element.id
        val ed = if (isAce) {
            AglEditorAce(element, id, id, aceOptions, workerScriptName, true)
        } else {
            AglEditorMonaco(element, id, id, aceOptions, workerScriptName, true)
        }
        Pair(id, ed)
    }

    demo = Demo(editors)
    demo!!.configure()
}

class Demo(
    val editors: Map<String, AglEditor>
) {
    val trees = TreeView.initialise(document)

    val exampleSelect = document.querySelector("select#example") as HTMLElement
    val sentenceEditor = editors["sentence-text"]!!
    val grammarEditor = editors["language-grammar"]!!
    val styleEditor = editors["language-style"]!!
    val referencesEditor = editors["language-references"]!!
    //val formatEditor = editors["language-format"]!!

    fun configure() {
        this.connectEditors()
        this.connectTrees()
        this.configExampleSelector()
    }

    fun connectEditors() {
        grammarEditor.languageIdentity = Agl.registry.agl.grammarLanguageIdentity
        styleEditor.languageIdentity = Agl.registry.agl.styleLanguageIdentity
        referencesEditor.languageIdentity = Agl.registry.agl.scopesLanguageIdentity
        sentenceEditor.languageIdentity= Agl.registry.register(
            identity = "user-language",
            grammar = "",
            defaultGoalRule = null,
            style = "",
            format = "",
            syntaxAnalyser = SyntaxAnalyserSimple(),
            semanticAnalyser = null
        ).identity

        var grammarContext:ContextFromGrammar? = null

        grammarEditor.onParse { event ->
            when {
                event.success -> {
                    try {
                        console.asDynamic().debug("Debug: Grammar parse success, resetting sentence processor")
                        sentenceEditor.languageDefinition.grammar = grammarEditor.text
                    } catch (t: Throwable) {
                        console.error(grammarEditor.editorId + ": " + t.message,t)
                        sentenceEditor.languageDefinition.grammar = null
                    }
                }
                event.failure -> {
                    console.error(grammarEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.grammar = null
                }
                else -> { }
            }
        }
        grammarEditor.onSyntaxAnalysis { event->
            when{
                event.success ->{
                    val grammar = event.asm as Grammar? ?: error("should always be a Grammar if success")
                    grammarContext = ContextFromGrammar(grammar)
                }
                event.failure -> grammarContext = null
            }
            referencesEditor.sentenceContext = grammarContext
            styleEditor.sentenceContext = grammarContext
        }

        styleEditor.onParse { event ->
            when {
                event.success -> {
                    try {
                        console.asDynamic().debug("Debug: Style parse success, resetting sentence style")
                        sentenceEditor.languageDefinition.style = styleEditor.text
                    } catch (t: Throwable) {
                        console.error(styleEditor.editorId + ": " + t.message,t)
                        sentenceEditor.languageDefinition.style = ""
                    }
                }
                event.failure -> {
                    console.error(styleEditor.editorId + ": " + event.message)
                    sentenceEditor.languageDefinition.style = ""
                }
                else -> { }
            }
        }
        referencesEditor.onSyntaxAnalysis { event ->
            when {
                event.success -> {
                    try {
                        console.asDynamic().debug("Debug: CrossReferences SyntaxAnalysis success, resetting scopes and references")
                        //(sentenceEditor.languageDefinition.syntaxAnalyser as SyntaxAnalyserSimple).scopeModel = event.asm as ScopeModel
                        sentenceEditor.configureSyntaxAnalyser(referencesEditor.text)
                    } catch (t: Throwable) {
                        console.error(referencesEditor.editorId + ": " + t.message,t)
                       // (sentenceEditor.languageDefinition.syntaxAnalyser as SyntaxAnalyserSimple).scopeModel = ScopeModel()
                    }
                }
                event.failure -> {
                    console.error(referencesEditor.editorId + ": " + event.message)
                    //(sentenceEditor.languageDefinition.syntaxAnalyser as SyntaxAnalyserSimple).scopeModel = ScopeModel()
                }
                else -> { }
            }
        }
    }

    fun connectTrees() {
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
                event.isStart -> {
                    trees["parse"]!!.loading = true
                    trees["ast"]!!.loading = true
                }
                else -> { //Failure
                    trees["parse"]!!.loading = false
                    trees["ast"]!!.loading = false
                }
            }
        }

        trees["ast"]!!.treeFunctions = TreeViewFunctions<dynamic>(
            label = {
                when {
                    it is Array<*> -> ": List"
                    it.isAsmElementSimple -> ": " + it.typeName
                    it.isAsmElementProperty -> {
                        val v = it.value
                        when {
                            null == v -> "${it.name} = null"
                            v is Array<*> -> "${it.name} : List"
                            v.isAsmElementSimple -> "${it.name} : ${v.typeName}"
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
                    it.isAsmElementSimple -> it.properties.size != 0
                    it.isAsmElementProperty -> {
                        val v = it.value
                        when {
                            null == v -> false
                            v is Array<*> -> true
                            v.isAsmElementSimple -> true
                            else -> false
                        }
                    }
                    else -> false
                }
            },
            children = {
                when {
                    it is Array<*> -> it
                    it.isAsmElementSimple -> it.properties
                    it.isAsmElementProperty -> {
                        val v = it.value
                        when {
                            v is Array<*> -> v
                            v.isAsmElementSimple -> v.properties
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
                    trees["ast"]!!.root = (event.asm as Array<*>)[0] //examples always have one element
                }
                else -> {//Failure
                    console.error(event.message)
                    trees["ast"]!!.loading = false
                    trees["ast"]!!.root = event.asm
                }
            }
        }
    }

    fun configExampleSelector() {
        exampleSelect.addEventListener("change", { _ ->
            val egName = js("event.target.value") as String
            val eg = Examples[egName]
            grammarEditor.text = eg.grammar
//            sentenceEditor.languageDefinition.grammar = grammarEditor.text // set this before setting the sentence text
            styleEditor.text = eg.style
//            sentenceEditor.languageDefinition.style = styleEditor.text  // set this before setting the sentence text
            referencesEditor.text = eg.references
            //formatEditor.text = eg.format
            sentenceEditor.sentenceContext = ContextSimple(null,eg.context)
            sentenceEditor.text = eg.sentence
        })

        // select initial example
        val eg = Datatypes.example
        (exampleSelect as HTMLSelectElement).value = eg.id
        grammarEditor.text = eg.grammar
//        sentenceEditor.languageDefinition.grammar = grammarEditor.text // set this before setting the sentence text
        styleEditor.text = eg.style
//        sentenceEditor.languageDefinition.style = styleEditor.text  // set this before setting the sentence text
        referencesEditor.text = eg.references // set this before setting the sentence text
        //formatEditor.text = eg.format
        sentenceEditor.sentenceContext = ContextSimple(null,eg.context)
        sentenceEditor.text = eg.sentence
    }

    fun finalize() {
        editors.values.forEach {
            it.finalize()
        }
    }
}


