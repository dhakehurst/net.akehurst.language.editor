@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

repositories {
    google()
}

kotlin {
    wasmJs {
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":information-editor"))

                implementation(libs.material.icons.core)
                implementation(libs.material.icons.extended)
                implementation(libs.compose.navigation)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.nak.compose.components)
                implementation(libs.nak.compose.layout.multipane)
            }
        }
    }
}

dependencies {
    commonMainApi(libs.nak.compose.code.editor)
    commonMainImplementation(libs.nale.agl.editor.compose)
    commonMainImplementation(libs.nale.agl.language.service)

    commonMainImplementation(compose.ui)
    commonMainImplementation(compose.foundation)
    commonMainImplementation(compose.material3)
    commonMainApi(libs.nak.compose.code.editor)

    jvmMainImplementation(compose.desktop.currentOs)

//    commonTestImplementation(libs.kotlinx.coroutines)

}