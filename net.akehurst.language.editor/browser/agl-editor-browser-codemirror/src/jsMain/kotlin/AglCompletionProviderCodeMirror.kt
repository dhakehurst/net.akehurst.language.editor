/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.browser.codemirror

import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.CompletionItemKind
import net.akehurst.language.editor.api.AglEditorCompletionProvider
import kotlin.js.Promise

class DeferredAutocomplete(
    val position: Int,
    val before: codemirror.autocomplete.CompletionContextMatchResult?,
    val resolve: (codemirror.autocomplete.CompletionResult) -> Unit,
    val reject: (Throwable) -> Unit
)

internal class AglCompletionProviderCodeMirror<AsmType : Any, ContextType : Any>(
    val editor: AglEditorCodeMirror<AsmType, ContextType>
) : AglEditorCompletionProvider {

    companion object {
        val REGEX_WORD = Regex("\\w+")
        val REGEX_WORD_JS = js("/\\w+/")
    }

    val promiseQueue = mutableListOf<DeferredAutocomplete>()

    /**
     * codemirror interface
     */
    fun autocompletion(context: codemirror.autocomplete.CompletionContext): Promise<codemirror.autocomplete.CompletionResult?> {
        val before = if (0 == context.pos) null else context.matchBefore(REGEX_WORD_JS)
        val from = before?.from ?: context.pos
        editor.languageServiceRequest.sentenceCodeCompleteRequest(
            editor.endPointId, editor.languageIdentity,
            editor.text,
            from,
            editor.processOptions
        )
        return Promise { resolve, reject ->
            promiseQueue.add(DeferredAutocomplete(context.pos, before, resolve, reject))
        }
    }


    override fun provide(completionItems: List<CompletionItem>) {
        val promise = promiseQueue.removeFirstOrNull()
        when (promise) {
            null -> Unit //should not happen!
            else -> {
                val before = promise.before
                val partialCompletion = before?.let { completionItems.any { it.text.startsWith(before.text) } } ?: false
                val from = when {
                    partialCompletion -> before!!.from
                    else -> promise.position
                }
                //TODO: sort literal words first, then symbols, then patterns
                val groups = completionItems.groupBy { it.kind }
                val references = groups[CompletionItemKind.REFERRED] ?: emptyList()
                val literals = groups[CompletionItemKind.LITERAL]?.groupBy { it.text.matches(REGEX_WORD) } ?: emptyMap()
                val literalWords = literals[true] ?: emptyList()
                val literalSymbols = literals[false] ?: emptyList()
                val patterns = groups[CompletionItemKind.PATTERN] ?: emptyList()

                val cmOptions = references.toCodeMirror(3) + literalWords.toCodeMirror(2) + literalSymbols.toCodeMirror(1) + patterns.toCodeMirror(0)
                val cr = object : codemirror.autocomplete.CompletionResult {
                    override val filter: Boolean? = null
                    override val from: Int = from
                    override val to: Int? = null
                    override val options: Array<codemirror.autocomplete.Completion> = cmOptions.toTypedArray()
                    override val validFor: ((text: String, from: Int, to: Int, dynamic) -> Boolean)? = {_,_,_,_ -> true }
                    override val update: ((current: codemirror.autocomplete.CompletionResult, from: Int, to: Int, context: codemirror.autocomplete.CompletionContext) -> codemirror.autocomplete.CompletionResult)? = null
                }
                promise.resolve(cr)
            }
        }
    }

    private fun List<CompletionItem>.toCodeMirror(boost:Int): List<codemirror.autocomplete.Completion> {
        return this.map {
            object : codemirror.autocomplete.Completion {
                override val boost: Int? = boost
                override val apply: String? = it.text
                override val label: String = it.text
                override val detail: String? = when (it.kind) {
                    CompletionItemKind.LITERAL -> null
                    else -> "\"${it.name}\""
                }
                override val displayLabel: String? = null
                override val info: String? = null
                override val section: String? = null
                override val type: String? = null
            }
        }
    }

}