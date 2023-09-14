/*
 * Based on [https://github.com/daemontus/kotlin-ace-wrapper]
 */
package ace

external interface LineTokens {
    val state: LineState
    val tokens : Array<Token>
}

external interface LineState

external interface Token {
    val type : String
    val value: String
    val line:Int
    var start:Int
}

external interface Tokenizer {
    fun getLineTokens(line: String, state: LineState?, row: Int): LineTokens
}

external interface BackgroundTokenizer {
    var tokenizer: ace.Tokenizer

    fun start(i: Int)
    fun setDocument(document: Any)
}