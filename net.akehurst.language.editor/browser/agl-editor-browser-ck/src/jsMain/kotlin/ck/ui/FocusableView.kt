@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui

import org.w3c.dom.HTMLElement

external interface FocusableView : View<HTMLElement> {
    fun focus()
}