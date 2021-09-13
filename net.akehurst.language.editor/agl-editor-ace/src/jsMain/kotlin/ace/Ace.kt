/*
 * Based on [https://github.com/daemontus/kotlin-ace-wrapper]
 */
package ace

@JsModule("ace-builds/src-noconflict/ace")
@JsNonModule
external object Ace {

    fun <T : Any> require(moduleName: String): T = definedExternally

    fun createEditSession(text: String): EditSession

}

external interface AceEditorOptions {
    var selectionStyle: String? // "line"|"text"
    var highlightActiveLine: Boolean?
    var highlightSelectedWord: Boolean?
    var readOnly: Boolean?
    var cursorStyle: String? // "ace"|"slim"|"smooth"|"wide"
    var mergeUndoDeltas: Any? // false|true|"always"
    var behavioursEnabled: Boolean?
    var wrapBehavioursEnabled: Boolean?

    /**
     * this is needed if editor is inside scrollable page (defaults to false)
     */
    var autoScrollEditorIntoView: Boolean?

    /**
     * copy/cut the full line if selection is empty, (defaults to false)
     */
    var copyWithEmptySelection: Boolean?

    /**
     * (defaults to false)
     */
    var useSoftTabs: Boolean?

    /**
     * (defaults to false)
     */
    var navigateWithinSoftTabs: Boolean?

    /**
     * (defaults to false)
     */
    var enableMultiselect: Boolean?
}

external interface AceRendererOptions {
    /**
     * (defaults to false)
     */
    var hScrollBarAlwaysVisible: Boolean?

    /**
     * (defaults to false)
     */
    var vScrollBarAlwaysVisible: Boolean?

    /**
     * (defaults to false)
     */
    var highlightGutterLine: Boolean?

    /**
     * (defaults to false)
     */
    var animatedScroll: Boolean?

    /**
     * (defaults to false)
     */
    var showInvisibles: Boolean?

    /**
     * (defaults to false)
     */
    var showPrintMargin: Boolean?

    /**
     * (defaults to 80)
     */
    var printMarginColumn: Int?

    /**
     * shortcut for showPrintMargin and printMarginColumn, false|number
     */
    var printMargin: Any?

    /**
     * (defaults to false)
     */
    var fadeFoldWidgets: Boolean?

    /**
     * (defaults to true)
     */
    var showFoldWidgets: Boolean?

    /**
     * (defaults to true)
     */
    var showLineNumbers: Boolean?

    /**
     * (defaults to true)
     */
    var showGutter: Boolean?

    /**
     * (defaults to true)
     */
    var displayIndentGuides: Boolean?

    /**
     * number or css font-size string
     */
    var fontSize: Any?

    /**
     * css font-family value
     */
    var fontFamily: String?

    /**
     * resize editor based on the contents of the editor until the number of lines reaches maxLines
     */
    var maxLines: Int?

    /**
     * (defaults to false)
     */
    var minLines: Int?


    /**
     * number of page sizes to scroll after document end (typical values are 0, 0.5, and 1), number|boolean
     */
    var scrollPastEnd: Any?

    /**
     * (defaults to false)
     */
    var fixedWidthGutter: Boolean?

    /**
     * path to a theme e.g "ace/theme/textmate"
     */
    var theme: Any?
}

external interface AceMouseHandlerOptions{
    //TODO:
}

external interface AceSessionOptions {
    //TODO:
}

//not actually defined/used by Ace, but used by AglEditorAce
external interface AceOptions {
    var editor: AceEditorOptions;
    var renderer: AceRendererOptions;
    var mouseHandler: AceMouseHandlerOptions;
    var session: AceSessionOptions;
}