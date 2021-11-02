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

package net.akehurst.language.editor.common.messages

import net.akehurst.language.api.processor.LanguageIssue
import net.akehurst.language.api.processor.LanguageIssueKind
import net.akehurst.language.api.processor.LanguageProcessorPhase
import net.akehurst.language.editor.common.AglToken
import net.akehurst.language.editor.common.objectJS

abstract class AglWorkerMessage(
    val action: String,
    val languageId: String,
    val editorId: String,
    val sessionId: String
) {
    companion object {
        fun <T : AglWorkerMessage> fromJsObject(jsObj: dynamic): T? {
            if (null == jsObj) {
                return null
            } else {
                val action = jsObj["action"]
                val languageId = jsObj["languageId"]
                val editorId = jsObj["editorId"]
                val sessionId = jsObj["sessionId"]
                return when (action) {
                    "MessageProcessorCreate" -> MessageProcessorCreate.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageProcessorCreateResponse" -> MessageProcessorCreateResponse.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageSyntaxAnalyserConfigure" -> MessageSyntaxAnalyserConfigure.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageProcessRequest" -> MessageProcessRequest.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageParseResult" -> MessageParseResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageParserInterruptRequest" -> MessageParserInterruptRequest.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageLineTokens" -> MessageLineTokens.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageSetStyle" -> MessageSetStyle.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageSetStyleResult" -> MessageSetStyleResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageSyntaxAnalysisResult" -> MessageSyntaxAnalysisResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageSemanticAnalysisResult" -> MessageSemanticAnalysisResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageCodeCompleteRequest" -> MessageCodeCompleteRequest.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    "MessageCodeCompleteResult" -> MessageCodeCompleteResult.fromJsObject(languageId, editorId, sessionId, jsObj) as T
                    else -> null
                }
            }
        }

        fun serialise(msg: AglWorkerMessage): String {
            val jsObj = msg.toJsObject()
            return JSON.stringify(jsObj) { k, v ->
                if (v is Enum<*>) {
                    objectJS {
                        __isEnum__ = true
                        __type__ = v::class.simpleName
                        __name__ = v.name
                    }
                } else {
                    v
                }
            }
        }

        fun deserialise(str: String): AglWorkerMessage? {
            val jsObj = JSON.parse<Any?>(str) { k, v ->
                if (null==v) {
                    v
                } else {
                    val d = v.asDynamic()
                    if (null != d.__isEnum__ && d.__isEnum__) {
                        when (d.__type__) {
                            "LanguageIssueKind" -> LanguageIssueKind.valueOf(d.__name__)
                            "LanguageProcessorPhase" -> LanguageProcessorPhase.valueOf(d.__name__)
                            else -> v
                        }
                    } else {
                        v
                    }
                }
            }
            return AglWorkerMessage.fromJsObject(jsObj)
        }
    }

    open fun toJsObject(): dynamic = objectJS {
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
        obj["grammarStr"] = grammarStr
        return obj
    }
}

class MessageSyntaxAnalyserConfigure(
    languageId: String, editorId: String, sessionId: String,
    val configuration: Any?
) : AglWorkerMessage("MessageSyntaxAnalyserConfigure", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSyntaxAnalyserConfigure =
            MessageSyntaxAnalyserConfigure(languageId, editorId, sessionId, jsObj["configuration"])
    }

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
        obj["configuration"] = configuration
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
        obj["goalRuleName"] = goalRuleName
        obj["text"] = text
        obj["context"] = context
        return obj
    }
}

class MessageParseResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val tree: Any?,
    val issues: Array<LanguageIssue>
) : AglWorkerMessage("MessageParseResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageParseResult =
            MessageParseResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["tree"], jsObj["issues"])
    }

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
        obj["success"] = success
        obj["message"] = message
        obj["tree"] = tree
        obj["issues"] = issues
        return obj
    }
}

class MessageSyntaxAnalysisResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val asm: Any?,
    val issues: Array<LanguageIssue>
) : AglWorkerMessage("MessageSyntaxAnalysisResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSyntaxAnalysisResult =
            MessageSyntaxAnalysisResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["asm"], jsObj["issues"])
    }

    override fun toJsObject(): dynamic = objectJS {
        val obj = super.toJsObject()
        obj["success"] = success
        obj["message"] = message
        obj["asm"] = asm
        obj["issues"] = issues
        return obj
    }
}

class MessageSemanticAnalysisResult(
    languageId: String, editorId: String, sessionId: String,
    val success: Boolean,
    val message: String,
    val issues: Array<LanguageIssue>
) : AglWorkerMessage("MessageSemanticAnalysisResult", languageId, editorId, sessionId) {

    companion object {
        fun fromJsObject(languageId: String, editorId: String, sessionId: String, jsObj: dynamic): MessageSemanticAnalysisResult =
            MessageSemanticAnalysisResult(languageId, editorId, sessionId, jsObj["success"], jsObj["message"], jsObj["issues"])
    }

    override fun toJsObject(): dynamic = objectJS {
        val obj = super.toJsObject()
        obj["success"] = success
        obj["message"] = message
        obj["issues"] = issues
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
        obj["success"] = success
        obj["message"] = message
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
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

    override fun toJsObject(): dynamic {
        val obj = super.toJsObject()
        obj["success"] = success
        obj["message"] = message
        obj["completionItems"] = completionItems
        return obj
    }
}