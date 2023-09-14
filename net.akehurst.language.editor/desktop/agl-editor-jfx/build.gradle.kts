import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
  kotlin("multiplatform")
}

kotlin {
    (project.ext["configureJvm"] as (KotlinMultiplatformExtension)->Unit).invoke(this)
}

val version_agl:String by project
val version_jfx:String by project
val version_coroutines:String by project
dependencies {

    "jvm8MainApi"(project(":agl-editor-common"))

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