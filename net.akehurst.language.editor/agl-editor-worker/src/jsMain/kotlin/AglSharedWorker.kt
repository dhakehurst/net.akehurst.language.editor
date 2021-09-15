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

package net.akehurst.language.editor.worker

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.parser.ParseFailedException
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.sppt.SPPTBranch
import net.akehurst.language.api.sppt.SPPTLeaf
import net.akehurst.language.api.sppt.SPPTNode
import net.akehurst.language.api.sppt.SharedPackedParseTree
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.api.syntaxAnalyser.AsmElementSimple
import net.akehurst.language.editor.common.*
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.MessageEvent
import org.w3c.dom.SharedWorkerGlobalScope
import org.w3c.dom.WorkerGlobalScope

class AglSharedWorker: AglWorkerAbstract() {

    private var _selfShared: dynamic? = null

    init {
        this._selfShared = self // as SharedWorkerGlobalScope
        start()
    }

    fun start() {
        _selfShared?.onconnect = { e:MessageEvent ->
            val port = e.ports[0]
            port.onmessage = { it ->
                val msg: dynamic = it.data
                when (msg.action) {
                    "MessageProcessorCreate" -> this.createProcessor(port, msg.languageId, msg.editorId, msg.grammarStr)
                    "MessageParserInterruptRequest" -> this.interrupt(port, msg.languageId, msg.editorId, msg.reason)
                    "MessageParseRequest" -> this.parse(port, msg.languageId, msg.editorId, msg.goalRuleName, msg.text)
                    "MessageSetStyle" -> this.setStyle(port, msg.languageId, msg.editorId, msg.css)
                }
            }
            true //onconnect insists on having a return value!
        }
    }

}