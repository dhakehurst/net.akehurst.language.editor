import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

kotlin {

    js("js") {
        browser {
            webpackTask {
                output.libraryTarget = KotlinWebpackOutput.Target.SELF
            }
        }
    }
}

val version_kjson:String by project
dependencies {
    commonMainApi(project(":agl-language-service-serialisation"))
    commonMainApi(libs.nal.agl.processor)
    //commonMainApi(platform("net.akehurst.language:agl-processor"))
    commonMainImplementation(libs.nak.json)
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Web Worker")
        }
    }
}