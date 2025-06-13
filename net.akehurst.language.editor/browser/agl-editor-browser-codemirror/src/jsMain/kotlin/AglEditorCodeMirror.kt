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


import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.*
import net.akehurst.language.editor.common.AglStyleHandlerAbstract.Companion.AGL_STYLE_PREFIX
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.issues.api.LanguageIssueKind
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.sentence.api.InputLocation
import org.w3c.dom.Element
import kotlin.js.Promise
import kotlin.math.min

class AglErrorAnnotation(
    val line: Int,
    val column: Int,
    val text: String,
    val type: String,
    val raw: String?
) {
    val row = line - 1
}

class DeferredIssues(
    val resolve: (Array<codemirror.lint.Diagnostic>) -> Unit,
    val reject: (Throwable) -> Unit
)

fun <AsmType : Any, ContextType : Any> Agl.attachToCodeMirror(
    languageService: LanguageService,
    containerElement: Element,
    cmEditor: codemirror.view.IEditorView,
    languageDefinition: LanguageDefinition<AsmType,ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction,
    codemirror: codemirror.ICodeMirror
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCodeMirror<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        cmEditorView = cmEditor,
        languageDefinition = languageDefinition,
        processOptions = processOptions,
        editorId = editorId,
        editorOptions = editorOptions,
        logFunction = logFunction,
        codemirrorFunctions = codemirror
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    return aglEditor
}

