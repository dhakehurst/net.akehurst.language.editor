import net.akehurst.language.agl.grammar.grammar.AglGrammarSemanticAnalyser
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.processor.LanguageProcessorConfiguration
import net.akehurst.language.editor.worker.AglSharedWorker


fun main() {
    //should start by construction
    val worker =AglSharedWorker<Any,Any>()


/*    val worker = AglSharedWorker<Any,Any>().also {
        Agl.registry.register(
            identity = "language-user",
            grammarStr = null,
            buildForDefaultGoal = false,
            aglOptions = Agl.options {
                semanticAnalysis {
                    option(AglGrammarSemanticAnalyser.OPTIONS_KEY_AMBIGUITY_ANALYSIS, false)
                }
            },
            configuration = Agl.configurationDefault()
        ).identity
    }*/
    //worker.start()
}

