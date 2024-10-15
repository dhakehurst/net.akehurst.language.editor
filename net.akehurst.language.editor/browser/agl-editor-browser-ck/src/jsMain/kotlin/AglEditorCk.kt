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

package net.akehurst.language.editor.browser.ck

import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.base.api.QualifiedName
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.AglTokenizerByWorker
import net.akehurst.language.issues.api.LanguageIssue
import org.w3c.dom.Element


fun <AsmType : Any, ContextType : Any> Agl.attachToCk(
    languageService: LanguageService,
    containerElement: Element,
    ckEditor: ck.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    logFunction: LogFunction?
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorCk<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        ckEditor = ckEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    return aglEditor
}

private class AglEditorCk<AsmType : Any, ContextType : Any>(
    languageServiceRequest: LanguageServiceRequest,
    val containerElement: Element,
    ckEditor: ck.Editor,
    languageId: LanguageIdentity,
    editorId: String,
    logFunction: LogFunction?
) : AglEditorAbstract<AsmType, ContextType>(languageServiceRequest, languageId, EndPointIdentity(editorId, "none"), logFunction) {

    override val baseEditor: Any = ckEditor
    override var text: String
        get() = TODO("not implemented")
        set(value) {}

    override val isConnected: Boolean
        get() = TODO("not implemented")
    override val workerTokenizer: AglTokenizerByWorker
        get() = TODO("not implemented")
    override val completionProvider: AglEditorCompletionProvider
        get() = TODO("not implemented")

    override fun resetTokenization(fromLine: Int) {
        TODO("not implemented")
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        TODO("not implemented")
    }

    override fun updateLanguage(oldId: LanguageIdentity?) {
        TODO("not implemented")
    }

    override fun updateEditorStyles() {
        TODO("not implemented")
    }

    override fun clearErrorMarkers() {
        TODO("not implemented")
    }

    override fun destroy() {
        TODO("not implemented")
    }

}