@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.view

external class Range {
    val start: Position;

    /**
     * End position.
     */
    val end: Position;
}