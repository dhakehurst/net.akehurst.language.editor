//val version_kotlin:String by project
val version_agl:String by project
val version_tiptap:String by project
val version_coroutines:String by project

plugins {
   // id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

dependencies {

    jsMainApi(project(":agl-editor-common"))

    jsMainImplementation(npm("@tiptap/core", version_tiptap))
    jsMainImplementation(npm("@tiptap/pm", version_tiptap))
    jsMainImplementation(npm("@tiptap/starter-kit", version_tiptap))

    //commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$version_coroutines")

    // for webpack
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
    buildConfigField("String", "versionEditorTiptap", "\"${version_tiptap}\"")
}


configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Firepad Editor")
        }
    }
}