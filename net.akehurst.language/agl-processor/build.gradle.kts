plugins {
    id("net.akehurst.kotlin.kt2ts") version "1.5.0"
}


kt2ts {
    jvmTargetName.set("jvm8")
    classPatterns.set(listOf(
            "net.akehurst.language.api.sppt.*",
            "net.akehurst.language.api.processor.*",
            "net.akehurst.language.api.style.*"
    ))
}

