package demo
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.editor.api.AglEditorCompletionProvider
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.LanguageServiceRequest
import net.akehurst.language.editor.api.LogFunction
import net.akehurst.language.editor.common.AglEditorAbstract
import net.akehurst.language.editor.common.AglTokenizerByWorker

class EditorDummy<AsmType : Any, ContextType : Any>(
    languageServiceRequest:LanguageServiceRequest,
    languageId: String,
    editorId: String,
    logFunction: LogFunction
) : AglEditorAbstract<AsmType, ContextType>(languageServiceRequest, languageId, EndPointIdentity(editorId,"none"), logFunction) {

    override val baseEditor: Any = this
    override val completionProvider: AglEditorCompletionProvider
        get() = TODO("not implemented")
    override val isConnected: Boolean
        get() = TODO("not implemented")


    private var _text: String = ""
    override var text: String
        get() = _text
        set(value) {
            _text = value
        }

    override val workerTokenizer: AglTokenizerByWorker
        get() = TODO("not implemented")

    override fun clearErrorMarkers() {
        TODO("not implemented")
    }

    override fun createIssueMarkers(issues: List<LanguageIssue>) {
        TODO("not implemented")
    }

    override fun destroy() {
        TODO("not implemented")
    }

    override fun resetTokenization(fromLine: Int) {
        TODO("not implemented")
    }

    override fun updateEditorStyles() {
        TODO("not implemented")
    }

    override fun updateLanguage(oldId: String?) {
        TODO("not implemented")
    }


}