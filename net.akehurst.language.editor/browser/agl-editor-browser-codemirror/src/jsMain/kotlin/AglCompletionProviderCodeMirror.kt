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

import codemirror.autocomplete.Completion
import codemirror.autocomplete.CompletionContext
import codemirror.autocomplete.CompletionResult
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.editor.api.AglEditorCompletionProvider
import kotlin.js.Promise

class DeferredPromise(
    val position:Int,
    val resolve: (CompletionResult) -> Unit,
    val reject: (Throwable) -> Unit
)

internal class AglCompletionProviderCodeMirror<AsmType : Any, ContextType : Any>(
    val editor: AglEditorCodeMirror<AsmType, ContextType>
) : AglEditorCompletionProvider {

    val promiseQueue = mutableListOf<DeferredPromise>()

    /**
     * codemirror interface
     */
    fun autocompletion(context: codemirror.autocomplete.CompletionContext): Promise<CompletionResult?> {
        editor.languageServiceRequest.sentenceCodeCompleteRequest(
            editor.endPointId, editor.languageIdentity,
            editor.text,
            context.pos,
            editor.processOptions
        )

        return Promise({ resolve, reject ->
            promiseQueue.add(DeferredPromise(context.pos,resolve, reject))
        })
    }


    override fun provide(completionItems: List<CompletionItem>) {
        val promise = promiseQueue.removeFirstOrNull()
        when (promise) {
            null -> Unit //should not happen!
            else -> {
                val cr = object : CompletionResult {
                    override val filter: Boolean?= null
                    override val from: Int = promise.position
                    override val options: Array<Completion> = completionItems.map {
                        object :Completion {
                            override val boost: Int? = null
                            override val detail: String? = it.kind.toString()
                            override val displayLabel: String? = null
                            override val info: String? = null
                            override val apply:String? = it.text
                            override val label: String = it.name
                            override val section: String? = null
                            override val type: String? = null
                        }
                    }.toTypedArray()
                    override val to: Int? = null
                    override val update: ((current: CompletionResult, from: Int, to: Int, context: CompletionContext) -> CompletionResult)? = null
                }
                promise.resolve(cr)
            }
        }
    }

}