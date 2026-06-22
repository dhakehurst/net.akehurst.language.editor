@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.conversion

external interface Mapper {
    fun toViewRange(modelRange: ck.engine.model.Range): ck.engine.view.Range
}