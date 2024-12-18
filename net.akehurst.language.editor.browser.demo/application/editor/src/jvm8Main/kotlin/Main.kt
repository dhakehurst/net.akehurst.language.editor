package net.akehurst.language.testEditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.singleWindowApplication
import net.akehurst.kotlin.compose.editor.CodeEditor
import net.akehurst.kotlin.compose.editor.EditorState

fun main() {
    singleWindowApplication(
        title = "AGL Editor",
    ) {
        content()
    }
}

@Composable
fun content() {

}