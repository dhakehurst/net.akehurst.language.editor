
val version_agl:String by project

plugins {
    id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

dependencies {
}

exportPublic {
    exportPatterns.set(listOf(
        "net.akehurst.language.editor.api.**"
    ))
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: API")
        }
    }
}