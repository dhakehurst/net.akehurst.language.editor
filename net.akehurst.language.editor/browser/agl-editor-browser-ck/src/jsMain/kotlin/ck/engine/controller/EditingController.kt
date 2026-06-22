@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.controller

external interface EditingController {
    val mapper : ck.engine.conversion.Mapper
    val view:ck.engine.view.View
}