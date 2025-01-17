package net.akehurst.language.editor.browser.ck.autocomplete


import kotlinx.browser.document
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.CompletionItemKind
import net.akehurst.language.editor.api.AglEditorCompletionProvider
import net.akehurst.language.editor.api.AglEditorLogger
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

    val itemView = ck.ui.button.ButtonView(ck.utils.Locale())

    init {
        itemView.label = when (item.kind) {
            CompletionItemKind.LITERAL -> item.text
            CompletionItemKind.PATTERN -> "${item.text} (${item.label})"
            CompletionItemKind.SEGMENT -> "${item.label}: ${item.text}"
            CompletionItemKind.REFERRED -> "${item.text} (${item.label})"
        }
        itemView.withText = true
        this.children.add(itemView)
        this.itemView.element.onclick = {
            this.fire<Any, ck.ui.button.ButtonExecuteEvent>("execute")
        }
    }

    fun indicateSelected() {
        itemView.element.classList.add("ck-on")
        itemView.element.classList.remove("ck-off")
    }

    fun indicateUnselected() {
        itemView.element.classList.add("ck-off")
        itemView.element.classList.remove("ck-on")
    }


}

class CkAutocomplete(
    val logger: AglEditorLogger,
    val ckEditor: ck.core.editor.Editor,
    val balloon: ck.ui.panel.balloon.ContextualBalloon
) : AglEditorCompletionProvider {

    val isVisible: Boolean get() = balloon.visibleView === acView

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
    private val handledKeys = commitKeys + listOf(ck.utils.keyCodes.arrowdown, ck.utils.keyCodes.arrowup, ck.utils.keyCodes.esc, ck.utils.keyCodes.arrowleft, ck.utils.keyCodes.arrowright)

    init {
        acView.render()
        //listView.render()

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
                    ck.utils.keyCodes.arrowleft -> hide()
                    ck.utils.keyCodes.arrowright -> hide()
                    else -> when {
                        commitKeys.contains(arg.keyCode) -> {
                            insertSelected()
                            hide()
                        }
                    }
                }
            }
        }, objectJS { priority = "highest" })
    }

    fun clear() {
        listView.items.clear()
    }

    fun show() {
        //store cursor position when invoked
        this.insertPosition = ckEditor.model.document.selection.getFirstPosition() ?: error("Should always be non-null!")
        val cursorRng = ckEditor.model.document.selection.getFirstRange() ?: error("Should always be non-null!")
        val cursorViewRng = ckEditor.editing.mapper.toViewRange(cursorRng)
        val domRng = ckEditor.editing.view.domConverter.viewRangeToDom(cursorViewRng)
        val targetRect = ck.utils.dom.Rect.getDomRangeRects(domRng).first()

        val viewPos = objectJSTyped<ck.utils.dom.Options> {
            target = targetRect
            //limiter =
            //positions =
        }
        if (!isVisible) {
            balloon.add(objectJSTyped {
                view = acView
                position = viewPos
                singleViewMode = true
            })
        }
    }

    // --- AglEditorCompletionProvider ---
    override fun provide(completionItems: List<CompletionItem>) {
        try {
            logger.logTrace("Provided ${completionItems.size} items.")
            for (item in completionItems) {
                val listItemView = AutocompleteItemView(item)
                listItemView.on<Any, ck.ui.button.ButtonExecuteEvent>("execute", { evt, arg ->
                    val idx = listView.items.getIndex(listItemView)
                    select(idx)
                    insertSelected()
                    hide()
                })
                listView.items.add(listItemView)
            }
            acView.resultsView.asDynamic().isVisible = true
            //listView.asDynamic().isVisible = true
            select(0)
        } catch (t: Throwable) {
            logger.logError("Exception trying to provide items.", t)
        }
    }

    // --- impl ---
    private fun shouldHandledKey(keyCode: Number): Boolean = handledKeys.contains(keyCode)

    private fun hide() {
        if (balloon.hasView(acView)) {
            this.clear()
            balloon.remove(acView)
        }
    }

    private fun insertSelected() {
        val sel = selected
        val pos = insertPosition
        if (null != sel && null != pos) {
            val textToInsert = sel.item.text
            ckEditor.model.change { writer ->
                writer.insertText(textToInsert, pos)
                when (sel.item.kind) {
                    // select the inserted 'Pattern' text so user can replace it
                    CompletionItemKind.PATTERN -> {
                        val rng = writer.createRange(pos, pos.getShiftedBy(textToInsert.length))
                        writer.setSelection(rng)
                    }

                    else -> Unit
                }
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
                if (isSelectedItemVisible().not()) {
                    acView.resultsView.element.scrollTop = item.element.offsetTop.toDouble()
                    //listView.element.scrollTop = item.element.offsetTop.toDouble()
                }
            }
        }
    }

    private fun isSelectedItemVisible(): Boolean {
        val itemRect = ck.utils.dom.Rect(selected!!.element)
        return ck.utils.dom.Rect(acView.resultsView.element).contains(itemRect)
        //return ck.utils.dom.Rect(listView.element).contains(itemRect)
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

}