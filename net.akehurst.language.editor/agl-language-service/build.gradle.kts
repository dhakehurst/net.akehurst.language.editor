plugins {
    alias(libs.plugins.reflect)
}

dependencies {
    commonMainApi(project(":agl-editor-api"))
    commonMainApi(project(":agl-editor-common"))

    //jsMainImplementation(project(":agl-language-service-serialisation"))
    commonMainImplementation(libs.nal.kotlinx.komposite)
    commonMainImplementation(libs.nak.kotlinx.collections)
    commonMainImplementation(libs.nak.kotlinx.reflect)
    commonMainImplementation(libs.nak.kserialisation.json)

    jvm8TestImplementation(libs.nal.agl.generators)
}

kotlinxReflect {
    forReflectionMain.set(
        listOf(
            "net.akehurst.language.editor.api.*",
            "net.akehurst.language.editor.common.*",
            "net.akehurst.language.editor.language.service.messages.*",//.**",
        )
    )
}
