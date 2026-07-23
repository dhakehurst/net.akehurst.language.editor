plugins {
    id("project-conventions")
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

repositories {
    google()
}

dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-editor-common"))
    commonMainApi(project(":agl-language-service"))

    commonMainApi(libs.nak.compose.code.editor.api)
    commonMainImplementation(libs.kotlinx.coroutines)

    // for AglComposeTextEditor
    commonMainApi(libs.nak.compose.code.editor)
    commonMainApi(libs.bundles.compose)

    // to test it
    commonTestImplementation(libs.nak.compose.code.editor)
    commonTestImplementation(libs.compose.ui)
    commonTestImplementation(libs.compose.foundation)
    commonTestImplementation(libs.kotlinx.coroutines)
    commonTestImplementation(libs.nak.kotlinx.logging.common)
    jvmTestImplementation(compose.desktop.currentOs)

}

