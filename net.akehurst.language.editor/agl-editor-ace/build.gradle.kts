val version_kotlin:String by project
val version_agl:String by project
val version_ace:String by project
val version_coroutines:String by project

dependencies {

    "jsMainApi"(project(":agl-editor-common"))

    "jsMainImplementation"(npm("ace-builds", version_ace))

    "jsMainImplementation"(npm("net.akehurst.language.editor-kotlin-ace-loader", "1.0.4"))
    //jsMainImplementation(project(":technology-kotlin-ace-loader"))
    //jsMainImplementation(npm("net.akehurst.language.editor-kotlin-ace-loader","https://nexus-intern.itemis.de/nexus/repository/akehurst-npm/net.akehurst.language.editor-kotlin-ace-loader/-/net.akehurst.language.editor-kotlin-ace-loader-1.0.4.tgz"))

    //commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$version_coroutines")
}


//tasks.withType<ProcessResources>  {
//    val map = project.properties.toMutableMap()
//    map["version_kotlin"] = version_kotlin
//    map["version_agl"] = version_agl
//    map["version_ace"] = version_ace
//    filesMatching("**/package.json") {
//        expand(map)
//    }
//}
