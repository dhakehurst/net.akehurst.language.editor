package net.akehurst.language.editor.api

import net.akehurst.language.agl.scanner.Matchable
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.api.language.style.AglStyleModel

interface LanguageService {
    val request: LanguageServiceRequest
    fun addResponseListener(endPointIdentity: EndPointIdentity, response: LanguageServiceResponse)
}

interface LanguageServiceRequest {
    fun processorCreateRequest(endPointIdentity: EndPointIdentity, languageId:String, grammarStr:String, crossReferenceModelStr:String?, editorOptions: EditorOptions)
    fun processorDeleteRequest(endPointIdentity: EndPointIdentity)
    fun processorSetStyleRequest(endPointIdentity: EndPointIdentity, languageId:String, styleStr:String)

    fun interruptRequest(endPointIdentity: EndPointIdentity, languageId:String, reason:String)
    fun <AsmType : Any, ContextType : Any> sentenceProcessRequest(endPointIdentity: EndPointIdentity, languageId:String, text: String, processOptions: ProcessOptions<AsmType, ContextType>)
    fun <AsmType : Any, ContextType : Any> sentenceCodeCompleteRequest(endPointIdentity: EndPointIdentity, languageId: String, text:String, position:Int, processOptions: ProcessOptions<AsmType, ContextType>)
}

interface LanguageServiceResponse {
    fun processorCreateResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>)
    fun processorDeleteResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String)
    fun processorSetStyleResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel:AglStyleModel?)

    fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>)
    fun sentenceParseResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?)
    fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String, issues: List<LanguageIssue>, asm:Any?)
    fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity,status: MessageStatus, message: String, issues: List<LanguageIssue>, asm:Any?)
    fun sentenceCodeCompleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems:List<CompletionItem>)
}

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
