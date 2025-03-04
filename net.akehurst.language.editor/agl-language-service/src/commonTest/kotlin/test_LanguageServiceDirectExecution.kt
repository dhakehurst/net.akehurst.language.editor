package net.akehurst.language.editor.common

import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.*
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel
import kotlin.test.Test

class test_LanguageServiceDirectExecution {

    private companion object {
        //TODO: record response (rather than print), and test expected result
        val responseRecorder = object : LanguageServiceResponse {
            override fun processorCreateResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>) {
                println("processorCreateResponse: $endPointIdentity, $requestId, $status, $message, $issues, $scannerMatchables")
            }

            override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String) {
                println("processorDeleteResponse: $endPointIdentity, $requestId, $status, $message")
            }

            override fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleModel?) {
                println("processorSetStyleResponse: $endPointIdentity, $requestId, $status, $message, $issues, $styleModel")
            }

            override fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>) {
                println("sentenceLineTokensResponse: $endPointIdentity, $requestId, $status, $message, $startLine, $lineTokens")
            }

            override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
                println("sentenceParseResponse: $endPointIdentity, $status, $requestId, $message, $issues, $tree")
            }

            override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
                println("sentenceSyntaxAnalysisResponse: $endPointIdentity, $requestId, $status, $message, $issues, $asm")
            }

            override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
                println("sentenceSemanticAnalysisResponse: $endPointIdentity, $requestId, $status, $message, $issues, $asm")
            }

            override fun sentenceCodeCompleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity<*>,status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems: List<CompletionItem>) {
                println("sentenceCodeCompleteResponse: $endPointIdentity, $requestId, $status, $message, $issues, $completionItems")
            }

        }
        val logFunction = { level: LogLevel, prefix: String, t: Throwable?,message: ()->String, -> println("$level: $prefix - ${message()}, $t") }
    }

    @Test
    fun construct() {
        val sut = LanguageServiceDirectExecution(logFunction)
    }

    @Test
    fun processorCreateRequest() {
        val sut = LanguageServiceDirectExecution(logFunction)

        val epi = EndPointIdentity("test-editor", "<nothing>")
        val ri = RequestIdentity(1)
        val li = LanguageIdentity("test-lang")

        sut.addResponseListener(epi, responseRecorder)

        val gs = GrammarString(
            """
            namespace test
            grammar Test {
                S = 'a' ;
            }
        """.trimIndent()
        )
        sut.request.processorCreateRequest(epi, ri, li, gs, null, null, null, aglEditorOptions())
    }

    @Test
    fun processorCreateRequest_alread_registered() {
        val sut = LanguageServiceDirectExecution(logFunction)

        val epi = EndPointIdentity("test-editor", "<nothing>")
        val ri = RequestIdentity(1)
        val li = LanguageIdentity("test-lang")

        sut.addResponseListener(epi, responseRecorder)

        val gs = GrammarString(
            """
            namespace test
            grammar Test {
                S = 'a' ;
            }
        """.trimIndent()
        )
        sut.request.processorCreateRequest(epi, ri, li, gs, null, null, null, aglEditorOptions())
    }

}