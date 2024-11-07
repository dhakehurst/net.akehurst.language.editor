@file:JsModule("@ckeditor/ckeditor5-editor-balloon")
@file:JsNonModule
package ck.editor.balloon

import kotlin.js.Promise

external interface BalloonEditor : ck.core.editor.Editor {
    companion object {
        fun create(element: dynamic, config: dynamic): Promise<BalloonEditor>
    }
}

external interface BalloonEditorUI : ck.ui.editorui.EditorUI {

}