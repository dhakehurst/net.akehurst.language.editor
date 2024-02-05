
dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-editor-common"))
    commonMainApi(project(":agl-language-service"))

    commonMainApi(libs.nak.compose.code.editor.api)

    commonMainImplementation(libs.kotlinx.coroutines)
}

