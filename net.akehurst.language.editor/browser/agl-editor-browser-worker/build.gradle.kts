import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

kotlin {
    js("js") {
        browser {
            webpackTask {
                output.libraryTarget = KotlinWebpackOutput.Target.SELF
            }
            binaries.library()
            generateTypeScriptDefinitions()
            compilerOptions {
                target.set("es2015")
            }
        }
    }
}

dependencies {
    commonMainApi(project(":agl-language-service"))
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