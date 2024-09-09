import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

//val version_kotlin:String by project
val version_agl:String by project
val version_coroutines:String by project


dependencies {

    "jsMainApi"(project(":agl-editor-common"))
//    "jsMainApi"(project(":agl-language-service-serialisation"))
    "jsMainApi"(project(":agl-editor-browser-worker"))

}

configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Monaco Editor")
        }
    }
}