plugins {
    alias(libs.plugins.reflect)
}

dependencies {
    commonMainApi(project(":agl-editor-api"))
    //commonMainApi(project(":agl-language-service"))
    commonMainImplementation(libs.nak.kotlinx.reflect)
}

exportPublic {
    exportPatterns.set(listOf(
        "net.akehurst.language.editor.common.*"
    ))
}

kotlinxReflect {
    forReflectionMain.set(
        listOf(
            "net.akehurst.language.editor.common.*"
        )
    )
}
