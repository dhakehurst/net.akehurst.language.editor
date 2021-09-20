package net.akehurst.language.editor.ace

import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.objectJS


class AglCodeCompleterByWorker(
        val agl: AglComponents
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
                meta = "(${ci.rule.name})"
            }
        }.toTypedArray()
        callback(null, aceCi)
    }

    private fun getCompletionItems(editor: ace.Editor, pos: Int): List<CompletionItem> {
        //TODO: get worker to provide this
        val proc = this.agl.languageDefinition.processor
        return if (null != proc) {
            val goalRule = this.agl.goalRule
            if (null == goalRule) {
                val list = proc.expectedAt(editor.getValue(), pos, 1);
                list
            } else {
                val list = proc.expectedAtForGoal(goalRule, editor.getValue(), pos, 1);
                list
            }
        } else {
            emptyList()
        }
    }

}