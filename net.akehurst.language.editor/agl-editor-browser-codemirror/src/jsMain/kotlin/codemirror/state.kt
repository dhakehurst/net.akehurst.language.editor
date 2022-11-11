package codemirror.state

external object EditorState {
    fun create()

    val doc : Text
}

external interface EditorStateConfig {
    var doc : String
}

external interface CodeMirrorOptions {

}

external interface Text {
    val length : Number

}
