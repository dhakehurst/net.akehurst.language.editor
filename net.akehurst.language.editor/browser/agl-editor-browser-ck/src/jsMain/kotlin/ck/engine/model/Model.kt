@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model

external interface Model {
    val document: Document
    val schema: Schema
    fun enqueueChange(func: (Writer) -> Unit)
    fun createPositionAt(itemOrPosition: dynamic, offset: Number = definedExternally): Position
    fun createRangeIn(element: Element) : Range
}

external interface  AttributeProperties {
    val isFormatting:Boolean
}

external interface Schema {
    fun extend(name: String, obj: dynamic)

    /**
     * context : Item | Position | SchemaContext | string | Array<string | Item>
     * def : string | Node | DocumentFragment
     */
    fun checkChild(context: dynamic, def: String): Boolean

    fun setAttributeProperties(attributeName:String, properties:dynamic)
    fun getAttributeProperties(attributeName:String) : AttributeProperties
}


external interface Writer {
    val model: Model

    // makes no sense to pass a Position, as it a copy of that position
    fun createPositionAt(itemOrPosition: Item, offset: dynamic): Position
    fun createRange(start: Position, end: Position): Range
    fun createRangeOn(item:Item): Range
    fun setAttribute(key: String, value: dynamic, range: Range)
    fun removeAttribute(key: String, item: Item)
    fun setSelection(pos: Position)
    fun addMarker(name: String, options: dynamic)
}

external class TreeWalker(options: dynamic) {

}


external interface TreeWalkerValue {
    val item:Item
    val length:Number
    val nextPosition:Position
    val previousPosition:Position
    val type: String // 'elementStart' | 'elementEnd' | 'text'
}