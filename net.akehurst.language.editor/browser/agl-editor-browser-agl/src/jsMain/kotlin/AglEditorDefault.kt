/**
 * Copyright (C) 2023 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.browser.agl

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.createElement
import kotlinx.dom.removeClass
import net.akehurst.kotlin.html5.create
import net.akehurst.kotlin.html5.update
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.SentenceContext
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.common.AglEditorJsAbstract
import net.akehurst.language.editor.common.AglStyleHandler
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import kotlin.js.RegExp

fun <AsmType : Any, ContextType : Any> Agl.attachToAglEditor(
    containerElement: Element,
    languageId: String,
    editorId: String,
    worker: AbstractWorker
): AglEditor<AsmType, ContextType> {
    return AglEditorDefault<AsmType, ContextType>(
        containerElement = containerElement,
        languageId = languageId,
        editorId = editorId,
        worker = worker
    )
}


class AglEditorDefault<AsmType : Any, ContextType : Any>(
    val containerElement: Element,
    languageId: String,
    editorId: String,
    worker:AbstractWorker
) : AglEditorJsAbstract<AsmType, ContextType>(languageId, editorId, worker) {

    override val baseEditor: Any get() = this
    override var text: String
        get() = editing.value
        set(value) {
            editing.value = value
        }

    override fun resetTokenization() {
        highlight(text)
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        //TODO("not implemented")
    }

    override val sessionId: String get() = "session" //TODO

    override fun updateLanguage(oldId: String?) {
        if (null != oldId) {
            val oldAglStyleClass = AglStyleHandler.languageIdToStyleClass(this.agl.styleHandler.cssClassPrefixStart, oldId)
            this.containerElement.removeClass(oldAglStyleClass)
        }
        this.containerElement.addClass(this.agl.styleHandler.aglStyleClass)
    }

    override fun updateStyle() {
        //TODO("not implemented")
    }

    override fun processSentence() {
        if (doUpdate) {
            this.clearErrorMarkers()
            this.aglWorker.interrupt(this.languageIdentity, editorId, sessionId)
            this.aglWorker.processSentence(this.languageIdentity, editorId, sessionId, this.agl.goalRule, this.text, this.agl.context as SentenceContext<Any>?)
        }
    }

    override fun configureSyntaxAnalyser(configuration: Map<String, Any>) {
        //TODO("not implemented")
    }

    override fun clearErrorMarkers() {
        //TODO("not implemented")
    }

    override fun destroy() {
        //TODO("not implemented")
    }

    // --- implementation ---
    lateinit var editing:HTMLTextAreaElement
    lateinit var highlightedContent:HTMLElement

    private var parseTimeout: dynamic = null

    init {
        this.init_()
    }

    private fun init_() {
        containerElement.update {
            class_.add("agl-editor")
            textarea {
                class_.add("editing")
            }
            htmlElement("pre") {
                class_.add("highlighted-pre")
                code {
                    class_.add("highlighted-content")
                }
            }
        }
        val css = """
.agl-editor {
  display: grid;
  grid-template-areas: "one";
}
.editing, .highlighted-pre {
  display: grid;
  grid-area: one; 
  overflow: auto;
  text-wrap: nowrap; /* enable horizontal scroll */
  white-space-collapse: preserve;
  
  margin: 0;
  padding: 1.2em;
  padding-bottom: 50%;
}

.editing, .highlighted-pre, .highlighted-pre * {
  font-size: 15pt;
  font-family: monospace;
  line-height: 1.5;
  tab-size: 2;
}

/* editing text area in front of result highlighting
   and editing should be transparent
*/
.editing {
  z-index: 1;
  color: transparent;
  background: transparent;
  caret-color: blue;
}
.highlighted-pre {
  z-index: 0;

}
.highlighted-code {

}
"""
        document.head?.update {
            htmlElement("style").textContent = css
        }
        editing = containerElement.querySelector(".editing") as HTMLTextAreaElement
        highlightedContent = containerElement.querySelector(".highlighted-content") as HTMLElement
        editing.oninput=this::oninput
        editing.onscroll=this::onscroll
        editing.onkeydown=this::onkeydown

        this.connectWorker(AglTokenizerByWorkerDefault(this.agl))

        this.updateLanguage(null)
        this.updateProcessor()
        this.updateStyle()
    }

    private fun oninput(ev:InputEvent) {
        var newText = editing.value
        when {
            newText.isBlank() -> {
                highlightedContent.innerHTML = ""
            }
            else -> {
                if(newText.last() == '\n') {
                    newText += " ";
                }
            }
        }
        textUpdated()
    }
    private fun onscroll(ev:Event) {

    }
    private fun onkeydown(ev:KeyboardEvent) {

    }

    private fun textUpdated() {
        if (doUpdate) {
            this.workerTokenizer.reset()
            window.clearTimeout(parseTimeout)
            this.parseTimeout = window.setTimeout({
                this.workerTokenizer.acceptingTokens = true
                this.processSentence()
            }, 500)
        }
    }

    private fun highlight(inputText:String) {
        //TODO: update only edited line onward
        val tokenised = this.workerTokenizer.tokensByLine.values.flatten().joinToString(separator = "") {
            val class_ = it.styles.joinToString(separator = " ")
            "<span class='$class_'>${it.value}</span>"
        }
        val encoded = tokenised.replace(Regex("&"), "&amp;").replace(Regex("<"), "&lt;")
        highlightedContent.innerHTML = encoded
    }
}