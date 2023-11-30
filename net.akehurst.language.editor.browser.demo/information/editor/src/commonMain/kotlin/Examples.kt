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

package net.akehurst.language.editor.information

import korlibs.io.file.VfsFile

object Examples {

    val map = mutableMapOf<String, Example>()

    suspend fun read(resources: VfsFile, id:String) {
        suspend fun VfsFile.readStringIfExists() : String? = if(this.exists()) this.readString() else null
        suspend fun VfsFile.readLinesIfExists() : List<String>? = if(this.exists()) this.readLines().toList() else null
        val dir = resources["examples"][id]
        val label = dir["info.txt"].readLinesIfExists()?.get(0)
        return if (label.isNullOrBlank()) {
            // do nothing
        } else {
            val grammarStr = dir["grammar.agl"].readStringIfExists() ?: ""
            val styleStr = dir["style.agl"].readStringIfExists() ?: ""
            val scopes = dir["references.agl"].readStringIfExists() ?: ""
            val format = dir["format.agl"].readStringIfExists() ?: ""
            val sentence = dir["sentence.txt"].readStringIfExists() ?: ""
            val context = dir["context.agl"].readStringIfExists() ?: ""
            val eg = Example(id, label, sentence, grammarStr, scopes, styleStr, format, context)
            Examples.add(eg)
        }
    }

    operator fun get(key: String): Example {
        return this.map[key]!!
    }

    fun add(eg:Example) {
        this.map[eg.id] = eg
    }

    fun add(
            id: String,
            label: String,
            sentence: String,
            grammar: String,
            references:String,
            style: String,
            format: String,
            context:String
    ) {
        this.map[id] = Example(id, label, sentence, grammar,  references, style, format, context)
    }
}

class Example(
        val id: String,
        val label: String,
        val sentence: String,
        val grammar: String,
        val references:String,
        val style: String,
        val format: String,
        val context: String
)
