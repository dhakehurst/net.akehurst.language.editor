plugins {
    id("project-conventions")
    alias(libs.plugins.reflect)
}

dependencies {
    commonMainApi(libs.nal.agl.processor)
    commonMainApi(libs.nak.kotlinx.logging.api)

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