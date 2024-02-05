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
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.api.LanguageServiceRequest
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
    languageService: LanguageService,
    containerElement: Element,
    cmEditor: codemirror.view.IEditorView,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    codemirror: codemirror.ICodeMirror
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCodeMirror<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        cmEditorView = cmEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        codemirror = codemirror
    )
    languageService.addResponseListener(aglEditor.endPointId, aglEditor)
    return aglEditor
}

private class AglEditorCodeMirror<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val cmEditorView: codemirror.view.IEditorView,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    val codemirror: codemirror.ICodeMirror,
) : AglEditorJsAbstract<AsmType, ContextType>(languageServiceRequest, languageId, editorId, logFunction) {

    private val errorParseMarkerIds = mutableListOf<Int>()
    private val errorProcessMarkerIds = mutableListOf<Int>()

    override val baseEditor: Any get() = this.cmEditorView
    private val _aglThemeCompartment = codemirror.createCompartment()
    private val _aglTokensCompartment = codemirror.createCompartment()
    private var _parseTimeout: dynamic = null

    override val sessionId: String get() = "none"
    override val isConnected: Boolean get() = this.containerElement.isConnected

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

    override val workerTokenizer = AglTokenizerByWorkerCodeMirror(this.codemirror, this.cmEditorView, this.agl)

    init {
        val tokenizer =
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
                            workerTokenizer._tokenUpdateListener,
                            workerTokenizer._decorationUpdater
                        )
                    )
                )
            })

        this.updateLanguage(null)
        this.updateProcessor()
        this.requestUpdateStyleModel()
    }

    override fun destroy() {
        //this.aglWorker.worker.terminate()
        //this.aceEditor.destroy()
    }

    fun format() {

    }

    override fun updateLanguage(oldId: String?) {
        if (null != oldId) {
            val oldAglStyleClass = AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun updateEditorStyles() {
        val aglStyleClass = this.agl.styleHandler.aglStyleClass
        val theme = objectJS {}
        for (r in this.agl.styleHandler.styleModel.rules) {
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

        // need to update because token style types may have changed, not just their attributes
        this.onEditorTextChange()
        this.resetTokenization(0)
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

    }

    override fun resetTokenization(fromLine: Int) {
        // (this.workerTokenizer as AglTokenizerByWorkerCodeMirror<AsmType, ContextType>).update()
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