@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.bindings

import org.w3c.dom.HTMLElement

external interface ClickOutsideOptions {
    var activator: () -> Boolean
    var callback: () -> Unit
    var contextElements: () -> Array<HTMLElement>
    var emitter : ck.utils.dom.DomEmitter
    var listenerOptions: Array<dynamic> //TODO: CallbackOptions
}

external fun clickOutsideHandler( options: ClickOutsideOptions )

