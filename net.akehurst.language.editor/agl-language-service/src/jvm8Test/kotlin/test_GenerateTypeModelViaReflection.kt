package net.akehurst.language.editor.language.service

import net.akehurst.language.agl.generators.FormatTypeModelAsKotlinTypeModelBuilder
import net.akehurst.language.agl.generators.GenerateTypeModelViaReflection
import net.akehurst.language.agl.generators.PropertiesTypeModelFormatConfiguration
import net.akehurst.language.agl.generators.TypeModelFormatConfiguration
import net.akehurst.language.asm.simple.AglAsm
import net.akehurst.language.base.api.Indent
import net.akehurst.language.base.api.QualifiedName
import net.akehurst.language.base.api.SimpleName
import net.akehurst.language.base.processor.AglBase
import net.akehurst.language.editor.language.service.messages.EditorMessage
import net.akehurst.language.scope.processor.AglScope
import net.akehurst.language.style.processor.AglStyle
import net.akehurst.language.typemodel.api.TypeModel
import net.akehurst.language.typemodel.asm.SimpleTypeModelStdLib
import net.akehurst.language.typemodel.processor.AglTypemodel
import kotlin.test.Test

class test_GenerateTypeModelViaReflection {

    //TODO: use a gradle-plugin when it is written ;-)

    fun gen_messages(): Pair<TypeModel, List<QualifiedName>> {
        val added = (
                AglStyle.typeModel.namespace +
                        AglAsm.typeModel.namespace +
                        AglScope.typeModel.namespace +
                        AglTypemodel.typeModel.namespace
                ).toSet().toList()
        val gen = GenerateTypeModelViaReflection(
            SimpleName("Test"),
            added,
            GenerateTypeModelViaReflection.KOTLIN_TO_AGL,
            listOf(EditorMessage.komposite, AglAsm.komposite, AglScope.komposite, AglTypemodel.komposite)
        )

//        gen.addPackage("net.akehurst.language.api.parser")
//        gen.addPackage("net.akehurst.language.api.sppt")
//        gen.addPackage("net.akehurst.language.agl.sppt")
//        gen.addPackage("net.akehurst.language.agl.api.runtime")

        gen.include("net.akehurst.language.sppt.api.TreeData")
        gen.include("net.akehurst.language.sppt.api.SpptDataNode")
        gen.include("net.akehurst.language.sppt.treedata.TreeDataComplete2")
        gen.include("net.akehurst.language.sppt.treedata.CompleteTreeDataNode")
        gen.include("net.akehurst.language.sppt.treedata.PreferredNode")

        gen.include("net.akehurst.language.parser.api.Rule")
        gen.include("net.akehurst.language.agl.runtime.structure.RuntimeRule")

        gen.include("net.akehurst.language.sentence.api.InputLocation")
        gen.include("net.akehurst.language.issues.api.LanguageIssueKind")
        gen.include("net.akehurst.language.issues.api.LanguageProcessorPhase")
        gen.include("net.akehurst.language.issues.api.LanguageIssue")
        gen.include("net.akehurst.language.scanner.api.Matchable")
        gen.include("net.akehurst.language.scanner.api.MatchableKind")
        gen.include("net.akehurst.language.scanner.api.ScanOptions")
        gen.include("net.akehurst.language.parser.api.ParseOptions")
        gen.include("net.akehurst.language.api.processor.LanguageIdentity")
        gen.include("net.akehurst.language.api.processor.ProcessOptions")
        gen.include("net.akehurst.language.api.processor.CompletionItem")
        gen.include("net.akehurst.language.api.processor.SyntaxAnalysisOptions")
        gen.include("net.akehurst.language.api.processor.SemanticAnalysisOptions")
        gen.include("net.akehurst.language.api.processor.CompletionProviderOptions")
        gen.include("net.akehurst.language.api.semanticAnalyser.SentenceContext")

        gen.include("net.akehurst.language.scanner.common.ScanOptionsDefault")
        gen.include("net.akehurst.language.parser.leftcorner.ParseOptionsDefault")
        gen.include("net.akehurst.language.agl.processor.SyntaxAnalysisOptionsDefault")
        gen.include("net.akehurst.language.agl.processor.SemanticAnalysisOptionsDefault")
        gen.include("net.akehurst.language.agl.processor.CompletionProviderOptionsDefault")
        gen.include("net.akehurst.language.agl.processor.ProcessOptionsDefault")
        gen.include("net.akehurst.language.agl.simple.ContextAsmSimple")
        gen.include("net.akehurst.language.grammar.processor.ContextFromGrammar")
        gen.include("net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModelReference")
        gen.include("net.akehurst.language.agl.semanticAnalyser.ContextFromTypeModel")

        gen.include("net.akehurst.language.editor.api.EndPointIdentity")
        gen.include("net.akehurst.language.editor.api.MessageStatus")
        gen.include("net.akehurst.language.editor.api.EditorOptions")
        gen.include("net.akehurst.language.editor.api.AglToken")

        gen.include("net.akehurst.language.editor.common.AglTokenDefault")
        gen.include("net.akehurst.language.editor.common.EditorOptionsDefault")
        gen.addPackage("net.akehurst.language.editor.language.service.messages")

        val tm = gen.generate()

        return Pair(tm, added.map { it.qualifiedName })
    }

    @Test
    fun test_format_messages() {
        val (tm, added) = gen_messages()
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