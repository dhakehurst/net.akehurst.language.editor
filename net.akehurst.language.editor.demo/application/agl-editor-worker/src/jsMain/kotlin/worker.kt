import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.agl.syntaxAnalyser.SyntaxAnalyserSimple
import net.akehurst.language.editor.worker.AglSharedWorker

var worker = AglSharedWorker().also {
    Agl.registry.register(
        identity = "user-language",
        grammar = "",
        defaultGoalRule = null,
        style = "",
        format = "",
        syntaxAnalyser = SyntaxAnalyserSimple(),
        semanticAnalyser = null
    ).identity
}

