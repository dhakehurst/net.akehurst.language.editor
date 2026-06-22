@file:JsModule("@ckeditor/ckeditor5-core")
@file:JsNonModule
package ck.core

external interface PluginCollection {
    fun <T:Any> get(key:JsClass<T>):T
}