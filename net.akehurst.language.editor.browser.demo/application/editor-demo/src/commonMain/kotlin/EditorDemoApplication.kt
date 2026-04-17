package net.akehurst.language.editor.demo

import kotlinx.coroutines.Deferred
import net.akehurst.kotlinx.logging.api.logger
import net.akehurst.language.agl.Agl
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.browser.demo.application_editor_demo.generated.resources.Res
import net.akehurst.language.editor.demo.gui.Gui
import net.akehurst.language.editor.information.Examples

class EditorDemoApplication(
    languageService: LanguageService,
) {
    companion object {
        val LOGGER = logger(EditorDemoApplication::class.simpleName!!)
    }

    val gui = Gui(languageService)

    private suspend fun initialiseExamples() {
        val examples = mutableListOf(
            "BasicTutorial",
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
            val info = readContent("files/examples/$path/info.txt") ?: path
            val sentence = readContent("files/examples/$path/sentence.txt")
            val grammar = readContent("files/examples/$path/grammar.agl-grm")
            val style = readContent("files/examples/$path/style.agl-sty")
            val asmTransform = readContent("files/examples/$path/asmTransform.agl-trn")
            val types = readContent("files/examples/$path/types.agl-typ")
            val references = readContent("files/examples/$path/references.agl-ref")
            val format = readContent("files/examples/$path/format.agl-fmt")
            val context = readContent("files/examples/$path/context.agl-ctx")
            Examples.add(path, info, sentence, grammar, types, asmTransform, references, style, format, context)
        }

        Agl.registry.initialise()
        for ((id, lang) in Agl.registry.languages) {
            val grammar = lang.grammarString?.value ?: ""
            val style = lang.styleString?.value ?: ""
            val asmTransform = lang.asmTransformString?.value ?: ""
            val types = lang.typesString?.value ?: ""
            val references = lang.crossReferenceString?.value ?: ""
            val format = lang.formatString?.value ?: ""
            val context = "" //TODO
            Examples.add(id.value, id.value, "", grammar, types, asmTransform, references, style, format, context)
        }
    }

    private suspend fun readContent(path: String): String? {
        try {
            val bytes = Res.readBytes(path)
            return bytes.decodeToString()
        } catch (e: Exception) {
            LOGGER.logInformation { e.message ?: "Error in readContent ${e::class.simpleName}" }
            return null
        }
    }

    suspend fun start(guiStart: suspend (Gui) -> Deferred<Unit>) {
        try {
            initialiseExamples()
            val guiJob = guiStart.invoke(gui)
            guiJob.join()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

}