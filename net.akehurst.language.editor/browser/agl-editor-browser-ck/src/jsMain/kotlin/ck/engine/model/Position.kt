@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model


external interface Position {
    val index:Number
    val isAtEnd:Boolean
    val isAtStart:Boolean
    val path:Array<Number>
    val nodeAfter: Node
    val nodeBefore: Node
    val offset:Number
    val textNode:Text?

    fun getShiftedBy(shift:Number): Position
    fun isBefore(value: Position): Boolean
    fun isEqual(other: Position): Boolean
    fun isAfter(value: Position): Boolean
}
