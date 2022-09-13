//val version_kotlin:String by project
val version_agl:String by project
val version_monaco:String by project
val version_coroutines:String by project

plugins {
   // id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

dependencies {

    jsMainApi(project(":agl-editor-common"))

    jsMainImplementation(npm("monaco-editor", version_monaco))

    //commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$version_coroutines")

    // for webpack
    jsMainImplementation(npm("monaco-editor-webpack-plugin", "1.9.0"))
    jsMainImplementation(npm("css-loader", "3.4.2"))
    jsMainImplementation(npm("style-loader", "1.1.3"))
    jsMainImplementation(npm("ts-loader", "6.2.1"))
    jsMainImplementation(npm("file-loader", "5.0.2"))
}

kotlin {
    js("js") {
        binaries.library()
    }
}

buildConfig {
    buildConfigField("String", "versionEditorMonaco", "\"${version_monaco}\"")
}


configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Monaco Editor")
        }
    }
}