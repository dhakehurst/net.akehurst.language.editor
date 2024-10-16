import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val version_monaco = "0.45.0"
val version_ace = "1.32.3"
val version_ck = "43.0.0"
val version_codemirror = libs.versions.nak.codemirror.editor.get()

dependencies {

    jsMainImplementation(project(":information-editor"))
    jsMainImplementation(project(":technology-gui-widgets"))

    jsMainImplementation(libs.nale.agl.language.service)

    jsMainImplementation(libs.nale.agl.editor.browser.agl)
    jsMainImplementation(libs.nak.html.builder)

    // Ace
    jsMainImplementation(libs.nale.agl.editor.browser.ace)
    jsMainImplementation(npm("ace-builds", "1.32.3"))
    jsMainImplementation(npm("net.akehurst.language.editor-kotlin-ace-loader", "1.5.1"))

    // Monaco
    jsMainImplementation(libs.nale.agl.editor.browser.monaco)
    jsMainImplementation(npm("monaco-editor", version_monaco))
    jsMainImplementation(npm("monaco-editor-webpack-plugin", "7.1.0"))
    jsMainImplementation(npm("css-loader", "6.8.1"))
    jsMainImplementation(npm("style-loader", "3.3.3"))
    jsMainImplementation(npm("ts-loader", "9.5.1"))
    jsMainImplementation(npm("file-loader", "6.2.0"))

    // Codemirror
    jsMainImplementation(libs.nak.codemirror.api.realisation)
    jsMainImplementation(libs.nale.agl.editor.browser.codemirror)

    // ComposeEditor
    jsMainImplementation(libs.nak.compose.code.editor)
    jsMainImplementation(libs.nale.agl.editor.compose)

    // CK-Editor
    jsMainImplementation(libs.nale.agl.editor.browser.ck)
    jsMainImplementation( npm("ckeditor5",version_ck))

    jsMainImplementation(libs.kotlinx.coroutines)
}

kotlin {
    js("js", IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                   // enabled.set(true)
                   // mode.set("import")
                }
            }
        }
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
    buildConfigField("String", "versionEditorCodeMirror", "\"${version_codemirror}\"")
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
//tasks.getByName("jsBrowserDevelopmentExecutableDistributeResources").dependsOn(workerTaskDev)

tasks.getByName("jsBrowserProductionRun").dependsOn(workerTask)
tasks.getByName("jsBrowserProductionWebpack").dependsOn(workerTask)
tasks.getByName("jsProductionExecutableCompileSync").dependsOn(workerTask)
//tasks.getByName("jsBrowserProductionExecutableDistributeResources").dependsOn(workerTask)
tasks.getByName("jsJar").dependsOn(workerTask)

tasks.getByName("jvm8ProcessResources").dependsOn("jsBrowserProductionWebpack")
tasks.getByName("jvm8ProcessResources").dependsOn("jsBrowserDistribution")


val pythonServerDev = tasks.register<Exec>("pythonServerDev") {
    group ="kotlin browser"
    dependsOn("jsBrowserDevelopmentExecutableDistribution")
    workingDir("$buildDir/dist/js/developmentExecutable")
    commandLine("python3", "-m", "http.server")
}
val pythonServerProd = tasks.register<Exec>("pythonServerProd") {
    group ="kotlin browser"
    dependsOn("jsBrowserProductionWebpack")
    workingDir("$buildDir/distributions")
    commandLine("python3", "-m", "http.server")
}