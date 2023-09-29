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

package net.akehurst.language.editor.information.examples

import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import net.akehurst.language.editor.information.Example

object GraphvizDot {
    private val dir = "examples/GraphvizDot"

    val id = "embedded-dot"
    val label = "Graphviz DOT Language (XML Embedded in DOT)"

    val references = """
    """.trimIndent()

    val format = """
        
    """.trimIndent()

    suspend fun example(resources: VfsFile): Example {
        val grammarStr = resources["$dir/grammar.agl"].readString()
        val styleStr = resources["$dir/style.agl"].readString()
        val sentence = resources["$dir/sentence.txt"].readString()
        return Example(id, label, sentence, grammarStr, references, styleStr, format)
    }

}