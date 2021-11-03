plugins {
    id("net.akehurst.kotlinx.kotlinx-reflect-gradle-plugin") version("1.4.1-1.6.0-RC")
}

val version_kserialisation:String by project
val version_kotlinx:String by project
dependencies {
    commonMainApi(project(":agl-editor-api"))

    commonMainImplementation("net.akehurst.kotlinx:kotlinx-reflect:$version_kotlinx")
    commonMainImplementation("net.akehurst.kotlin.kserialisation:kserialisation-json:$version_kserialisation")
}

kotlinxReflect {
    forReflection.set(listOf(
        "net.akehurst.language.editor.common.messages",
        "net.akehurst.language.api.asm",
        "net.akehurst.language.agl.grammar.grammar.asm",
        "net.akehurst.language.agl.syntaxAnalyser"
    ))
}


configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Common Parts")
        }
    }
}