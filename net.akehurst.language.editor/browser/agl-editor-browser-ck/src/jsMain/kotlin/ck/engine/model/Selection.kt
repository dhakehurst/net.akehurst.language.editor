@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model


external interface Selection {
    fun getSelectedElement(): Element?

    fun getFirstRange():Range?
    fun getFirstPosition(): Position?

    val anchor : Position?
}