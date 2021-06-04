

dependencies {
    jsMainImplementation(npm("loader-utils","2.0.0"))
    jsMainImplementation(npm("script-loader","0.7.2"))
}

kotlin {
    js("js") {
        binaries.library()
    }
}

tasks.named<Jar>("jsJar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
}