/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.language.editor.common

import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.CompletionItem
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.processor.ProcessOptions
import net.akehurst.language.editor.api.*
import net.akehurst.language.issues.api.IssueCollection
import net.akehurst.language.issues.api.LanguageIssue
import net.akehurst.language.parser.api.ParseResult
import net.akehurst.language.parser.api.ParseStreamListener
import net.akehurst.language.scanner.api.Matchable
import net.akehurst.language.sppt.api.SpptDataNode
import net.akehurst.language.style.api.AglStyleDomain
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.*

/**
 * The language service gives each request its own ParseInterrupt,
 * so a new editor request stops only the older request of that editor,
 * not other parses that run at the same time on the same processor.
 */
class test_LanguageServiceByJvmThread_parallelParse {

    private companion object {
        val logFunction = { level: LogLevel, prefix: String, t: Throwable?, message: () -> String -> if (level.severity <= LogLevel.Error.severity) println("$level: $prefix - ${message()}, $t") }

        val grammarStr = GrammarString(
            """
            namespace test
            grammar Test {
                skip leaf WS = "\s+" ;
                statements = statement* ;
                statement = ID '=' NUM ';' ;
                leaf ID = "[a-z]+" ;
                leaf NUM = "[0-9]+" ;
            }
            """.trimIndent()
        )

        val DIRECT_SIZE = 40000

        fun sentence(numStatements: Int) = (1..numStatements).joinToString(separator = "\n") { "v = $it ;" }

        // each statement has 4 leaves, so each one takes at least 4ms to parse with a SlowListener
        val SENTENCE_EDITOR_1 = sentence(5000) // >= 20s, it must be stopped long before that
        // a parse with a stream listener gives no tree, so the direct parse is made slow by its size
        val SENTENCE_DIRECT = sentence(DIRECT_SIZE)
        val SENTENCE_EDITOR_2 = sentence(100)
    }

    data class ParseResponse(val requestId: RequestIdentity, val status: MessageResponseStatus, val message: String, val issues: List<LanguageIssue>, val tree: Any?)

    /** sleeps for each leaf, so that a parse runs long enough for something else to happen while it is running */
    class SlowListener : ParseStreamListener {
        val started = CountDownLatch(1)

        @Volatile
        var numLeaves = 0

        override fun leaf(node: SpptDataNode) {
            numLeaves++
            started.countDown()
            Thread.sleep(1)
        }

        override fun skip(startPosition: Int, nextInputPosition: Int) {}
        override fun branch(node: SpptDataNode, numChildren: Int) {}
        override fun finished(issues: IssueCollection<LanguageIssue>) {}
        override fun failed(issues: IssueCollection<LanguageIssue>, committedUpTo: Int) {}
    }

    class Recorder : LanguageServiceResponse {
        val created = CountDownLatch(1)
        var createStatus: MessageResponseStatus? = null
        var createIssues: List<LanguageIssue> = emptyList()
        val parseResponses = ConcurrentLinkedQueue<ParseResponse>()
        val finished = mutableMapOf<RequestIdentity, CountDownLatch>()

        fun finishedLatch(requestId: RequestIdentity) = synchronized(finished) { finished.getOrPut(requestId) { CountDownLatch(1) } }

        override fun processorCreateResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, scannerMatchables: List<Matchable>
        ) {
            createStatus = status
            createIssues = issues
            created.countDown()
        }

