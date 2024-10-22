/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.browser.ck

import js.iterable
import kotlinx.browser.window
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.EditorOptionsDefault
import net.akehurst.language.editor.common.objectJSTyped
import net.akehurst.language.issues.api.LanguageIssue
import org.w3c.dom.Element

fun <AsmType : Any, ContextType : Any> Agl.attachToCk(
    languageService: LanguageService,
    containerElement: Element,
    ckEditor: ck.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCk<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        ckEditor = ckEditor,
        languageId = languageId,
        editorId = editorId,
        editorOptions = editorOptions,
        logFunction = logFunction
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    aglEditor.initialise()
    return aglEditor
}

private class AglEditorCk<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val ckEditor: ck.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    editorOptions: EditorOptions,
    logFunction: LogFunction?
) : AglEditorAbstract<AsmType, ContextType>(
    languageServiceRequest, languageId, EndPointIdentity(editorId, "none"),
    editorOptions, logFunction
) {

    override val baseEditor: Any = ckEditor
    override var text: String
        get() = emi.rawText
        set(value) {
            console.log("Editor '${this.editorId}' text set to '$value'")
            val data = value.split("\n")
                .map {
                    "<p>$it</p>"
                }
                .joinToString(separator = "\n")
            ckEditor.setData(data)
        }

    override val isConnected: Boolean get() = this.containerElement.isConnected

    override val completionProvider: AglEditorCompletionProvider
        get() = TODO("not implemented")

    private var parseTimeout: dynamic = null
    private var emi: EditorModelIndex = EditorModelIndex()

    override val workerTokenizer: AglTokenizerByWorkerCk<AsmType, ContextType> = AglTokenizerByWorkerCk(this.agl, this.emi, logger)

    fun initialise() {
        // create style for underlining errors
        ckEditor.model.schema.extend("\$text", objectJSTyped { allowAttributes = "error" })
        val underlineStyle = objectJSTyped<dynamic> { }
        underlineStyle["text-decoration-line"] = "underline"
        underlineStyle["text-decoration-style"] = "wavy"
        underlineStyle["text-decoration-color"] = "red"
        ckEditor.conversion.attributeToElement(objectJSTyped {
            model = CkEditorHelper.ERROR_MARKER_ATTRIBUTE_NAME
            view = objectJSTyped {
                name = CkEditorHelper.ERROR_MARKER_ATTRIBUTE_NAME
                styles = underlineStyle
            }
        })

        ckEditor.model.document.on("change:data") {
            onEditorTextChangeInternal()
        }

        this.updateLanguage(null)
        this.updateProcessor()
        this.requestUpdateStyleModel()

        // trigger first sentence process
        onEditorTextChangeInternal()
    }

    override fun resetTokenization(fromLine: Int) {
        logger.log(LogLevel.Trace, "resetTokenization $fromLine")
        workerTokenizer.refresh()
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        logger.log(LogLevel.Trace, "updateLanguage $oldId")
    }

    override fun updateEditorStyles() {
        logger.log(LogLevel.Trace, "updateEditorStyles")
        val styleToAttrMap = mutableMapOf<String, Map<String, Any>>()
//TODO: clear current styles!
        this.agl.styleHandler.styleModel.allDefinitions.forEach { ss ->
            ss.rules.forEach { rule ->
                val ruleClasses = rule.selector.map {
                    this.agl.styleHandler.mapClass(it.value)
                }
                val attribs = rule.declaration.values.associate { oldStyle ->
                    when (oldStyle.name) {
                        "foreground" -> Pair("fontColor", oldStyle.value)
                        "background" -> Pair("fontBackgroundColor", oldStyle.value)
                        "font-style" -> when (oldStyle.value) {
                            "bold" -> Pair("bold", true)
                            "italic" -> Pair("italic", true)
                            else -> Pair(oldStyle.name, oldStyle.value)
                        }

                        else -> Pair(oldStyle.name, oldStyle.value)
                    }
                }
                ruleClasses.forEach {
                    styleToAttrMap[it] = attribs
                }
            }
        }

        this.workerTokenizer.updateStyleMap(styleToAttrMap)
    }

    override fun clearIssueMarkers() {
        logger.log(LogLevel.Trace, "clearIssueMarkers")

        // No need to explicitly do this as issues are added like styles
        // and styles are cleared before calling this method

        /*
        ckEditor.model.enqueueChange { writer ->
            val rootRange = writer.model.createRangeIn(writer.model.document.getRoot())
            val items = rootRange.getItems().iterable()
            for (item in items) {
                //val itemRng = writer.createRangeOn(item)
                for (attributeName in CkEditorHelper.getFormattingAttributeNames(item, writer.model.schema)) {
                    if (CkEditorHelper.ERROR_MARKER_ATTRIBUTE_NAME==attributeName) {
                        writer.removeAttribute(attributeName, item)
                    }
                }
            }
        }
         */
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        logger.log(LogLevel.Trace, "createIssueMarkers $issues")

    }

    override fun destroyAglEditor() {
    }

    override fun destroyBaseEditor() {
        this.ckEditor.destroy()
    }

    // --- AglEditorAbstract ---
    override fun onEditorTextChangeInternal() {
        logger.log(LogLevel.Trace, "onEditorTextChangeInternal")
        //console.log("onEditorTextChangeInternal, editor '${this.editorId}' text is '${this.text}'")
        if (doUpdate) {
            //console.log("doUpdate")
            super.onEditorTextChangeInternal()
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
                //console.log("new timeout")
                val oldText = this.text
                emi.update(ckEditor.model)
                if (emi.rawText != oldText) {
                    //console.log("rawtext changed")
                    this.processSentence()
                }
            }, 500)
        }
    }

    override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
        super.sentenceParseResponse(endPointIdentity, status, message, issues, tree)
        when (status) {
            MessageStatus.FAILURE -> this.resetTokenization(0) // reset to trigger use of scan tokens
            else -> Unit
        }
    }
}