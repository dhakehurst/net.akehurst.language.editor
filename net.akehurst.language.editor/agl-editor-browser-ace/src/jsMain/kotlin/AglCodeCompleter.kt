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

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.objectJS


class AglCodeCompleter<AsmType : Any, ContextType : Any>(
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
            val result = proc.expectedTerminalsAt(editor.getValue(), pos, 1, Agl.options { parse { goalRuleName(goalRule) } })
            result.items
        } else {
            emptyList()
        }
    }

}