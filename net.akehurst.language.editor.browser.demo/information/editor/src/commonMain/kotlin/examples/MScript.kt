package net.akehurst.language.editor.information.examples

import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import net.akehurst.language.editor.information.Example

object MScript {

    private val id = "MScript"
    private val label = "Matlab's Script Language"
    private val dir = "examples/MScript"

    private val references = """
    """.trimIndent()

    private val format = """
        
    """.trimIndent()

    suspend fun example(resources: VfsFile): Example {
        val grammarStr = resources["$dir/grammar.agl"].readString()
        val styleStr = resources["$dir/style.agl"].readString()
        val sentence = resources["$dir/sentence.txt"].readString()
        return Example(id, label, sentence, grammarStr, references, styleStr, format)
    }
}