/**
 * Copyright (C) 2018 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.parser.scannerless

import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.agl.runtime.structure.RuntimeRule
import net.akehurst.language.parser.sppt.SPPTLeafDefault

internal class InputFromCharSequence(val text: CharSequence) {

    companion object {
        val END_OF_TEXT = 3.toChar().toString()
    }

    internal fun isStart(position: Int): Boolean {
        // TODO what if we want t0 parse part of the text?, e.g. sub grammar
        return 0 == position
    }

    internal fun isEnd(position: Int): Boolean {
        // TODO what if we want t0 parse part of the text?, e.g. sub grammar
        return position >= this.text.length
    }

    private fun matchLiteral(position: Int, patternText: String): String? {
        val match = this.text.regionMatches(position, patternText, 0, patternText.length, false)
        return if (match) patternText else null
    }

    private fun matchRegEx(position: Int, patternText: String): String? {
        val pattern = Regex(patternText, setOf(RegexOption.MULTILINE))
        val m = pattern.find(this.text, position)
        val lookingAt = (m?.range?.start == position)
        return if (lookingAt) m?.value else null
    }

    internal fun calcLineAndColumn(position: Int): InputLocation {
        var line = 1
        var column = 0

        for (count in 0 until position) {
            if (this.text[count] == '\n') {
                ++line
                column = 0
            } else {
                ++column
            }
        }
        return InputLocation(column,line,0)
    }

    internal fun tryMatchText(position: Int, patternText: String, isPattern: Boolean): String? {
        return when {
            (position >= this.text.length) -> if (patternText== END_OF_TEXT) END_OF_TEXT else null// TODO: should we need to do this?
            (!isPattern) -> this.matchLiteral(position, patternText)
            else -> this.matchRegEx(position, patternText)
        }
    }
}