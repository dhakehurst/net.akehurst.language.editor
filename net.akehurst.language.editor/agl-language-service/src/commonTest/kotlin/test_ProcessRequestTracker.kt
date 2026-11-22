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
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class test_ProcessRequestTracker {

    private val lang = LanguageIdentity("test.Lang")
    private val ed1 = EndPointIdentity("ed1", "s")
    private val ed2 = EndPointIdentity("ed2", "s")

    @Test
    fun older_request_is_stale() {
        val sut = ProcessRequestTracker()
        assertTrue(sut.received(ed1, RequestIdentity("1")))
        assertTrue(sut.received(ed1, RequestIdentity("2")))
        assertFalse(sut.received(ed1, RequestIdentity("1")))
        assertFalse(sut.isLatest(ed1, RequestIdentity("1")))
        assertTrue(sut.isLatest(ed1, RequestIdentity("2")))
        // other editors are independent
        assertTrue(sut.received(ed2, RequestIdentity("1")))
    }

    @Test
    fun interrupt_only_older_request_of_same_editor() {
        val sut = ProcessRequestTracker()
        sut.received(ed1, RequestIdentity("1"))
        sut.start(lang, ed1, RequestIdentity("1"))

        // another editor of the same language must not interrupt it
        assertFalse(sut.shouldInterrupt(lang, ed2, RequestIdentity("5")))
        // the same request must not interrupt itself
        assertFalse(sut.shouldInterrupt(lang, ed1, RequestIdentity("1")))
        // a newer request of the same editor does
        assertTrue(sut.shouldInterrupt(lang, ed1, RequestIdentity("2")))

        sut.finish(lang, ed1, RequestIdentity("1"))
        assertFalse(sut.shouldInterrupt(lang, ed1, RequestIdentity("2")))
    }
}
