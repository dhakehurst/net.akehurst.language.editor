package net.akehurst.language.editor.common

import net.akehurst.language.agl.processor.Agl
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglComponents {

    val logger = AglEditorLogger().also {
        it.bind = { logLevel: LogLevel, msg: String, throwable: Throwable? ->
            println("$logLevel: $msg")
        }
    }

    @Test
    fun modifyObserver() {
        var modified = null as String?
        val langId = "test"
        val def = Agl.registry.register(
            identity = langId,
            grammarStr = null,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationDefault(),
        )
        def.grammarStrObservers.add { s1: String?, s2: String? ->
            println("Grammar changed: $s1, $s2")
            modified = s2
        }

        def.grammarStr = "something new"

        assertEquals("something new", modified)
    }

    @Test
    fun modifyIdentity() {
        val langId1 = "test1"
        val langId2 = Agl.registry.agl.grammarLanguageIdentity
        val def = Agl.registry.register(
            identity = langId1,
            grammarStr = null,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationDefault(),
        )

        val sut = AglComponents<Any,Any>(langId1,"", logger)
        assertEquals(langId1, sut.languageDefinition.identity)

        sut.languageIdentity = Agl.registry.agl.grammarLanguageIdentity

        assertEquals(Agl.registry.agl.grammarLanguageIdentity, sut.languageDefinition.identity)
    }

}