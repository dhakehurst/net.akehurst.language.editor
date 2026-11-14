/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Demo application for the AGL Compose editor: JVM desktop and wasmJs (browser)

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
    jvm {
        mainRun {
            mainClass = "net.akehurst.language.editor.demo.MainJvmKt"
        }
    }
    wasmJs {
        outputModuleName = "agl-editor-demo"
        browser {
            commonWebpackConfig {
                outputFileName = "agl-editor-demo.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":agl-editor-compose"))
                implementation(project(":agl-language-service"))
                implementation(libs.nak.compose.code.editor)
                implementation(libs.nak.compose.components)
                implementation(libs.nak.compose.layout.multipane)
                implementation(libs.bundles.compose)
                implementation(libs.compose.navigation)
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

compose.resources {
    packageOfResClass = "net.akehurst.language.editor.demo.resources"
}

compose.desktop {
    application {
        mainClass = "net.akehurst.language.editor.demo.MainJvmKt"
        nativeDistributions {
            packageName = "agl-editor-demo"
        }
    }
}
