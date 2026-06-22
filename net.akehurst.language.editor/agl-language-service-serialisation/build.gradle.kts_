

dependencies {
    commonMainApi(project(":agl-editor-api"))
    //commonMainApi(project(":agl-language-service"))

    commonMainApi(project(":agl-editor-common")) // for utils

    commonMainImplementation(libs.nak.serialisation.json)
    commonMainImplementation(libs.nak.kotlinx.collections)
    commonMainImplementation(libs.nak.kotlinx.reflect)
}



configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Common Parts")
        }
    }
}