internal class AglEditorCodeMirror<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val cmEditorView: codemirror.view.IEditorView,
    languageDefinition: LanguageDefinition<AsmType,ContextType>,
    processOptions: () -> ProcessOptions<AsmType, ContextType>,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction,
    val codemirrorFunctions: codemirror.ICodeMirror,
) : AglEditorAbstract<AsmType, ContextType, CssClassStyle>(
    languageServiceRequest, languageDefinition, processOptions, EndPointIdentity(editorId,"none"),
    editorOptions,logFunction, AglStyleHandlerCssClass(languageDefinition.identity)
) {

    override val baseEditor: Any get() = this.cmEditorView
    private val _aglThemeCompartment = codemirrorFunctions.state.createCompartment()
    private val _aglTokensCompartment = codemirrorFunctions.state.createCompartment()
    private var _parseTimeout: dynamic = null
    private val _issueMarkers = mutableListOf<codemirror.lint.Diagnostic>()
    private var _needsRefresh = true


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

    override val workerTokenizer = AglTokenizerByWorkerCodeMirror(this.codemirrorFunctions, this.cmEditorView, this.agl)
    override val completionProvider = AglCompletionProviderCodeMirror(this)
    private var _linterPromise = mutableListOf<DeferredIssues>()
    private val _cmStyleHandler get() = agl.styleHandler as AglStyleHandlerCssClass

    init {
        // add agl extensions
        this.cmEditorView.dispatch(objectJSTyped<codemirror.state.TransactionSpec> {
            effects = arrayOf(
                codemirrorFunctions.state.StateEffect.appendConfig<Any>().of(
                    arrayOf(
                        // react to text changes
                        codemirrorFunctions.view.EditorView.updateListener.of({ view: codemirror.view.IViewUpdate ->
                            if (view.docChanged || view.viewportChanged) {
                                this@AglEditorCodeMirror.onEditorTextChanged(view.view.state.doc.toString())
                            }
                        }),
                        // theme and token colors
                        _aglThemeCompartment.of(codemirrorFunctions.view.EditorView.theme(objectJS {})),
                        workerTokenizer._tokenUpdateListener,
                        workerTokenizer._decorationUpdater,
                        // autocomplete
                        codemirrorFunctions.extensions.autocomplete.autocompletion(
                            (objectJS {} as Any).set(
                                "override", arrayOf(completionProvider::autocompletion)
                            )
                        ),
                        // markers (errors etc)
                        codemirrorFunctions.extensions.lint.lintGutter(objectJS { }),
                        codemirrorFunctions.extensions.lint.linter(
                            source = ::lintSource,
                            config = codemirror.lint.LinterConfigDefault(
                                needsRefresh = { _ -> _needsRefresh.also { _needsRefresh = false } }
                            )
                        )
                    )
                )
            )
        })

        this.updateLanguage(null)
        this.refreshProcessor()
        this.refreshStyleHandler()
    }

    override fun destroyAglEditor() {

    }

    override fun destroyBaseEditor() {

    }

    fun format() {

    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        if (null != oldId) {
            val oldAglStyleClass = CssClassStyle(EditorStyleIdentity("$AGL_STYLE_PREFIX-${oldId.value}")).cssClassName
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(_cmStyleHandler.languageCssClassStyle.cssClassName)
    }

    override fun updateEditorStyles() {
        val mappedCss = _cmStyleHandler.stylesToCss()

        val theme = objectJS {}
        TODO()
        /*
        for (ss in this.agl.styleHandler.styleModel.allDefinitions) {
            for (r in ss.rules) {
            val sel:String = when(r) {
                is AglStyleTagRule ->r.selector.joinToString(separator = ", ") {
                    val mappedSelName = this._cmStyleHandler.convert(it)
                    ".$mappedSelName"
                }
                is AglStyleMetaRule -> {
                    val mappedSelName = this._cmStyleHandler.convert("\$\$" + r.pattern.pattern)
                    ".$mappedSelName"
                }
                else -> error("Subtype not handled")
            }
            val css = objectJS {}
            for (oldStyle in r.declaration.values) {
                val style = when (oldStyle.name) {
                    "foreground" -> Pair("color", oldStyle.value)
                    "background" -> Pair("background-color", oldStyle.value)
                    "font-style" -> when (oldStyle.value) {
                        "bold" -> Pair("font-weight", oldStyle.value)
                        "italic" -> Pair("font-style", oldStyle.value)
                        else -> Pair(oldStyle.name, oldStyle.value)
                    }

                    else -> Pair(oldStyle.name, oldStyle.value)
                }
                (css as Any).set(style.first, style.second)
            }
            (theme as Any).set(sel, css)
        }
        }
         */
        //TODO: use computed facet instead of reconfiguring!
        this.cmEditorView.dispatch(objectJSTyped<codemirror.state.TransactionSpec> {
            effects = arrayOf(
                _aglThemeCompartment.reconfigure(
                    codemirrorFunctions.view.EditorView.theme(theme)
                )
            )
        })

        // need to update because token style types may have changed, not just their attributes
        this.onEditorTextChangeInternal(this.text)
        this.resetTokenization(0)
    }

    // linter detects changes and calls this when idle
    private fun lintSource(view: codemirror.view.IEditorView): Promise<Array<codemirror.lint.Diagnostic>> {
        return if(doUpdate) {
            this.workerTokenizer.reset()
            this.processSentence(view.state.doc.toString())
            Promise { resolve, reject ->
                _linterPromise.add(DeferredIssues(resolve, reject))
            }
        } else {
            Promise { resolve, reject ->  resolve(emptyArray()) }
        }

    }

    private fun onEditorTextChanged(newText:String) {
        _needsRefresh = true
        super.onEditorTextChangeInternal(newText)
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
    override fun clearIssueMarkers() {
        _issueMarkers.clear()
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        val editorMarkers = issues.map { issue ->
            val loc = issue.location ?: InputLocation(0, 1, 1, 1, agl.options.invoke().parse.sentenceIdentity.invoke() ?: editorId)
            val errMsg = when (issue.phase) {
                LanguageProcessorPhase.GRAMMAR -> "Grammar Error: ${issue.message}"
                LanguageProcessorPhase.SCAN -> "Scan Error ${issue.message}"
                LanguageProcessorPhase.PARSE -> {
                    val expected = issue.data as Set<String>?
                    when {
                        null == expected -> "Parse Error"
                        expected.isEmpty() -> "Parse Error"
                        1 == expected.size -> "Parse Error, expected: $expected"
                        else -> "Parse Error, expected one of: $expected"
                    }
                }
                LanguageProcessorPhase.SYNTAX_ANALYSIS -> "Syntax Analysis Error ${issue.message}"
                LanguageProcessorPhase.SEMANTIC_ANALYSIS -> "Semantic Analysis Error ${issue.message}"
                LanguageProcessorPhase.FORMAT -> "Format Error ${issue.message}"
                LanguageProcessorPhase.INTERPRET -> "Interpret Error ${issue.message}"
                LanguageProcessorPhase.GENERATE -> "Generate ${issue.message}"
                LanguageProcessorPhase.ALL -> "Error ${issue.message}"
            }
            val eot = text.length
            codemirror.lint.DiagnosticDefault(
                from = min(eot, loc.position),
                to = min(eot, loc.position + loc.length),
                severity = when (issue.kind) {
                    LanguageIssueKind.ERROR -> codemirror.lint.SeverityKind.error
                    LanguageIssueKind.WARNING -> codemirror.lint.SeverityKind.warning
                    LanguageIssueKind.INFORMATION -> codemirror.lint.SeverityKind.info
                },
                message = errMsg
            )
        }
        displayErrorMarkers(editorMarkers)
    }

    private fun displayErrorMarkers(editorMarkers: List<codemirror.lint.Diagnostic>) {
        val prm = _linterPromise.removeFirstOrNull()
        when {
            null == prm -> Unit
            else -> {
                prm.resolve(editorMarkers.toTypedArray())
            }
        }
    }
}