@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model

import js.JsIterator

external interface Range {
    val start: Position
    val end: Position
    fun getItems(): JsIterator<Item>
}