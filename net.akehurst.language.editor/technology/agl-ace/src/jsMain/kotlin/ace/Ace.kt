/*
 * Based on [https://github.com/daemontus/kotlin-ace-wrapper]
 */
package ace

@JsModule("ace-builds")
@JsNonModule
external object Ace {

    fun <T: Any> require(moduleName: String): T = definedExternally

    fun createEditSession(text: String): EditSession

}