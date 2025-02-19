package net.akehurst.language.editor.common

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import net.akehurst.language.base.api.QualifiedName
import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.LogLevel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglComponents {

    val logger = AglEditorLogger("Log") { logLevel: LogLevel, prefix:String, msg: String, throwable: Throwable? ->
        println("$logLevel: $msg")
    }

    @BeforeTest
    fun before() {
        // ensure grammar language is registered
        Agl.registry.agl.grammar
    }

    @Test
    fun modifyObserver() {
        var modified = null as GrammarString?
        val langId = "test"
        val def = Agl.registry.register(
            identity = LanguageIdentity(langId),
            grammarStr = null,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationSimple(),
        )
        def.grammarStrObservers.add { s1: GrammarString?, s2: GrammarString? ->
            println("Grammar changed: $s1, $s2")
            modified = s2
        }

        def.grammarStr =GrammarString( "something new")

        assertEquals("something new", modified?.value)
    }

    @Test
    fun modifyIdentity() {
        val langId1 = LanguageIdentity("test1")
        val langId2 = Agl.registry.agl.grammarLanguageIdentity
        val def = Agl.registry.register(
            identity = langId1,
            grammarStr = null,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationSimple(),
        )

        val sut = AglComponents<Any, Any>(langId1, "", logger,AglStyleHandlerCssClass(langId1))
        assertEquals(langId1.value, sut.languageDefinition.identity.value)

        sut.languageDefinition =  Agl.registry.findOrPlaceholder( Agl.registry.agl.grammarLanguageIdentity)

        assertEquals(Agl.registry.agl.grammarLanguageIdentity, sut.languageDefinition.identity)
    }

}