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

package net.akehurst.language.editor.monaco

import ResizeObserver
import kotlinx.browser.document
import kotlinx.browser.window
import monaco.MarkerSeverity
import monaco.editor.IMarkerData
import monaco.editor.IStandaloneCodeEditor
import monaco.editor.ITokenThemeRule
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.style.AglStyle
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.api.syntaxAnalyser.SyntaxAnalyserException
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.AglWorkerClient
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.common.objectJSTyped
import org.w3c.dom.Element
import org.w3c.dom.ParentNode

class AglEditorMonaco(
    val element: Element,
    editorId: String,
    languageId: String,
    options: dynamic, //TODO: types for this
    workerScriptName: String,
    sharedWorker: Boolean
) : AglEditorAbstract(languageId, editorId) {

    companion object {
        private val init = js(
            """
            var self = {MonacoEnvironment: {}};
            self.MonacoEnvironment = {
                getWorkerUrl: function(moduleId, label) {
                    return './main.js';
                }
            }
        """
        )

        // https://github.com/Microsoft/monaco-editor/issues/338
        // all editors on the same page must share the same theme!
        // hence we create a global theme and modify it as needed.
        private val aglGlobalTheme = "agl-theme"
        val allAglGlobalThemeRules = mutableMapOf<String, ITokenThemeRule>()
    }

    lateinit var monacoEditor: IStandaloneCodeEditor
    val languageThemePrefix = this.languageIdentity + "-"

    override var text: String
        get() {
            try {
                return this.monacoEditor.getModel().getValue()
            } catch (t: Throwable) {
                throw RuntimeException("Failed to get text from editor")
            }
        }
        set(value) {
            try {
                this.monacoEditor.getModel().setValue(value)
            } catch (t: Throwable) {
                throw RuntimeException("Failed to set text in editor")
            }
        }

    var aglWorker = AglWorkerClient(workerScriptName,sharedWorker)
    lateinit var workerTokenizer: AglTokenizerByWorkerMonaco
    var parseTimeout: dynamic = null

    init {
        this.init_()
    }

    private fun init_() {
        try {
            this.workerTokenizer = AglTokenizerByWorkerMonaco(this, this.agl)
            val themeData = objectJS {
                base = "vs"
                inherit = false
                rules = emptyArray<Any>()
            }
            // https://github.com/Microsoft/monaco-editor/issues/338
            // all editors on the same page must share the same theme!
            // hence we create a global theme and modify it as needed.
            monaco.editor.defineTheme(aglGlobalTheme, themeData);

            monaco.languages.register(objectJSTyped<monaco.languages.ILanguageExtensionPoint> {
                id = languageIdentity
            })
            //val languageId = this.languageId
            //val editorOptions = js("{language: languageId, value: initialContent, theme: theme, wordBasedSuggestions:false}")
            val editorOptions = objectJSTyped<monaco.editor.IStandaloneEditorConstructionOptions> {
                language = languageIdentity
                value = ""
                theme = aglGlobalTheme
                wordBasedSuggestions = false
            }
            this.monacoEditor = monaco.editor.create(this.element, editorOptions, null)
            monaco.languages.setTokensProvider(this.languageIdentity, this.workerTokenizer);
            monaco.languages.registerCompletionItemProvider(this.languageIdentity, AglCompletionProviderMonaco(this.agl))

            this.onChange { this.update() }

            val resizeObserver = ResizeObserver { entries -> onResize(entries) }
            resizeObserver.observe(this.element)

            this.aglWorker.initialise()
            this.aglWorker.setStyleResult = { success, message ->
                if (success) {
                    this.resetTokenization()
                } else {
                    console.error("Error: $message")
                }
            }
            this.aglWorker.processorCreateSuccess = this::processorCreateSuccess
            this.aglWorker.processorCreateFailure = { msg -> console.error("Failed to create processor $msg") }
            this.aglWorker.parseStart = { this.notifyParse(ParseEventStart()) }
            this.aglWorker.parseSuccess = this::parseSuccess
            this.aglWorker.parseFailure = this::parseFailure
            this.aglWorker.lineTokens = {
                if(it.success) {
                    console.asDynamic().debug("Debug: new line tokens from successful parse of ${editorId}")
                    this.workerTokenizer.receiveTokens(it.lineTokens)
                    this.resetTokenization()
                } else {
                    console.error("LineTokens - ${it.message}")
                }
            }
            this.aglWorker.processStart = { this.notifyProcess(ProcessEventStart()) }
            this.aglWorker.processSuccess = { tree ->
                this.notifyProcess(ProcessEventSuccess(tree))
            }
            this.aglWorker.processFailure = { message ->
                this.notifyProcess(ProcessEventFailure(message, "No Asm"))
            }
        } catch (t: Throwable) {
            console.error(t.message)
        }
    }

    override fun destroy() {
        //this.aglWorker.worker.terminate()
        //this.monacoEditor.destroy()
    }

    override fun finalize() {
        //this.aglWorker.worker.terminate()
    }

    override fun updateStyle() {
        val str = this.agl.languageDefinition.style
        if (null != str && str.isNotEmpty()) {
            this.agl.styleHandler.reset()
            val rules: List<AglStyleRule> = Agl.registry.agl.style.processor!!.process(List::class, str)
            var mappedCss = ""
            rules.forEach { rule ->
                val cssClass = '.' + this.languageIdentity + ' ' + ".monaco_" + this.agl.styleHandler.mapClass(rule.selector);
                val mappedRule = AglStyleRule(cssClass)
                mappedRule.styles = rule.styles.values.associate { oldStyle ->
                    val style = when (oldStyle.name) {
                        "foreground" -> AglStyle("color", oldStyle.value)
                        "background" -> AglStyle("background-color", oldStyle.value)
                        "font-style" -> when (oldStyle.value) {
                            "bold" -> AglStyle("font-weight", oldStyle.value)
                            "italic" -> AglStyle("font-style", oldStyle.value)
                            else -> oldStyle
                        }
                        else -> oldStyle
                    }
                    Pair(style.name, style)
                }.toMutableMap()
                mappedCss = mappedCss + "\n" + mappedRule.toCss()
            }
            val cssText: String = mappedCss
            // remove the current style element for 'languageId' (which is used as the theme name) from the container
            // else the theme css is not reapplied
            val curStyle = (this.element.getRootNode() as ParentNode).querySelector("style#" + this.languageIdentity)
            curStyle?.remove()

            //add style element
            val styleElement = this.element.ownerDocument?.createElement("style")!!
            styleElement.setAttribute("id", this.languageIdentity)
            styleElement.textContent = cssText
            this.element.ownerDocument?.querySelector("head")?.appendChild(
                styleElement
            )
            this.aglWorker.setStyle(languageIdentity, editorId, str)

            // need to update because token style types may have changed, not just their attributes
            this.update()
        }
    }

    override fun updateGrammar() {
        this.clearErrorMarkers()
        this.aglWorker.createProcessor(languageIdentity, editorId, this.agl.languageDefinition.grammar)
        this.workerTokenizer.reset()
        this.resetTokenization() //new processor so find new tokens, first by scan
    }

    private fun update() {
        this.workerTokenizer.reset()
        window.clearTimeout(parseTimeout)
        this.parseTimeout = window.setTimeout({
            this.workerTokenizer.acceptingTokens = true
            this.doBackgroundTryParse()
        }, 500)
    }

    private fun processorCreateSuccess(message: String) {
        when (message) {
            "OK" -> {
                console.asDynamic().debug("Debug: New Processor created for ${editorId}")
                this.workerTokenizer.acceptingTokens = true
                this.doBackgroundTryParse()
                this.resetTokenization()
            }
            "reset" -> {
                console.asDynamic().debug("Debug: reset Processor for ${editorId}")
            }
            else -> {
                console.error("Error: unknown result message from create Processor for ${editorId}: $message")
            }
        }
    }

    @JsName("onResize")
    private fun onResize(entries: Array<dynamic>) {
        entries.forEach { entry ->
            if (entry.target == this.element) {
                this.monacoEditor.layout()
            }
        }
    }

    override fun clearErrorMarkers() {
        monaco.editor.setModelMarkers(this.monacoEditor.getModel(), "", emptyArray())
    }

    fun onChange(handler: (String) -> Unit) {
        this.monacoEditor.onDidChangeModelContent { event ->
            val text = this.text
            handler(text);
        }
    }

    fun resetTokenization() {
        this.monacoEditor.getModel().resetTokenization()
    }

    fun doBackgroundTryParse() {
        this.clearErrorMarkers()
        this.aglWorker.interrupt(languageIdentity, editorId)
        this.aglWorker.tryParse(languageIdentity, editorId, this.agl.languageDefinition.defaultGoalRule, this.text)
    }

    private fun tryParse() {
        val proc = this.agl.languageDefinition.processor
        if (null != proc) {
            try {

                val goalRule = this.agl.languageDefinition.defaultGoalRule
                val sppt = if (null == goalRule) {
                    proc.parse(this.text)
                } else {
                    proc.parseForGoal(goalRule, this.text)
                }
                this.agl.sppt = sppt
                this.resetTokenization()
                val event = ParseEventSuccess(sppt)
                this.notifyParse(event)
                //this.doBackgroundTryProcess()
            } catch (e: ParseFailedException) {
                this.agl.sppt = null
                // parse failed so re-tokenize from scan
                this.resetTokenization()
                console.error("Error parsing text in " + this.editorId + " for language " + this.languageIdentity, e.message);
                val errors = mutableListOf<IMarkerData>()
                errors.add(objectJSTyped<IMarkerData> {
                    code = null
                    severity = MarkerSeverity.Error
                    startLineNumber = e.location.line
                    startColumn = e.location.column
                    endLineNumber = e.location.line
                    endColumn = e.location.column
                    this.message = e.message!!
                    source = null
                })
                monaco.editor.setModelMarkers(this.monacoEditor.getModel(), "", errors.toTypedArray())
                val event = ParseEventFailure(e.message!!, e.longestMatch)
                this.notifyParse(event)
            } catch (t: Throwable) {
                console.error("Error parsing text in " + this.editorId + " for language " + this.languageIdentity, t.message);
            }
        }
    }

    private fun tryProcess() {
        val proc = this.agl.languageDefinition.processor
        val sppt = this.agl.sppt
        if (null != proc && null != sppt) {
            try {
                this.agl.asm = proc.processFromSPPT(Any::class, sppt)
                val event = ProcessEventSuccess(this.agl.asm!!)
                this.notifyProcess(event)
            } catch (e: SyntaxAnalyserException) {
                this.agl.asm = null
                val event = ProcessEventFailure(e.message!!, "No Asm")
                this.notifyProcess(event)
            } catch (t: Throwable) {
                console.error("Error processing parse result in " + this.editorId + " for language " + this.languageIdentity, t.message)
            }
        }
    }

    private fun convertColor(value: String?): String? {
        if (null == value) {
            return null
        } else {
            val cvs = document.createElement("canvas")
            val ctx = cvs.asDynamic().getContext("2d")
            ctx.fillStyle = value
            val col = ctx.fillStyle as String
            return col.substring(1) //leave out the #
        }
    }

    private fun setupCommands() {

    }

    private fun parseSuccess(tree: Any) {
        this.resetTokenization()
        val event = ParseEventSuccess(tree)
        this.notifyParse(event)
    }

    private fun parseFailure(message: String, location: InputLocation?, expected: Array<String>, tree: Any?) {
        console.error("Error parsing text in ${this.editorId}: $message")
        // parse failed so re-tokenize from scan
        this.workerTokenizer.reset()
        this.resetTokenization()

        if (null != location) {
            val errMsg = when {
                expected.isEmpty() -> "Syntax Error"
                1 == expected.size -> "Syntax Error, expected: $expected"
                else -> "Syntax Error, expected one of: $expected"
            }
            val errors = mutableListOf<IMarkerData>()
            errors.add(objectJSTyped<IMarkerData> {
                code = null
                severity = MarkerSeverity.Error
                startLineNumber = location.line
                startColumn = location.column
                endLineNumber = location.line
                endColumn = location.column
                this.message = errMsg
                source = null
            })
            monaco.editor.setModelMarkers(this.monacoEditor.getModel(), "", errors.toTypedArray())
            val event = ParseEventFailure(message, tree)
            this.notifyParse(event)

        }
    }

}

