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
    jvmTestImplementation(libs.nal.agl.generators)
}

kotlinxReflect {
    forReflectionMain.set(
        listOf(
            "net.akehurst.language.editor.api.*",
            "net.akehurst.language.editor.common.*",
            "net.akehurst.language.editor.language.service.messages.*",

            // from agl-parser
            "net.akehurst.language.sppt.**",
            "net.akehurst.language.sentence.**",
            "net.akehurst.language.issues.**",
            "net.akehurst.language.scanner.**",
            "net.akehurst.language.parser.**",
            "net.akehurst.language.agl.runtime.structure.**",

            // from agl-processor
            "net.akehurst.language.base.**",
            "net.akehurst.language.grammar.**",
            "net.akehurst.language.style.**",
            "net.akehurst.language.types.**",
            "net.akehurst.language.grammarTypemodel.**",
            "net.akehurst.language.asm.**",
            "net.akehurst.language.expressions.**",
            "net.akehurst.language.reference.**",
            "net.akehurst.language.scope.**",
            "net.akehurst.language.api.semanticAnalyser.SentenceContext",

            "net.akehurst.language.api.processor.**",
            "net.akehurst.language.agl.processor.**",
            "net.akehurst.language.agl.simple.ContextWithScope",
            "net.akehurst.language.agl.simple.NULL_SENTENCE_IDENTIFIER",
            "net.akehurst.language.agl.simple.CreateScopedItemDefault",
            "net.akehurst.language.agl.simple.ResolveScopedItemDefault",
            "net.akehurst.language.agl.syntaxAnalyser.LocationMapDefault",
            "net.akehurst.language.agl.semanticAnalyser.**"
        )
    )
}
