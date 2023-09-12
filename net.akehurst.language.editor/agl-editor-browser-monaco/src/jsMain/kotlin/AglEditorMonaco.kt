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

package net.akehurst.language.editor.browser.monaco

import ResizeObserver
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import monaco.MarkerSeverity
import monaco.editor.IMarkerData
import monaco.editor.IStandaloneCodeEditor
import monaco.editor.ITokenThemeRule
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.processor.SentenceContext
import net.akehurst.language.api.style.AglStyle
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.*
import net.akehurst.language.editor.common.messages.*
import org.w3c.dom.AbstractWorker
import org.w3c.dom.Element
import org.w3c.dom.ParentNode

fun <AsmType : Any, ContextType : Any> Agl.attachToMonaco(
    containerElement: Element,
    monacoEditor: IStandaloneCodeEditor,
    languageId: String,
    editorId: String,
    worker:AbstractWorker
): AglEditor<AsmType, ContextType> {
    return AglEditorMonaco<AsmType, ContextType>(
        containerElement = containerElement,
        monacoEditor = monacoEditor,
        languageId = languageId,
        editorId = editorId,
        worker = worker
    )
}

private class AglEditorMonaco<AsmType : Any, ContextType : Any>(
    val containerElement: Element,
    val monacoEditor: IStandaloneCodeEditor,
    languageId: String,
    editorId: String,
    worker:AbstractWorker
) : AglEditorJsAbstract<AsmType, ContextType>(languageId, editorId, worker) {

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

    val languageThemePrefix = this.languageIdentity + "-"

    override val baseEditor: Any get() = this.monacoEditor

    override val sessionId: String get() = "none"

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

    var parseTimeout: dynamic = null

    init {
        this.init_()
    }

    private fun init_() {
        try {
            this.connectWorker(AglTokenizerByWorkerMonaco(this.monacoEditor, this.agl))
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

            monaco.editor.setModelLanguage(this.monacoEditor.getModel(), languageIdentity)
            monaco.editor.setTheme(aglGlobalTheme)

            monaco.languages.setTokensProvider(
                this.languageIdentity,
                this.workerTokenizer as monaco.languages.TokensProvider
            )
            monaco.languages.registerCompletionItemProvider(
                this.languageIdentity,
                AglCompletionProviderMonaco(this.agl)
            )

            this.onChange { this.update() }

            val resizeObserver = ResizeObserver { entries -> onResize(entries) }
            resizeObserver.observe(this.containerElement)

        } catch (t: Throwable) {
            console.error(t.message)
        }
    }

    override fun destroy() {
        //this.aglWorker.worker.terminate()
        //this.monacoEditor.destroy()
    }

    override fun updateLanguage(oldId: String?) {
        if (null != oldId) {
            val oldAglStyleClass =
                AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun configureSyntaxAnalyser(configuration: Map<String, Any>) {
        //this.aceEditor.getSession()?.also { session ->
        this.aglWorker.configureSyntaxAnalyser(this.languageIdentity, editorId, "", configuration) //TODO: sessionId
        //}
    }

    override fun updateStyle() {
        if (this.containerElement.isConnected) {
            val aglStyleClass = this.agl.styleHandler.aglStyleClass
            val styleStr = this.editorSpecificStyleStr
            if (null != styleStr && styleStr.isNotBlank()) {
                this.agl.styleHandler.reset()
                val styleMdl = Agl.registry.agl.style.processor!!.process(styleStr).asm //Why parse this again here !
                if (null != styleMdl) {
                    var mappedCss = ""
                    styleMdl.rules.forEach { rule ->
                        val ruleClasses = rule.selector.map {
                            val mappedSelName = this.agl.styleHandler.mapClass(it)
                            ".monaco_$mappedSelName"
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
                    val cssText: String = mappedCss
                    // remove the current style element for 'languageId' (which is used as the theme name) from the container
                    // else the theme css is not reapplied
                    val curStyle =
                        (this.containerElement.getRootNode() as ParentNode).querySelector("style#" + this.languageIdentity)
                    curStyle?.remove()

                    //add style element
                    val styleElement = this.containerElement.ownerDocument?.createElement("style")!!
                    styleElement.setAttribute("id", this.languageIdentity)
                    styleElement.textContent = cssText
                    this.containerElement.ownerDocument?.querySelector("head")?.appendChild(
                        styleElement
                    )
                    this.aglWorker.setStyle(languageIdentity, editorId, "", styleStr) //TODO: sessionId

                    // need to update because token style types may have changed, not just their attributes
                    this.update()
                    this.resetTokenization()
                } else {
                    //TODO: cannot process style rules
                }
            }
        }
    }

    private fun update() {
        if (doUpdate) {
            this.workerTokenizer.reset()
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

    override fun resetTokenization() {
        this.monacoEditor.getModel().resetTokenization()
    }

    override fun processSentence() {
        if (doUpdate) {
            this.clearErrorMarkers()
            this.aglWorker.interrupt(languageIdentity, editorId, "")//TODO: get session
            this.aglWorker.processSentence(
                languageIdentity,
                editorId,
                "",
                this.agl.goalRule,
                this.text,
                this.agl.context as SentenceContext<Any>?
            )
        }
    }

    /*
    private fun tryParse() {
        val proc = this.agl.languageDefinition.processor
        if (null != proc) {
            try {

                val goalRule = this.agl.goalRule
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
                val event = SyntaxAnalysisEventFailure(e.message!!, "No Asm")
                this.notifyProcess(event)
            } catch (t: Throwable) {
                console.error("Error processing parse result in " + this.editorId + " for language " + this.languageIdentity, t.message)
            }
        }
    }
    */
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

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        val monIssues = issues.mapNotNull { issue ->
            var errMsg: String? = null
            when (issue.phase) {
                LanguageProcessorPhase.GRAMMAR -> Unit
                LanguageProcessorPhase.PARSE -> {
                    val expected = issue.data as Set<String>?
                    errMsg = when {
                        null == expected -> "Syntax Error"
                        expected.isEmpty() -> "Syntax Error"
                        1 == expected.size -> "Syntax Error, expected: $expected"
                        else -> "Syntax Error, expected one of: $expected"
                    }
                }

                LanguageProcessorPhase.SYNTAX_ANALYSIS -> errMsg = "Error ${issue.message}"
                LanguageProcessorPhase.SEMANTIC_ANALYSIS -> errMsg = "Error ${issue.message}"
                LanguageProcessorPhase.FORMATTER -> errMsg = "Error ${issue.message}"
                LanguageProcessorPhase.INTERPRETER -> errMsg = "Error ${issue.message}"
                LanguageProcessorPhase.GENERATOR -> errMsg = "Error ${issue.message}"
                LanguageProcessorPhase.ALL -> errMsg = "Error ${issue.message}"
            }
            if (null != errMsg) {
                objectJSTyped<IMarkerData> {
                    code = null
                    severity = MarkerSeverity.Error
                    startLineNumber = issue.location?.line ?: 0
                    startColumn = issue.location?.column ?: 0
                    endLineNumber = issue.location?.line ?: 0
                    endColumn = issue.location?.column ?: 0
                    this.message = errMsg
                    source = null
                }
            } else {
                null
            }
        }
        monaco.editor.setModelMarkers(this.monacoEditor.getModel(), "", monIssues.toTypedArray())
    }

}

