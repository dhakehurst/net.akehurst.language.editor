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

import net.akehurst.language.agl.util.ReentrantLockCommon
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.RequestIdentity
import net.akehurst.language.issues.api.IssueCollection
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.parser.api.ParseInterrupt
import net.akehurst.language.parser.leftcorner.ParseInterruptDefault
import net.akehurst.language.parsermessages.IssueMessage

/**
 * Keeps the [ParseInterrupt] of the request that is running now, for each endPoint, language and kind of request.
 *
 * Each request is given a new [ParseInterrupt] (put in its ParseOptions.interrupt), so that it can be stopped on its own.
 * LanguageProcessor.interrupt is not used, because it stops every parse of the processor that is running,
 * including parses that other code (e.g. a direct call of processor.parse) runs on the same processor.
 *
 * Thread safe.
 */
class ParseInterruptRegister {

    companion object {
        /** the start of the message of the parse error given by an interrupted parse */
        val PARSER_TERMINATED_MESSAGE_PREFIX = IssueMessage.parserTerminated("")

        /** true if [issues] hold the error of an interrupted parse */
        fun isTerminated(issues: IssueCollection<LanguageIssue>): Boolean =
            issues.errors.any { it.message.startsWith(PARSER_TERMINATED_MESSAGE_PREFIX) }
    }

    enum class Kind { PROCESS, CODE_COMPLETE }

    private data class Key(val endPointIdentity: EndPointIdentity, val languageId: LanguageIdentity, val kind: Kind)
    private class Running(val requestId: RequestIdentity, val interrupt: ParseInterrupt)

    private val _lock = ReentrantLockCommon()
    private val _running = mutableMapOf<Key, Running>()

    /**
     * Start [requestId]: interrupt the request of the same endPoint, language and kind that is running (if any),
     * and return a new [ParseInterrupt] for [requestId].
     */
    fun start(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, languageId: LanguageIdentity, kind: Kind, reason: String): ParseInterrupt {
        val handle = ParseInterruptDefault()
        val previous = _lock.withLock { _running.put(Key(endPointIdentity, languageId, kind), Running(requestId, handle)) }
        previous?.interrupt?.interrupt(reason)
        return handle
    }

    /** [handle] is no longer running */
    fun finish(endPointIdentity: EndPointIdentity, languageId: LanguageIdentity, kind: Kind, handle: ParseInterrupt) {
        _lock.withLock {
            val key = Key(endPointIdentity, languageId, kind)
            if (_running[key]?.interrupt === handle) {
                _running.remove(key)
            }
        }
    }

    /** interrupt the request of [endPointIdentity] for [languageId] that is running, unless it is [requestId] */
    fun interrupt(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, languageId: LanguageIdentity, kind: Kind, reason: String) {
        val running = _lock.withLock { _running[Key(endPointIdentity, languageId, kind)] }
        if (null != running && running.requestId != requestId) {
            running.interrupt.interrupt(reason)
        }
    }

    /** the request given [handle] was interrupted, or its parse was terminated */
    fun isCancelled(handle: ParseInterrupt, issues: IssueCollection<LanguageIssue>? = null): Boolean =
        null != handle.message || (null != issues && isTerminated(issues))

}
