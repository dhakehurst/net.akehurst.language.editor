plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    commonMainApi(libs.nak.compose.code.editor)
    commonMainImplementation(libs.nale.agl.editor.compose)
    commonMainImplementation(libs.nale.agl.language.service)

    commonMainImplementation(compose.ui)
    commonMainImplementation(compose.foundation)
    commonMainImplementation(compose.material3)
    commonMainApi(libs.nak.compose.code.editor)

    jvm8MainImplementation(compose.desktop.currentOs)

}