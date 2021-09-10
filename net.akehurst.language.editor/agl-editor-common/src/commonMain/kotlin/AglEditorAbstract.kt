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
package net.akehurst.language.editor.common

import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.ParseEvent
import net.akehurst.language.editor.api.ProcessEvent

abstract class AglEditorAbstract(
    val languageId: String,
    override val editorId: String
) : AglEditor {

    protected val agl = AglComponents(languageId)

    private val _onParseHandler = mutableListOf<(ParseEvent) -> Unit>()
    private val _onProcessHandler = mutableListOf<(ProcessEvent) -> Unit>()
    private var _styleStr_cache: String? = null

    override var grammarStr: String?
        get() {
            try {
                return this.agl.processor?.grammar?.toString()
            } catch (t: Throwable) {
                throw RuntimeException("Failed to set text in editor")
            }
        }
        set(value) {
            this.setGrammar(value)
        }

    override var goalRuleName: String?
        get() = this.agl.goalRule
        set(value) {
            this.agl.goalRule = value
        }

    override var styleStr: String?
        get() = _styleStr_cache
        set(value) {
            _styleStr_cache = value; this.setStyle(value)
        }

    override fun onParse(handler: (ParseEvent) -> Unit) {
        this._onParseHandler.add(handler)
    }

    fun notifyParse(event: ParseEvent) {
        this._onParseHandler.forEach {
            it.invoke(event)
        }
    }

    override fun onProcess(handler: (ProcessEvent) -> Unit) {
        this._onProcessHandler.add(handler)
    }

    fun notifyProcess(event: ProcessEvent) {
        this._onProcessHandler.forEach {
            it.invoke(event)
        }
    }

    protected abstract fun setGrammar(str: String?)
    protected abstract fun setStyle(str: String?)
}