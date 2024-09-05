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


fun <AsmType : Any, ContextType : Any> Agl.attachToCk(
    languageService: LanguageService,
    containerElement: Element,
    ckEditor: ck.IEditor,
    languageId: String,
    editorId: String,
    logFunction: LogFunction?,
    ace: IAce
): AglEditor<AsmType, ContextType> {
    val aglEditor = AglEditorAce<AsmType, ContextType>(
        languageServiceRequest = languageService.request,
        containerElement = containerElement,
        aceEditor = aceEditor,
        languageId = languageId,
        editorId = editorId,
        logFunction = logFunction,
        ace = ace
    )
    languageService.addResponseListener(aglEditor.endPointIdentity, aglEditor)
    return aglEditor
}