/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.semanticAnalyser.SemanticAnalyserItem

abstract class AglWorkerMessage(
    val action: String,
    val languageId: String,
    val editorId: String,
    val sessionId: String
) {
    companion object {
        fun <T : AglWorkerMessage> fromJsObject(jsObj: dynamic): T? {
            val action = jsObj["action"]
            val languageId = jsObj["languageId"]
            val editorId = jsObj["editorId"]
            val sessionId = jsObj["sessionId"]
            return when (action) {
                "MessageProcessorCreate" -> MessageProcessorCreate.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageProcessorCreateResponse" -> MessageProcessorCreateResponse.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageProcessRequest" -> MessageProcessRequest.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageParseStart" -> MessageParseStart.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageParseResult" -> MessageParseResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageParserInterruptRequest" -> MessageParserInterruptRequest.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageLineTokens" -> MessageLineTokens.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageSetStyle" -> MessageSetStyle.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageSetStyleResult" -> MessageSetStyleResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageSyntaxAnalysisStart" -> MessageSyntaxAnalysisStart.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageSyntaxAnalysisResult" -> MessageSyntaxAnalysisResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageCodeCompleteRequest" -> MessageCodeCompleteRequest.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                "MessageCodeCompleteResult" -> MessageCodeCompleteResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                else -> null
            }
        }
    }

    open fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"] = languageId
        this["editorId"] = editorId
        this["sessionId"] = sessionId
    }
}

class MessageProcessorCreate(
    languageId: String, editorId: String, sessionId: String,
    val grammarStr: String?
) : AglWorkerMessage("MessageProcessorCreate", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageProcessorCreate =
            MessageProcessorCreate(languageId, editorId, sessionId, jsObj["grammarStr"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["grammarStr"] = grammarStr
        return obj
    }
}

class MessageProcessorCreateResponse(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String
) : AglWorkerMessage("MessageProcessorCreateResponse", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageProcessorCreateResponse =
            MessageProcessorCreateResponse(languageId, editorId, sessionId, jsObj["success"], jsObj["message"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        return obj
    }
}

class MessageProcessRequest(
    languageId: String, editorId: String, sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val context: Any?
) : AglWorkerMessage("MessageProcessRequest", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageProcessRequest =
            MessageProcessRequest(languageId, editorId, sessionId, jsObj["goalRuleName"], jsObj["text"], jsObj["context"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["goalRuleName"] = goalRuleName
        obj["text"] = text
        obj["context"] = context
        return obj
    }
}

class MessageParseStart(
    languageId: String, editorId: String, sessionId: String,
) : AglWorkerMessage("MessageParseStart", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageParseStart =
            MessageParseStart(languageId, editorId, sessionId)
    }

    override fun toObjectJS(): dynamic = super.toObjectJS()
}

class MessageParseResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val tree: Any?,
    val location: InputLocation?,
    val expected: Array<String>?
) : AglWorkerMessage("MessageParseResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageParseResult =
            MessageParseResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["tree"], jsObj["location"], jsObj["expected"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        obj["tree"] = tree
        obj["location"] = location
        obj["expected"] = expected
        return obj
    }
}

class MessageParserInterruptRequest(
    languageId: String, editorId: String, sessionId: String,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageParserInterruptRequest =
            MessageParserInterruptRequest(languageId, editorId, sessionId, jsObj["reason"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["reason"] = reason
        return obj
    }
}

class MessageLineTokens(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val lineTokens: Array<Array<AglToken>>,
) : AglWorkerMessage("MessageLineTokens", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageLineTokens =
            MessageLineTokens(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["lineTokens"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        obj["lineTokens"] = lineTokens
        return obj
    }
}

class MessageSetStyle(
    languageId: String, editorId: String, sessionId: String,
    val css: String
) : AglWorkerMessage("MessageSetStyle", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSetStyle =
            MessageSetStyle(languageId, editorId, sessionId, jsObj["css"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["css"] = css
        return obj
    }
}

class MessageSetStyleResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String
) : AglWorkerMessage("MessageSetStyleResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSetStyleResult =
            MessageSetStyleResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        return obj
    }
}

class MessageSyntaxAnalysisStart(
    languageId: String, editorId: String, sessionId: String,
) : AglWorkerMessage("MessageSyntaxAnalysisStart", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSyntaxAnalysisStart =
            MessageSyntaxAnalysisStart(languageId, editorId, sessionId)
    }

    override fun toObjectJS(): dynamic = super.toObjectJS()
}

class MessageSyntaxAnalysisResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val asm: Any?
) : AglWorkerMessage("MessageSyntaxAnalysisResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSyntaxAnalysisResult =
            MessageSyntaxAnalysisResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["asm"])
    }

    override fun toObjectJS(): dynamic = objectJS {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        obj["asm"] = asm
        return obj
    }
}

class MessageSemanticAnalysisStart(
    languageId: String, editorId: String, sessionId: String,
) : AglWorkerMessage("MessageSemanticAnalysisStart", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSemanticAnalysisStart =
            MessageSemanticAnalysisStart(languageId, editorId, sessionId)
    }

    override fun toObjectJS(): dynamic = super.toObjectJS()
}

class MessageSemanticAnalysisResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val items: List<SemanticAnalyserItem>
) : AglWorkerMessage("MessageSemanticAnalysisResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSemanticAnalysisResult =
            MessageSemanticAnalysisResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["items"])
    }

    override fun toObjectJS(): dynamic = objectJS {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        obj["items"] = items
        return obj
    }
}

class MessageCodeCompleteRequest(
    languageId: String, editorId: String, sessionId: String,
    val goalRuleName: String?,
    val text: String,
    val position: Int
) : AglWorkerMessage("MessageCodeCompleteRequest", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageCodeCompleteRequest =
            MessageCodeCompleteRequest(languageId, editorId, sessionId, jsObj["goalRuleName"], jsObj["text"], jsObj["position"])
    }

    override fun toObjectJS(): dynamic  {
        val obj = super.toObjectJS()
        obj["goalRuleName"] = goalRuleName
        obj["text"] = text
        obj["position"] = position
        return obj
    }
}

class MessageCodeCompleteResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val completionItems: Array<Pair<String, String>>?
) : AglWorkerMessage("MessageCodeCompleteResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageCodeCompleteResult =
            MessageCodeCompleteResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["completionItems"])
    }

    override fun toObjectJS(): dynamic {
        val obj = super.toObjectJS()
        obj["success"] = success
        obj["message"] = message
        obj["completionItems"] = completionItems
        return obj
    }
}

class MessageAnalyseRequest(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
)