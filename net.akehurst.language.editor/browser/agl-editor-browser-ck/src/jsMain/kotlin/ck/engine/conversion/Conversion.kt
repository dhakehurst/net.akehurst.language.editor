@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.conversion

external interface Conversion {
    fun attributeToElement(definition: dynamic)
}
