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

import kotlinx.browser.window
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.objectJSTyped
import net.akehurst.language.issues.api.LanguageIssue
import org.w3c.dom.Element

fun <AsmType : Any, ContextType : Any> Agl.attachToCk(
    languageService: LanguageService,
    containerElement: Element,
    ckEditor: ck.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    logFunction: LogFunction?
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCk<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        ckEditor = ckEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    return aglEditor
}

private class AglEditorCk<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    val ckEditor: ck.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    logFunction: LogFunction?
) : AglEditorAbstract<AsmType, ContextType>(languageServiceRequest, languageId, EndPointIdentity(editorId, "none"), logFunction) {

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
    private var emi:EditorModelIndex = EditorModelIndex()

    override val workerTokenizer: AglTokenizerByWorkerCk<AsmType, ContextType> = AglTokenizerByWorkerCk(this.agl, this.emi, logger)

    init {

        // create style for underlining errors
        ckEditor.model.schema.extend("\$text", objectJSTyped { allowAttributes = "error" })
        val underlineStyle = objectJSTyped<dynamic> {  }
        underlineStyle["text-decoration-line"] = "underline"
        underlineStyle["text-decoration-style"] = "wavy"
        underlineStyle["text-decoration-color"] = "red"
        ckEditor.conversion.attributeToElement(objectJSTyped {
            model = "error"
            view = objectJSTyped {
                name = "error"
                styles = underlineStyle
            }
        })

        ckEditor.model.document.on("change:data") {
            onEditorTextChangeInternal()
        }

        this.updateLanguage(null)
        this.updateProcessor()
        this.requestUpdateStyleModel()
    }

    override fun resetTokenization(fromLine: Int) {
        workerTokenizer.reset()
        workerTokenizer.refresh()
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        //TODO("not implemented")
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        //TODO("not implemented")
    }

    override fun updateEditorStyles() {
        val styleToAttrMap = mutableMapOf<String,Map<String,Any>>()

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

    override fun clearErrorMarkers() {
        //TODO("not implemented")
    }

    override fun destroyAglEditor() {
    }

    override fun destroyBaseEditor() {
        this.ckEditor.destroy()
    }

    // --- AglEditorAbstract ---
    override fun onEditorTextChangeInternal() {
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
}