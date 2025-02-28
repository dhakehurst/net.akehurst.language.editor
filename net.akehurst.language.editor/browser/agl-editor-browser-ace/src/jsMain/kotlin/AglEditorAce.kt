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

import ace.AceAnnotation
import ace.IRange
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.*
import net.akehurst.language.editor.common.AglStyleHandlerAbstract.Companion.AGL_STYLE_PREFIX
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind
import net.akehurst.language.issues.api.LanguageProcessorPhase
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
    fun createRange(startRow: Int, startColumn: Int, endRow: Int, endColumn: Int): IRange
}

/**
 * e.g.
 * worker = SharedWorker(workerScriptName, options = WorkerOptions(type = WorkerType.MODULE))
 */
fun <AsmType : Any, ContextType : Any> Agl.attachToAce(
    languageService: LanguageService,
    containerElement: Element,
    aceEditor: ace.IEditor,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?,
    ace: IAce
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorAce<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        aceEditor = aceEditor,
        languageDefinition = languageDefinition,
        editorId = editorId,
        editorOptions = editorOptions,
        logFunction = logFunction,
        ace = ace
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    aglEditor.initialise()
    return aglEditor
}

private class AglEditorAce<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val aceEditor: ace.IEditor,
    languageDefinition: LanguageDefinition<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?,
    val ace: IAce,
) : AglEditorAbstract<AsmType, ContextType, CssClassStyle>(
    languageServiceRequest, languageDefinition, EndPointIdentity(editorId, aceEditor.getSession()?.id!!),
    editorOptions, logFunction, AglStyleHandlerCssClass(languageDefinition.identity)
) {

    private val errorParseMarkerIds = mutableListOf<Int>()
    private val errorProcessMarkerIds = mutableListOf<Int>()
    private val _annotations = mutableListOf<AceAnnotation>()

    override val baseEditor: Any get() = this.aceEditor

    override val isConnected: Boolean get() = this.containerElement.isConnected

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

    private var parseTimeout: dynamic = null

    override var workerTokenizer: AglTokenizerByWorker<CssClassStyle> = AglTokenizerByWorkerAce(this.agl)
    override val completionProvider: AglEditorCompletionProvider
        get() = TODO("not implemented")

    private val _aceStyleHandler get() = agl.styleHandler as AglStyleHandlerCssClass

    fun initialise() {
        //TODO: set session and mouseHandler options
        this.aceEditor.getSession()?.bgTokenizer?.setTokenizer(this.workerTokenizer as ace.Tokenizer)
        this.aceEditor.getSession()?.bgTokenizer?.setDocument(this.aceEditor.getSession()?.getDocument())
        //this.aceEditor.commands.addCommand(ace.ext.Autocomplete.startCommand)
        this.aceEditor.completers = arrayOf(AglCodeCompleterAce(this.agl, this.languageServiceRequest))

        this.aceEditor.on("change") { eventName -> this.onEditorTextChangeInternal(this.text) }

        this.updateLanguage(null)
        this.refreshProcessor()
        this.refreshStyleHandler()
    }

    override fun resetTokenization(fromLine: Int) {
        val sess = this.aceEditor.getSession()
        if (null == sess) {
            this.logger.logError { "session is null ??" }
        } else {
            val bgt = sess.bgTokenizer
            if (null == bgt) {
                logger.logError{ "bgTokenizer is null ??"}
            } else {
                bgt.start(fromLine)
                this.aceEditor.renderer.updateText()
            }
        }
    }

    override fun destroyAglEditor() {
    }

    override fun destroyBaseEditor() {
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

    override fun updateLanguage(oldId: LanguageIdentity?) {
        if (null != oldId) {
            val oldAglStyleClass = CssClassStyle(EditorStyleIdentity("$AGL_STYLE_PREFIX-${oldId.value}")).cssClassName
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(_aceStyleHandler.languageCssClassStyle.cssClassName)
    }

    override fun updateEditorStyles() {
        val mappedCss = _aceStyleHandler.stylesToCss()
        val root = this.containerElement.getRootNode() as ParentNode?
        if (null != root) {
            var curStyle = root.querySelector("style#${_aceStyleHandler.languageCssClassStyle.cssClassName}")
            if (null == curStyle) {
                curStyle = this.containerElement.ownerDocument!!.createElement("style")
                curStyle.id = _aceStyleHandler.languageCssClassStyle.cssClassName
                if (root == curStyle.ownerDocument) {
                    curStyle.ownerDocument!!.head!!.prepend(curStyle)
                } else {
                    //shadowDom case
                    root.prepend(curStyle)
                }
            }
            curStyle.textContent = mappedCss
        }

        // need to update because token style types may have changed, not just their attributes
        this.onEditorTextChangeInternal(this.text)
        this.resetTokenization(0)
    }

    override fun onEditorTextChangeInternal(newText: String) {
        if (doUpdate) {
            super.onEditorTextChangeInternal(newText)
            //this.workerTokenizer.reset()
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
//                this.workerTokenizer.acceptingTokens = true
                this.processSentence(newText)
            }, 500)
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

    override fun clearIssueMarkers() {
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
            val range = ace.createRange(row, startColumn, row, endColumn)
            val cls = "ace_marker_text_$errType"
            val errMrkId = this.aceEditor.getSession()?.addMarker(range, cls, "text")
            if (null != errMrkId) this.errorParseMarkerIds.add(errMrkId)
        }
    }
}