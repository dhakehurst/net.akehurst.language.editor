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
package net.akehurst.language.editor.common.serialisation

import net.akehurst.kotlin.json.JsonDocument
import net.akehurst.kotlin.kserialisation.json.KSerialiserJson
import net.akehurst.language.api.grammar.Grammar
import net.akehurst.kotlinx.reflect.ModuleRegistry
import net.akehurst.language.agl.grammar.grammar.asm.*

object GrammarSerialisation {

    private val serialiser = KSerialiserJson().also {
        it.registerKotlinStdPrimitives()

    }
    private var initialised = false

    private fun initialise() {
        if (!initialised) {
            //TODO: enable kserialisation/komposite/reflect to auto add these some how!!
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.NamespaceDefault", NamespaceDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.GrammarDefault", GrammarDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.RuleDefault", RuleDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.ConcatenationDefault",ConcatenationDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.ChoiceLongestDefault",ChoiceLongestDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.ChoicePriorityDefault",ChoicePriorityDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.EmptyRuleDefault",EmptyRuleDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.NonTerminalDefault",NonTerminalDefault::class)
            ModuleRegistry.registerClass("net.akehurst.language.agl.grammar.grammar.asm.TerminalDefault",TerminalDefault::class)
            initialised = true
        }
    }

    fun toJsonDocument(grammar: Grammar): JsonDocument {
        return serialiser.toJson(grammar, grammar)
    }

    fun serialise(grammar: Grammar): String {
        return toJsonDocument(grammar).toJsonString()
    }

    fun deserialise(jsonString: String): Grammar {
        initialise()
        return serialiser.toData<Grammar>(jsonString)
    }
}