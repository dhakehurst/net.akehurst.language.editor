@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.list

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

open external class ListView {
    val element: HTMLUListElement?

    /**
     * Collection of the child list views.
     */
    val items: ck.ui.ViewCollection<ListItemView> //TODO: ViewCollection<ListItemView | ListItemGroupView | ListSeparatorView>;


    fun focus()
    fun focusFirst()
    fun focusLast()
}

open external class ListItemView(locale: ck.utils.Locale? = definedExternally) : ck.ui.View<HTMLElement> {
    /**
     * Collection of the child views inside of the list item {@link #element}.
     */
    val children: ck.ui.ViewCollection<ck.ui.FocusableView>
}