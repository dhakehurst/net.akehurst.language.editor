@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.button

external class ButtonView(locale: ck.utils.Locale?) : ck.ui.FocusableView {
    var label: String?
    var withText: Boolean

    override fun focus()
}