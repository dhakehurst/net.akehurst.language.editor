/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.language.service

import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.RequestIdentity

/**
 * Bookkeeping for a language service that executes requests concurrently.
 *
 * A LanguageProcessor (and its parser) is shared by all editors of a language and is not thread safe,
 * so requests that use the processor of one language must be executed one at a time.
 * An interrupt from an editor must only stop that editor's own older request,
 * never the request of another editor that uses the same language.
 *
 * Not thread safe: the caller must guard every call with one lock.
 */
class ProcessRequestTracker {

    private data class Running(val endPointIdentity: EndPointIdentity, val requestId: RequestIdentity)

    // endPoint -> latest sentenceProcessRequest
    private val _latest = mutableMapOf<EndPointIdentity, RequestIdentity>()

    // language -> request currently being processed
    private val _running = mutableMapOf<LanguageIdentity, Running>()

    /**
     * Record that [requestId] has been received from [endPointIdentity].
     * @return false if a newer request from the same endPoint has already been received (this one is stale)
     */
    fun received(endPointIdentity: EndPointIdentity, requestId: RequestIdentity): Boolean {
        val latest = _latest[endPointIdentity]
        return if (null == latest || isNewer(requestId, latest)) {
            _latest[endPointIdentity] = requestId
            true
        } else {
            requestId == latest
        }
    }

    /** true if [requestId] is (still) the latest request received from [endPointIdentity] */
    fun isLatest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity): Boolean =
        _latest[endPointIdentity]?.let { it == requestId } ?: true

    fun start(languageId: LanguageIdentity, endPointIdentity: EndPointIdentity, requestId: RequestIdentity) {
        _running[languageId] = Running(endPointIdentity, requestId)
    }

    fun finish(languageId: LanguageIdentity, endPointIdentity: EndPointIdentity, requestId: RequestIdentity) {
        if (_running[languageId] == Running(endPointIdentity, requestId)) {
            _running.remove(languageId)
        }
    }

    /**
     * true if the request being processed for [languageId] is an older request from the same [endPointIdentity],
     * i.e. the running processing may be interrupted for [requestId]
     */
    fun shouldInterrupt(languageId: LanguageIdentity, endPointIdentity: EndPointIdentity, requestId: RequestIdentity): Boolean {
        val running = _running[languageId] ?: return false
        return running.endPointIdentity == endPointIdentity && running.requestId != requestId && isNewer(requestId, running.requestId)
    }

    /**
     * Editors number their requests; when both identities are numbers they are compared,
     * otherwise a request received later is taken as newer.
     */
    private fun isNewer(requestId: RequestIdentity, other: RequestIdentity): Boolean {
        val a = requestId.value.toLongOrNull()
        val b = other.value.toLongOrNull()
        return when {
            null != a && null != b -> a > b
            else -> requestId != other
        }
    }
}
