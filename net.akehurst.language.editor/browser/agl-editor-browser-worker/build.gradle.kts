import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

plugins {
    kotlin("multiplatform")
}

kotlin {
    (project.ext["configureCommon"] as (KotlinMultiplatformExtension)->Unit).invoke(this)

    js("js") {
        browser {
            webpackTask {
                output.libraryTarget = KotlinWebpackOutput.Target.SELF
            }
        }
    }
}

val version_agl:String by project
val version_kjson:String by project
dependencies {
    commonMainApi(project(":agl-language-service-serialisation"))
    commonMainApi("net.akehurst.language:agl-processor:$version_agl")
    //commonMainApi(platform("net.akehurst.language:agl-processor"))
    commonMainImplementation("net.akehurst.kotlin.json:json:$version_kjson")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Web Worker")
        }
    }
}