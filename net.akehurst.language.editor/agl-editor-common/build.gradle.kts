plugins {
    alias(libs.plugins.reflect)
}

dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainImplementation(libs.nak.kotlinx.logging.common)
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
