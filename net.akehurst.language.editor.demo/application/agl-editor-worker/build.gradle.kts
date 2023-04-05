import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput


val version_agl_editor:String by project

dependencies {
    jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-worker:$version_agl_editor")
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
