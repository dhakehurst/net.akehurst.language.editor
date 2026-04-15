@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm {
        mainRun {
            mainClass = "demo.MainKt"
        }
    }
    js {
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":information-editor"))
                implementation(libs.nale.agl.editor.common)
                implementation(libs.nale.agl.language.service)
                implementation(libs.nale.agl.editor.compose)
                implementation(libs.nak.compose.code.editor)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(libs.kotlinx.coroutines.core)
                //implementation(libs.nak.kotlinx.logging.common)
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

