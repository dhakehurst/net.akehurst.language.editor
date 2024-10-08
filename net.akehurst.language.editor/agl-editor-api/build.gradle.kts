plugins {
    alias(libs.plugins.reflect)
}

dependencies {
    commonMainApi(libs.nal.agl.processor)

    commonMainImplementation(libs.nak.kotlinx.reflect)
}

exportPublic {
    exportPatterns.set(listOf(
        "net.akehurst.language.editor.api.**"
    ))
}

kotlinxReflect {
    forReflectionMain.set(
        listOf(
            "net.akehurst.language.editor.api.*"
        )
    )
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: API")
        }
    }
}