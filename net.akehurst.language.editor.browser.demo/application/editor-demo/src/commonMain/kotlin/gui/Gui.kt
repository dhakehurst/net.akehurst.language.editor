package net.akehurst.language.editor.demo.gui

import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.editor.CodeEditorStateHolder
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.logger
import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.processor.contextFromGrammarRegistry
import net.akehurst.language.agl.simple.SentenceContextAny
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.semanticAnalyser.SentenceContext
import net.akehurst.language.asm.api.Asm
import net.akehurst.language.asmTransform.api.AsmTransformDomain
import net.akehurst.language.editor.api.AglEditor
import net.akehurst.language.editor.api.LanguageService
import net.akehurst.language.editor.common.EditorOptionsDefault
import net.akehurst.language.editor.compose.attachToComposeEditor
import net.akehurst.language.formatter.api.AglFormatDomain
import net.akehurst.language.grammar.api.GrammarDomain
import net.akehurst.language.grammar.processor.AglGrammarSemanticAnalyser
import net.akehurst.language.m2mTransform.api.M2mTransformDomain
import net.akehurst.language.reference.api.CrossReferenceDomain
import net.akehurst.language.style.api.AglStyleDomain
import net.akehurst.language.types.api.TypesDomain

class Gui(
    val languageService: LanguageService
) {
    companion object {
        val LOGGER = logger("Gui")

        object Constants {
            const val sentenceEditorId = "editor-sentence"
            const val grammarEditorId = "editor-grammar"
            const val typesEditorId = "editor-types"
            const val asmTransEditorId = "editor-asmTrans"
            const val referencesEditorId = "editor-references"
            const val styleEditorId = "editor-style"
            const val formatEditorId = "editor-format"
            const val m2mEditorId = "editor-m2m"

            const val CP_DEPTH = 1

            val sentenceLanguageId = LanguageIdentity("language-user")

            val sentenceProcessOptions = Agl.options<Asm, SentenceContext> {
                semanticAnalysis { sentenceContext(contextAsmSimple()) }
                completionProvider { depth(CP_DEPTH) }
            }
            val grammarProcessOptions = Agl.options<GrammarDomain, SentenceContext> {
                semanticAnalysis { sentenceContext(contextAsmSimple()) }
                completionProvider { depth(CP_DEPTH) }
            }
            val styleProcessOptions = Agl.options<AglStyleDomain, SentenceContext> {
                semanticAnalysis { sentenceContext(contextAsmSimple()) }
                completionProvider { depth(CP_DEPTH) }
            }
            val typesProcessOptions = Agl.options<TypesDomain, SentenceContext> {
                completionProvider { depth(CP_DEPTH) }
            }
            val asmTransProcessOptions = Agl.options<AsmTransformDomain, SentenceContext> {
                completionProvider { depth(CP_DEPTH) }
            }
            val referencesProcessOptions = Agl.options<CrossReferenceDomain, SentenceContext> {
                completionProvider { depth(CP_DEPTH) }
            }
            val formatProcessOptions = Agl.options<AglFormatDomain, SentenceContext> {
                completionProvider { depth(CP_DEPTH) }
            }
            val m2mProcessOptions = Agl.options<M2mTransformDomain, SentenceContext> {
                completionProvider { depth(CP_DEPTH) }
            }
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
        val typesLanguage = Agl.registry.agl.types
        val asmTransLanguage = Agl.registry.agl.asmTransform
        val referencesLanguage = Agl.registry.agl.crossReference
        val formatLanguage = Agl.registry.agl.format
        val m2mLanguage = Agl.registry.agl.m2mTransform

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
    val sentenceEditor by lazy {
        createComposeEditor(
            stateHolder.sentenceMode.editorState, Constants.sentenceEditorId, logFunction, languageService, sentenceLanguage, Constants.sentenceProcessOptions
        )
    }
    val grammarEditor by lazy {
        createComposeEditor(
            stateHolder.languageMode.grammareEditorState, Constants.grammarEditorId, logFunction, languageService, grammarLanguage, Constants.grammarProcessOptions
        )
    }
    val styleEditor by lazy {
        createComposeEditor(
            stateHolder.languageMode.styleEditorState, Constants.styleEditorId, logFunction, languageService, styleLanguage, Constants.styleProcessOptions
        )
    }
    val typesEditor by lazy {
        createComposeEditor(
            stateHolder.languageMode.typesEditorState, Constants.typesEditorId, logFunction, languageService, typesLanguage, Constants.typesProcessOptions
        )
    }
    val asmTransEditor by lazy {
        createComposeEditor(
            stateHolder.languageMode.asmTransEditorState, Constants.asmTransEditorId, logFunction, languageService, asmTransLanguage, Constants.asmTransProcessOptions
        )
    }
    val referencesEditor by lazy {
        createComposeEditor(
            stateHolder.languageMode.refsEditorState, Constants.referencesEditorId, logFunction, languageService, referencesLanguage, Constants.referencesProcessOptions
        )
    }
    val formatEditor by lazy {
        createComposeEditor(
            stateHolder.languageMode.formatEditorState, Constants.formatEditorId, logFunction, languageService, formatLanguage, Constants.formatProcessOptions
        )
    }

    val handler = GuiHandler(this)
    val view = GuiView()

    fun doStart() {
        handler.connectEditors()
    }

    @Composable
    fun content() {
        view.content(stateHolder)
    }
}