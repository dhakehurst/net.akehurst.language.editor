package net.akehurst.language.editor.browser.ck


import js.iterable

class EditorModelIndex() {

    var model: ck.Model? = null
    var rawText: String = "" ; private set
    // preserves insertion order, which should be a 'sorted' order by key
    private val _reverseIndex = linkedMapOf<Int, ck.Position>()
    val reverseIndex : Map<Int, ck.Position> = _reverseIndex

    fun update(model: ck.Model): EditorModelIndex {
        this.model = model
        rawText = ""
        _reverseIndex.clear()

        val rootRange = model.createRangeIn( model.document.getRoot() )
        // Iterate items based on [https://ckeditor.com/docs/ckeditor5/latest/framework/how-tos.html#how-to-find-words-in-a-document-and-get-their-ranges]
        val items =  rootRange.getItems().iterable()
        for (item in items) {
            if ( item.is_( "element" ) && model.schema.checkChild( item, "\$text" ) ) {
                val el = item as ck.Element

                // Get the whole text from block.
                // Inline elements (like softBreak or imageInline) are replaced
                // with a single whitespace to keep the position offset correct.
                val blockText = el.getChildren().iterable().joinToString(separator = "") { ch ->
                    when {
                        //is ck.Text -> {
                        ch.is_("\$text") -> {
                            val txt = (ch as ck.Text).data
                            console.log("child text: '$txt'")
                            txt
                        }
                        //is ck.Element -> {
                        ch.is_("element") -> {
                            ch as ck.Element
                            when {
                                "br"==ch.name -> "\n"
                                else -> ""
                            }
                        }
                        else -> {
                            console.log("child '${ch::class.simpleName}'")
                            "\n"
                        }
                    }
                }

                // Find all words.
                //not sure we need to do this
                /*
                val mrs = Regex("\\S+").findAll(blockText)
                for ( mr  in mrs ) {
                    // The position in a text node is always parented by the block element.
                    val sp = model.createPositionAt( item, mr.range.first )
                    val ep = model.createPositionAt( item, mr.range.last )
                    reverseIndex[]

                    wordRanges.push( model.createRange( startPosition, endPosition ) );
                }
                 */
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

    fun toModelPosition(characterIndex: Int): ck.Position {
        //console.log(reverseIndex.entries.joinToString { "${it.key}:${it.value}" })
        val x = reverseIndex.entries.last { characterIndex >= it.key }
        val offset = characterIndex-x.key
        val pos = x.value.getShiftedBy(offset)
        return pos
    }
}