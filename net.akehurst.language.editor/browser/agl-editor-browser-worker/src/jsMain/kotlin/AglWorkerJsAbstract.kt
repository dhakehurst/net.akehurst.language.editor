/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.worker

import net.akehurst.language.collections.mutableStackOf
import net.akehurst.language.editor.common.objectJS
import net.akehurst.language.editor.language.service.AglWorkerSerialisation
import net.akehurst.language.editor.language.service.messages.AglWorkerMessage
import net.akehurst.language.sppt.api.PathFunction
import net.akehurst.language.sppt.api.SharedPackedParseTree
import net.akehurst.language.sppt.api.SpptDataNodeInfo
import net.akehurst.language.sppt.api.SpptWalker
import org.w3c.dom.MessageEvent

abstract class AglWorkerJsAbstract<AsmType : Any, ContextType : Any> : AglWorkerAbstract() {

    override fun sendMessage(port: dynamic, msg: AglWorkerMessage, transferables: Array<dynamic>) {
        try {
            val str = AglWorkerSerialisation.serialise(msg)
            port.postMessage(str, transferables)
        } catch (t: Throwable) {
            val str = "Error: Worker cannot sendMessage: ${t.message}"
            port.postMessage(str, transferables)
        }
    }

    protected fun receiveMessageEventFromJsPort(port: dynamic, ev: MessageEvent) {
        try {
            if (ev.data is String) {
                val str = ev.data as String
                //val msg: AglWorkerMessage? = AglWorkerMessage.deserialise(str)
                val msg: AglWorkerMessage? = AglWorkerSerialisation.deserialise(str)
                if (null == msg) {
                    port.postMessage("Error: Worker cannot handle message: $str")
                } else {
                    super.receiveAglWorkerMessage(port, msg)
                }
            } else {
                port.postMessage("Error: Worker error in receiveMessage: data content should be a String, got - '${ev.data}'")
            }
        } catch (t: Throwable) {
            val st = t.stackTraceToString().substring(0, 100)
            val msg = "Error: Worker error in receiveMessage: ${t::class.simpleName} - ${t.message!!}\n$st"
            port.postMessage(msg)
        }
    }

    override fun serialiseParseTreeToStringJson(sentence: String, sppt: SharedPackedParseTree?): String? =
        serialiseParseTreeToStringJson1(sentence, sppt)

    private fun serialiseParseTreeToStringJson1(sentence: String, sppt: SharedPackedParseTree?): String? {
        try {
            return if (null == sppt) {
                null
            } else {
                val root = objectJS {
                    children = mutableListOf<dynamic>()
                }
                val stack = mutableStackOf<dynamic>()
                stack.push(root)
                val walker = object : SpptWalker {
                    override fun skip(startPosition: Int, nextInputPosition: Int) {
                        val parent = stack.peek()
                        val matchedText = sentence.substring(startPosition, nextInputPosition)
                        val node = objectJS {
                            isBranch = false
                            isSkip = true
                            name = ruleName
                            nonSkipMatchedText = escapeCtrlCodes(matchedText)
                        }
                        (parent.children as MutableList<dynamic>).add(node)
                    }

                    override fun beginTree() {}

                    override fun endTree() {}

                    override fun leaf(nodeInfo: SpptDataNodeInfo) {
                        val parent = stack.peek()
                        val matchedText = sentence.substring(nodeInfo.node.startPosition, nodeInfo.node.nextInputNoSkip)
                        val node = objectJS {
                            isBranch = false
                            isSkip = false
                            name = nodeInfo.node.rule.tag
                            nonSkipMatchedText = escapeCtrlCodes(matchedText)
                        }
                        if (nodeInfo.alt.index == 0) {
                            (parent.children as MutableList<dynamic>).add(node)
                        } else {
                            //TODO:
                        }
                    }

                    override fun beginBranch(nodeInfo: SpptDataNodeInfo) {
                        stack.push(objectJS {
                            isBranch = true
                            isSkip = false
                            name = nodeInfo.node.rule.tag
                            children = mutableListOf<dynamic>()
                        })
                    }

                    override fun endBranch(nodeInfo: SpptDataNodeInfo) {
                        val node = stack.pop()
                        node.children = (node.children as MutableList<dynamic>).toTypedArray()
                        val parent = stack.peek()
                        if (nodeInfo.alt.index == 0) {
                            (parent.children as MutableList<dynamic>).add(node)
                        } else {
                            //TODO:
                        }
                    }

                    override fun beginEmbedded(nodeInfo: SpptDataNodeInfo) {
                        this.beginBranch(nodeInfo)
                    }

                    override fun endEmbedded(nodeInfo: SpptDataNodeInfo) {
                        this.endBranch(nodeInfo)
                    }

                    override fun error(msg: String, path: PathFunction) {
                        val parent = stack.peek()
                        val node = objectJS {
                            isBranch = false
                            isSkip = false
                            name = "ERROR"
                            nonSkipMatchedText = msg
                        }
                        (parent.children as MutableList<dynamic>).add(node)
                    }
                }

                sppt.traverseTreeDepthFirst(walker, true)

                val obj = (root.children as MutableList<dynamic>)[0]
                JSON.stringify(obj)
            }
        } catch (t: Throwable) {
            error("Error trying to serialise SPPT to JSON: ${t.message}")
        }
    }

    private fun serialiseParseTreeToStringJson2(sentence: String, sppt: SharedPackedParseTree?): String? {
        try {
            return if (null == sppt) {
                null
            } else {
                //TODO, try serialising just the TreeData, because main knows the sentence already
                val sb = StringBuilder()
                val walker = object : SpptWalker {
                    override fun skip(startPosition: Int, nextInputPosition: Int) {
                        val matchedText = sentence.substring(startPosition, nextInputPosition)
                        sb.append("<SKIP>:'${escapeCtrlCodes(matchedText)}'")
                    }

                    override fun beginTree() {}

                    override fun endTree() {}

                    override fun leaf(nodeInfo: SpptDataNodeInfo) {
                        val matchedText = sentence.substring(nodeInfo.node.startPosition, nodeInfo.node.nextInputNoSkip)
                        sb.append("${nodeInfo.node.rule.tag}:'${escapeCtrlCodes(matchedText)}'")
                    }

                    override fun beginBranch(nodeInfo: SpptDataNodeInfo) {
                        sb.append("${nodeInfo.node.rule.tag}{")
                    }

                    override fun endBranch(nodeInfo: SpptDataNodeInfo) {
                        sb.append("}")
                    }

                    override fun beginEmbedded(nodeInfo: SpptDataNodeInfo) {
                        this.beginBranch(nodeInfo)
                    }

                    override fun endEmbedded(nodeInfo: SpptDataNodeInfo) {
                        this.endBranch(nodeInfo)
                    }

                    override fun error(msg: String, path: PathFunction) {
                        sb.append("<ERROR>:'${escapeCtrlCodes(msg)}'")
                    }
                }
                sppt.traverseTreeDepthFirst(walker, true)

                sb.toString()
            }
        } catch (t: Throwable) {
            error("Error trying to serialise SPPT to JSON: ${t.message}")
        }
    }

    private fun escapeCtrlCodes(input: String): String = input
        .replace("\n", "\u23CE")
        .replace("\t", "\u2B72")
        .replace(Regex("\r"), "")


}
