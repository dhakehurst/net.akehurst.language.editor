/**
 * Copyright (C) 2023 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.processor.ProcessResultDefault
import net.akehurst.language.agl.simple.NULL_SENTENCE_IDENTIFIER
import net.akehurst.language.agl.simple.SentenceContextAny
import net.akehurst.language.agl.simple.contextAsmSimple
import net.akehurst.language.agl.syntaxAnalyser.SyntaxAnalyserByMethodRegistrationAbstract
import net.akehurst.language.api.processor.LanguageProcessor
import net.akehurst.language.asm.api.AsmPath
import net.akehurst.language.base.api.QualifiedName
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.scope.api.Scope
import net.akehurst.language.sentence.api.Sentence
import net.akehurst.language.sppt.api.SpptDataNodeInfo

object ExternalContextLanguage {
    val grammarStr = """
        namespace demo
        grammar ExternalContext {
            skip leaf WS = "\s+" ;
        
            context = item* ;
            item = referableName ':' typeReference scope? ;
            scope = '{' item* '}' ;
            referableName = STRING ;
            typeReference = qualifiedName ;
            qualifiedName = [NAME / '.']+ ;
            leaf STRING = "'([^']|\\')*'" ;
            leaf NAME = "[a-zA-Z_][a-zA-Z_0-9]*" ;
        }
    """.trimIndent()

    val processor: LanguageProcessor<SentenceContextAny, Unit> by lazy {
        val result = Agl.processorFromString<SentenceContextAny, Unit>(
            grammarDefinitionStr = grammarStr,
            configuration = Agl.configuration {
                syntaxAnalyserResolver {
                    ProcessResultDefault(
                        ExternalContextSyntaxAnalyser(),
                        processIssues = IssueHolder(LanguageProcessorPhase.ALL)
                    )
                }
            }
        )
        result.processor ?: error("Internal Error: processor not created")
    }
}

class ExternalContextSyntaxAnalyser : SyntaxAnalyserByMethodRegistrationAbstract<SentenceContextAny>() {
    override fun registerHandlers() {
        register(this::context)
        register(this::item)
        register(this::scope)
        register(this::referableName)
        register(this::typeReference)
        register(this::qualifiedName)
    }

    // context = item* ;
    private fun context(target: SpptDataNodeInfo, children: List<Any?>, sentence: Sentence): SentenceContextAny {
        val ctx = contextAsmSimple {  }
        val itemList = children as List<(Scope<Any>) -> Unit>
        itemList.forEach { it.invoke(ctx.scopeForSentence[NULL_SENTENCE_IDENTIFIER]!!) }
        return ctx
    }

    // item = referableName ':' typeReference scope? ;
    private fun item(target: SpptDataNodeInfo, children: List<Any?>, sentence: Sentence): (Scope<Any>) -> Unit {
        val referableName = children[0] as String
        val typeReference = children[2] as QualifiedName
        val item = null
        val itemScope = children[3] as ((Scope<Any>) -> Unit)?
        return { scope: Scope<Any> ->
            scope.addToScope(referableName, typeReference, item, "?", true)
            if (null != itemScope) {
                val s = scope.createOrGetChildScope(referableName, typeReference)
                itemScope.invoke(s)
            }
        }
    }

    // scope = '{' item* '}' ;
    private fun scope(target: SpptDataNodeInfo, children: List<Any?>, sentence: Sentence): (Scope<AsmPath>) -> Unit {
        return { scope ->
            val itemList = children[1] as List<(Scope<AsmPath>) -> Unit>
            itemList.forEach { it.invoke(scope) }
        }
    }

    // referableName = STRING ;
    private fun referableName(target: SpptDataNodeInfo, children: List<Any?>, sentence: Sentence): String =
        (children[0] as String).removeSurrounding("'")

    // typeReference = qualifiedName ;
    private fun typeReference(target: SpptDataNodeInfo, children: List<Any?>, sentence: Sentence): QualifiedName =
        children[0] as QualifiedName

    // qualifiedName = [NAME / '.']+ ;
    private fun qualifiedName(target: SpptDataNodeInfo, children: List<Any?>, sentence: Sentence): QualifiedName =
        QualifiedName( (children as List<String>).joinToString(separator = "") )

}