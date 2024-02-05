import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

//val version_kotlin:String by project
val version_agl:String by project
val version_firepad:String by project
val version_coroutines:String by project



dependencies {

    "jsMainApi"(project(":agl-language-service-serialisation"))

}

buildConfig {
    buildConfigField("String", "versionEditorFirepad", "\"${version_firepad}\"")
}

configure<PublishingExtension> {
   publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Firepad Editor")
        }
    }
}