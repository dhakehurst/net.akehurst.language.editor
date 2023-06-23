import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

plugins {
}

val version_agl:String by project
val version_kjson:String by project
dependencies {
    commonMainApi(project(":agl-editor-common"))
    commonMainApi("net.akehurst.language:agl-processor:$version_agl")
    //commonMainApi(platform("net.akehurst.language:agl-processor"))
    commonMainImplementation("net.akehurst.kotlin.json:json:$version_kjson")

    jsMainImplementation(npm("big-json", "3.2.0"))
}

kotlin {
    js("js") {
        binaries.library()
        nodejs()
        browser {
            webpackTask {
                output.libraryTarget = KotlinWebpackOutput.Target.SELF
            }
        }
    }
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Web Worker")
        }
    }
}