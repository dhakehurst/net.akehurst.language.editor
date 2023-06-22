plugins {
    application
}

application {
    mainClass.set( "demo.MainKt")
}

val version_agl_editor: String by project
val version_html_builder: String by project

dependencies {
    // need this so that the gradle application-plugin can find the module built by the kotlin-plugin
    "runtimeOnly"( project(path=":application-editor-desktop", configuration="jvm8RuntimeElements") )

    commonMainImplementation(project(":information-editor"))
    commonMainImplementation("net.akehurst.language.editor:agl-editor-common:$version_agl_editor")
    //jsMainImplementation(project(":technology-gui-widgets"))
    //jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-ace:$version_agl_editor")
    //jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-monaco:$version_agl_editor")
    //jsMainImplementation("net.akehurst.language.editor:agl-editor-browser-codemirror:$version_agl_editor")

}

kotlin {
    js {
        binaries.executable()
    }
}