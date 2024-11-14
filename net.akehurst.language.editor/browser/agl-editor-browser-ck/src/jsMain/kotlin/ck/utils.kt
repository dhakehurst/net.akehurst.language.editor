@file:JsModule("@ckeditor/ckeditor5-utils")
@file:JsNonModule
package ck.utils

import org.w3c.dom.events.KeyboardEvent

external val keyCodes: dynamic

open external class Collection<T> {
    val length : Int
    val first : T
    val last : T

    fun clear()

    fun get(index:Int):T?
    fun add( item: T, index: Int? = definedExternally)

    fun getIndex(item:T) : Int
}

external interface FocusTracker : Emitter {
    //fun on(event: String, callback: () -> Unit)
}

external interface BaseEvent<TArg> {
    val name:String
    val arg:TArg
}

external interface EventInfo {
    val source:Any
    val name:String
    val path:Array<Any>
    fun stop()
    fun off()
}

external interface Emitter {
    fun <TArg, TEvent : BaseEvent<TArg>> on(eventName: String, callback: (ev:EventInfo, arg:TArg) -> Unit, options: dynamic = definedExternally )
}

external class Locale

external interface KeystrokeInfo {
    /**
     * The [key code](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/keyCode).
     */
    val keyCode: Number

    /**
     * Whether the <kbd>Alt</kbd> modifier was pressed.
     */
    val altKey: Boolean

    /**
     * Whether the <kbd>Cmd</kbd> modifier was pressed.
     */
    val metaKey: Boolean

    /**
     * Whether the <kbd>Ctrl</kbd> modifier was pressed.
     */
    val ctrlKey: Boolean

    /**
     * Whether the <kbd>Shift</kbd> modifier was pressed.
     */
    val shiftKey: Boolean
}

external interface KeystrokeHandler {

    /**
     * Registers a handler for the specified keystroke.
     *
     * @param keystroke Keystroke defined in a format accepted by
     * the {@link module:utils/keyboard~parseKeystroke} function. string | ReadonlyArray<string | number>
     * @param callback A function called with the
     * {@link module:engine/view/observer/keyobserver~KeyEventData key event data} object and
     * a helper function to call both `preventDefault()` and `stopPropagation()` on the underlying event.
     * @param options Additional options.
     */
    fun set(keystroke:dynamic, callback: (ev:KeyboardEvent, cancel:()->Unit) -> Unit, options: KeystrokeHandlerOptions = definedExternally )
}

external interface KeystrokeHandlerOptions {

    /**
     * The priority of the keystroke callback. The higher the priority value the sooner the callback will be executed.
     * Keystrokes having the same priority are called in the order they were added.
     */
    val priority: String?

    /**
     * An optional callback function allowing for filtering keystrokes based on arbitrary criteria.
     * The callback function receives `keydown` DOM event as a parameter.
     */
    val filter: (( keyEvtData: KeyboardEvent ) -> Boolean)?
}