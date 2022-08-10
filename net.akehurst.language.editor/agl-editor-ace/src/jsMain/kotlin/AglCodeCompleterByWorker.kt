package net.akehurst.language.editor.ace

import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.objectJS


class AglCodeCompleterByWorker<AsmType : Any, ContextType : Any>(
        val agl: AglComponents<AsmType, ContextType>
) {

    // called by Ace
    @JsName("getCompletions")
    fun getCompletions(editor: ace.Editor, session: ace.EditSession, pos: dynamic, prefix: dynamic, callback: dynamic) {
        val posn = session.getDocument().positionToIndex(pos, 0)
        val wordList = this.getCompletionItems(editor, posn)
        val aceCi = wordList.map { ci ->
            objectJS {
                caption = ci.text
                value = ci.text
                meta = "(${ci.ruleName})"
            }
        }.toTypedArray()
        callback(null, aceCi)
    }

    private fun getCompletionItems(editor: ace.Editor, pos: Int): List<CompletionItem> {
        //TODO: get worker to provide this
        val proc = this.agl.languageDefinition.processor
        return if (null != proc) {
            val goalRule = this.agl.goalRule
            val result = proc.expectedAt(editor.getValue(), pos, 1,proc.options { parse { goalRuleName(goalRule) } })
            result.items
        } else {
            emptyList()
        }
    }

}