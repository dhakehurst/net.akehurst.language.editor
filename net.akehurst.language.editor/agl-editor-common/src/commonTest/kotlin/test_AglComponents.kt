package net.akehurst.language.editor.common

import net.akehurst.kotlinx.logging.common.LoggerCommon
import net.akehurst.language.agl.Agl
import net.akehurst.language.api.processor.GrammarString
import net.akehurst.language.api.processor.LanguageIdentity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AglComponents {

    val logger = LoggerCommon("Log") { logLevel, prefix, throwable, msg ->
        println("$logLevel: $prefix - ${msg()}")
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
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationSimple(),
        )
        def.grammarStrObservers.add { s1: GrammarString?, s2: GrammarString? ->
            println("Grammar changed: $s1, $s2")
            modified = s2
        }

        def.configuration = Agl.configuration(Agl.configurationSimple()){
            grammarString(GrammarString( "something new"))
        }

        assertEquals("something new", modified?.value)
    }

    @Test
    fun modifyIdentity() {
        val langId1 = LanguageIdentity("test1")
        val langId2 = Agl.registry.agl.grammarLanguageIdentity
        val def = Agl.registry.register(
            identity = langId1,
            aglOptions = null,
            buildForDefaultGoal = false,
            configuration = Agl.configurationSimple(),
        )

        val sut = AglComponents(def, "", logger,AglStyleHandlerCssClass(langId1))
        assertEquals(langId1.value, sut.languageDefinition.identity.value)

        sut.languageDefinition =  Agl.registry.findOrPlaceholder( Agl.registry.agl.grammarLanguageIdentity,null,null)

        assertEquals(Agl.registry.agl.grammarLanguageIdentity, sut.languageDefinition.identity)
    }

}