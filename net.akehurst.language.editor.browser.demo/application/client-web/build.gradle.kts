import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val version_agl_editor: String by project
val version_html_builder: String by project
val version_ace: String by project
val version_monaco:String by project

dependencies {

    jsMainImplementation(project(":information-editor"))
    jsMainImplementation(project(":technology-gui-widgets"))
    jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-ace:$version_agl_editor")
    jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-monaco:$version_agl_editor")
    jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-codemirror:$version_agl_editor")
    jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-agl:$version_agl_editor")

    jsMainImplementation("net.akehurst.kotlin.html5:html-builder:$version_html_builder")

    jsMainImplementation(npm("ace-builds", version_ace))
    jsMainImplementation(npm("net.akehurst.language.editor-kotlin-ace-loader", "1.5.1"))
    jsMainImplementation(npm("monaco-editor", version_monaco))

    // for webpack
    jsMainImplementation(npm("monaco-editor-webpack-plugin", "1.8.2"))
    jsMainImplementation(npm("css-loader", "3.4.2"))
    jsMainImplementation(npm("style-loader", "1.1.3"))
    jsMainImplementation(npm("ts-loader", "6.2.1"))
    jsMainImplementation(npm("file-loader", "5.0.2"))

    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")
}

kotlin {
    js("js", IR) {
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            // add the example files to resources of the web-client
            val kotlinExtension = project(":information-editor").extensions.getByName("kotlin") as KotlinMultiplatformExtension
            val res = kotlinExtension.sourceSets.getByName("commonMain").resources
            resources.srcDir(res)
        }
        val jvm8Main by getting {
            resources.srcDir("$buildDir/dist/js/developmentExecutable")
        }
    }
}

buildConfig {
    buildConfigField("String", "versionEditorMonaco", "\"${version_monaco}\"")
    buildConfigField("String", "versionEditorAce", "\"${version_ace}\"")
}

val workerTask = tasks.register<Copy>("copyAglEditorWorkerJs") {
    dependsOn(":application-agl-editor-worker:jsBrowserProductionWebpack")
    dependsOn(":application-agl-editor-worker:jsBrowserDistribution")
    dependsOn("jsProcessResources")
    from("$buildDir/../application-agl-editor-worker/dist/js/productionExecutable") {
        include("application-agl-editor-worker.js")
        include("application-agl-editor-worker.js.map")
    }
    into(file("$buildDir/processedResources/js/main"))

}

val workerTaskDev = tasks.register<Copy>("copyAglEditorWorkerJsDev") {
    dependsOn(":application-agl-editor-worker:jsBrowserDevelopmentWebpack")
    dependsOn(":application-agl-editor-worker:jsBrowserDevelopmentExecutableDistribution")
    dependsOn("jsProcessResources")
    from("$buildDir/../application-agl-editor-worker/dist/js/developmentExecutable") {
        include("application-agl-editor-worker.js")
        include("application-agl-editor-worker.js.map")
    }
    into(file("$buildDir/processedResources/js/main"))

}

tasks.getByName("jsBrowserDevelopmentRun").dependsOn(workerTaskDev)
tasks.getByName("jsBrowserDevelopmentWebpack").dependsOn(workerTaskDev)
tasks.getByName("jsDevelopmentExecutableCompileSync").dependsOn(workerTaskDev)
tasks.getByName("jsBrowserDevelopmentExecutableDistributeResources").dependsOn(workerTaskDev)

tasks.getByName("jsBrowserProductionRun").dependsOn(workerTask)
tasks.getByName("jsBrowserProductionWebpack").dependsOn(workerTask)
tasks.getByName("jsProductionExecutableCompileSync").dependsOn(workerTask)
tasks.getByName("jsBrowserProductionExecutableDistributeResources").dependsOn(workerTask)
tasks.getByName("jsJar").dependsOn(workerTask)

tasks.getByName("jvm8ProcessResources").dependsOn("jsBrowserProductionWebpack")
tasks.getByName("jvm8ProcessResources").dependsOn("jsBrowserDistribution")


val pythonServerDev = tasks.register<Exec>("pythonServerDev") {
    group ="kotlin browser"
    dependsOn("jsBrowserDevelopmentWebpack")
    workingDir("$buildDir/developmentExecutable")
    commandLine("python3", "-m", "http.server")
}
val pythonServerProd = tasks.register<Exec>("pythonServerProd") {
    group ="kotlin browser"
    dependsOn("jsBrowserProductionWebpack")
    workingDir("$buildDir/distributions")
    commandLine("python3", "-m", "http.server")
}