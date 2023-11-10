val version_agl:String by project
val version_korge:String by project
dependencies {
    commonMainImplementation("net.akehurst.language:agl-processor:$version_agl")
    commonMainApi("com.soywiz.korge:korge-core:$version_korge")
}
