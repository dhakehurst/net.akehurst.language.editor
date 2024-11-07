@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.view

external interface DomConverter {
    fun viewRangeToDom( viewRange: Range ): org.w3c.dom.Range
}