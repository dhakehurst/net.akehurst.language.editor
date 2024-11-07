@file:JsModule("@ckeditor/ckeditor5-ui")
@file:JsNonModule
package ck.ui.search

external interface FilteredView {
    /**
     * Filters the view by the given regular expression.
     */
    fun filter( regExp: kotlin.js.RegExp? ): FilteredViewResult
}

external interface FilteredViewResult {
    var resultsCount: Number
    var totalItemsCount: Number
}