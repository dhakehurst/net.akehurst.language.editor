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

package net.akehurst.language.editor.api

import net.akehurst.language.api.processor.LanguageDefinition

enum class LogLevel { None, Fatal, Error, Warning, Debug, Trace, All }

interface AglEditor {

    val editorId: String

    /**
     * the language identity for this editor
     */
    var languageIdentity: String

    /**
     * The language definition for the editor, found or created using the languageId
     */
    val languageDefinition: LanguageDefinition

    /**
     * The name of a rule in the grammar from which to start the parse.
     * If null, the first non-skip rule will be used
     */
    var goalRuleName: String?

    /**
     * Set style specific to this editor (rather than using the one from LanguageDefinition associated with the languageId).
     * If null, then the style from the LanguageDefinition is used. (default is null)
     */
    var editorSpecificStyleStr: String?

    /**
     * the content of the editor
     */
    var text: String

    /**
     * destination for logging messages
     */
    var logger: ((level:LogLevel, message:String) -> Unit)?

    fun onParse(handler: (ParseEvent) -> Unit)

    fun onProcess(handler: (ProcessEvent) -> Unit)

    fun clearErrorMarkers()

    fun finalize()

    fun destroy()
}


sealed class ParseEvent(val message: String) {
    open val success: Boolean = false
    open val tree: Any? = null
}

class ParseEventStart() : ParseEvent("Parse started")
class ParseEventSuccess(
    override val tree: Any
) : ParseEvent("Parse success") {
    override val success = true
}

class ParseEventFailure(
    message: String,
    override val tree: Any?
) : ParseEvent(message)

sealed class ProcessEvent(
    val message: String
)

class ProcessEventStart() : ProcessEvent("Process started")
class ProcessEventSuccess(
    val tree: Any
) : ProcessEvent("Process success")

class ProcessEventFailure(
    message: String,
    val tree: Any?
) : ProcessEvent(message)