@file:JsModule("@ckeditor/ckeditor5-mention")
@file:JsNonModule
package ck.mention

import ck.utils.Locale
import org.w3c.dom.HTMLElement

/**
 * mentions support, from mentions
 */
external class Mention {
    companion object {
        fun pluginName(): String = definedExternally
    }
}

external class DomWrapperView(
    locale: Locale,
    val domElement: HTMLElement
) : ck.ui.FocusableView {

}