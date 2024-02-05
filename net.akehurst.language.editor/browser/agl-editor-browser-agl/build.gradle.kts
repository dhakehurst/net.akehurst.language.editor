import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

//val version_kotlin:String by project
val version_agl:String by project
val version_ace:String by project
val version_coroutines:String by project
val version_html_builder:String by project

plugins {
    kotlin("multiplatform")
}

kotlin {
    (project.ext["configureJs"] as (KotlinMultiplatformExtension)->Unit).invoke(this)
}

dependencies {
    "jsMainApi"(project(":agl-language-service-serialisation"))
    "jsMainApi"(project(":agl-editor-browser-worker"))
    "jsMainImplementation"("net.akehurst.kotlin.html5:html-builder:$version_html_builder")
}

buildConfig {
    buildConfigField("String", "versionEditorAgl", "\"${project.version}\"")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor")
        }
    }
}
