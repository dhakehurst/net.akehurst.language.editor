//val version_kotlin:String by project
val version_agl:String by project
val version_ace:String by project
val version_coroutines:String by project
val version_html_builder:String by project

plugins {
    id("net.akehurst.kotlin.gradle.plugin.exportPublic")
}

dependencies {
    "jsMainApi"(project(":agl-editor-common"))
    "jsMainApi"(project(":agl-editor-browser-worker"))
    jsMainImplementation("net.akehurst.kotlin.html5:html-builder:$version_html_builder")
}

kotlin {
    js("js") {
        binaries.library()
    }
}

buildConfig {
    buildConfigField("String", "versionEditorAgl", "\"${project.version}\"")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor")
        }
    }
}
