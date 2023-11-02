import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    kotlin("multiplatform")
    id("net.akehurst.kotlinx.kotlinx-reflect-gradle-plugin")
}

kotlin {
    (project.ext["configureCommon"] as (KotlinMultiplatformExtension)->Unit).invoke(this)
}

val version_kserialisation:String by project
val version_kotlinx:String by project
dependencies {
    commonMainApi(project(":agl-editor-api"))

    commonMainImplementation("net.akehurst.kotlinx:kotlinx-reflect:$version_kotlinx")
    commonMainImplementation("net.akehurst.kotlin.kserialisation:kserialisation-json:$version_kserialisation")

    commonMainImplementation("net.akehurst.kotlinx:kotlinx-collections:$version_kotlinx")

}

kotlinxReflect {

    forReflectionMain.set(listOf(
        "net.akehurst.language.editor.common",
        "net.akehurst.language.editor.common.messages",

        "net.akehurst.language.typemodel.api",
        "net.akehurst.language.api.automaton",
        "net.akehurst.language.api.asm",
        "net.akehurst.language.api.parser",
        "net.akehurst.language.api.processor",
        "net.akehurst.language.api.style",
        "net.akehurst.language.api.language.grammar",
        "net.akehurst.language.agl.grammarTypeModel",
        "net.akehurst.language.api.syntaxAnalyser",
        "net.akehurst.language.api.semanticAnalyser",

        "net.akehurst.language.typemodel.simple",
        "net.akehurst.language.agl.grammarTypeModel",
        "net.akehurst.language.agl.syntaxAnalyser",
        "net.akehurst.language.agl.semanticAnalyser",
        "net.akehurst.language.agl.sppt",
        "net.akehurst.language.agl.language.expressions",
        "net.akehurst.language.agl.language.grammar",
        "net.akehurst.language.agl.language.grammar.asm",
        "net.akehurst.language.agl.language.reference",
        "net.akehurst.language.agl.language.style",
        "net.akehurst.language.agl.language.style.asm",
        "net.akehurst.language.agl.language.format",
        "net.akehurst.language.agl.default"
    ))
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Common Parts")
        }
    }
}