@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui

import ck.utils.BaseEvent
import ck.utils.EventInfo
import org.w3c.dom.HTMLElement

 open external class View<TElement : HTMLElement> : ck.utils.Emitter, ck.utils.dom.DomEmitter {
    val element: HTMLElement
    val isRendered: Boolean
    val locale: ck.utils.Locale?

     override fun <TArg, TEvent : BaseEvent<TArg>> on1(eventName: String, callback: (ev: EventInfo, arg: TArg) -> Unit, options: dynamic)
     override fun <TArg, TEvent : BaseEvent<TArg>> on2(eventName: String, callback: (ev: EventInfo, arg1: TArg, arg2: TArg) -> Unit, options: dynamic)
     override fun <TArg, TEvent : BaseEvent<TArg>> on3(eventName: String, callback: (ev: EventInfo, arg1: TArg, arg2: TArg, arg3: TArg) -> Unit, options: dynamic)
     override fun <TArg, TEvent : BaseEvent<TArg>> on5(eventName: String, callback: (ev: EventInfo, arg1: TArg, arg2: TArg, arg3: TArg, arg4: TArg, arg5: TArg) -> Unit, options: dynamic)
     override fun <TArg, TEvent : BaseEvent<TArg>> fire(eventInfoName: String, vararg args: TEvent)

     fun render()
}