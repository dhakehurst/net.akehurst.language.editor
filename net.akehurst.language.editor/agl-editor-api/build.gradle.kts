plugins {
   id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

val version_agl:String by project
dependencies {
    commonMainApi("net.akehurst.language:agl-processor:$version_agl")
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