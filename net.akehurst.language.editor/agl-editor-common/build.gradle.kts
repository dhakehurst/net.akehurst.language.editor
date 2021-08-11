
val version_agl:String by project

dependencies {

    commonMainApi(project(":agl-editor-api"))
    commonMainApi("net.akehurst.language:agl-processor:$version_agl")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Common Parts")
        }
    }
}