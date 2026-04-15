@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType



kotlin {
    jvm {
        mainRun {
            mainClass = "net.akehurst.language.editor.web.server.MainKt"
        }
    }
}

dependencies {

    jvmMainImplementation(project(":application-client-web"))

    // ktor server modules
    jvmMainImplementation(libs.ktor.websockets)
    jvmMainImplementation(libs.ktor.server)
    jvmMainImplementation(libs.ktor.server.core)
    jvmMainImplementation(libs.ktor.server.jetty)

    // for logging
    jvmMainImplementation(libs.slf4j.simple)
}
