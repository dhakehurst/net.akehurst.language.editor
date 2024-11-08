@file:JsModule("@ckeditor/ckeditor5-engine")
@file:JsNonModule
package ck.engine.view



external interface Document : ck.utils.Emitter {

}

external interface View {
    val domConverter:DomConverter
    val document:Document
}