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

import net.akehurst.language.agl.grammar.grammar.ContextFromGrammar
import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.api.asm.AsmElementSimple
import net.akehurst.language.api.asm.AsmSimple
import net.akehurst.language.api.parser.InputLocation
import net.akehurst.language.api.processor.LanguageDefinition
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.api.sppt.*
import net.akehurst.language.api.style.AglStyleRule
import net.akehurst.language.collections.mutableStackOf
import net.akehurst.language.editor.common.*
import net.akehurst.language.editor.common.messages.*
import org.w3c.dom.MessageEvent

abstract class AglWorkerJsAbstract<AsmType : Any, ContextType : Any> : AglWorkerAbstract<AsmType, ContextType>() {

    override fun sendMessage(port: dynamic, msg: AglWorkerMessage, transferables: Array<dynamic>) {
        //val str = AglWorkerMessage.serialise(msg)
        try {
            val str = AglWorkerSerialisation.serialise(msg)
            port.postMessage(str, transferables)
        } catch (t:Throwable) {
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
        } catch (e: Throwable) {
            port.postMessage("Error: Worker error in receiveMessage: ${e.message!!}")
        }
    }

    override fun serialiseParseTreeToStringJson(sentence:String, sppt: SharedPackedParseTree?) : String? {
        try {
            return sppt?.let {
                val root = objectJS {
                    children = mutableListOf<dynamic>()
                }
                val stack = mutableStackOf<dynamic>()
                stack.push(root)
                val walker = object : SpptWalker {
                    override fun beginBranch(option: Int, ruleName: String, startPosition: Int, nextInputPosition: Int) {
                        stack.push(objectJS {
                            isBranch = true
                            isSkip = false
                            name = ruleName
                            children = mutableListOf<dynamic>()
                        })
                    }

                    override fun endBranch(opt: Int, ruleName: String, startPosition: Int, nextInputPosition: Int) {
                        val node = stack.pop()
                        node.children = (node.children as MutableList<dynamic>).toTypedArray()
                        val parent = stack.peek()
                        (parent.children as MutableList<dynamic>).add(node)
                    }

                    override fun error(msg: String, path: () -> List<SpptPathNode>) {
                        val parent = stack.peek()
                        val node = objectJS {
                            isBranch = false
                            isSkip = false
                            name = "ERROR"
                            nonSkipMatchedText = msg
                        }
                        (parent.children as MutableList<dynamic>).add(node)
                    }

                    override fun leaf(ruleName: String, startPosition: Int, nextInputPosition: Int) {
                        val parent = stack.peek()
                        val matchedText = sentence.substring(startPosition, nextInputPosition)
                        val node = objectJS {
                            isBranch = false
                            isSkip = false
                            name = ruleName
                            nonSkipMatchedText = escapeCtrlCodes(matchedText)
                        }
                        (parent.children as MutableList<dynamic>).add(node)
                    }

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

                }

                sppt.traverseTreeDepthFirst(walker)

                return JSON.stringify((root.children as MutableList<dynamic>)[0])

                //val jsObj = parseTreeToJS2(it)
                //return JSON.stringify(jsObj)
            }
        } catch (t:Throwable) {
            error("Error trying to serialise SPPT to JSON: ${t.message}")
        }
    }

    private fun parseTreeToJS2(spptNode: SPPTNode): dynamic {
        val deep = DeepRecursiveFunction<SPPTNode, dynamic> {
            when (it) {
                is SPPTLeaf -> objectJS {
                    isBranch = false
                    name = it.name
                    nonSkipMatchedText = escapeCtrlCodes(it.nonSkipMatchedText)
                }
                is SPPTBranch -> objectJS {
                    isBranch = true
                    name = it.name
                    children = it.children.map {
                        callRecursive(it)
                    }.toTypedArray()
                }
                else -> error("Not supported")
            }
        }
        return deep(spptNode)
    }

    private fun parseTreeToJS(spptNode: SPPTNode): dynamic {
        return when (spptNode) {
            is SPPTLeaf -> objectJS {
                isBranch = false
                name = spptNode.name
                nonSkipMatchedText = escapeCtrlCodes(spptNode.nonSkipMatchedText)
            }
            is SPPTBranch -> objectJS {
                isBranch = true
                name = spptNode.name
                children = spptNode.children.map {
                    parseTreeToJS(it)
                }.toTypedArray()
            }
            else -> error("Not supported")
        }
    }

    private fun escapeCtrlCodes(input:String) : String = input
        .replace(Regex("\n"),"\\n")
        .replace(Regex("\r"),"\\r")
        .replace(Regex("\t"),"\\t")

    /*
    private fun createAsmTree(asm: Any?): Any? {
        return if (null == asm) {
            null
        } else {
            when (asm) {
                is AsmSimple ->{
                    createAsmTree(asm.rootElements)
                }
                is AsmElementSimple -> {
                    objectJS {
                        isAsmElementSimple = true
                        typeName = asm.typeName
                        properties = asm.properties.map {
                            objectJS {
                                isAsmElementProperty = true
                                name = it.name
                                value = createAsmTree(it.value)
                            }
                        }.toTypedArray()
                    }
                }
                is List<*> -> asm.map {
                    createAsmTree(it)
                }.toTypedArray()
                else -> JSON.stringify(asm)
            }
        }
    }
*/
}
