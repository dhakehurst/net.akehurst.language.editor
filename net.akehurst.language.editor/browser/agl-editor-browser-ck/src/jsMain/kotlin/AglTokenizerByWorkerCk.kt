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


import js.iterable
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.LogLevel
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenizer
import net.akehurst.language.editor.common.AglTokenizerByWorker

data class CkAttributeData(
    val firstPosition: ck.Position,
    val lastPosition: ck.Position,
    val attributes : Map<String,Any>
)

class AglTokenizerByWorkerCk<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>,
    val emi: EditorModelIndex,
    val logger: AglEditorLogger
) : AglTokenizerByWorker {

    val aglTokenizer = AglTokenizer(agl)
    private var count = 0
    private var styleMap: Map<String, Map<String, Any>> = emptyMap()

    fun updateStyleMap(value: Map<String, Map<String, Any>>) {
        count += 1
        this.styleMap = value
        logger.log(LogLevel.Trace, "Updated Styles $count '${aglTokenizer.agl.editorId}': ${this.styleMap}", null)
    }

    override fun reset() {
        this.aglTokenizer.reset()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        logger.log(LogLevel.Trace, "Received tokens: $startLine, $tokensForLines", null)
        this.aglTokenizer.receiveTokens(startLine, tokensForLines)
        //TODO: Line based update - maybe 'CK-Block' based update as blocks somehow map to lines
        refresh()
    }

    private fun updateCkModel(ckTokens: List<CkAttributeData>) {
        emi.model?.let { CkEditorHelper.addAttributes(logger, it, ckTokens, styleMap.keys) }
    }

    /*
        private fun updateCkModel(token: AglToken) {
            logger.log(LogLevel.Trace, "Current Styles $count '${aglTokenizer.agl.editorId}': ${this.styleMap}", null)
            val firstPosition = token.position
            val lastPosition = firstPosition + token.length
            emi.model?.enqueueChange { writer ->
                val fp = emi.toModelPosition(firstPosition)
                val lp = emi.toModelPosition(lastPosition)
                val rng = writer.createRange(fp, lp)
                for (style in token.styles) {
                    val atts = this.styleMap[style] ?: emptyMap()
                    //console.log("In editor '${aglTokenizer.agl.editorId}' styles for '$style': $atts")
                    for (att in atts.entries) {
                        logger.log(LogLevel.Trace, "Set '${att.key}' = '${att.value}' for [${rng.start.path} - ${rng.end.path}]", null)
                        writer.setAttribute(att.key, att.value, rng)
                    }
                }

                // test fails !
                val cursor = emi.model?.document?.selection
                if (null != cursor && true == cursor.anchor?.isEqual(lp)) {
                    val n = cursor.anchor?.nodeAfter
                    if (null != n) {
                        val after = writer.createPositionAt(n, 0)
                        writer.setSelection(after)
                    }
                }
            }
        }
    */

    /*
     * refresh the style of each cached token
     */
    fun refresh() {
        val ckTokens = mutableListOf<CkAttributeData>()
        val tokens = aglTokenizer.getAllTokens(emi.rawText)
        logger.log(LogLevel.Trace, "Refresh Tokens: $tokens", null)
        for (token in tokens) {
            val fp = emi.toModelPosition(token.position)
            val lp = emi.toModelPosition(token.position + token.length)
            // to ensure only the last attribute value is set, overwrite map entries
            val atts = token.styles.map { style -> this.styleMap[style] ?: emptyMap() }
            val flatAtts = atts.fold(emptyMap<String,Any>()) { acc, it -> acc + it }
            ckTokens.add(CkAttributeData(fp,lp,flatAtts))
        }
        updateCkModel(ckTokens)
    }
}