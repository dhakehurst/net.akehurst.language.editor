@file:JsModule("@ckeditor/ckeditor5-core")
@file:JsNonModule
package ck.core.editor

external interface Editor {
    val model: ck.engine.model.Model
    val conversion: ck.engine.conversion.Conversion
    val ui: ck.ui.editorui.EditorUI
    val keystrokes : ck.core.EditingKeystrokeHandler
    val plugins: ck.core.PluginCollection
    val editing : ck.engine.controller.EditingController

    fun on(event: String, callback: () -> Unit)
    fun destroy()
    fun getData(): String
    fun setData(data: String)
    fun focus()
}