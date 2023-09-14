package ace

import org.w3c.dom.Element

@JsModule("ace-builds/src-noconflict/ace.js")
@JsNonModule
external object Ace {

    fun <T : Any> require(moduleName: String): T = definedExternally

    fun createEditSession(text: String): EditSession

}

@JsModule("net.akehurst.language.editor-kotlin-ace-loader!?id=ace/editor&name=Editor")
@JsNonModule
external class Editor(
    renderer: VirtualRenderer,
    session: EditSession,
    options:Any?
) : ace.IEditor {
    override val commands: dynamic
    override var completers: Array<dynamic> //TODO:
    override  val renderer: dynamic

    override  fun getValue(): String
    override  fun setValue(value: String, cursorPos: Int)
    override  fun getSession(): EditSession?
    override  fun setOption(option: String, module: dynamic)
    override fun setOptions(options: dynamic)
    override fun on(eventName: String, function: (dynamic) -> Unit)
    override fun resize(force: Boolean)
    override fun getSelection(): dynamic //TODO:

    override  fun destroy()
}

@JsModule("net.akehurst.language.editor-kotlin-ace-loader!?id=ace/virtual_renderer&name=VirtualRenderer")
@JsNonModule
external class VirtualRenderer(
    container: Element,
    theme: String?
)

@JsModule("net.akehurst.language.editor-kotlin-ace-loader!?id=ace/range&name=Range")
@JsNonModule
external class Range(
    startRow:Int,
    startColumn:Int,
    endRow:Int,
    endColumn:Int
) : IRange {
    override var startRow: Int
    override var startColumn: Int
    override var endRow: Int
    override var endColumn: Int
}