/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
package net.akehurst.language.editor.common

import net.akehurst.kotlin.json.JsonString
import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.messages.*

abstract class AglEditorJsAbstract<AsmType : Any, ContextType : Any>(
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    languageService: LanguageService
) : AglEditorAbstract<AsmType, ContextType>(languageId, editorId, logFunction, languageService) {

    override fun updateProcessor() {
        val grammarStr = this.agl.languageDefinition.grammarStr
        if (grammarStr.isNullOrBlank()) {
            //do nothing
        } else {
            this.clearErrorMarkers()
            this.languageService.request.processorCreateRequest(this.endPointId, this.languageIdentity, grammarStr, this.agl.languageDefinition.crossReferenceModelStr, this.editorOptions)
            this.workerTokenizer.reset()
            this.resetTokenization(0) //new processor so find new tokens, first by scan
        }
    }

}