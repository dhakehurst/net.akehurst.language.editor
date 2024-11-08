@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.view.observer

import ck.utils.BaseEvent

external interface ViewDocumentKeyDownEvent : BaseEvent<KeyEventData>
external interface ViewDocumentKeyUpEvent : BaseEvent<KeyEventData>

external interface KeyEventData : DomEventData<org.w3c.dom.events.KeyboardEvent>, ck.utils.KeystrokeInfo {
    val keystroke:Number
}

external interface DomEventData<TEvent : org.w3c.dom.events.Event> {
    fun preventDefault()
    fun stopPropagation()
}