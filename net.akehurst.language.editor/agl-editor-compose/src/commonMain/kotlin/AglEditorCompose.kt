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

package net.akehurst.language.editor.common.compose

import net.akehurst.kotlin.compose.editor.api.ComposeCodeEditor
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.AglTokenizerByWorker

fun <AsmType : Any, ContextType : Any> Agl.attachToComposeEditor(
    languageService: LanguageService,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    composeEditor: ComposeCodeEditor
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCompose<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        composeEditor = composeEditor
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    return aglEditor
}


class AglEditorCompose<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    val composeEditor: ComposeCodeEditor
) : AglEditorAbstract<AsmType, ContextType>(languageServiceRequest, languageId, EndPointIdentity(editorId,"none"), logFunction) {

    override val baseEditor: Any get() = composeEditor
    override val isConnected: Boolean get() = true
    override var text: String
        get() = composeEditor.text
        set(value) {
            composeEditor.text = value
        }

    override var workerTokenizer: AglTokenizerByWorker
        get() = TODO("not implemented")
        set(value) {}

    override val completionProvider: AglEditorCompletionProvider
        get() = TODO("not implemented")

    override fun destroy() {
        composeEditor.destroy()
    }

    override fun updateLanguage(oldId: String?) {
        TODO("not implemented")
    }

    override fun updateEditorStyles() {
        TODO("not implemented")
    }

    override fun resetTokenization(fromLine: Int) {
        TODO("not implemented")
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        TODO("not implemented")
    }

    override fun clearErrorMarkers() {
        TODO("not implemented")
    }

}
