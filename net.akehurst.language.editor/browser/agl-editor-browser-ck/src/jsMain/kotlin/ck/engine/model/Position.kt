@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model


external interface Position {
    val path:Array<Number>
    val nodeAfter: Node

    fun getShiftedBy(shift:Number): Position
    fun isEqual(other: Position): Boolean
}