        override fun sentenceParseResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, tree: Any?) {
            parseResponses.add(ParseResponse(requestId, status, message, issues, tree))
            if (MessageResponseStatus.RECEIVED != status) finishedLatch(requestId).countDown()
        }

        fun parseResponsesFor(requestId: RequestIdentity) = parseResponses.filter { it.requestId == requestId && it.status != MessageResponseStatus.RECEIVED }

        override fun processorDeleteResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String) {}
        override fun processorSetStyleResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, styleModel: AglStyleDomain?
        ) {
        }

        override fun sentenceScanResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>) {}
        override fun sentenceLineTokensResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, startLine: Int, lineTokens: List<List<AglToken>>
        ) {
        }

        override fun sentenceSyntaxAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {}
        override fun sentenceSemanticAnalysisResponse(endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, asm: Any?) {}
        override fun sentenceCodeCompleteResponse(
            endPointIdentity: EndPointIdentity, requestId: RequestIdentity, status: MessageResponseStatus, message: String, issues: List<LanguageIssue>, offset: Int, completionItems: List<CompletionItem>
        ) {
        }
    }

    @Test
    fun new_editor_request_stops_only_the_older_editor_parse() {
        val languageId = LanguageIdentity("test.parallelParse")
        val ld = Agl.registry.findOrPlaceholder<Any, Any>(
            identity = languageId,
            aglOptions = null,
            configuration = Agl.configuration(Agl.configurationSimple() as net.akehurst.language.api.processor.LanguageProcessorConfiguration<Any, Any>) {
                grammarString(grammarStr)
            }
        )
        val processor: LanguageProcessor<Any, Any> = ld.processor ?: fail("no processor: ${ld.issues}")
        assertTrue(ld.issues.errors.isEmpty(), ld.issues.toString())

        val executor = Executors.newFixedThreadPool(4)
        try {
            val service = LanguageServiceByJvmThread(executor, logFunction)
            val endPoint = EndPointIdentity("editor-1", "session-1")
            val recorder = Recorder()
            service.addResponseListener(endPoint, recorder)
            val editorOptions = EditorOptionsDefault(
                scan = false, scanLineTokens = false, parseLineTokens = false, syntaxAnalysis = false, semanticAnalysis = false
            )
            service.request.processorCreateRequest(endPoint, RequestIdentity("0"), ld, editorOptions)
            assertTrue(recorder.created.await(30, TimeUnit.SECONDS), "processor not created")
            assertEquals(MessageResponseStatus.SUCCESS, recorder.createStatus, recorder.createIssues.toString())

            // first editor request: a large sentence
            val req1 = RequestIdentity("1")
            val listener1 = SlowListener()
            val opts1: ProcessOptions<Any, Any> = Agl.options { parse { streamListener(listener1) } }
            service.request.interruptRequest(endPoint, req1, languageId, "process Sentence")
            service.request.sentenceProcessRequest(endPoint, req1, languageId, SENTENCE_EDITOR_1, opts1)
            assertTrue(listener1.started.await(30, TimeUnit.SECONDS), "first editor parse did not start")

            // at the same time, a direct parse with the same processor
            var directResult: ParseResult? = null
            var directException: Throwable? = null
            var directTime = 0L
            val directThread = thread {
                try {
                    val start = System.currentTimeMillis()
                    directResult = processor.parse(SENTENCE_DIRECT)
                    directTime = System.currentTimeMillis() - start
                } catch (t: Throwable) {
                    directException = t
                }
            }
            Thread.sleep(200) // let the direct parse start

            // second editor request, sent (like the editor does) while the first is still running
            val req2 = RequestIdentity("2")
            service.request.interruptRequest(endPoint, req2, languageId, "process Sentence")
            service.request.sentenceProcessRequest(endPoint, req2, languageId, SENTENCE_EDITOR_2, Agl.options { })

            assertTrue(recorder.finishedLatch(req1).await(30, TimeUnit.SECONDS), "first editor parse did not finish")
            // otherwise the test does not show that the interrupt leaves the direct parse running (make DIRECT_SIZE larger)
            assertTrue(directThread.isAlive, "direct parse finished before the first editor parse was stopped")
            assertTrue(recorder.finishedLatch(req2).await(60, TimeUnit.SECONDS), "second editor parse did not finish")
            directThread.join(60_000)
            assertFalse(directThread.isAlive, "direct parse did not finish")

            // the first editor parse is stopped, and that is not reported as a syntax error
            val resp1 = recorder.parseResponsesFor(req1)
            assertEquals(1, resp1.size, resp1.toString())
            assertEquals(MessageResponseStatus.IGNORED, resp1[0].status, resp1.toString())
            assertTrue(resp1[0].message.startsWith("Cancelled"), resp1[0].message)
            assertEquals(emptyList(), resp1[0].issues)
            assertNull(resp1[0].tree)
            assertTrue(listener1.numLeaves < 4 * 5000, "first editor parse was not stopped early")

            // the direct parse is not stopped
            assertNull(directException, directException?.stackTraceToString())
            val dr = directResult ?: fail("no result from the direct parse")
            assertEquals(emptyList(), dr.issues.errors.map { it.toString() })
            assertNotNull(dr.sppt)
            println("direct parse of $DIRECT_SIZE statements took $directTime ms")

            // the second editor parse finishes with no errors
            val resp2 = recorder.parseResponsesFor(req2)
            assertEquals(1, resp2.size, resp2.toString())
            assertEquals(MessageResponseStatus.SUCCESS, resp2[0].status, resp2.toString())
            assertEquals(emptyList(), resp2[0].issues.filter { it.kind == net.akehurst.language.issues.api.LanguageIssueKind.ERROR }.map { it.toString() })
            assertNotNull(resp2[0].tree)

            // no 'Parser was terminated' error is sent to the editor
            assertTrue(recorder.parseResponses.none { r -> r.issues.any { it.message.startsWith("The Parser was terminated") } }, recorder.parseResponses.toString())
        } finally {
            executor.shutdownNow()
            Agl.registry.unregister(languageId)
        }
    }
}
