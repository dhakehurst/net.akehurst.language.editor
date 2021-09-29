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
    element: Element,
    languageId: String,
    editorId: String,
    options: AceOptions, //TODO: types for this
    workerScriptName: String,
    sharedWorker: Boolean
) : AglEditorJsAbstract(element, languageId, editorId, workerScriptName, sharedWorker) {

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

    var parseTimeout: dynamic = null

    init {
        this.init_(options)
    }

    fun init_(options: AceOptions) {
        this.connectWorker(AglTokenizerByWorkerAce(this.agl))


        if (null != options.editor) this.aceEditor.setOptions(options.editor)
        if (null != options.renderer) this.aceEditor.renderer.setOptions(options.renderer)
        //TODO: set session and mouseHandler options

        this.aceEditor.getSession()?.bgTokenizer = AglBackgroundTokenizer(this.workerTokenizer as ace.Tokenizer , this.aceEditor)
        this.aceEditor.getSession()?.bgTokenizer?.setDocument(this.aceEditor.getSession()?.getDocument())
        this.aceEditor.commands.addCommand(ace.ext.Autocomplete.startCommand)
        this.aceEditor.completers = arrayOf(AglCodeCompleter(this.agl))

        this.aceEditor.on("change") { event -> this.update() }

        val resizeObserver = ResizeObserver { entries -> onResize(entries) }
        resizeObserver.observe(this.element)

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
            val formattedText: String = proc.format<AsmElementSimple, Any>(this.text);
            this.aceEditor.setValue(formattedText, -1);
        }
    }

    override fun updateLanguage(oldId: String?) {
        if (null != oldId) {
            val oldAglStyleClass = AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.element.removeClass(oldAglStyleClass)
        }
        this.element.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun updateGrammar() {
        this.clearErrorMarkers()
        this.aceEditor.getSession()?.also { session ->
            this.aglWorker.createProcessor(this.languageIdentity, editorId, session.id, this.agl.languageDefinition.grammar)
            this.workerTokenizer.reset()
            this.resetTokenization() //new processor so find new tokens, first by scan
        }
    }

    override fun updateStyle() {
        // style requires that the element is part of the dom
        if (this.element.isConnected) {
            this.aceEditor.getSession()?.also { session ->
                val aglStyleClass = this.agl.styleHandler.aglStyleClass
                val str = this.editorSpecificStyleStr
                if (null != str && str.isNotEmpty()) {
                    this.agl.styleHandler.reset()
                    val rules: List<AglStyleRule> = Agl.registry.agl.style.processor!!.process<List<AglStyleRule>,Any>(str).first //TODO: pass context?
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
                    this.aglWorker.setStyle(this.languageIdentity, editorId, session.id, str)

                    // need to update because token style types may have changed, not just their attributes
                    this.update()
                    this.resetTokenization()
                }
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

    override fun resetTokenization() {
        this.aceEditor.renderer.updateText()
        val sess = this.aceEditor.getSession()
        if (null == sess) {
            this.log(LogLevel.Error, "session is null ??")
        } else {
            val bgt = sess.bgTokenizer
            if (null == bgt) {
                this.log(LogLevel.Error, "bgTokenizer is null ??")
            } else {
                bgt.start(0)
            }
        }
    }

    override fun doBackgroundTryParse() {
        this.clearErrorMarkers()
        this.aceEditor.getSession()?.also { session ->
            this.aglWorker.interrupt(this.languageIdentity, editorId, session.id)
            this.notifyParse(ParseEventStart())
            this.aglWorker.tryParse(this.languageIdentity, editorId, session.id, this.agl.goalRule, this.text, this.agl.context)
        }
    }

    //TODO: allow this to be enabled for if a Worker is not wanted or not supported
    private fun foregroundParse() {
        val proc = this.agl.languageDefinition.processor
        if (null != proc) {
            try {
                val goalRule = this.agl.goalRule
                val sppt = proc.parse(this.text,goalRule)
                this.parseSuccess(sppt)
            } catch (e: ParseFailedException) {
                this.parseFailure(e.message!!, e.location, e.expected.toTypedArray(), e.longestMatch)
            } catch (t: Throwable) {
                this.log(LogLevel.Error, "Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: ${t.message}")
            }
        }
    }

    private fun foregroundSyntaxAnalysis() {
        val proc = this.agl.languageDefinition.processor
        val sppt = this.agl.sppt
        if (null != proc && null != sppt) {
            try {
                this.agl.asm = proc.syntaxAnalysis<Any,Any>(sppt)
                val event = SyntaxAnalysisEvent(true, "success", this.agl.asm!!)
                this.notifySyntaxAnalysis(event)
            } catch (e: SyntaxAnalyserException) {
                this.agl.asm = null
                val event = SyntaxAnalysisEvent(false, e.message!!, null)
                this.notifySyntaxAnalysis(event)
            } catch (t: Throwable) {
                this.log(LogLevel.Error, "Cannot process parse result in ${this.editorId} for language ${this.languageIdentity}: ${t.message}")
            }
        }
    }

    override fun clearErrorMarkers() {
        this.aceEditor.getSession()?.clearAnnotations(); //assume there are no parse errors or there would be no sppt!
        this.errorParseMarkerIds.forEach { id -> this.aceEditor.getSession()?.removeMarker(id) }
    }

    override fun parseFailure(message: String, location: InputLocation?, expected: Array<String>?, tree: Any?) {
        // a failure to parse is not an 'error' in the editor - we expect some parse failures
        this.log(LogLevel.Debug, "Cannot parse text in ${this.editorId} for language ${this.languageIdentity}: $message")
        // parse failed so re-tokenize from scan
        this.workerTokenizer.reset()
        this.resetTokenization()

        if (null != location) {
            val errMsg = when {
                null == expected -> "Syntax Error"
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
            this.aceEditor.getSession()?.setAnnotations(errors.toTypedArray())
            errors.forEach { err ->
                val range = ace.Range(err.row, err.column, err.row, err.column + 1)
                val cls = "ace_marker_text_error"
                val errMrkId = this.aceEditor.getSession()?.addMarker(range, cls, "text")
                if (null != errMrkId) this.errorParseMarkerIds.add(errMrkId)
            }
            val event = ParseEventFailure(message, tree)
            this.notifyParse(event)
        }
    }
}