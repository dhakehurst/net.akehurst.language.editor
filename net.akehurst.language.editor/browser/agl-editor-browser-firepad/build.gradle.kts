import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

//val version_kotlin:String by project
val version_agl:String by project
val version_firepad:String by project
val version_coroutines:String by project

plugins {
    kotlin("multiplatform")
}

kotlin {
    (project.ext["configureJs"] as (KotlinMultiplatformExtension)->Unit).invoke(this)
}

dependencies {

    "jsMainApi"(project(":agl-editor-common"))

}

buildConfig {
    buildConfigField("String", "versionEditorFirepad", "\"${version_firepad}\"")
}

configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Firepad Editor")
        }
    }
}