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


import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.common.AglComponents
import net.akehurst.language.editor.common.AglTokenizer
import net.akehurst.language.editor.common.AglTokenizerByWorker

data class CkAttributeData(
    val firstPosition: ck.engine.model.Position,
    val lastPosition: ck.engine.model.Position,
    val attributes: Map<String, Any>
)

class AglTokenizerByWorkerCk<AsmType : Any, ContextType : Any>(
    agl: AglComponents<AsmType, ContextType>,
    val emi: EditorModelIndex,
    val logger: AglEditorLogger
) : AglTokenizerByWorker<CkStyle> {

    val aglTokenizer = AglTokenizer<AsmType, ContextType, CkStyle>(agl, agl.logger)
    private var count = 0

    override fun reset() {
        this.aglTokenizer.reset()
    }

    override fun receiveTokens(startLine: Int, tokensForLines: List<List<AglToken>>) {
        logger.logTrace { "Received tokens: $startLine, $tokensForLines" }
        this.aglTokenizer.receiveTokens(startLine, tokensForLines)
        //TODO: Line based update - maybe 'CK-Block' based update as blocks somehow map to lines
        refresh()
    }

    private fun updateCkModel(ckTokens: List<CkAttributeData>) {
        try {
            emi.model?.let { CkEditorHelper.addAttributes(logger, it, ckTokens, CkEditorHelper.ATTRIBUTE_SET_SYNTAX_STYLE) }
        } catch (t: Throwable) {
            logger.logError(t) { "Failed to add CK attribute (could be because the model has changed)" }
        }
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
        logger.logTrace { "Refresh Tokens: $tokens" }
        for (token in tokens) {
            val fp = emi.toModelPosition(token.position)
            val lp = emi.toModelPosition(token.position + token.length)
            // to ensure only the last attribute value is set, overwrite map entries
            val styleIds = token.styles
            val flatAtts = styleIds.fold(emptyMap<String, Any>()) { acc, it ->
                val ckStyle = aglTokenizer.agl.styleHandler.editorStyleFor(it) as CkStyle?
                acc + (ckStyle?.attribs ?: emptyMap())
            }
            ckTokens.add(CkAttributeData(fp, lp, flatAtts))
        }
        updateCkModel(ckTokens)
    }
}