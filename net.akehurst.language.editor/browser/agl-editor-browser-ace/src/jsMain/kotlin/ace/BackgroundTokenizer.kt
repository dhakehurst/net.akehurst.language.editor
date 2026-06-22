/*
 * Based on [https://github.com/daemontus/kotlin-ace-wrapper]
 */
package ace

external interface LineTokens {
    val state: LineState
    val tokens : Array<Token>
}

/**
 * toString is used as part of checking equality on these objects by ace.BackgroundTokenizer
 */
external interface LineState

external interface Token {
    val type : String
    val value: String
    var index:Int?
    var start:Int?
}

external interface Tokenizer {
    fun getLineTokens(line: String, state: LineState?): LineTokens
}

external interface BackgroundTokenizer {
    fun setTokenizer(tokenizer:Tokenizer)
    fun start(i: Int)
    fun setDocument(document: Any)
}