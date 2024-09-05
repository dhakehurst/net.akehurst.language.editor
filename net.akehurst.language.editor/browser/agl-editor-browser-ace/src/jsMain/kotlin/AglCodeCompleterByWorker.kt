/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.language.editor.browser.ace


import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.CompletionItemKind
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.objectJS


class AglCodeCompleterByWorker<AsmType : Any, ContextType : Any>(
    val agl: AglComponents<AsmType, ContextType>
) {

    // called by Ace
    @JsName("getCompletions")
    fun getCompletions(editor: ace.IEditor, session: ace.EditSession, pos: dynamic, prefix: dynamic, callback: dynamic) {
        val posn = session.getDocument().positionToIndex(pos, 0)
        val wordList = this.getCompletionItems(editor, posn)
        val aceCi = wordList.map { ci ->
            val m = when (ci.kind) {
                CompletionItemKind.LITERAL -> ""
                CompletionItemKind.PATTERN -> "(${ci.name})"
                CompletionItemKind.SEGMENT -> "(${ci.name})"
                CompletionItemKind.REFERRED  -> "(${ci.name})"
            }
            val s = when (ci.kind) {
                CompletionItemKind.REFERRED -> 4
                CompletionItemKind.LITERAL -> 3
                CompletionItemKind.PATTERN -> 2
                CompletionItemKind.SEGMENT -> 1
            }
            objectJS {
                caption = ci.text
                value = ci.text
                meta = m
                score = s
            }
        }.toTypedArray()
        callback(null, aceCi)
    }

    private fun getCompletionItems(editor: ace.IEditor, pos: Int): List<CompletionItem> {
        //TODO: get worker to provide this
        val proc = this.agl.languageDefinition.processor
        return if (null != proc) {
            val goalRule = this.agl.goalRule
            val context = this.agl.context
            if (null==context) {
                this.agl.logger.log(LogLevel.Debug,"context is null for code completion.",null)
            }
            val result = proc.expectedItemsAt(editor.getValue(), pos, 1,
                Agl.options {
                    parse { goalRuleName(goalRule) }
                    completionProvider { context(context) }
                }
            )
            val sep = mapOf(
                CompletionItemKind.LITERAL to mutableListOf<CompletionItem>(),
                CompletionItemKind.PATTERN to mutableListOf<CompletionItem>(),
                CompletionItemKind.SEGMENT to mutableListOf<CompletionItem>()
            )
            result.items.forEach { sep[it.kind]!!.add(it) }
            val lits = sep[CompletionItemKind.LITERAL]!!.sortedBy { it.text } ?: emptyList<CompletionItem>()
            val pats = sep[CompletionItemKind.PATTERN]!!.sortedBy { it.text } ?: emptyList<CompletionItem>()
            val segs = sep[CompletionItemKind.SEGMENT]!!.sortedBy { it.text } ?: emptyList<CompletionItem>()
            segs + pats + lits
        } else {
            emptyList()
        }
    }

}