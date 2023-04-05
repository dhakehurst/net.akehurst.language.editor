import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.editor.worker.AglSharedWorker


fun main() {
    val worker = AglSharedWorker<Any,Any>().also {
        Agl.registry.register(
            identity = "user-language",
            grammarStr = null,
            buildForDefaultGoal = false,
            aglOptions = Agl.options {
                semanticAnalysis {
                    active(false)
                }
            },
            configuration = Agl.configurationDefault()
        ).identity
    }
    worker.start()
}

