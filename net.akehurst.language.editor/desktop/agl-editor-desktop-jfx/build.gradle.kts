
val version_agl:String by project
val version_jfx:String by project
val version_coroutines:String by project
dependencies {

    jvm11MainApi(project(":agl-language-service"))

    //jvm8MainImplementation("org.openjfx:javafx:$version_jfx")

}

configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: JFX HTMLEditor")
        }
    }
}