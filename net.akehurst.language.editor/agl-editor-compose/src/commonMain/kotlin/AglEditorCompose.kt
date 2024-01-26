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

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.api.LogFunction
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.AglTokenizerByWorker

fun <AsmType : Any, ContextType : Any> Agl.attachToComposeEditor(
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    languageService: LanguageService
): AglEditor<AsmType, ContextType> {
    return AglEditorCompose(
        languageService = languageService,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction
    )
}

class AglEditorCompose<AsmType : Any, ContextType : Any>(
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    languageService: LanguageService
) : AglEditorAbstract<AsmType, ContextType>(languageId, editorId, logFunction, languageService) {

    override val baseEditor: Any
        get() = TODO("not implemented")
    override val sessionId: String
        get() = TODO("not implemented")
    override var text: String
        get() = TODO("not implemented")
        set(value) {}

    override var workerTokenizer: AglTokenizerByWorker
        get() = TODO("not implemented")
        set(value) {}

    override fun destroy() {
        TODO("not implemented")
    }

    override fun updateLanguage(oldId: String?) {
        TODO("not implemented")
    }

    override fun updateProcessor() {
        TODO("not implemented")
    }

    override fun updateStyle() {
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
