plugins {
    id("org.jetbrains.intellij") version "1.15.0"
    kotlin("jvm")
}

intellij {
    version.set("2023.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}