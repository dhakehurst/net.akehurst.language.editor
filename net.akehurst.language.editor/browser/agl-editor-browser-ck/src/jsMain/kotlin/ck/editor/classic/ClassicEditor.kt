@file:JsModule("@ckeditor/ckeditor5-editor-classic")
@file:JsNonModule
package ck.editor.classic

import kotlin.js.Promise

external interface ClassicEditor : ck.core.editor.Editor {
    companion object {
        fun create(element: dynamic, config: dynamic): Promise<ClassicEditor>
    }
}

external interface ClassicEditorUI : ck.ui.editorui.EditorUI {

}
