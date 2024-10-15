val version_agl:String by project
val version_korge:String by project
dependencies {
    commonMainImplementation(libs.nal.agl.processor)
    commonMainApi(libs.korlibs.io)
}
