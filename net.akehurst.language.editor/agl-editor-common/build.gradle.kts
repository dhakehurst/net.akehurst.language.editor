plugins {
    id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

val version_kserialisation:String by project
val version_kotlinx:String by project
dependencies {
    commonMainApi(project(":agl-editor-api"))

    commonMainImplementation("net.akehurst.kotlinx:kotlinx-reflect:$version_kotlinx")
    commonMainImplementation("net.akehurst.kotlin.kserialisation:kserialisation-json:$version_kserialisation")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Common Parts")
        }
    }
}