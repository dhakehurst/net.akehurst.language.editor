import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput


dependencies {
    jsMainImplementation(libs.nale.agl.editor.browser.worker)
}

kotlin {
    js("js",IR) {
        binaries.executable()
        nodejs()
        browser {
            webpackTask {
                output.libraryTarget = KotlinWebpackOutput.Target.SELF
            }
        }
    }
}


//tasks.getByName("jsBrowserProductionWebpack").dependsOn("jsProductionLibraryCompileSync")
//tasks.getByName("jsBrowserProductionLibraryPrepare").dependsOn("jsProductionExecutableCompileSync")
//tasks.getByName("jsNodeProductionLibraryPrepare").dependsOn("jsProductionExecutableCompileSync")