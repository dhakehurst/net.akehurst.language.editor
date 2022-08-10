import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.editor.worker.AglSharedWorker


fun main() {
    val worker = AglSharedWorker<Any,Any>().also {
        Agl.registry.register<Any,Any>(
            identity = "user-language",
            grammar = "",
            targetGrammar = null,
            defaultGoalRule = null,
            buildForDefaultGoal = false,
            style = "",
            format = "",
            syntaxAnalyserResolver = null,
            semanticAnalyserResolver = null
        ).identity
    }
    worker.start()
}

