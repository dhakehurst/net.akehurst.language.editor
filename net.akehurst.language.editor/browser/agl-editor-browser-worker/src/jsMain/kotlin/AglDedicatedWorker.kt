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

import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.MessageEvent

class AglDedicatedWorker<AsmType : Any, ContextType : Any> : AglWorkerJsAbstract<AsmType, ContextType>() {

    private var _selfDedicated: dynamic? = null

    init {
        _selfDedicated = self //as DedicatedWorkerGlobalScope
        start()
    }

    fun start() {
        _selfDedicated?.onerror = {

        }
        _selfDedicated?.onmessage = { ev: MessageEvent ->
            try {
                receiveMessageEventFromJsPort(_selfDedicated, ev)
            } catch (e: Throwable) {
                (_selfDedicated as DedicatedWorkerGlobalScope).postMessage("Error: Worker error: ${e.message!!}")
            }
        }
    }


}