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

package net.akehurst.language.editor.browser.ace

import ResizeObserver
import ace.AceAnnotation
import ace.IRange
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.style.*
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.LogFunction
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.AglEditorJsAbstract
import net.akehurst.language.editor.common.AglStyleHandler
import net.akehurst.language.editor.common.objectJSTyped
import org.w3c.dom.AbstractWorker
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

interface IAce {
    fun createRange(startRow:Int, startColumn:Int, endRow:Int, endColumn:Int): IRange
}

/**
 * e.g.
 * worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
 */
fun <AsmType : Any, ContextType : Any> Agl.attachToAce(
    containerElement: Element,
    aceEditor: ace.IEditor,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    worker: AbstractWorker,
    ace:IAce
): AglEditor<AsmType, ContextType> {
    return AglEditorAce<AsmType, ContextType>(
        containerElement = containerElement,
        aceEditor = aceEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        worker = worker,
        ace=ace
    )
}

private class AglEditorAce<AsmType : Any, ContextType : Any>(
    val containerElement: Element,
    val aceEditor: ace.IEditor,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    worker: AbstractWorker,
    val ace:IAce
) : AglEditorJsAbstract<AsmType, ContextType>(languageId, editorId, logFunction, worker) {

    private val errorParseMarkerIds = mutableListOf<Int>()
    private val errorProcessMarkerIds = mutableListOf<Int>()
    private val _annotations = mutableListOf<AceAnnotation>()

    override val baseEditor: Any get() = this.aceEditor

    override val sessionId: String get() = this.aceEditor.getSession()?.id ?: "none"

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
        this.connectWorker(AglTokenizerByWorkerAce(this.agl))

        //TODO: set session and mouseHandler options

//        this.aceEditor.getSession()?.bgTokenizer = AglBackgroundTokenizer(this.workerTokenizer as ace.Tokenizer, this.aceEditor)
        this.aceEditor.getSession()?.bgTokenizer?.setTokenizer(this.workerTokenizer as ace.Tokenizer)
        this.aceEditor.getSession()?.bgTokenizer?.setDocument(this.aceEditor.getSession()?.getDocument())
        //this.aceEditor.commands.addCommand(ace.ext.Autocomplete.startCommand)
        this.aceEditor.completers = arrayOf(AglCodeCompleter(this.agl, this.aglWorker))

        this.aceEditor.on("change") { _ -> this.onEditorTextChange() }

        val resizeObserver = ResizeObserver { entries -> onResize(entries) }
        resizeObserver.observe(this.containerElement)

        this.updateLanguage(null)
        this.updateProcessor()
        this.updateStyle()
    }

    override fun destroy() {
        //this.aglWorker.worker.terminate()
        this.aceEditor.destroy()
    }

    fun format() {
        val proc = this.agl.languageDefinition.processor
        if (null != proc) {
            //val pos = this.aceEditor.getSelection().getCursor()
            val result = proc.format(this.text)
            if (null != result.sentence) {
                this.aceEditor.setValue(result.sentence!!, -1)
            } else {
                TODO()
            }
        }
    }

    override fun configureSyntaxAnalyser(configuration: Map<String, Any>) {
        this.aceEditor.getSession()?.also { session ->
            this.aglWorker.configureSyntaxAnalyser(this.languageIdentity, editorId, session.id, configuration)
        }
    }

    override fun updateLanguage(oldId: String?) {
        if (null != oldId) {
            val oldAglStyleClass = AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun updateStyle() {
        // style requires that the element is part of the dom
        if (this.containerElement.isConnected) {
            this.aceEditor.getSession()?.also { session ->
                val aglStyleClass = this.agl.styleHandler.aglStyleClass
                val str = this.editorSpecificStyleStr
                if (!str.isNullOrEmpty()) {
                    this.agl.styleHandler.reset()
                    val styleMdl: AglStyleModel? = Agl.registry.agl.style.processor!!.process(str).asm //TODO: pass context?
                    if (null != styleMdl) {
                        var mappedCss = "" //TODO? this.agl.styleHandler.theme_cache // stored when theme is externally changed
                        styleMdl.rules.forEach { rule ->
                            val ruleClasses = rule.selector.map {
                                val mappedSelName = this.agl.styleHandler.mapClass(it.value)
                                AglStyleSelector(".ace_$mappedSelName", it.kind)
                            }
                            val cssClasses = listOf(AglStyleSelector(".$aglStyleClass", AglStyleSelectorKind.LITERAL)) + ruleClasses
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

                        val root = this.containerElement.getRootNode() as ParentNode?
                        if (null != root) {
                            var curStyle = root.querySelector("style#$aglStyleClass")
                            if (null == curStyle) {
                                curStyle = this.containerElement.ownerDocument!!.createElement("style")
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
                        this.onEditorTextChange()
                        this.resetTokenization(0)
                    } else {
                        //TODO: cannot parse style rules
                    }
                }
            }
        }
    }

    override fun onEditorTextChange() {
        if (doUpdate) {
            super.onEditorTextChange()
            //this.workerTokenizer.reset()
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
                this.workerTokenizer.acceptingTokens = true
                this.processSentence()
            }, 500)
        }
    }

    @JsName("onResize")
    private fun onResize(entries: Array<dynamic>) {
        entries.forEach { entry ->
            if (entry.target == this.containerElement) {
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

    override fun resetTokenization(fromLine:Int) {
        val sess = this.aceEditor.getSession()
        if (null == sess) {
            this.log(LogLevel.Error, "session is null ??", null)
        } else {
            val bgt = sess.bgTokenizer
            if (null == bgt) {
                this.log(LogLevel.Error, "bgTokenizer is null ??", null)
            } else {
                bgt.start(fromLine)
                this.aceEditor.renderer.updateText()
            }
        }
    }

    override fun processSentence() {
        if (doUpdate) {
            this.clearErrorMarkers()
            this.aceEditor.getSession()?.also { session ->
                this.aglWorker.interrupt(this.languageIdentity, editorId, session.id)
                this.aglWorker.processSentence(this.languageIdentity, editorId, session.id, this.text, this.agl.options)
            }
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
        this._annotations.clear()
        this.aceEditor.getSession()?.clearAnnotations(); //assume there are no parse errors or there would be no sppt!
        this.errorParseMarkerIds.forEach { id -> this.aceEditor.getSession()?.removeMarker(id) }
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        val aceIssues = issues.mapNotNull { issue ->
            val aceColumn = issue.location?.let { it.column - 1 } ?: 0
            var errMsg: String? = null
            when (issue.phase) {
                LanguageProcessorPhase.GRAMMAR -> Unit
                LanguageProcessorPhase.SCAN -> errMsg = "Scan Error ${issue.message}"
                LanguageProcessorPhase.PARSE -> {
                    val expected = issue.data as Set<String>?
                    errMsg = when {
                        null == expected -> "Parse Error"
                        expected.isEmpty() -> "Parse Error"
                        1 == expected.size -> "Parse Error, expected: $expected"
                        else -> "Parse Error, expected one of: $expected"
                    }
                }

                LanguageProcessorPhase.SYNTAX_ANALYSIS -> errMsg = "Syntax Analysis Error ${issue.message}"
                LanguageProcessorPhase.SEMANTIC_ANALYSIS -> errMsg = "Semantic Analysis Error ${issue.message}"
                LanguageProcessorPhase.FORMAT -> errMsg = "Format Error ${issue.message}"
                LanguageProcessorPhase.INTERPRET -> errMsg = "Interpret Error ${issue.message}"
                LanguageProcessorPhase.GENERATE -> errMsg = "Generate Error ${issue.message}"
                LanguageProcessorPhase.ALL -> errMsg = "Error ${issue.message}"
            }
            if (null != errMsg) {
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
            } else {
                null
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
            val range = ace.createRange( row, startColumn, row, endColumn)
            val cls = "ace_marker_text_$errType"
            val errMrkId = this.aceEditor.getSession()?.addMarker(range, cls, "text")
            if (null != errMrkId) this.errorParseMarkerIds.add(errMrkId)
        }
    }
}