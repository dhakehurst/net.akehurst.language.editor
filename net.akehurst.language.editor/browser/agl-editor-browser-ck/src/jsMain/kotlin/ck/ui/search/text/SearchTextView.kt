@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.search.text

import org.w3c.dom.HTMLElement

open external class SearchTextView<TQueryFieldView> : ck.ui.View<HTMLElement> {

    val keystrokes: ck.utils.KeystrokeHandler

    var queryView: SearchTextQueryViewConfig<TQueryFieldView>
    val filteredView: ck.ui.search.FilteredView
    val resultsView:ck.ui.search.SearchResultsView

    fun render()

}

external interface SearchTextQueryViewConfig<TConfigSearchField> {
    /**
     * The human-readable label of the search field.
     */
    var label:String

    /**
     * Determines whether the button that resets the search should be visible.
     *
     * @default true
     */
    var showResetButton: Boolean?

    /**
     * Determines whether the loupe icon should be visible.
     *
     * @default true
     */
    var showIcon: Boolean?

    /**
     * The function that creates the search field input view. By default, a plain
     * {@link module:ui/inputtext/inputtextview~InputTextView} is used for this purpose.
     */
    var creator: dynamic? //TODO: LabeledFieldViewCreator<TConfigSearchField>?
}

external interface SearchTextViewConfig<TConfigSearchField> {

    /**
     * The configuration of the view's query field.
     */
    var queryView: SearchTextQueryViewConfig<TConfigSearchField>

    /**
     * The view that is filtered by the search query.
     */
    var filteredView: ck.ui.search.FilteredView

    /**
     * The view that displays the information about the search results.
     */
    var infoView: dynamic? //TODO

    /**
     * The custom CSS class name to be added to the search view element.
     */
    @JsName("class")
    var class_: String?
}
