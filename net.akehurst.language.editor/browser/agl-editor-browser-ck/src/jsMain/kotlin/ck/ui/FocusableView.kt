@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui

import org.w3c.dom.HTMLElement

open external class FocusableView : View<HTMLElement> {
    open fun focus()
}