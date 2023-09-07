package net.akehurst.language.editor.browser.ace

import kotlinx.browser.document
import kotlinx.browser.window
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.common.external.WebComponent
import net.akehurst.language.editor.common.objectJS
import org.w3c.dom.*
import kotlin.reflect.KClass

open external class HTMLElement {
    fun querySelector(selector:String):Element
    fun hasAttribute(name:String):Boolean
    fun getAttribute(name:String): String
    fun setAttribute(name:String, value:String)
    fun removeAttribute(name:String)
    fun appendChild(node: Node): Node
    fun attachShadow(init: ShadowRootInit):ShadowRoot
}

abstract class AglEditorAceWebComponent<AsmType : Any, ContextType : Any> : HTMLElement(), WebComponent {

    companion object {
        fun defineAs(tag: String = "agl-editor-ace") {
            window.customElements.define(tag, AglEditorAceWebComponent::class.js.unsafeCast<() -> dynamic>())
        }
    }

    val KClass<AglEditorAceWebComponent<*,*>>.observedAttributes get() =
        arrayOf("languageId","editorId","workerScript","sharedWorker","grammarStr","styleStr")

    var aglEditor:AglEditor<AsmType,ContextType>? = null; private set

    var languageId: String?
        get() = this.getAttribute("languageId")
        set(value) {
            if(null!=value) {
                this.setAttribute("languageId", value)
                if(null!=this.aglEditor) this.aglEditor?.languageIdentity = value
            } else {
                this.removeAttribute("languageId")
            }
        }

    var editorId:String?
        get() = this.getAttribute("editorId")
        set(value) {
            if(null!=value) {
                this.setAttribute("editorId", value)
            } else {
                this.removeAttribute("editorId")
            }
        }

    var workerScript: String?
        get() = this.getAttribute("workerScript")
        set(value) {
            if(null!=value) {
                this.setAttribute("workerScript", value)
            } else {
                this.removeAttribute("workerScript")
            }
        }

    var sharedWorker: Boolean?
        get() = this.hasAttribute("sharedWorker")
        set(value) {
            if(null!=value) {
                this.setAttribute("sharedWorker", "true")
            } else {
                this.removeAttribute("workerScript")
            }
        }

    var grammarStr:String?
        get() = this.aglEditor?.languageDefinition?.grammarStr
        set(value) {
            if(value is String) {
                this.aglEditor?.languageDefinition?.grammarStr = value
            } else {
                this.aglEditor?.languageDefinition?.grammarStr = null
            }
        }

    var styleStr:String?
        get() = this.aglEditor?.editorSpecificStyleStr?: this.aglEditor?.languageDefinition?.styleStr
        set(value) {
            if(value is String) {
                this.aglEditor?.editorSpecificStyleStr = value
            } else {
                this.aglEditor?.editorSpecificStyleStr = null
            }
        }

    var text: String?
        get() = this.aglEditor?.text
        set(value) {
            if(null!=value) {
                this.aglEditor?.text = value
            } else {
                aglEditor?.text = ""
            }
        }

    var baseEditorStyle:String?
        get() = this.getAttribute("baseEditorStyle")
        set(value) {
            if(null!=value) {
                this.setAttribute("baseEditorStyle", value)
            } else {
                this.removeAttribute("baseEditorStyle")
            }
        }

    private var _initialised = false


    override fun connectedCallback() {
        if (this._initialised) {
            // do nothing
        } else {
            if (!this.hasAttribute("editorId")) this.editorId = this.getAttribute("id")
            if (!this.hasAttribute("languageId")) this.languageId = this.editorId
            if (!this.hasAttribute("workerScript")) this.workerScript = "net.akehurst.language.editor-agl-editor-worker.js"
            //if (this.hasAttribute("options")) this.options = JSON.parse(this.getAttribute("options")!!)
            if (!this.hasAttribute("grammarStr")) this.grammarStr = "net.akehurst.language.editor-agl-editor-worker.js"
            if (!this.hasAttribute("styleStr")) this.styleStr = "net.akehurst.language.editor-agl-editor-worker.js"

            val shadowRoot = this.attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).unsafeCast<HTMLElement>()

            val style = document.createElement("style")
            style.textContent = """
                :host {
                    display: flex;
                    min-height: 1em;
                    flex-direction: column;
                }
                #ace_editor {
                  flex: 1;
                  height:100%;
                }
            """.trimIndent()
            shadowRoot.appendChild(style)

            val editorStyle = document.createElement("style")
            editorStyle.textContent = this.baseEditorStyle
            shadowRoot.appendChild(editorStyle)

            val element = document.createElement("div")
            element.id = "ace_editor"

            val aceElement = this.querySelector("#ace_editor") ?: error("element with id '#ace_editor' not found")
            this.aglEditor = this.createAce(aceElement)
            (this.aglEditor!!.baseEditor as ace.Editor).renderer.attachToShadowRoot()
            this._initialised = true
        }

    }
    override fun disconnectedCallback() {
    }
    override fun adoptedCallback() {}
    override fun attributeChangedCallback(name: String, oldValue: String, newValue: String) {
        if (newValue !== oldValue) {
            this.setAttribute(name,newValue)
        }
    }

    private fun createAce(editorElement: Element): AglEditor<AsmType, ContextType> {
        val ed: ace.Editor = ace.Editor(
            ace.VirtualRenderer(editorElement, null),
            ace.Ace.createEditSession(""),
            objectJS { }
        )
        val aceOptions = objectJS {
            editor = objectJS {
                enableBasicAutocompletion = true
                enableSnippets = true
                enableLiveAutocompletion = false
            }
            renderer = {

            }
        }
        ed.setOptions(aceOptions.editor)
        ed.renderer.setOptions(aceOptions.renderer)
        return Agl.attachToAce(
            editorElement, ed,
            this.languageId!!, this.editorId!!, this.workerScript!!, true
        )
    }
}