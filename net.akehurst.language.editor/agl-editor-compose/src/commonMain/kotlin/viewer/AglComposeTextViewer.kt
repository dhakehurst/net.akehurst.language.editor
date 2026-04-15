package net.akehurst.language.editor.compose.viewer

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlin.compose.editor.CodeEditorView
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.AsmTransformString
import net.akehurst.language.api.processor.CrossReferenceString
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.api.processor.TypesString
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.common.aglEditorOptions
import net.akehurst.language.editor.compose.attachToComposeEditor
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution

class AglComposeTextViewer(
    initialText: String = "Hello World!",
    grammarString: GrammarString = GrammarString(
        """
        namespace example
        grammar HelloWorld {
          skip leaf WS = "\s+" ; 
          S = 'Hello' target ;
          target = 'World' '!' | NAME ;
          leaf NAME = "[a-zA-Z_][a-zA-Z0-9_]*" ;
        }
    """.trimIndent()
    ),
    typeModelString: TypesString = TypesString(""),
    asmTransformString: AsmTransformString = AsmTransformString(""),
    crossReferenceString: CrossReferenceString = CrossReferenceString(""),
    styleString: StyleString = StyleString("")
) {
    val languageIdentity = LanguageIdentity("example")
    val languageDefinition = Agl.registry.findOrPlaceholder(
        languageIdentity,
        aglOptions = Agl.options {  },
        configuration = Agl.configurationSimple()
    )
    var processOptions
        get() = aglEditor.processOptions
        set(value) {
            aglEditor.processOptions = value
        }
    val editorOptions = aglEditorOptions { }

    val editorId = "text-editor"
    val endPointIdentity: EndPointIdentity = EndPointIdentity(editorId, "<none>")

    val logFunction: LogFunction = { lvl, prefix, msg, t -> println("$lvl: $prefix - $msg") }
    val languageService = LanguageServiceDirectExecution(logFunction)

    val editorState = CodeEditorStateHolder(
        initialText = initialText
    )

    val aglEditor = Agl.attachToComposeEditor(
        languageService,
        languageDefinition,
        { Agl.options { semanticAnalysis { sentenceContext(contextAsmSimple()) } } },
        editorId, editorOptions, logFunction, editorState
    )

    init {
        //aglEditor =
        updateLanguageDefinition(grammarString, typeModelString, asmTransformString, crossReferenceString, styleString)
    }

    @Composable
    fun content() {
        Surface {
            CodeEditorView(editorState)
        }
    }

    var text: String
        get() = editorState.rawText
        set(value) {
            editorState.setNewText(value)
        }

    fun updateLanguageDefinition(
        grammarString: GrammarString,
        typeModelString: TypesString = TypesString(""),
        asmTransformString: AsmTransformString = AsmTransformString(""),
        crossReferenceString: CrossReferenceString = CrossReferenceString(""),
        styleString: StyleString = StyleString("")
    ) {
        aglEditor.languageDefinition.update(
            grammarString,
            typeModelString,
            asmTransformString,
            crossReferenceString,
            styleString
        )
        aglEditor.refreshProcessor()
        aglEditor.refreshStyleHandler()
        aglEditor.processSentence(text)
    }
}