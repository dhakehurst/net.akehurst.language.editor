plugins {
    id("net.akehurst.kotlin.kt2ts") version "1.5.0"
}

val version_agl:String by project
val version_ace:String = "1.4.8"
dependencies {

    nodeKotlin("net.akehurst.language:agl-processor:$version_agl")

}

val srcDir = project.layout.projectDirectory.dir("src/angular")
val outDir = project.layout.buildDirectory.dir("angular")

kt2ts {
    nodeSrcDirectory.set(srcDir)
    nodeOutDirectory.set(outDir)

    nodeBuildCommand.set(
            if (project.hasProperty("prod")) {
                listOf("webpack", "--output=${outDir.get()}/main.js")
            } else {
                listOf("webpack", "--mode=development", "--output=${outDir.get()}/main.js")
            }
    )
}