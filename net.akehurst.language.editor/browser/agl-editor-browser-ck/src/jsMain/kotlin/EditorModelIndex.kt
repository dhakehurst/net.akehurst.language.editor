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

class EditorModelIndex() {

    var model: ck.engine.model.Model? = null
    var rawText: String = "" ; private set
    // preserves insertion order, which should be a 'sorted' order by key
    private val _reverseIndex = linkedMapOf<Int, ck.engine.model.Position>()
    val reverseIndex : Map<Int, ck.engine.model.Position> = _reverseIndex

    fun update(model: ck.engine.model.Model): EditorModelIndex {
        this.model = model
        rawText = ""
        _reverseIndex.clear()

        val rootRange = model.createRangeIn( model.document.getRoot() )
        // Iterate items based on [https://ckeditor.com/docs/ckeditor5/latest/framework/how-tos.html#how-to-find-words-in-a-document-and-get-their-ranges]
        val items =  rootRange.getItems().iterable()
        for (item in items) {
            if ( item.is_( "element" ) && model.schema.checkChild( item, "\$text" ) ) {
                val el = item as ck.engine.model.Element

                // Get the whole text from block.
                // Inline elements (like softBreak or imageInline) are replaced
                // with a single whitespace to keep the position offset correct.
                val blockText = el.getChildren().iterable().joinToString(separator = "") { ch ->
                    when {
                        //is ck.Text -> {
                        ch.is_("\$text") -> {
                            val txt = (ch as ck.engine.model.Text).data
                            //console.log("child text: '$txt'")
                            txt
                        }
                        //is ck.Element -> {
                        ch.is_("element") -> {
                            ch as ck.engine.model.Element
                            when {
                                "br"==ch.name -> "\n"
                                else -> ""
                            }
                        }
                        else -> {
                            //console.log("child '${ch::class.simpleName}'")
                            "\n"
                        }
                    }
                }
                val start = rawText.length
                val startPos = model.createPositionAt(item, 0)
                //console.log("'$blockText' $start : $startPos")
                _reverseIndex[start] = startPos
                rawText += blockText + "\n"
            }
        }
        //add a last position which is the end of the text
        val last = rawText.length
        val lastPos = model.createPositionAt(rootRange.end, 0)
        _reverseIndex[last] = lastPos
        return this
    }

    fun toModelPosition(characterIndex: Int): ck.engine.model.Position {
        //console.log(reverseIndex.entries.joinToString { "${it.key}:${it.value}" })
        val x = reverseIndex.entries.last { characterIndex >= it.key }
        val offset = characterIndex-x.key
        val pos = x.value.getShiftedBy(offset)
        return pos
    }

    fun toSentencePosition(cursorPos: ck.engine.model.Position ): Int {
        val lst = reverseIndex.entries.last { cursorPos.isAfter(it.value) }
        val pos = lst.key + cursorPos.offset.toInt()
        return pos
    }
}