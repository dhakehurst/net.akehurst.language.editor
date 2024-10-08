/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.credentials) apply true
    alias(libs.plugins.exportPublic) apply false
    alias(libs.plugins.reflect) apply false
}

allprojects {

    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("net\\.akehurst.+")
            }
        }
        mavenCentral()
    }

    group = rootProject.name
    version = rootProject.libs.versions.project.get()

    project.layout.buildDirectory = File(rootProject.projectDir, ".gradle-build/${project.name}")

}

subprojects {
    val kotlin_languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
    val kotlin_apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
    val jvmTargetVersion = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8

    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.github.gmazzo.buildconfig")
    apply(plugin = "net.akehurst.kotlin.gradle.plugin.exportPublic")

    configure<BuildConfigExtension> {
        useKotlinOutput {
            this.internalVisibility = false
        }
        val now = java.time.Instant.now()
        fun fBbuildStamp(): String = java.time.format.DateTimeFormatter.ISO_DATE_TIME.withZone(java.time.ZoneId.of("UTC")).format(now)
        fun fBuildDate(): String = java.time.format.DateTimeFormatter.ofPattern("yyyy-MMM-dd").withZone(java.time.ZoneId.of("UTC")).format(now)
        fun fBuildTime(): String = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss z").withZone(java.time.ZoneId.of("UTC")).format(now)

        packageName("${project.group}.${project.name.replace("-", ".")}")
        buildConfigField("String", "version", "\"${project.version}\"")
        buildConfigField("String", "buildStamp", "\"${fBbuildStamp()}\"")
        buildConfigField("String", "buildDate", "\"${fBuildDate()}\"")
        buildConfigField("String", "buildTime", "\"${fBuildTime()}\"")
    }

    project.ext.set("jvmTarget", true)
    project.ext.set("jsTarget", true)
    project.ext.set("macosArm64Target", true)

    configure<KotlinMultiplatformExtension> {
        jvm("jvm8") {
            compilations {
                val main by getting {
                    compilerOptions.configure {
                        languageVersion.set(kotlin_languageVersion)
                        apiVersion.set(kotlin_apiVersion)
                        jvmTarget.set(jvmTargetVersion)
                    }
                }
                val test by getting {
                    compilerOptions.configure {
                        languageVersion.set(kotlin_languageVersion)
                        apiVersion.set(kotlin_apiVersion)
                        jvmTarget.set(jvmTargetVersion)
                    }
                }
            }
        }

        js("js", IR) {
            binaries.library()
            generateTypeScriptDefinitions()
            useEsModules()
            tasks.withType<KotlinJsCompile>().configureEach {
                kotlinOptions {
                    useEsClasses = true
                }
            }
            nodejs()
            browser {
                webpackTask {
                    mainOutputFileName = "${project.group}-${project.name}.js"
                }
            }
        }

//        if (project.name != "agl-language-service-serialisation") {
//            macosArm64()
//        }
        //        @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
//        wasmJs() {
//            binaries.library()
//            browser()
//        }

        sourceSets {
            val commonMain by getting {
                kotlin.srcDir("$buildDir/generated/kotlin")
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            all {
                languageSettings.optIn("kotlin.ExperimentalStdlibApi")
            }
        }
    }

    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }
    tasks.named("publish").get().dependsOn("javadocJar")

    fun getProjectProperty(s: String) = project.findProperty(s) as String?

    val creds = project.properties["credentials"] as nu.studer.gradle.credentials.domain.CredentialsContainer
    val sonatype_pwd = creds.forKey("SONATYPE_PASSWORD") as String?
        ?: getProjectProperty("SONATYPE_PASSWORD")
        ?: error("Must set project property with Sonatype Password (-P SONATYPE_PASSWORD=<...> or set in ~/.gradle/gradle.properties)")
    project.ext.set("signing.password", sonatype_pwd)

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getProjectProperty("SONATYPE_USERNAME")
                        ?: error("Must set project property with Sonatype Username (-P SONATYPE_USERNAME=<...> or set in ~/.gradle/gradle.properties)")
                    password = sonatype_pwd
                }
            }
        }
        publications.withType<MavenPublication> {
//            artifact(javadocJar.get())

            pom {
                name.set("AGL Processor integration with Editor")
                description.set("Dynamic, scan-on-demand, parsing; when a regular expression is just not enough")
                url.set("https://medium.com/@dr.david.h.akehurst/a-kotlin-multi-platform-parser-usable-from-a-jvm-or-javascript-59e870832a79")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Dr. David H. Akehurst")
                        email.set("dr.david.h@akehurst.net")
                    }
                }
                scm {
                    url.set("https://github.com/dhakehurst/net.akehurst.language.editor")
                }
            }
        }
    }

    configure<SigningExtension> {
        useGpgCmd()
        val publishing = project.properties["publishing"] as PublishingExtension
        sign(publishing.publications)
    }


    configurations.all {
        // Check for updates every build
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}