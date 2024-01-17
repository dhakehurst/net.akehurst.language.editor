/**
 * Copyright (C) 2022 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
package net.akehurst.language.editor.browser.codemirror


import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.style.AglStyle
import net.akehurst.language.api.style.AglStyleModel
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.LogFunction
import net.akehurst.language.editor.common.*
import org.w3c.dom.AbstractWorker
import org.w3c.dom.Element

fun <T : Any> T.set(key: String, value: String): T {
    val self = this
    js("self[key] = value")
    return self
}

class AglErrorAnnotation(
    val line: Int,
    val column: Int,
    val text: String,
    val type: String,
    val raw: String?
) {
    val row = line - 1
}

fun <AsmType : Any, ContextType : Any> Agl.attachToCodeMirror(
    containerElement: Element,
    cmEditor: codemirror.view.IEditorView,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    worker: AbstractWorker,
    codemirror: codemirror.ICodeMirror
): AglEditor<AsmType, ContextType> {
    return AglEditorCodeMirror<AsmType, ContextType>(
        containerElement = containerElement,
        cmEditorView = cmEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        worker = worker,
        codemirror
    )
}

private class AglEditorCodeMirror<AsmType : Any, ContextType : Any>(
    val containerElement: Element,
    val cmEditorView: codemirror.view.IEditorView,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    worker: AbstractWorker,
    val codemirror: codemirror.ICodeMirror
) : AglEditorJsAbstract<AsmType, ContextType>(languageId, editorId, logFunction, worker) {

    private val errorParseMarkerIds = mutableListOf<Int>()
    private val errorProcessMarkerIds = mutableListOf<Int>()

    override val baseEditor: Any get() = this.cmEditorView
    private val _aglThemeCompartment = codemirror.createCompartment()
    private val _aglTokensCompartment = codemirror.createCompartment()
    private var _parseTimeout: dynamic = null

    override val sessionId: String get() = "none"

    /*
       private val _annotations = mutableListOf<CodeMirrorAnnotation>()

       val aceEditor: ace.Editor = ace.Editor(
           ace.VirtualRenderer(this.element, null),
           ace.Ace.createEditSession(""),
           objectJS { } //options are set later in init_
       )
   */
    override var text: String
        get() {
            try {
                return this.cmEditorView.state.doc.toString()
            } catch (t: Throwable) {
                throw RuntimeException("Failed to get text from editor")
            }
        }
        set(value) {
            try {
                this.cmEditorView.dispatch(
                    objectJSTyped<codemirror.state.TransactionSpec> {
                        changes = objectJSTyped<codemirror.state.ChangeSpec> {
                            from = 0
                            to = cmEditorView.state.doc.length
                            insert = value
                        }
                    }
                )
            } catch (t: Throwable) {
                throw RuntimeException("Failed to set text in editor")
            }
        }
    /*
        var parseTimeout: dynamic = null

        init {
            this.init_(options)
        }
     */
    /*
        fun init_(options: CodeMirrorOptions) {
            this.connectWorker(AglTokenizerByWorkerCodeMirror(this.agl))


            if (null != options.editor) this.aceEditor.setOptions(options.editor)
            if (null != options.renderer) this.aceEditor.renderer.setOptions(options.renderer)
            //TODO: set session and mouseHandler options

            this.aceEditor.getSession()?.bgTokenizer = AglBackgroundTokenizer(this.workerTokenizer as ace.Tokenizer, this.aceEditor)
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
    */

    init {
        val tokenizer = AglTokenizerByWorkerCodeMirror(this.codemirror, this.cmEditorView, this.agl)
        this.connectWorker(tokenizer)
        // add agl extensions
        this.cmEditorView.dispatch(objectJSTyped<codemirror.state.TransactionSpec> {
            effects = arrayOf(
                codemirror.StateEffect.appendConfig<Any>().of(
                    arrayOf(
                        codemirror.view.EditorView.updateListener.of({ view: codemirror.view.IViewUpdate ->
                            if (!view.docChanged || !view.viewportChanged) {
                                // do nothing
                            } else {
                                this@AglEditorCodeMirror.onEditorTextChange()
                            }
                        }),
                        _aglThemeCompartment.of(
                            codemirror.view.EditorView.theme(objectJS {})
                        ),
                        tokenizer._tokenUpdateListener,
                        tokenizer._decorationUpdater
                    )
                )
            )
        })

        this.updateLanguage(null)
        this.updateProcessor()
        this.updateStyle()
    }

    override fun destroy() {
        //this.aglWorker.worker.terminate()
        //this.aceEditor.destroy()
    }

    fun format() {
        /*
        val proc = this.agl.languageDefinition.processor
        if (null != proc) {
            val pos = this.aceEditor.getSelection().getCursor()
            val result = proc.format(this.text)
            if (null != result.sentence) {
                this.aceEditor.setValue(result.sentence!!, -1)
            } else {
                TODO()
            }
        }
         */
    }

    override fun configureSyntaxAnalyser(configuration: Map<String, Any>) {
        /*
        this.aceEditor.getSession()?.also { session ->
            this.aglWorker.configureSyntaxAnalyser(this.languageIdentity, editorId, session.id, configuration)
        }
        */
    }

    override fun updateLanguage(oldId: String?) {
        if (null != oldId) {
            val oldAglStyleClass = AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun updateStyle() {
        if (this.containerElement.isConnected) {
            val aglStyleClass = this.agl.styleHandler.aglStyleClass
            val str = this.editorSpecificStyleStr
            if (!str.isNullOrEmpty()) {
                this.agl.styleHandler.reset()
                val styleMdl: AglStyleModel? = Agl.registry.agl.style.processor!!.process(str).asm //TODO: pass context?
                if (null != styleMdl) {
                    val theme = objectJS {}
                    for (r in styleMdl.rules) {
                        val sel = r.selector.joinToString(separator = ", ") { ".${this.agl.styleHandler.mapClass(it.value)}" }
                        val css = objectJS {}
                        for (oldStyle in r.styles.values) {
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
                            (css as Any).set(style.name, style.value)
                        }
                        (theme as Any).set(sel, css)
                    }
                    //TODO: use computed facet instead of reconfiguring!
                    this.cmEditorView.dispatch(objectJSTyped<codemirror.state.TransactionSpec> {
                        effects = arrayOf(
                            _aglThemeCompartment.reconfigure(
                                codemirror.view.EditorView.theme(theme)
                            )
                        )
                    })

                    this.aglWorker.setStyle(this.languageIdentity, editorId, "", str)

                    // need to update because token style types may have changed, not just their attributes
                    this.onEditorTextChange()
                    this.resetTokenization(0)
                } else {
                    //TODO: cannot parse style rules
                }
            }
        }

        /*
        // style requires that the element is part of the dom
        if (this.element.isConnected) {
            this.aceEditor.getSession()?.also { session ->
                val aglStyleClass = this.agl.styleHandler.aglStyleClass
                val str = this.editorSpecificStyleStr
                if (null != str && str.isNotEmpty()) {
                    this.agl.styleHandler.reset()
                    val rules: List<AglStyleRule>? = Agl.registry.agl.style.processor!!.process(str).asm //TODO: pass context?
                    if (null != rules) {
                        var mappedCss = "" //TODO? this.agl.styleHandler.theme_cache // stored when theme is externally changed
                        rules.forEach { rule ->
                            val ruleClasses = rule.selector.map{
                                val mappedSelName = this.agl.styleHandler.mapClass(it)
                                ".ace_$mappedSelName"
                            }
                            val cssClasses = listOf(".$aglStyleClass") + ruleClasses
                            val mappedRule = AglStyleRule(cssClasses) // just used to map to css string
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
                    } else {
                        //TODO: cannot parse style rules
                    }
                }
            }
        }
         */
    }

    override fun onEditorTextChange() {
        if (doUpdate) {
            super.onEditorTextChange()
            this.workerTokenizer.reset() //current tokens are invalid if text changes
            window.clearTimeout(_parseTimeout)
            this._parseTimeout = window.setTimeout({
                this.workerTokenizer.acceptingTokens = true
                this.processSentence()
            }, 500)
        }
    }

    private fun update() {
        /*
        this.workerTokenizer.reset()
        window.clearTimeout(parseTimeout)
        this.parseTimeout = window.setTimeout({
            this.workerTokenizer.acceptingTokens = true
            this.processSentence()
        }, 500)

         */
    }

    @JsName("onResize")
    private fun onResize(entries: Array<dynamic>) {
        entries.forEach { entry ->
            if (entry.target == this.containerElement) {
                //               this.aceEditor.resize(true)
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

    override fun resetTokenization(fromLine: Int) {
       // (this.workerTokenizer as AglTokenizerByWorkerCodeMirror<AsmType, ContextType>).update()
    }

    override fun processSentence() {
        if (doUpdate) {
            this.clearErrorMarkers()
            this.aglWorker.interrupt(this.languageIdentity, editorId, "")
            this.aglWorker.processSentence(this.languageIdentity, editorId, "", this.text, this.agl.options)
        }
    }

    /*
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
*/
    override fun clearErrorMarkers() {
        /*
            this._annotations.clear()
            this.aceEditor.getSession()?.clearAnnotations(); //assume there are no parse errors or there would be no sppt!
            this.errorParseMarkerIds.forEach { id -> this.aceEditor.getSession()?.removeMarker(id) }
            */
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        /*
        val aceIssues = issues.map { issue ->
            val aceColumn = issue.location?.let { it.column - 1 } ?: 0
            val errMsg: String = when (issue.phase) {
                LanguageProcessorPhase.PARSE -> {
                    val expected = issue.data as Set<String>?
                    when {
                        null == expected -> "Syntax Error"
                        expected.isEmpty() -> "Syntax Error"
                        1 == expected.size -> "Syntax Error, expected: $expected"
                        else -> "Syntax Error, expected one of: $expected"
                    }
                }
                LanguageProcessorPhase.SYNTAX_ANALYSIS -> "Error ${issue.message}"
                LanguageProcessorPhase.SEMANTIC_ANALYSIS -> "Error ${issue.message}"
            }
            val errType = when (issue.kind) {
                LanguageIssueKind.ERROR -> "error"
                LanguageIssueKind.WARNING -> "warning"
                LanguageIssueKind.INFORMATION -> "information"
            }
            objectJSTyped<AceAnnotation> {
                row = issue.location?.let { it.line - 1 } ?: 0
                column = aceColumn
                text = errMsg //issue.message
                type = errType
                raw = null
            }
        }
        // add/update annotations to indicate errors in gutter
        this._annotations.addAll(aceIssues)
        this.aceEditor.getSession()?.setAnnotations(this._annotations.toTypedArray())
        // add markers to indicate in the actual text - i.e. underline
        issues.forEach { issue ->
            val errType = when (issue.kind) {
                LanguageIssueKind.ERROR -> "error"
                LanguageIssueKind.WARNING -> "warning"
                LanguageIssueKind.INFORMATION -> "information"
            }
            val row = issue.location?.let { it.line - 1 } ?: 0
            val startColumn = issue.location?.let { it.column - 1 } ?: 0
            val endColumn = startColumn + (issue.location?.length ?: 1)
            val range = ace.Range(row, startColumn, row, endColumn)
            val cls = "ace_marker_text_$errType"
            val errMrkId = this.aceEditor.getSession()?.addMarker(range, cls, "text")
            if (null != errMrkId) this.errorParseMarkerIds.add(errMrkId)
        }
         */
    }
}