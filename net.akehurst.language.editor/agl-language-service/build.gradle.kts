plugins {
    id("net.akehurst.kotlinx.kotlinx-reflect-gradle-plugin")
}

dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-editor-common"))

    //jsMainImplementation(project(":agl-language-service-serialisation"))
    commonMainImplementation(libs.nak.kotlinx.collections)
    commonMainImplementation(libs.nak.kotlinx.reflect)
    commonMainImplementation(libs.nak.serialisation.json)
}

kotlinxReflect {
    forReflectionMain.set(
        listOf(
            "net.akehurst.language.editor.api",
            "net.akehurst.language.editor.common",
            "net.akehurst.language.editor.language.service.messages",//.**",

            "net.akehurst.language.typemodel.api",
            "net.akehurst.language.api.automaton",
            "net.akehurst.language.api.parser",
            "net.akehurst.language.api.processor",
            "net.akehurst.language.api.style",
            "net.akehurst.language.api.language.grammar",
            "net.akehurst.language.agl.grammarTypeModel",
            "net.akehurst.language.api.syntaxAnalyser",
            "net.akehurst.language.api.semanticAnalyser",

            "net.akehurst.language.typemodel.simple",
            "net.akehurst.language.agl.grammarTypeModel",
            "net.akehurst.language.agl.scanner",
            "net.akehurst.language.agl.syntaxAnalyser",
            "net.akehurst.language.agl.semanticAnalyser",
            "net.akehurst.language.agl.asm",
            "net.akehurst.language.agl.sppt",
            "net.akehurst.language.agl.language.expressions",
            "net.akehurst.language.agl.language.grammar",
            "net.akehurst.language.agl.language.grammar.asm",
            "net.akehurst.language.agl.language.reference",
            "net.akehurst.language.agl.language.reference.asm",
            "net.akehurst.language.agl.language.style",
            "net.akehurst.language.agl.language.style.asm",
            "net.akehurst.language.agl.language.format",
            "net.akehurst.language.agl.default",
            "net.akehurst.language.agl.processor",
            "net.akehurst.language.agl.runtime.structure"
        )
    )
}