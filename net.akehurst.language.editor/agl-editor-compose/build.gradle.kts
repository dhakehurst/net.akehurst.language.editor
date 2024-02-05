import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    kotlin("multiplatform")
}

kotlin {
    (project.ext["configureCommon"] as (KotlinMultiplatformExtension) -> Unit).invoke(this)

    macosArm64()
}

val version_kserialisation: String by project
val version_kotlinx: String by project
dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-language-service"))

    commonMainApi("net.akehurst.kotlin.compose:code-editor-api:1.9.22")

    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
}

