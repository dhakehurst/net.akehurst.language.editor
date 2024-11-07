package net.akehurst.language.editor.browser.ck.autocomplete


import net.akehurst.language.editor.common.objectJSTyped
import org.w3c.dom.HTMLInputElement

class FilteredListView : ck.ui.list.ListView(), ck.ui.search.FilteredView {
    override fun filter(regExp: kotlin.js.RegExp?): ck.ui.search.FilteredViewResult {
        return objectJSTyped<ck.ui.search.FilteredViewResult> {
            resultsCount = 5
            totalItemsCount = 10
        }
    }
}

data class AutoCompleteItem(
    val label: String,
    val value: String,
    val doc: String
)

class AutocompleteItemView() : ck.ui.list.ListItemView() {
    var item :AutoCompleteItem? = null
}

class Autocomplete {

    val selected: AutoCompleteItem? = null

    fun show(balloon:ck.ui.panel.balloon.ContextualBalloon, targetRect:ck.utils.dom.Rect, items: List<AutoCompleteItem>) {
        val locale = ck.utils.Locale()
        val listView = FilteredListView()
        for (item in items) {
            val itemView = ck.ui.button.ButtonView(locale)
            itemView.label = item.label
            itemView.withText = true

            val listItemView = AutocompleteItemView()
            listItemView.children.add(itemView)
            listItemView.item = item
            listView.items.add(listItemView)
        }

        val config = objectJSTyped<ck.ui.autocomplete.AutocompleteViewConfig<HTMLInputElement>> {
            filteredView = listView
            queryView = objectJSTyped<ck.ui.search.text.SearchTextQueryViewConfig<HTMLInputElement>> {
                label = "Search Field"
                showIcon = false
                showResetButton = false
            }
        }
        val acView = ck.ui.autocomplete.AutocompleteView<HTMLInputElement>(locale, config)


        val viewPos = objectJSTyped<ck.utils.dom.Options> {
            target = targetRect
            //limiter =
            //positions =
        }

        balloon.add(objectJSTyped<ck.ui.panel.balloon.ViewConfiguration> {
            view = acView
            position = viewPos
            singleViewMode = true
        })
    }
}