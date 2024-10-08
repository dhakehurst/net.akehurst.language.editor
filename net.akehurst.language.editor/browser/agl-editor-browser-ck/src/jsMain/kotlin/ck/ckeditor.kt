@file:JsModule("ckeditor5")
@file:JsNonModule

package ck

import js.JsIterator
import kotlin.js.Promise


external interface CkConfigToolbar {
    var items: Array<String>
}

external interface EditorUI {
    val focusTracker:FocusTracker
}

external interface Editor {
    val model: Model
    val conversion: Conversion
    val ui: EditorUI

    fun on(event: String, callback: () -> Unit)
    fun destroy()
    fun getData(): String
    fun setData(data: String)
    fun focus()
}

external interface ClassicEditor : Editor {
    companion object {
        fun create(element: dynamic, config: dynamic): Promise<ClassicEditor>
    }
}

external interface ClassicEditorUI : EditorUI {

}

external interface BalloonEditor : Editor {
    companion object {
        fun create(element: dynamic, config: dynamic): Promise<BalloonEditor>
    }
}

external interface BalloonEditorUI : EditorUI {

}


external interface FocusTracker {
    fun on(event: String, callback: () -> Unit)
}

external interface Model {
    val document: Document
    val schema: Schema
    fun enqueueChange(func: (Writer) -> Unit)
    fun createPositionAt(itemOrPosition: dynamic, offset: Number = definedExternally): Position
    fun createRangeIn(element: Element) : Range
}

external interface Document {

    val selection: Selection

    fun getRoot(name: String = definedExternally): RootElement

    fun on(event: String, callback: () -> Unit)
}

external interface Selection {
    fun getSelectedElement(): Element?

    val anchor : Position?
}

external interface Schema {
    fun extend(name: String, obj: dynamic)

    /**
     * context : Item | Position | SchemaContext | string | Array<string | Item>
     * def : string | Node | DocumentFragment
     */
    fun checkChild(context: dynamic, def: String): Boolean
}

external interface Conversion {
    fun attributeToElement(definition: dynamic)
}

/**
 * Clipboard
 * Enter
 * SelectAll
 * Typing
 * Undo
 */
external class Essentials {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

/**
 * from basic-styles
 */
external class Bold {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

/**
 * from basic-styles
 */
external class Italic {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

/*
 * font size,
 * font family,
 * font color,
 * font background color.
 */
external class Font {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

/**
 * paragraph support
 */
external class Paragraph {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

/**
 * mentions support, from mentions
 */
external class Mention {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

external interface Writer {
    val model: Model

    // makes no sense to pass a Position, as it a copy of that position
    fun createPositionAt(itemOrPosition: Item, offset: dynamic): Position
    fun createRange(start: Position, end: Position): Range
    fun setAttribute(key: String, value: dynamic, range: Range)
    fun setSelection(pos: Position)
    fun addMarker(name: String, options: dynamic)
}

external interface Range {
    val end:Position
    fun getItems(): JsIterator<Item>
}


external interface Position {
    val nodeAfter: Node

    fun getShiftedBy(shift:Number): Position
     fun isEqual(other: Position): Boolean
}

external interface JsIterable<T> {
    fun iterator(): Iterator<T>
}

// Item is a Node or TextProxy.
external interface Item {
    @JsName("is")
    fun is_(type:String) : Boolean
}
external interface TextProxy : Item

external interface Node : Item
external interface Text : Node {
    val data:String
}
external interface Element : Node {
    fun getChildren() : JsIterator<Node>
}


external interface RootElement : Element {
    val root: dynamic
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