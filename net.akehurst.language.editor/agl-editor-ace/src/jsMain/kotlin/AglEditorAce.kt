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

package net.akehurst.language.editor.ace

import ResizeObserver
import ace.AceAnnotation
import ace.AceOptions
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.hasClass
import kotlinx.dom.removeClass
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.style.AglStyle
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.api.syntaxAnalyser.AsmElementSimple
import net.akehurst.language.api.syntaxAnalyser.SyntaxAnalyserException
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.*
import org.w3c.dom.Element
import org.w3c.dom.ParentNode
import kotlin.js.Date

class AglErrorAnnotation(
    val line: Int,
    val column: Int,
    val text: String,
    val type: String,
    val raw: String?
) {
    val row = line - 1
}

//FIXME: some inefficiency due to some updates being triggered multiple times
class AglEditorAce(
    val element: Element,
    languageId: String,
    editorId: String,
    options: AceOptions, //TODO: types for this
    workerScriptName: String,
    sharedWorker: Boolean
) : AglEditorAbstract(languageId, editorId) {

    private val errorParseMarkerIds = mutableListOf<Int>()
    private val errorProcessMarkerIds = mutableListOf<Int>()

    val aceEditor: ace.Editor = ace.Editor(
        ace.VirtualRenderer(this.element, null),
        ace.Ace.createEditSession(""),
        objectJS { } //options are set later in init_
    )
    override var text: String
        get() {
            try {
                return this.aceEditor.getValue()
            } catch (t: Throwable) {
                throw RuntimeException("Failed to get text from editor")
            }
        }
        set(value) {
            try {
                this.aceEditor.setValue(value, -1)
            } catch (t: Throwable) {
                throw RuntimeException("Failed to set text in editor")
            }
        }

    var aglWorker = AglWorkerClient(workerScriptName, sharedWorker)
    lateinit var workerTokenizer: AglTokenizerByWorkerAce
    var parseTimeout: dynamic = null

    init {
        this.init_(options)
    }

    fun init_(options: AceOptions) {
        this.workerTokenizer = AglTokenizerByWorkerAce(this.agl)

        if (null != options.editor) this.aceEditor.setOptions(options.editor)
        if (null != options.renderer) this.aceEditor.renderer.setOptions(options.renderer)
        //TODO: set session and mouseHandler options

        this.aceEditor.getSession().bgTokenizer = AglBackgroundTokenizer(this.workerTokenizer, this.aceEditor)
        this.aceEditor.getSession().bgTokenizer.setDocument(this.aceEditor.getSession().getDocument())
        this.aceEditor.commands.addCommand(ace.ext.Autocomplete.startCommand)
        this.aceEditor.completers = arrayOf(AglCodeCompleter(this.agl))

        this.aceEditor.on("change") { event -> this.update() }

        val resizeObserver = ResizeObserver { entries -> onResize(entries) }
        resizeObserver.observe(this.element)

        this.aglWorker.initialise()
        this.aglWorker.setStyleResult = { success, message -> if (success) this.resetTokenization() else this.log(LogLevel.Error,message) }
        this.aglWorker.processorCreateSuccess = this::processorCreateSuccess
        this.aglWorker.processorCreateFailure = { msg -> this.log(LogLevel.Error,"Failed to create processor $msg") }
        this.aglWorker.parseStart = { this.notifyParse(ParseEventStart()) }
        this.aglWorker.parseSuccess = this::parseSuccess
        this.aglWorker.parseFailure = this::parseFailure
        this.aglWorker.lineTokens = {
            if (it.success) {
                this.log(LogLevel.Debug,"Debug: new line tokens from successful parse of ${editorId}")
                this.workerTokenizer.receiveTokens(it.lineTokens)
                this.resetTokenization()
            } else {
                this.log(LogLevel.Error,"LineTokens - ${it.message}")
            }
        }
        this.aglWorker.processStart = { this.notifyProcess(ProcessEventStart()) }
        this.aglWorker.processSuccess = { tree ->
            this.notifyProcess(ProcessEventSuccess(tree))
        }
        this.aglWorker.processFailure = { message ->
            this.notifyProcess(ProcessEventFailure(message, "No Asm"))
        }

        this.updateLanguage(null)
        this.updateGrammar()
        this.updateStyle()
    }

    override fun finalize() {
        //this.aglWorker.worker.terminate()
    }

    override fun destroy() {
        //this.aglWorker.worker.terminate()
        this.aceEditor.destroy()
    }

    fun format() {
        val proc = this.agl.languageDefinition?.processor
        if (null != proc) {
            val pos = this.aceEditor.getSelection().getCursor();
            val formattedText: String = proc.formatText<AsmElementSimple>(AsmElementSimple::class, this.text);
            this.aceEditor.setValue(formattedText, -1);
        }
    }

    override fun updateLanguage(oldId: String?) {
        if (null!=oldId) {
            val oldAglStyleClass = AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.element.removeClass(oldAglStyleClass)
        }
        this.element.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun updateGrammar() {
        this.clearErrorMarkers()
        this.aglWorker.createProcessor(this.languageIdentity, editorId, this.agl.languageDefinition.grammar)

        this.workerTokenizer.reset()
        this.resetTokenization() //new processor so find new tokens, first by scan
    }

    override fun updateStyle() {
        // style requires that the element is part of the dom
        if (this.element.isConnected) {
            val aglStyleClass = this.agl.styleHandler.aglStyleClass
            val str = this.editorSpecificStyleStr
            if (null != str && str.isNotEmpty()) {
                this.agl.styleHandler.reset()
                val rules: List<AglStyleRule> = Agl.registry.agl.style.processor!!.process(List::class, str)
                var mappedCss = "" //TODO? this.agl.styleHandler.theme_cache // stored when theme is externally changed
                rules.forEach { rule ->
                    val ruleClass = this.agl.styleHandler.mapClass(rule.selector)
                    val cssClass = ".$aglStyleClass .ace_$ruleClass"
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

                val root = this.element.getRootNode() as ParentNode?
                if (null != root) {
                    var curStyle = root.querySelector("style#$aglStyleClass")
                    if (null == curStyle) {
                        curStyle = this.element.ownerDocument!!.createElement("style")
                        curStyle.id = aglStyleClass
                        if (root == curStyle.ownerDocument) {
                            curStyle.ownerDocument!!.head!!.prepend(curStyle)
                        } else {
                            //shadowDom case
                            root.prepend(curStyle)
                        }
                    }
                    curStyle.textContent = mappedCss
                }
                this.aglWorker.setStyle(this.languageIdentity, editorId, str)

                // need to update because token style types may have changed, not just their attributes
                this.update()
                this.resetTokenization()
            }
        }
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
                this.log(LogLevel.Debug,"New Processor created for ${editorId}")
                this.workerTokenizer.acceptingTokens = true
                this.doBackgroundTryParse()
                this.resetTokenization()
            }
            "reset" -> {
                this.log(LogLevel.Debug,"Reset Processor for ${editorId}")
            }
            else -> {
                this.log(LogLevel.Error,"Unknown result message from create Processor for ${editorId}: $message")
            }
        }
    }

    @JsName("onResize")
    private fun onResize(entries: Array<dynamic>) {
        entries.forEach { entry ->
            if (entry.target == this.element) {
                this.aceEditor.resize(true)
            }
        }
    }

    private fun setupCommands() {
        /*
        this.aceEditor.commands.addCommand({
            name: 'format',
            bindKey: {win: 'Ctrl-F', mac: 'Command-F'},
            exec: (editor) => this.format(),
            readOnly: false
        })
         */
    }

    private fun resetTokenization() {
        this.aceEditor.renderer.updateText();
        this.aceEditor.getSession().bgTokenizer.start(0);
    }

    private fun doBackgroundTryParse() {
        this.clearErrorMarkers()
        this.aglWorker.interrupt(this.languageIdentity, editorId)
        this.notifyParse(ParseEventStart())
        this.aglWorker.tryParse(this.languageIdentity, editorId, this.agl.goalRule, this.text)
    }

    private fun foregroundParse() {
        val proc = this.agl.languageDefinition.processor
        if (null != proc) {
            try {
                val goalRule = this.agl.goalRule
                val sppt = if (null == goalRule) {
                    proc.parse(this.text)
                } else {
                    proc.parseForGoal(goalRule, this.text)
                }
                this.parseSuccess(sppt)
            } catch (e: ParseFailedException) {
                this.parseFailure(e.message!!, e.location, e.expected.toTypedArray(), e.longestMatch)
            } catch (t: Throwable) {
                this.log(LogLevel.Error,"Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: ${t.message}")
            }
        }
    }

    private fun foregroundProcess() {
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
                this.log(LogLevel.Error,"Cannot process parse result in ${this.editorId} for language ${this.languageIdentity}: ${t.message}")
            }
        }
    }

    override fun clearErrorMarkers() {
        this.aceEditor.getSession().clearAnnotations(); //assume there are no parse errors or there would be no sppt!
        this.errorParseMarkerIds.forEach { id -> this.aceEditor.getSession().removeMarker(id) }
    }

    private fun parseSuccess(tree: Any) {
        this.resetTokenization()
        val event = ParseEventSuccess(tree)
        this.notifyParse(event)
    }

    private fun parseFailure(message: String, location: InputLocation?, expected: Array<String>, tree: Any?) {
        this.log(LogLevel.Error,"Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: $message")
        // parse failed so re-tokenize from scan
        this.workerTokenizer.reset()
        this.resetTokenization()

        if (null != location) {
            val errMsg = when {
                expected.isEmpty() -> "Syntax Error"
                1 == expected.size -> "Syntax Error, expected: $expected"
                else -> "Syntax Error, expected one of: $expected"
            }
            val errors = listOf(
                objectJSTyped<AceAnnotation> {
                    row = location.line - 1
                    column = location.column - 1
                    text = errMsg
                    type = "error"
                    raw = null
                }
            )
            this.aceEditor.getSession().setAnnotations(errors.toTypedArray())
            errors.forEach { err ->
                val range = ace.Range(err.row, err.column, err.row, err.column + 1)
                val cls = "ace_marker_text_error"
                val errMrkId = this.aceEditor.getSession().addMarker(range, cls, "text")
                this.errorParseMarkerIds.add(errMrkId)
            }
            val event = ParseEventFailure(message, tree)
            this.notifyParse(event)
        }
    }
}