package net.akehurst.language.editor.api

import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.language.api.processor.*
import net.akehurst.language.base.api.PublicValueType
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleDomain
import kotlin.jvm.JvmInline

interface LanguageService {
    val logFunction: LogFunction // expose this because it is useful to be able to access it for reuse
    val request: LanguageServiceRequest
    fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse)
}

interface LanguageServiceRequest {
//    fun processorCreateRequest(
//        endPointIdentity: EndPointIdentity, requestId: RequestIdentity,
//        languageId: LanguageIdentity,
//        grammarStr: GrammarString,
//        typeModelStr: TypesString?,
//        asmTransformStr: TransformString?,
//        crossReferenceModelStr: CrossReferenceString?,
//        editorOptions: EditorOptions
//    )

    fun <AsmType : Any, ContextType : Any> processorCreateRequest(
        endPointIdentity: EndPointIdentity, requestId: RequestIdentity,
        languageDefinition: LanguageDefinition<AsmType, ContextType>,
        editorOptions: EditorOptions
    )

    fun processorDeleteRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, languageId: LanguageIdentity)
    fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, languageId: LanguageIdentity, styleStr: StyleString)

    fun interruptRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, languageId: LanguageIdentity, reason: String)
    fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity,
        languageId: LanguageIdentity,
        sentence: String,
        processOptions: ProcessOptions<AsmType, ContextType>
    )

    fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity,
        languageId: LanguageIdentity,
        sentence: String,
        position: Int,
        processOptions: ProcessOptions<AsmType, ContextType>
    )
}

interface LanguageServiceResponse {
    fun processorCreateResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        scannerMatchables: List<Matchable>
    )

    fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String)
    fun processorSetStyleResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        styleModel: AglStyleDomain?
    )

    fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>)
    fun sentenceScanResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>)
    fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, tree: Any?)
    fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, asm: Any?)
    fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, asm: Any?)
    fun sentenceCodeCompleteResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity,
        status: MessageResponseStatus,
        message: String,
        issues: List<LanguageIssue>,
        offset: Int,
        completionItems: List<CompletionItem>
    )
}

@JvmInline
value class RequestIdentity(override val value: String) : PublicValueType

data class EndPointIdentity(
    val editorId: String,
    val sessionId: String
) {
    override fun toString(): String = "editorId=$editorId, sessionId=$sessionId"
}

enum class MessageResponseStatus {
    /** request to perform action was received */
    RECEIVED,
    /** requested action was not performed, e.g. due to it being disabled */
    IGNORED,
    /** requested action failed */
    FAILURE,
    /** requested action succeeded */
    SUCCESS
}

@JvmInline
value class EditorStyleIdentity(override val value: String) : PublicValueType {
    companion object {
        val NO_STYLE = EditorStyleIdentity("nostyle")
    }
}

interface EditorStyle {
    val identity: EditorStyleIdentity
}

interface AglToken {
    /**
     * editor specific style info is not stored in the token
     * or we would have to serialise it.
     */
    val styles: List<EditorStyleIdentity>
    val position: Int
    val length: Int
}
