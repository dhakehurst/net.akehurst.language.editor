/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.browser.ck


import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.browser.ck.EditorModelIndex
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenizer
import net.akehurst.language.editor.common.AglTokenizerByWorker

class AglTokenizerByWorkerCk<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>,
    val emi: EditorModelIndex
) : AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)
    private var styleMap:Map<String,Map<String,Any>> = emptyMap()

    fun updateStyleMap(value:Map<String,Map<String,Any>>) {
        this.styleMap = value
        console.log("Updated Styles '${aglTokenizer.agl.editorId}': ${this.styleMap}")
    }

    override fun reset() {
        this.aglTokenizer.reset()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        console.log("Received tokens: $startLine, $tokensForLines")
        this.aglTokenizer.receiveTokens(startLine, tokensForLines)
        for (line in tokensForLines) {
            for (token in line) {
                updateCkModel(token)
            }
        }
    }


    private fun updateCkModel(token: AglToken) {
        console.log("Current Styles '${aglTokenizer.agl.editorId}': ${this.styleMap}")
        val firstPosition = token.position
        val lastPosition = firstPosition + token.length
        emi.model?.enqueueChange { writer ->
            val fp = emi.toModelPosition(firstPosition)
            val lp = emi.toModelPosition(lastPosition)
            val rng = writer.createRange(fp, lp)
            for(style in token.styles) {
                val atts = this.styleMap[style] ?: emptyMap()
                console.log("In editor '${aglTokenizer.agl.editorId}' styles for '$style': $atts")
                for(att in atts.entries) {
                    console.log("Set '${att.key}' = '${att.value}' for [${rng.start.path} - ${rng.end.path}]")
                    writer.setAttribute(att.key, att.value, rng)
                }
            }

            // test fails !
            val cursor = emi.model?.document?.selection
            if (null!=cursor && true == cursor.anchor?.isEqual(lp)) {
                val n = cursor.anchor?.nodeAfter
                if (null != n) {
                    val after = writer.createPositionAt(n, 0)
                    writer.setSelection(after)
                }
            }
        }
    }
}