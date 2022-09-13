//val version_kotlin:String by project
val version_agl:String by project
val version_ace:String by project
val version_coroutines:String by project

plugins {
    id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

dependencies {

    "jsMainApi"(project(":agl-editor-common"))
    "jsMainApi"(project(":agl-editor-worker"))

    "jsMainImplementation"(npm("ace-builds", version_ace))

    "jsMainImplementation"(npm("net.akehurst.language.editor-kotlin-ace-loader", "1.0.4"))
    //jsMainImplementation(project(":technology-kotlin-ace-loader"))
    //jsMainImplementation(npm("net.akehurst.language.editor-kotlin-ace-loader","https://nexus-intern.itemis.de/nexus/repository/akehurst-npm/net.akehurst.language.editor-kotlin-ace-loader/-/net.akehurst.language.editor-kotlin-ace-loader-1.0.4.tgz"))

    //commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$version_coroutines")
}

kotlin {
    js("js") {
        binaries.library()
    }
}

buildConfig {
    buildConfigField("String", "versionEditorAce", "\"${version_ace}\"")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Ace Editor")
        }
    }
}
