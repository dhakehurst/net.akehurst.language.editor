package demo
import net.akehurst.language.editor.common.AglEditorAbstract

class EditorDummy<AsmType : Any, ContextType : Any>(
    languageId: String,
    editorId: String,
) : AglEditorAbstract<AsmType, ContextType>(languageId, editorId) {

    override val sessionId: String get() = ""

    private var _text: String = ""
    override var text: String
        get() = _text
        set(value) {
            _text = value
        }

    override fun clearErrorMarkers() {

    }

    override fun configureSyntaxAnalyser(configuration: Map<String, Any>) {

    }

    override fun destroy() {

    }

    override fun processSentence() {

    }

    override fun updateLanguage(oldId: String?) {

    }

    override fun updateProcessor() {

    }

    override fun updateStyle() {

    }
}