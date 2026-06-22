@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model

external interface Document {

    val selection: Selection

    fun getRoot(name: String = definedExternally): RootElement

    fun on(event: String, callback: () -> Unit)
}