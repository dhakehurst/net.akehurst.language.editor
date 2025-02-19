import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    application
}

// so that the application plugin can find the jars from the kotlin-plugin jvm configuration
val runtimeClasspath by configurations.getting {
    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
}
application {
    mainClass.set( "net.akehurst.language.editor.web.server.MainKt")
}

dependencies {
    // need this so that the gradle application-plugin can find the module built by the kotlin-plugin
    runtimeOnly( project(path=":application-editor-web-server", configuration="jvm8RuntimeElements") )

    jvm11MainImplementation(project(":application-client-web"))

    // ktor server modules
    jvm11MainImplementation(libs.ktor.websockets)
    jvm11MainImplementation(libs.ktor.server)
    jvm11MainImplementation(libs.ktor.server.core)
    jvm11MainImplementation(libs.ktor.server.jetty)

    // for logging
    jvm11MainImplementation(libs.slf4j.simple)
}
