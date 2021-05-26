/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JsModule("monaco-editor/esm/vs/editor/editor.api")
@file:JsNonModule

package monaco

import org.w3c.dom.Element

external interface IEvent<T> {
    //val listener: (e: T) -> Any, thisArg?: any): IDisposable;
}

external interface IDisposable

external interface CancellationToken {
    val isCancellationRequested: Boolean

    /**
     * An event emitted when cancellation is requested
     * @event
     */
    val onCancellationRequested: IEvent<Any>
}

external object MarkerSeverity {
    val Hint: MarkerSeverity = definedExternally
    val Info: MarkerSeverity = definedExternally
    val Warning: MarkerSeverity = definedExternally
    val Error: MarkerSeverity = definedExternally
}

external interface IPosition {
    /**
     * line number (starts at 1)
     */
    val lineNumber: Int;

    /**
     * column (the first character in a line is between column 1 and column 2)
     */
    val column: Int;
}

external interface IRange {
    val endColumn: Int
    val endLineNumber: Int
    val startColumn: Int
    val startLineNumber: Int

}

external class Position(
        lineNumber: Int,
        column: Int
) : IPosition {
    override val lineNumber: Int
    override val column: Int
    //...
}

external object editor {

    fun create(element: Element, options: IStandaloneEditorConstructionOptions?, override: IEditorOverrideServices?): IStandaloneCodeEditor

    fun defineTheme(themeName: String, themeData: IStandaloneThemeData)

    fun setModelMarkers(model: ITextModel, owner: String, markers: Array<IMarkerData>)


    enum class EndOfLinePreference {
        TextDefined,
        LF,
        CRLF
    }

    interface IModelDecorationOptions {
        val afterContentClassName: String?
        val beforeContentClassName: String?
        val className: String?
        val glyphMarginClassName: String?
        val glyphMarginHoverMessage: dynamic  //IMarkdownString | IMarkdownString[] | null
        val hoverMessage: dynamic  //IMarkdownString | IMarkdownString[] | null
        val inlineClassName: String?
        val inlineClassNameAffectsLetterSpacing: Boolean?
        val isWholeLine: Boolean?
        val linesDecorationsClassName: String?
        val marginClassName: String?
        val minimap: dynamic
        val overviewRuler: dynamic
        val stickiness: dynamic
        val zindex: dynamic
    }

    interface IModelDeltaDecoration {
        val range: IRange
        val options: IModelDecorationOptions
    }

    interface IEditor {
        fun layout(dimension: IDimension? = definedExternally)
    }

    interface ICodeEditor : IEditor {
        fun getModel(): ITextModel

        fun onDidChangeModelContent(listener: (IModelContentChangedEvent) -> Unit): IDisposable
        fun deltaDecorations(oldDecorations: Array<String>, newDecorations: Array<IModelDeltaDecoration>) : Array<String>
        fun getLineDecorations(lineNum: Int): dynamic
    }

    interface IStandaloneCodeEditor : ICodeEditor

    interface IStandaloneEditorConstructionOptions

    interface IEditorOverrideServices

    interface IStandaloneThemeData {
        val base: Any
        val inherit: Boolean;
        val rules: Array<ITokenThemeRule>
        //val encodedTokensColors: Array<String>?
        //val colors: IColors
    }

    interface IModelContentChangedEvent

    interface IDimension

    interface ITextModel {
        fun getValue(eol: EndOfLinePreference? = definedExternally, preserveBOM: Boolean? = definedExternally): String
        fun setValue(newValue: String)

        fun getOffsetAt(position: IPosition): Int

        fun resetTokenization()
    }

    interface ITokenThemeRule {
        val token: String
        val foreground: String?
        val background: String?
        val fontStyle: String?
    }

    interface IMarkerData {
        val code: String?
        val severity: MarkerSeverity;
        val message: String
        val source: String?
        val startLineNumber: Int
        val startColumn: Int
        val endLineNumber: Int
        val endColumn: Int
        //val relatedInformation: Array<IRelatedInformation>?
        //val tags: Array<MarkerTag>?
    }
}

