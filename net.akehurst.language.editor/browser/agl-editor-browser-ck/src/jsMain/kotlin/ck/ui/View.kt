@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui

import org.w3c.dom.HTMLElement

 open external class View<TElement : HTMLElement> : ck.utils.dom.DomEmitter {
    val element: HTMLElement
    val isRendered: Boolean
}