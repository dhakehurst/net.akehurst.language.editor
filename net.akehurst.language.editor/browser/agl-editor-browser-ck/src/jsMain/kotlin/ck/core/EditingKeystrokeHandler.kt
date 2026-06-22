@file:JsModule("@ckeditor/ckeditor5-core")
@file:JsNonModule
package ck.core

external interface EditingKeystrokeHandler {
    /**
     * Registers a handler for the specified keystroke.
     * @param keystroke : string | Array<string | number>
     */
    fun set(keystroke:dynamic, callback: () -> Unit, options: EditingKeystrokeHandlerOptions = definedExternally)
}

external interface EditingKeystrokeHandlerOptions {
    /** 'highest' | 'high' | 'normal' | 'low' | 'lowest' | number */
    val priority: dynamic
}