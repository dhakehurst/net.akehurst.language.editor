package codemirror.state

external object EditorState {
    fun create()

    val doc: Text
}

external interface EditorStateConfig {
    var doc: String
    var selection: dynamic //EditorSelection | {anchor: number, head?: number}
    var extensions: dynamic //Extension
}

external interface CodeMirrorOptions {

}

external interface Text {
    var length: Number

}
