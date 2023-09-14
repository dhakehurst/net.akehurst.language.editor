import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

//val version_kotlin:String by project
val version_agl:String by project
val version_monaco:String by project
val version_coroutines:String by project

plugins {
    kotlin("multiplatform")
}

kotlin {
    (project.ext["configureJs"] as (KotlinMultiplatformExtension)->Unit).invoke(this)
}

dependencies {

    "jsMainApi"(project(":agl-editor-common"))
    "jsMainApi"(project(":agl-editor-browser-worker"))

}

buildConfig {
    buildConfigField("String", "versionEditorMonaco", "\"${version_monaco}\"")
}

configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Monaco Editor")
        }
    }
}