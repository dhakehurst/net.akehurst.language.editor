import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    kotlin("multiplatform")
    id("net.akehurst.kotlinx.kotlinx-reflect-gradle-plugin")
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

//    commonMainImplementation("net.akehurst.kotlinx:kotlinx-collections:$version_kotlinx")

}