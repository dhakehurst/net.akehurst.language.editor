

dependencies {
    jsMainImplementation(npm("loader-utils","2.0.0"))
    jsMainImplementation(npm("script-loader","0.7.2"))
}

kotlin {
    js("js") {
        binaries.library()
        compilations["main"].packageJson {
            customField("author", mapOf(
                "name" to "Dr. David H. Akehurst",
                "email" to "dr.david.h@akehurst.net",
                "url" to "https://medium.com/@dr.david.h.akehurst"
            ))
            customField("license", "Apache-2.0")
            customField("keywords", listOf("ace","webpack","loader"))
            customField("homepage", "https://github.com/dhakehurst/net.akehurst.language.editor")
            customField("description:", "Webpack loader for using Ace Editor with kotlin")
        }
    }
}

tasks.named<Copy>("jsProductionLibraryCompileSync") {
   duplicatesStrategy = DuplicatesStrategy.WARN
}
tasks.named<Copy>("jsDevelopmentLibraryCompileSync") {
   duplicatesStrategy = DuplicatesStrategy.WARN
}
configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        pom {
            name.set("AGL Processor Editor integration: Ace Loader for webpack")
        }
    }
}