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

abstract class AglWorkerMessage(
    val action: String
) {
    abstract fun toObjectJS(): dynamic
}

class MessageProcessorCreate(
    val languageId: String,
    val editorId: String,
    val grammarStr: String?
) : AglWorkerMessage("MessageProcessorCreate") {
        override fun toObjectJS(): dynamic = objectJS {
            this["action"] = action
            this["languageId"]=languageId
            this["editorId"]=editorId
            this["grammarStr"]=grammarStr
        }
}

class MessageProcessorCreateSuccess(
    val languageId: String,
    val editorId: String,
    val message: String
) : AglWorkerMessage("MessageProcessorCreateSuccess") {
        override fun toObjectJS(): dynamic = objectJS {
            this["action"] = action
            this["languageId"]=languageId
            this["editorId"]=editorId
            this["message"]=message
        }
}

class MessageProcessorCreateFailure(
    val languageId: String,
    val editorId: String,
    val message: String
) : AglWorkerMessage("MessageProcessorCreateFailure") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["message"]=message
    }
}


class MessageParseRequest(
    val languageId: String,
    val editorId: String,
    val goalRuleName:String?,
    val text: String
) : AglWorkerMessage("MessageParseRequest") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["goalRuleName"]=goalRuleName
        this["text"]=text
    }
}


class MessageParseStart(
    val languageId: String,
    val editorId: String
) : AglWorkerMessage("MessageParseStart") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
    }
}


class MessageParseSuccess(
    val languageId: String,
    val editorId: String,
    val tree: Any
) : AglWorkerMessage("MessageParseSuccess") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["tree"]=tree
    }
}


class MessageParseFailure(
    val languageId: String,
    val editorId: String,
    val message: String,
    val location: InputLocation?,
    val expected: Array<String>,
    val tree: Any?
) : AglWorkerMessage("MessageParseFailure") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["message"]=message
        this["location"]=location
        this["expected"]=expected
        this["tree"]=tree
    }
}


class MessageParserInterruptRequest(
    val languageId: String,
    val editorId: String,
    val reason: String
) : AglWorkerMessage("MessageParserInterruptRequest") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["reason"]=reason
    }
}


class MessageLineTokens(
    val languageId: String,
    val editorId: String,
    val success: Boolean,
    val message:String,
    val lineTokens: Array<Array<AglToken>>,
) : AglWorkerMessage("MessageLineTokens") {
    companion object{
        fun fromJsObject(jsObj:dynamic):MessageLineTokens =MessageLineTokens(
            jsObj["languageId"],
            jsObj["editorId"],
            jsObj["success"],
            jsObj["message"],
            jsObj["lineTokens"]
        )
    }
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["success"]=success
        this["message"]=message
        this["lineTokens"]=lineTokens
    }
}


class MessageSetStyle(
    val languageId: String,
    val editorId: String,
    val css: String
) : AglWorkerMessage("MessageSetStyle") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["css"]=css
    }
}


class MessageSetStyleResult(
    val languageId: String,
    val editorId: String,
    val success: Boolean,
    val message: String
) : AglWorkerMessage("MessageSetStyleResult") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["success"]=success
        this["message"]=message
    }
}


class MessageProcessStart(
    val languageId: String,
    val editorId: String
) : AglWorkerMessage("MessageProcessStart") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
    }
}


class MessageProcessSuccess(
    val languageId: String,
    val editorId: String,
    val asm: Any
) : AglWorkerMessage("MessageProcessSuccess") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["asm"]=asm
    }
}


class MessageProcessFailure(
    val languageId: String,
    val editorId: String,
    val message: String
) : AglWorkerMessage("MessageProcessFailure") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["editorId"]=editorId
        this["message"]=message
    }
}

class MessageCodeCompleteRequest(
    val languageId: String,
    val editorId: String,
    val goalRuleName:String?,
    val text: String,
    val position:Int
) : AglWorkerMessage("MessageCodeCompleteRequest") {
    override fun toObjectJS(): dynamic = objectJS {
        this["action"] = action
        this["languageId"]=languageId
        this["goalRuleName"]=goalRuleName
        this["text"]=text
        this["position"]=position
    }
}