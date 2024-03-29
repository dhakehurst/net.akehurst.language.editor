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

package net.akehurst.language.editor.monaco

import monaco.CancellationToken
import monaco.Position
import monaco.editor.ITextModel
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.common.AglComponents

class AglCompletionProvider(
        val agl: AglComponents
) : monaco.languages.CompletionItemProvider {
    override val triggerCharacters: Array<String>? = null

    override fun provideCompletionItems(model: ITextModel, position: Position, context: monaco.languages.CompletionContext, token: CancellationToken): monaco.languages.CompletionList? {
        val posn = model.getOffsetAt(position)
        val wordList = this.getCompletionItems(model, posn);
        val cil = wordList.map { ci ->
            object : monaco.languages.CompletionItem {
                override val label: String = "${ci.text} (${ci.rule.name})"
                override val insertText: String = ci.text
                override val kind: monaco.languages.CompletionItemKind = monaco.languages.CompletionItemKind.Text
            }
        }
        return object : monaco.languages.CompletionList {
            override val incomplete = false
            override val suggestions: Array<monaco.languages.CompletionItem> = cil.toTypedArray()
        }
    }

    override fun resolveCompletionItem(model: ITextModel, position: Position, item: monaco.languages.CompletionItem, token: CancellationToken): monaco.languages.CompletionList? {
        return null
    }

    private fun getCompletionItems(model: ITextModel, offset: Int): List<CompletionItem> {
        val text = model.getValue()
        val proc = this.agl.processor
        val goalRule = this.agl.goalRule
        return if (null == proc) {
            emptyList()
        } else {
            if (null == goalRule) {
                val list = proc.expectedAt(text, offset, 1);
                list
            } else {
                val list = proc.expectedAt(goalRule, text, offset, 1);
                list
            }
        }
    }
}