package net.akehurst.language.editor.language.service

import net.akehurst.language.agl.generators.FormatTypeModelAsKotlinTypeModelBuilder
import net.akehurst.language.agl.generators.GenerateTypeModelViaReflection
import net.akehurst.language.agl.generators.PropertiesTypeModelFormatConfiguration
import net.akehurst.language.agl.generators.TypeModelFormatConfiguration
import net.akehurst.language.base.api.Indent
import net.akehurst.language.base.api.QualifiedName
import net.akehurst.language.base.api.SimpleName
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.asm.SimpleTypeModelStdLib
import kotlin.test.Test

class test_GenerateTypeModelViaReflection {

    //TODO: use a gradle-plugin when it is written ;-)

    fun gen_messages():Pair<TypeModel,List<QualifiedName>> {
        val gen = GenerateTypeModelViaReflection(
            SimpleName("Test"),
            listOf(SimpleTypeModelStdLib),
            GenerateTypeModelViaReflection.KOTLIN_TO_AGL,
            emptyList()
        )
        gen.addPackage("net.akehurst.language.api.language.base")
        gen.addPackage("net.akehurst.language.agl.language.base")

        gen.addPackage("net.akehurst.language.api.language.grammar")
        gen.addPackage("net.akehurst.language.agl.language.grammar.asm")
        gen.addPackage("net.akehurst.language.typemodel.api")
        gen.addPackage("net.akehurst.language.typemodel.simple")
        gen.addPackage("net.akehurst.language.api.grammarTypeModel")
        gen.addPackage("net.akehurst.language.agl.grammarTypeModel")
        gen.addPackage("net.akehurst.language.api.language.expressions")
        gen.addPackage("net.akehurst.language.agl.language.expressions.asm")
        gen.addPackage("net.akehurst.language.api.language.reference")
        gen.addPackage("net.akehurst.language.agl.language.reference.asm")

        gen.include("net.akehurst.language.collections.ListSeparated")
        gen.exclude("net.akehurst.language.api.asm.AsmSimpleBuilder")
        gen.addPackage("net.akehurst.language.api.scope")
        gen.addPackage("net.akehurst.language.agl.scope")
        gen.addPackage("net.akehurst.language.api.asm")
        gen.addPackage("net.akehurst.language.agl.asm")

        gen.addPackage("net.akehurst.language.api.parser")
        gen.addPackage("net.akehurst.language.api.sppt")
        gen.addPackage("net.akehurst.language.agl.sppt")
        gen.addPackage("net.akehurst.language.agl.api.runtime")

        gen.include("net.akehurst.language.editor.api.EndPointIdentity")
        gen.include("net.akehurst.language.editor.api.MessageStatus")
        gen.include("net.akehurst.language.editor.api.EditorOptions")
        gen.include("net.akehurst.language.editor.api.AglToken")
        gen.include("net.akehurst.language.editor.common.AglTokenDefault")
        gen.include("net.akehurst.language.agl.scanner.Matchable")
        gen.include("net.akehurst.language.api.processor.LanguageIssue")
        gen.include("net.akehurst.language.api.processor.ProcessOptions")
        gen.include("net.akehurst.language.api.processor.CompletionItem")
        gen.addPackage("net.akehurst.language.api.language.style")
        gen.addPackage("net.akehurst.language.editor.language.service.messages")

        val tm = gen.generate()
        return Pair(tm, emptyList())
    }

    @Test
    fun test_format_messages() {
        val (tm,added) = gen_messages()
        val fmrtr = FormatTypeModelAsKotlinTypeModelBuilder(
            TypeModelFormatConfiguration(
            exludedNamespaces = added,
            includeInterfaces = true,
            properties = PropertiesTypeModelFormatConfiguration(
                includeDerived = false
            )
        )
        )
        println(fmrtr.formatTypeModel(Indent(), tm, true, listOf("SimpleTypeModelStdLib")))
    }

}