package codemirror.view

import codemirror.state.EditorStateConfig

external class EditorView(config: EditorViewConfig) {

}

external interface EditorViewConfig : EditorStateConfig {
    var state: dynamic
    var parent: dynamic
    var root: dynamic
    var dispatch: dynamic
}