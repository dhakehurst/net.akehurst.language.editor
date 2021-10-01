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

import org.w3c.dom.MessageEvent

class AglSharedWorker : AglWorkerAbstract() {

    private var _selfShared: dynamic? = null

    init {
        this._selfShared = self // as SharedWorkerGlobalScope
        start()
    }

    fun start() {
        _selfShared?.onconnect = { ev1: MessageEvent ->
            val port = ev1.ports[0]
            port.onmessage = { ev: MessageEvent ->
                try {
                    receiveMessage(port, ev)
                } catch (e: Throwable) {
                    port.postMessage("Error: Worker error: ${e.message!!}")
                }
            }
            true //onconnect insists on having a return value!
        }
    }

}