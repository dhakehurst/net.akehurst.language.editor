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

import korlibs.io.file.std.resourcesVfs
import net.akehurst.language.editor.information.Example

object SysML_2 {
    val id = "SysML_2"
    val label = "SysML v2"
    val sentence = """
    """.trimIndent()

    val references = """
    """.trimIndent()


    val format = """
        
    """.trimIndent()

    suspend fun example(): Example {
        val grammarStr = resourcesVfs["examples/SysML_2/grammar.agl"].readString()
        val styleStr = resourcesVfs["examples/SysML_2/style.agl"].readString()
        return Example(id, label, "S", sentence, grammarStr, references, styleStr, format)
    }
}