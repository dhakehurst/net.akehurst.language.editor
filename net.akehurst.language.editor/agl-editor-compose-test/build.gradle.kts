plugins {
    id("project-conventions")
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

// do not publish
tasks.withType<AbstractPublishToMaven> { onlyIf { false } }

repositories {
    google()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":agl-editor-compose"))
                implementation(libs.nak.compose.code.editor)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.nak.kotlinx.logging.common)
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
