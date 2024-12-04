package net.akehurst.language.editor.api

import net.akehurst.language.agl.*
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel
import kotlin.jvm.JvmInline

interface LanguageService {
    val request: LanguageServiceRequest
    fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse)
}

interface LanguageServiceRequest {
    fun processorCreateRequest(
        endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        grammarStr: GrammarString,
        typeModelStr: TypeModelString?,
        asmTransformStr: TransformString?,
        crossReferenceModelStr: CrossReferenceString?,
        editorOptions: EditorOptions
    )

    fun processorDeleteRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity)
    fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity, styleStr: StyleString)

    fun interruptRequest(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, languageId: LanguageIdentity, reason: String)
    fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        sentence: String,
        processOptions: ProcessOptions<AsmType, ContextType>
    )

    fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        languageId: LanguageIdentity,
        sentence: String,
        position: Int,
        processOptions: ProcessOptions<AsmType, ContextType>
    )
}

interface LanguageServiceResponse {
    fun processorCreateResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageStatus,
        message: String,
        issues: List<LanguageIssue>,
        scannerMatchables: List<Matchable>
    )

    fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String)
    fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleModel?)

    fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>)
    fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?)
    fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?)
    fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?)
    fun sentenceCodeCompleteResponse(
        endPointIdentity: EndPointIdentity,
        requestId: RequestIdentity<*>,
        status: MessageStatus,
        message: String,
        issues: List<LanguageIssue>,
        completionItems: List<CompletionItem>
    )
}

@JvmInline
value class RequestIdentity<T : Any>(val value: T)

data class EndPointIdentity(
    val editorId: String,
    val sessionId: String
) {
    override fun toString(): String = "editorId=$editorId, sessionId=$sessionId"
}

enum class MessageStatus { START, FAILURE, SUCCESS }

interface AglToken {
    val styles: List<String>
    val position: Int
    val length: Int
}
