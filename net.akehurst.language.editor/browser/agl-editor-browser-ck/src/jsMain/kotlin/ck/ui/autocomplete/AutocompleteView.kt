@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.autocomplete

import org.w3c.dom.HTMLInputElement

external class AutocompleteView<TQueryFieldView>(locale: ck.utils.Locale, config: AutocompleteViewConfig<HTMLInputElement>)
    : ck.ui.search.text.SearchTextView<TQueryFieldView> {

}

external interface AutocompleteViewConfig<TConfigSearchField> : ck.ui.search.text.SearchTextViewConfig<TConfigSearchField> {
    /**
     * When set `true`, the query view will be reset when the autocomplete view loses focus.
     */
    var resetOnBlur: Boolean?

    /**
     * Minimum number of characters that need to be typed before the search is performed.
     *
     * @default 0
     */
    var queryMinChars: Number?
}

external class AutocompleteResultsView : ck.ui.search.SearchResultsView {

    /**
     * Controls the visibility of the results view.
     *
     * @observable
     */
    val isVisible: Boolean

    /**
     * Controls the position (CSS class suffix) of the results view.
     *
     * @internal
     */
    val _position: String?

    /**
     * The observable property determining the CSS width of the results view.
     *
     * @internal
     */
    val _width: Number
}
