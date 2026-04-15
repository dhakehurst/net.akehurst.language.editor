package net.akehurst.language.editor.demo.gui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.kotlinx.logging.api.logger
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.processor.contextFromGrammarRegistry
import net.akehurst.language.agl.semanticAnalyser.ContextFromTypesDomain
import net.akehurst.language.agl.semanticAnalyser.contextFromTypesDomain
import net.akehurst.language.agl.simple.SentenceContextAny
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.CrossReferenceString
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.processor.StyleString
import net.akehurst.language.asm.api.Asm
import net.akehurst.language.asmTransform.asm.AsmTransformDomainDefault
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.EventStatus
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.common.EditorOptionsDefault
import net.akehurst.language.editor.compose.attachToComposeEditor
import net.akehurst.language.grammar.api.GrammarDomain
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.grammar.processor.contextFromGrammar
import net.akehurst.language.reference.api.CrossReferenceDomain
import net.akehurst.language.style.api.AglStyleDomain
import kotlin.String

class Gui(
    val languageService: LanguageService
) {
    companion object {
        val LOGGER = logger("Gui")

        object Constants {
            const val sentenceEditorId = "editor-sentence"
            const val grammarEditorId = "editor-grammar"
            const val referencesEditorId = "editor-references"
            const val styleEditorId = "editor-style"
            const val formatEditorId = "editor-format"

            val sentenceLanguageId = LanguageIdentity("language-user")

            val sentenceProcessOptions = Agl.options<Asm, SentenceContextAny> { semanticAnalysis { sentenceContext(contextAsmSimple()) } }
            val grammarProcessOptions = Agl.options<GrammarDomain, SentenceContextAny> { semanticAnalysis { sentenceContext(contextAsmSimple()) } }
            val styleProcessOptions = Agl.options<AglStyleDomain, SentenceContextAny> { semanticAnalysis { sentenceContext(contextAsmSimple()) } }
            val referencesProcessOptions = Agl.options<CrossReferenceDomain, SentenceContextAny> { }
        }

        val sentenceLanguage = Agl.registry.register(
            identity = Constants.sentenceLanguageId,
            buildForDefaultGoal = false,
            aglOptions = Agl.options {
                semanticAnalysis {
                    sentenceContext(contextFromGrammarRegistry(Agl.registry))
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            configuration = Agl.configurationSimple()
        )
        val grammarLanguage = Agl.registry.agl.grammar
        val styleLanguage = Agl.registry.agl.style
        val referencesLanguage = Agl.registry.agl.crossReference

        fun <T : Any, C : Any> createComposeEditor(
            editorState: CodeEditorStateHolder,
            editorId: String,
            logFunction: LogFunction,
            languageService: LanguageService,
            landDef: LanguageDefinition<T, C>,
            procOpts: ProcessOptions<T, C>
        ): AglEditor<T, C> {
            return Agl.attachToComposeEditor(
                languageService = languageService,
                languageDefinition = landDef,
                processOptions = { procOpts },
                editorId = editorId,
                editorOptions = EditorOptionsDefault(),
                logFunction = logFunction,
                composeEditor = editorState
            )
        }
    }

    val logFunction: LogFunction = { logLevel, prefix, t, msg ->
        LOGGER.log(logLevel, t) { prefix + "-" + msg.invoke() }
    }

    val stateHolder = GuiStateHolder(this)
    lateinit var sentenceEditor: AglEditor<Asm, SentenceContextAny>
    lateinit var grammarEditor: AglEditor<GrammarDomain, SentenceContextAny>
    lateinit var styleEditor: AglEditor<AglStyleDomain, SentenceContextAny>
    lateinit var referencesEditor: AglEditor<CrossReferenceDomain, SentenceContextAny>

    val handler = GuiHandler(this)
    val view = GuiView()

    fun doStart() {
        // create AGL Editors
        sentenceEditor = createComposeEditor(
            stateHolder.sentenceMode.editorState, Constants.sentenceEditorId, logFunction, languageService, sentenceLanguage, Constants.sentenceProcessOptions
        )
        grammarEditor = createComposeEditor(
            stateHolder.languageMode.grammareEditorState, Constants.grammarEditorId, logFunction, languageService, grammarLanguage, Constants.grammarProcessOptions
        )
        styleEditor = createComposeEditor(
            stateHolder.languageMode.styleEditorState, Constants.styleEditorId, logFunction, languageService, styleLanguage, Constants.styleProcessOptions
        )
        referencesEditor = createComposeEditor(
            stateHolder.languageMode.refsEditorState, Constants.referencesEditorId, logFunction, languageService, referencesLanguage, Constants.referencesProcessOptions
        )

        handler.connectEditors()
    }

    @Composable
    fun content() {
        view.content(stateHolder)
    }
}