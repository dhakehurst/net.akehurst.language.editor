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
import monaco.IDisposable
import monaco.IPosition
import monaco.MarkerSeverity
import monaco.editor.*
import monaco.languages.CompletionItemKind
import monaco.languages.CompletionItemProvider
import monaco.languages.ILanguageExtensionPoint
import monaco.languages.TokensProvider
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.api.style.AglStyle
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.api.style.AglStyleSelector
import net.akehurst.language.api.style.AglStyleSelectorKind
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.AglStyleHandler
import net.akehurst.language.editor.common.AglTokenizerByWorker
import net.akehurst.language.editor.common.objectJSTyped
import org.w3c.dom.Element
import org.w3c.dom.ParentNode

fun <AsmType : Any, ContextType : Any> Agl.attachToMonaco(
    languageService: LanguageService,
    containerElement: Element,
    monacoEditor: IStandaloneCodeEditor,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    monaco: Monaco
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorMonaco<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        monacoEditor = monacoEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        monaco = monaco
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    return aglEditor
}

interface Monaco {
    fun defineTheme(themeName: String, themeData: IStandaloneThemeData)
    fun setModelMarkers(model: ITextModel, owner: String, markers: Array<IMarkerData>)
    fun setModelLanguage(model: ITextModel, languageId: String)
    fun setTheme(themeName: String)

    fun register(language: ILanguageExtensionPoint)
    fun setTokensProvider(languageId: String, provider: TokensProvider): IDisposable
    fun registerCompletionItemProvider(languageId: String, provider: CompletionItemProvider): IDisposable

    fun createCompletionItem(position: IPosition, aglCompletionItem: CompletionItem): monaco.languages.CompletionItem
}

interface ICompletionItemKind {
    val Text: CompletionItemKind
    val Snippet: CompletionItemKind
}

private class AglEditorMonaco<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val monacoEditor: IStandaloneCodeEditor,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    val monaco: Monaco,
) : AglEditorAbstract<AsmType, ContextType>(languageServiceRequest, languageId, EndPointIdentity(editorId,"none"), logFunction) {

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

    override val isConnected: Boolean get() = this.containerElement.isConnected

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

    override var workerTokenizer: AglTokenizerByWorker = AglTokenizerByWorkerMonaco(this.monacoEditor, this.agl)
    override val completionProvider: AglEditorCompletionProvider
        get() = TODO("not implemented")
    init {
        try {
            val themeData = objectJSTyped<IStandaloneThemeData> {
                base = "vs"
                inherit = false
                rules = emptyArray<ITokenThemeRule>()
                colors = object {}.also { o: dynamic ->
                    o["editor.foreground"] = "#000000"
                    o["editor.background"] = "#FFFFFE"
                }

            }
            // https://github.com/Microsoft/monaco-editor/issues/338
            // all editors on the same page must share the same theme!
            // hence we create a global theme and modify it as needed.
            monaco.defineTheme(aglGlobalTheme, themeData);

            monaco.register(objectJSTyped<monaco.languages.ILanguageExtensionPoint> {
                id = languageIdentity
            })
            //val languageId = this.languageId
            //val editorOptions = js("{language: languageId, value: initialContent, theme: theme, wordBasedSuggestions:false}")

            monaco.setModelLanguage(this.monacoEditor.getModel(), languageIdentity)
            monaco.setTheme(aglGlobalTheme)

            monaco.setTokensProvider(
                this.languageIdentity,
                this.workerTokenizer as monaco.languages.TokensProvider
            )
            monaco.registerCompletionItemProvider(
                this.languageIdentity,
                AglCompletionProviderMonaco(monaco, this.agl)
            )

            this.onChange { this.onEditorTextChangeInternal() }

            val resizeObserver = ResizeObserver { entries -> onResize(entries) }
            resizeObserver.observe(this.containerElement)

            this.updateLanguage(null)
            this.updateProcessor()
            this.requestUpdateStyleModel()
        } catch (t: Throwable) {
            console.error(t.message, t)
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

    override fun updateEditorStyles() {
        val aglStyleClass = this.agl.styleHandler.aglStyleClass
        var mappedCss = ""
        this.agl.styleHandler.styleModel.rules.forEach { rule ->
            val ruleClasses = rule.selector.map {
                val mappedSelName = this.agl.styleHandler.mapClass(it.value)
                AglStyleSelector(".monaco_$mappedSelName", it.kind)
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

        // need to update because token style types may have changed, not just their attributes
        this.onEditorTextChangeInternal()
        this.resetTokenization(0)
    }

    override fun onEditorTextChangeInternal() {
        if (doUpdate) {
            super.onEditorTextChangeInternal()
            this.workerTokenizer.reset()
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
//                this.workerTokenizer.acceptingTokens = true
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
        monaco.setModelMarkers(this.monacoEditor.getModel(), "", emptyArray())
    }

    fun onChange(handler: (String) -> Unit) {
        this.monacoEditor.onDidChangeModelContent { event ->
            val text = this.text
            handler(text);
        }
    }

    override fun resetTokenization(fromLine: Int) {
        this.monacoEditor.getModel().tokenization.resetTokenization()
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

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        val monIssues = issues.mapNotNull { issue ->
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
                LanguageProcessorPhase.GENERATE -> errMsg = "Generate ${issue.message}"
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
        monaco.setModelMarkers(this.monacoEditor.getModel(), "", monIssues.toTypedArray())
    }

}

