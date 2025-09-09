/*
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.akehurst.language.editor.compose

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.editor.CodeEditorState
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlin.compose.editor.CodeEditorView
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.*
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.common.aglEditorOptions
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution

class AglComposeTextEditor(
    initialText: String = "Hello World!",
    grammarString: GrammarString = GrammarString(
        """
        namespace example
        grammar HelloWorld {
          S = 'Hello' target ;
          target = 'World' '!' | NAME ;
          leaf NAME = "[a-zA-Z_][a-zA-Z0-9_]*]" ;
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
        languageService, languageDefinition,
        { Agl.options { semanticAnalysis { context(contextAsmSimple()) } } },
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