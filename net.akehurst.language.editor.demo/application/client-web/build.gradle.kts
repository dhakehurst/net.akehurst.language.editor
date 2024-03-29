val version_agl_editor:String by project
val version_html_builder:String by project

dependencies {

    jsMainImplementation(project(":information-editor"))
    jsMainImplementation(project(":technology-gui-widgets"))
    jsMainImplementation("net.akehurst.language.editor:agl-editor-ace:$version_agl_editor")
    jsMainImplementation("net.akehurst.language.editor:agl-editor-monaco:$version_agl_editor")

    jsMainImplementation("net.akehurst.kotlin.html5:html-builder:$version_html_builder")

    //result of this will be copied into resources
    //jsMainImplementation(project(":technology-agl-editor-worker"))

    // for webpack
    jsMainImplementation(npm("monaco-editor-webpack-plugin", "1.8.2"))
    jsMainImplementation(npm("css-loader", "3.4.2"))
    jsMainImplementation(npm("style-loader", "1.1.3"))
    jsMainImplementation(npm("ts-loader", "6.2.1"))
    jsMainImplementation(npm("file-loader", "5.0.2"))

}

kotlin {
    js("js") {
        binaries.executable()
    }
    sourceSets {
        val jvm8Main by getting {
            resources.srcDir("$buildDir/distributions")
        }
    }
}

val workerTask = tasks.register<Copy>("copyAglEditorWorkerJs") {
    dependsOn(":application-agl-editor-worker:jsBrowserProductionWebpack")
    dependsOn("jsProcessResources")
    from("$buildDir/../application-agl-editor-worker/distributions") {
        include("application-agl-editor-worker.js")
        include("application-agl-editor-worker.js.map")
    }
    into(file("$buildDir/processedResources/js/main"))

}

tasks.getByName("jsBrowserDistribution").dependsOn(workerTask)
tasks.getByName("jsBrowserDevelopmentRun").dependsOn(workerTask)
tasks.getByName("jsBrowserProductionRun").dependsOn(workerTask)
tasks.getByName("jsBrowserProductionWebpack").dependsOn(workerTask)
tasks.getByName("jsProductionExecutableCompileSync").dependsOn(workerTask)
tasks.getByName("jvm8ProcessResources").dependsOn("jsBrowserProductionWebpack")
