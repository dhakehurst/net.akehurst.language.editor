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



class AglDedicatedWorker : AglWorkerAbstract() {

    private var _selfDedicated: dynamic? = null

    init {
        start()
        _selfDedicated = self //as DedicatedWorkerGlobalScope
    }

    fun start() {
        _selfDedicated?.onmessage = {e: MessageEvent ->
            val msg: dynamic = e.data
            when (msg.action) {
                "MessageProcessorCreate" -> this.createProcessor(_selfDedicated, msg.languageId, msg.editorId, msg.grammarStr)
                "MessageParserInterruptRequest" -> this.interrupt(_selfDedicated, msg.languageId, msg.editorId, msg.reason)
                "MessageParseRequest" -> this.parse(_selfDedicated, msg.languageId, msg.editorId, msg.goalRuleName, msg.text)
                "MessageSetStyle" -> this.setStyle(_selfDedicated, msg.languageId, msg.editorId, msg.css)
            }
        }
    }



}