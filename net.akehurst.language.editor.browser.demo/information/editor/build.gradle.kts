val version_agl:String by project
val version_korio:String by project
dependencies {
    commonMainImplementation("net.akehurst.language:agl-processor:$version_agl")
    commonMainApi("com.soywiz.korlibs.korio:korio:$version_korio")
}
