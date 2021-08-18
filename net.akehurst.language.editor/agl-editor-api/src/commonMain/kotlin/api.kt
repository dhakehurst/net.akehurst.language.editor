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

interface AglEditor {

    val editorId:String

    /**
     * the content of the editor
     */
    var text:String

    /**
     * the grammar for the editor as a string
     */
    var grammarStr:String?

    /**
     * CSS styles as a string
     */
    var styleStr: String?

    fun onParse(handler: (ParseEvent) -> Unit)

    fun onProcess(handler: (ProcessEvent) -> Unit)

    fun clearErrorMarkers()

    fun finalize()
}

sealed class ParseEvent(val message: String )
class ParseEventStart(): ParseEvent("Parse started")
class ParseEventSuccess(
        val tree:Any
) : ParseEvent("Parse success")
class ParseEventFailure(
        message: String,
        val tree:Any?
): ParseEvent(message)

sealed class ProcessEvent(
        val message: String
)
class ProcessEventStart() : ProcessEvent("Process started")
class ProcessEventSuccess(
        val tree:Any
) : ProcessEvent("Process success")
class ProcessEventFailure(
        message: String,
        val tree:Any?
) : ProcessEvent(message)