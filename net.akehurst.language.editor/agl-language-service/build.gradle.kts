plugins {
    id("project-conventions")
    alias(libs.plugins.reflect)
}

dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-editor-common"))

    //jsMainImplementation(project(":agl-language-service-serialisation"))
    commonMainImplementation(libs.nak.kotlinx.logging.common)
    commonMainImplementation(libs.nal.kotlinx.komposite)
    commonMainImplementation(libs.nak.kotlinx.collections)
    commonMainImplementation(libs.nak.kotlinx.reflect)
    commonMainImplementation(libs.nak.kserialisation.json)
    commonMainImplementation(libs.kotlinx.coroutines)
//    jvmTestImplementation(libs.nal.agl.generators)
}

kotlinxReflect {
    forReflectionMain.set(
        listOf(
            "net.akehurst.language.editor.api.*",
            "net.akehurst.language.editor.common.*",
            "net.akehurst.language.editor.language.service.messages.*",

            // classes from dependencies: the KSP processor's '**' only reaches the named package,
            // so each sub-package is listed
            // from agl-parser
            "net.akehurst.language.sppt.api.*",
            "net.akehurst.language.sppt.treedata.*",
            "net.akehurst.language.sentence.api.*",
            "net.akehurst.language.sentence.common.*",
            "net.akehurst.language.issues.api.*",
            "net.akehurst.language.issues.ram.*",
            "net.akehurst.language.scanner.api.*",
            "net.akehurst.language.scanner.common.*",
            "net.akehurst.language.parser.aMinimalVersion.*",
            "net.akehurst.language.parser.api.*",
            "net.akehurst.language.parser.leftcorner.*",
            "net.akehurst.language.agl.runtime.structure.*",

            // from agl-regex
            "net.akehurst.language.regex.api.*",

            // from agl-processor
            "net.akehurst.language.base.api.*",
            "net.akehurst.language.base.asm.*",
            "net.akehurst.language.base.processor.*",
            "net.akehurst.language.grammar.api.*",
            "net.akehurst.language.grammar.asm.*",
            "net.akehurst.language.grammar.builder.*",
            "net.akehurst.language.grammar.processor.*",
            "net.akehurst.language.style.api.*",
            "net.akehurst.language.style.asm.*",
            "net.akehurst.language.style.builder.*",
            "net.akehurst.language.style.processor.*",
            "net.akehurst.language.types.api.*",
            "net.akehurst.language.types.asm.*",
            "net.akehurst.language.types.builder.*",
            "net.akehurst.language.types.processor.*",
            "net.akehurst.language.grammarTypemodel.api.*",
            "net.akehurst.language.grammarTypemodel.asm.*",
            "net.akehurst.language.grammarTypemodel.builder.*",
            "net.akehurst.language.asm.api.*",
            "net.akehurst.language.asm.builder.*",
            "net.akehurst.language.asm.simple.*",
            "net.akehurst.language.expressions.api.*",
            "net.akehurst.language.expressions.asm.*",
            "net.akehurst.language.expressions.processor.*",
            "net.akehurst.language.reference.api.*",
            "net.akehurst.language.reference.asm.*",
            "net.akehurst.language.reference.builder.*",
            "net.akehurst.language.reference.processor.*",
            "net.akehurst.language.scope.api.*",
            "net.akehurst.language.scope.asm.*",
            "net.akehurst.language.scope.processor.*",

            "net.akehurst.language.api.semanticAnalyser.SentenceContext",

            "net.akehurst.language.api.processor.*",
            "net.akehurst.language.agl.processor.*",
            "net.akehurst.language.agl.simple.SentenceContextAny",
            "net.akehurst.language.agl.simple.NULL_SENTENCE_IDENTIFIER",
            "net.akehurst.language.agl.simple.CreateScopedItemDefault",
            "net.akehurst.language.agl.simple.ResolveScopedItemDefault",
            "net.akehurst.language.agl.syntaxAnalyser.LocationMapDefault",
            "net.akehurst.language.agl.semanticAnalyser.*"
        )
    )
}
