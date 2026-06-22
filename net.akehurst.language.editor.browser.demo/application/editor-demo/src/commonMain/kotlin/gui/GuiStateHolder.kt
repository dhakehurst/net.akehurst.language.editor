package net.akehurst.language.editor.demo.gui

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.akehurst.kotlin.compose.components.ModalDialog
import net.akehurst.kotlin.compose.components.SplitOrientation
import net.akehurst.kotlin.compose.components.tree.TreeView
import net.akehurst.kotlin.compose.components.tree.TreeViewStateHolder
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlin.compose.editor.CodeEditorView
import net.akehurst.kotlin.compose.layout.multipane.MultiPaneLayoutState
import net.akehurst.kotlin.compose.layout.multipane.layoutNode
import net.akehurst.language.editor.information.Example

class GuiStateHolder(
    val gui: Gui
) {

    companion object {
        enum class ViewMode {
            SENTENCE, LANGUAGE, CONFIGURE
        }
    }

    var modalDialog by mutableStateOf<ModalDialog?>(null)

    var sentenceMode = SentenceModeStateHolder()
    var languageMode = LanguageModeStateHolder()
    var configureMode = ConfigureModeStateHolder()

    fun handler_openLanguageDirectory() = gui.handler.openLanguageDirectory()
    fun handler_loadExample(eg: Example) = gui.handler.loadExample(eg)
}

class SentenceModeStateHolder() {
    val editorState = CodeEditorStateHolder()
    val parseTreeState = TreeViewStateHolder()
    val asmTreeState = TreeViewStateHolder()

    val layout by mutableStateOf(MultiPaneLayoutState(layoutNode {
        split(orientation = SplitOrientation.Horizontal) {
            pane(1f, "sentence", "Sentence") {
                CodeEditorView(editorState)
            }
            tabbed(1f) {
                pane("parseTree", "Parse Tree") {
                    TreeView(parseTreeState)
                }
                pane("ast", "AST") {
                    TreeView(asmTreeState)
                }
            }
        }
    }))
}

class LanguageModeStateHolder() {

    val grammareEditorState = CodeEditorStateHolder()
    val styleEditorState = CodeEditorStateHolder()
    val typesEditorState = CodeEditorStateHolder()
    val asmTransEditorState = CodeEditorStateHolder()
    val refsEditorState = CodeEditorStateHolder()
    val formatEditorState = CodeEditorStateHolder()
    val m2mEditorState = CodeEditorStateHolder()

    val typesTreeStateHolder = TreeViewStateHolder()

    val layout by mutableStateOf(MultiPaneLayoutState(layoutNode {
        split(orientation = SplitOrientation.Horizontal) {
            tabbed(1f) {
                pane("grammar", "Grammar") {
                    CodeEditorView(grammareEditorState)
                }
                pane("style", "Style") {
                    CodeEditorView(styleEditorState)
                }
                pane("types", "Types") {
                    CodeEditorView(typesEditorState)
                }
                pane("asmTrans", "Asm Transform") {
                    CodeEditorView(asmTransEditorState)
                }
                pane("refs", "References") {
                    CodeEditorView(refsEditorState)
                }
                pane("format", "Format") {
                    CodeEditorView(formatEditorState)
                }
            }
            tabbed(1f) {
                pane("types", "Types") {
                    TreeView(typesTreeStateHolder)
                }
            }
        }
    }))
}

class ConfigureModeStateHolder() {

}