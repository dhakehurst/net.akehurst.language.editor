package ace

external interface EditSession {
    val id: String
    var bgTokenizer: BackgroundTokenizer?

    fun clearAnnotations()

    fun removeMarker(id:Int)
    fun setAnnotations(errors: Array<AceAnnotation>)
    fun getDocument(): dynamic
    fun addMarker(range: Range, cls: String, type: String): Int
}

external interface AceAnnotation {
    var row: Int
    var column: Int
    var text: String
    var type: String
    var raw: String?
}

external interface Range{
    var startRow:Int
    var startColumn:Int
    var endRow:Int
    var endColumn:Int
}