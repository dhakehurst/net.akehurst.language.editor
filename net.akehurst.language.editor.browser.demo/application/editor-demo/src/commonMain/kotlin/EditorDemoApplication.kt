package net.akehurst.language.editor.demo

import kotlinx.coroutines.Deferred
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.browser.demo.application_editor_demo.generated.resources.Res
import net.akehurst.language.editor.demo.gui.Gui
import net.akehurst.language.editor.information.Examples

class EditorDemoApplication(
    languageService: LanguageService,
) {

    val gui = Gui(languageService)

    private suspend fun initialiseExamples() {
        val examples = mutableListOf(
            "Datatypes",
            "GraphvizDot",
            "Java8",
            "MScript",
            "SQL",
            "Statecharts",
            "TraceabilityQuery",
            "XML",
        )
        for (path in examples) {
            val info = readContent("files/examples/$path/info.txt")
            val sentence = readContent("files/examples/$path/sentence.txt")
            val grammar = readContent("files/examples/$path/grammar.agl-grm")
            val style = readContent("files/examples/$path/style.agl-sty")
            val references = readContent("files/examples/$path/references.agl-ref")
            val format = readContent("files/examples/$path/format.agl-fmt")
            val context = readContent("files/examples/$path/context.agl-ctx")
            val types = readContent("files/examples/$path/types.agl-typ")
            Examples.add(path, info, sentence, grammar, types, references, style, format, context)
        }

    }
    private suspend fun readContent(path:String):String {
        try {
            val bytes = Res.readBytes(path)
            return bytes.decodeToString()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    suspend fun start(guiStart: suspend (Gui) -> Deferred<Unit>) {
        initialiseExamples()
        val guiJob = guiStart.invoke(gui)
        guiJob.join()
    }

}