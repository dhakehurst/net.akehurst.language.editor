//val version_kotlin:String by project
val version_agl:String by project
val version_jfx:String by project
val version_coroutines:String by project

plugins {
   // id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

dependencies {

    jvm8MainApi(project(":agl-editor-common"))

    //jvm8MainImplementation("org.openjfx:javafx:$version_jfx")

}

kotlin {
}

configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: JFX HTMLEditor")
        }
    }
}