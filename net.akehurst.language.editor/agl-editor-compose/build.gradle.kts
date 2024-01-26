import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    kotlin("multiplatform")
}

kotlin {
    (project.ext["configureCommon"] as (KotlinMultiplatformExtension) -> Unit).invoke(this)
}

val version_kserialisation: String by project
val version_kotlinx: String by project
dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-editor-common"))


    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
}

