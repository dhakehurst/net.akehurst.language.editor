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

import net.akehurst.language.agl.Agl
import net.akehurst.language.editor.information.Example

object AglStyle {
    val id = "AglStyle"
    val label = "Agl Style"
    val sentence = """
${'$'}keyword {
  foreground: purple;
  font-style: bold;
}
'xyz','abc' {
  foreground: blue;
  font-style: bold;
}
"'([^'\\]|\\.)*'" {
  foreground: green;
  font-style: italic;
}
    """.trimIndent()
    val grammar = Agl.registry.agl.style.grammarStr!!

    val references = """
    """.trimIndent()

    val style = Agl.registry.agl.style.styleStr!!
    val format = """
        
    """.trimIndent()

    val example = Example(id, label, sentence, grammar, references, style, format,"")

}