@file:JsModule("@ckeditor/ckeditor5-utils")
@file:JsNonModule
package ck.utils

external interface FocusTracker {
    fun on(event: String, callback: () -> Unit)
}
