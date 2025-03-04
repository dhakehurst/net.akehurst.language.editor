/**
 * Copyright (C) 2025 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglStyleHandlerAsHtml {

    @Test
    fun decodeFromHtml() {
        val encodedHtml = "&lt; Max Valve Position AND Thermostat Head's Valve Position &amp;gt;"
        val actual = AglStyleHandlerAsHtml.decodeFromHtml(encodedHtml)

        val expected = "< Max Valve Position AND Thermostat Head's Valve Position >"

        assertEquals(expected, actual)
    }

    @Test
    fun decodeFromHtml2() {
        val encodedHtml = """
            The External System definition Radiator accepts the transition
            from state Completely Closed Valve to state Partially Open Valve
            when the event Thermostat Head's Valve Position &lt; Max Valve
            Position AND Thermostat Head's Valve Position &amp;gt; Min Valve
            Position becomes true occurs.
        """
        val actual = AglStyleHandlerAsHtml.decodeFromHtml(encodedHtml)

        val expected = """
            The External System definition Radiator accepts the transition
            from state Completely Closed Valve to state Partially Open Valve
            when the event Thermostat Head's Valve Position < Max Valve
            Position AND Thermostat Head's Valve Position > Min Valve
            Position becomes true occurs.
        """

        assertEquals(expected, actual)
    }
}