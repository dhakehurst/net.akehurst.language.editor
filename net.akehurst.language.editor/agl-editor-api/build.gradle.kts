
dependencies {
    commonMainApi(libs.nal.agl.processor)
}

exportPublic {
    exportPatterns.set(listOf(
        "net.akehurst.language.editor.api.**"
    ))
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: API")
        }
    }
}