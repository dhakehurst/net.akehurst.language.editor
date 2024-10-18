package net.akehurst.language.editor.common

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.editor.api.AglToken
import net.akehurst.language.editor.api.EndPointIdentity
import net.akehurst.language.editor.api.LanguageServiceResponse
import net.akehurst.language.editor.api.MessageStatus
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.style.api.AglStyleModel
import kotlin.test.Test

class test_LanguageServiceDirectExecution {

    private companion object {
        //TODO: record response (rather than print), and test expected result
        val responseRecorder = object : LanguageServiceResponse {
            override fun processorCreateResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>) {
               println("processorCreateResponse: $endPointIdentity, $status, $message, $issues, $scannerMatchables")
            }

            override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String) {
                println("processorDeleteResponse: $endPointIdentity, $status, $message")
            }

            override fun processorSetStyleResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleModel?) {
                println("processorSetStyleResponse: $endPointIdentity, $status, $message, $issues, $styleModel")
            }

            override fun sentenceLineTokensResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>) {
                println("sentenceLineTokensResponse: $endPointIdentity, $status, $message, $startLine, $lineTokens")
            }

            override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
                println("sentenceParseResponse: $endPointIdentity, $status, $message, $issues, $tree")
            }

            override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
                println("sentenceSyntaxAnalysisResponse: $endPointIdentity, $status, $message, $issues, $asm")
            }

            override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {
                println("sentenceSemanticAnalysisResponse: $endPointIdentity, $status, $message, $issues, $asm")
            }

            override fun sentenceCodeCompleteResponse(endPointIdentity: EndPointIdentity, status: MessageStatus, message: String, issues: List<LanguageIssue>, completionItems: List<CompletionItem>) {
                println("sentenceCodeCompleteResponse: $endPointIdentity, $status, $message, $issues, $completionItems")
            }

        }
    }

    @Test
    fun construct() {
        val sut = LanguageServiceDirectExecution()
    }

    @Test
    fun processorCreateRequest() {
        val sut = LanguageServiceDirectExecution()

        val epi = EndPointIdentity("test-editor","<nothing>")
        val li = LanguageIdentity("test-lang")

        sut.addResponseListener(epi, responseRecorder)

        val gs = GrammarString("""
            namespace test
            grammar Test {
                S = 'a' ;
            }
        """.trimIndent())
        sut.request.processorCreateRequest(epi, li, gs, null, aglEditorOptions())
    }

}