@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.model

import js.JsIterator

// Item is a Node or TextProxy.
external interface Item {
    @JsName("is")
    fun is_(type:String) : Boolean

    /**
     * CK defines this separately on Node and Text
     * returns Array of two items [key,value]
     */
    fun getAttributes(): JsIterator<Array<Any>>
}
external interface TextProxy : Item

external interface Node : Item
external interface Text : Node {
    val data:String
}
external interface Element : Node {
    val name:String
    fun getChildren() : JsIterator<Node>
}

external interface RootElement : Element {
    val root: dynamic
}
