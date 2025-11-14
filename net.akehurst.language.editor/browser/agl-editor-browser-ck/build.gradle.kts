import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

//val version_kotlin:String by project
val version_agl: String by project
val version_coroutines: String by project

plugins {
    id("project-conventions")
}


dependencies {

    commonMainApi(project(":agl-editor-common"))
//    jsMainApi(project(":agl-language-service-serialisation"))
    commonMainApi(project(":agl-editor-browser-worker"))
    commonMainImplementation(libs.nak.kotlinx.logging.common)
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: CK Editor")
        }
    }
}
