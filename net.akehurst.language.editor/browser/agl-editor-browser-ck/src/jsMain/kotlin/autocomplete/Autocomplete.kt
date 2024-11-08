package net.akehurst.language.editor.browser.ck.autocomplete


import kotlinx.browser.document
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.api.AglEditorCompletionProvider
import net.akehurst.language.editor.common.objectJS
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

class AutocompleteItemView(val item: CompletionItem) : ck.ui.list.ListItemView() {
    fun indicateSelected() {
        //(children.first as DomWrapperView).isOn = true
    }

    fun indicateUnselected() {
        //(children.first as DomWrapperView).isOn = false
    }


}

class CkAutocomplete(
    val ckEditor: ck.core.editor.Editor,
    val balloon: ck.ui.panel.balloon.ContextualBalloon
) : AglEditorCompletionProvider {
    val listView = FilteredListView()
    val acView = ck.ui.autocomplete.AutocompleteView<HTMLInputElement>(ck.utils.Locale(), objectJSTyped<ck.ui.autocomplete.AutocompleteViewConfig<HTMLInputElement>> {
        filteredView = listView
        queryView = objectJSTyped<ck.ui.search.text.SearchTextQueryViewConfig<HTMLInputElement>> {
            label = "Search Field"
            showIcon = false
            showResetButton = false
        }
        resetOnBlur = true
    })

    var insertPosition: ck.engine.model.Position? = null
    var selected: AutocompleteItemView? = null

    private val commitKeys = listOf(ck.utils.keyCodes.enter, ck.utils.keyCodes.tab)
    private val handledKeys = commitKeys + listOf(ck.utils.keyCodes.arrowdown, ck.utils.keyCodes.arrowup, ck.utils.keyCodes.esc)

    init {
        acView.render()
        //document.body?.appendChild( acView.element )

        ck.ui.bindings.clickOutsideHandler(objectJSTyped {
            emitter = acView
            activator = { isVisible }
            contextElements = { arrayOf(balloon.view.element) }
            callback = { hide() }
        })

        ckEditor.editing.view.document.on<ck.engine.view.observer.KeyEventData, ck.engine.view.observer.ViewDocumentKeyDownEvent>("keydown", { evt, arg ->
            if (isVisible && shouldHandledKey(arg.keyCode)) {
                arg.preventDefault()
                evt.stop()
                when (arg.keyCode) {
                    ck.utils.keyCodes.arrowdown -> selectNext()
                    ck.utils.keyCodes.arrowup -> selectPrevious()
                    ck.utils.keyCodes.esc -> hide()
                    else -> when {
                        commitKeys.contains(arg.keyCode) -> insertSelected()
                    }
                }
            }
        }, objectJS { priority = "highest" })

    }

    private val isVisible: Boolean get() = balloon.visibleView === acView

    private fun shouldHandledKey(keyCode: Number): Boolean = handledKeys.contains(keyCode)

    private fun hide() {
        if (balloon.hasView(acView)) {
            listView.items.clear()
            balloon.remove(acView)
        }
    }

    private fun insertSelected() {
        val sel = selected
        val pos = insertPosition
        if (null != sel && null!=pos) {
            val textToInsert = sel.item.text
            ckEditor.model.change { writer ->
                writer.insertText(textToInsert, pos)
            }
        }
    }

    private fun select(index: Int) {
        // handle wrap around
        val idx = when {
            index < 0 -> listView.items.length - 1
            index >= listView.items.length -> 0
            else -> index
        }
        val item = listView.items.get(idx) as AutocompleteItemView?

        when {
            null == item -> return
            selected === item -> return
            else -> {
                selected?.indicateUnselected()
                item.indicateSelected()
                selected = item
                listView.element?.scrollTop = item.element.offsetTop.toDouble()
            }
        }


    }

    private fun selectNext() {
        if (null != selected) {
            val idx = listView.items.getIndex(selected!!)
            select(idx + 1)
        }
    }

    private fun selectPrevious() {
        if (null != selected) {
            val idx = listView.items.getIndex(selected!!)
            select(idx - 1)
        }
    }

    fun show() {
        //store cursor position when invoked
        this.insertPosition =  ckEditor.model.document.selection.getFirstPosition() ?: error("Should always be non-null!")
        val cursorRng = ckEditor.model.document.selection.getFirstRange() ?: error("Should always be non-null!")
        val cursorViewRng = ckEditor.editing.mapper.toViewRange(cursorRng)
        val domRng = ckEditor.editing.view.domConverter.viewRangeToDom(cursorViewRng)
        val targetRect = ck.utils.dom.Rect.getDomRangeRects(domRng).first()

        val viewPos = objectJSTyped<ck.utils.dom.Options> {
            target = targetRect
            //limiter =
            //positions =
        }

        balloon.add(objectJSTyped {
            view = acView
            position = viewPos
            singleViewMode = true
        })
    }

    // --- AglEditorCompletionProvider ---
    override fun provide(completionItems: List<CompletionItem>) {
        //val items = completionItems.map {
        //    AutoCompleteItem(it.text, it.name, it.description)
        //}
        for (item in completionItems) {
            val itemView = ck.ui.button.ButtonView(ck.utils.Locale())
            itemView.label = item.label
            itemView.withText = true

            val listItemView = AutocompleteItemView(item)
            listItemView.children.add(itemView)
            listView.items.add(listItemView)
        }
        acView.resultsView.asDynamic().isVisible = true
        select(0)
        listView.focusFirst()
    }

}