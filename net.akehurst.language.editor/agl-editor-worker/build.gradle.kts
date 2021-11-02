import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput
val version_agl:String by project

plugins {
}

dependencies {
    commonMainApi(project(":agl-editor-common"))
    commonMainApi("net.akehurst.language:agl-processor:$version_agl")
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