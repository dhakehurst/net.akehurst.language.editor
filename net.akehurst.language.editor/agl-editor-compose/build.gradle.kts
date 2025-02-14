plugins {
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

    commonTestImplementation(libs.nak.compose.code.editor)
    commonTestImplementation(compose.ui)
    commonTestImplementation(compose.foundation)
    commonTestImplementation(compose.material3)
    commonTestImplementation(libs.kotlinx.coroutines)
    jvm11TestImplementation(compose.desktop.currentOs)
}

