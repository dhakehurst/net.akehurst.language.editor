package net.akehurst.language.testEditor

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.window.singleWindowApplication
import net.akehurst.language.editor.compose.AglComposeTextEditor

fun main() {
    val sentenceEditor = AglComposeTextEditor()

    singleWindowApplication(
        title = "AGL Editor",
    ) {
        sentenceEditor.content()
    }
}

