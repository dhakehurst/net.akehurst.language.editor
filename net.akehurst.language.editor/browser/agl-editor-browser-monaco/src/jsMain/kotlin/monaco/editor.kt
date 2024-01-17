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

package monaco.editor

import monaco.IDisposable
import monaco.IPosition
import monaco.IRange
import monaco.MarkerSeverity
import org.w3c.dom.Element

external enum class EndOfLinePreference {
    TextDefined,
    LF,
    CRLF
}

external interface IModelDecorationOptions {
    var afterContentClassName: String?
    var beforeContentClassName: String?
    var className: String?
    var glyphMarginClassName: String?
    var glyphMarginHoverMessage: dynamic  //IMarkdownString | IMarkdownString[] | null
    var hoverMessage: dynamic  //IMarkdownString | IMarkdownString[] | null
    var inlineClassName: String?
    var inlineClassNameAffectsLetterSpacing: Boolean?
    var isWholeLine: Boolean?
    var linesDecorationsClassName: String?
    var marginClassName: String?
    var minimap: dynamic
    var overviewRuler: dynamic
    var stickiness: dynamic
    var zindex: dynamic
}

external interface IModelDeltaDecoration {
    var range: IRange
    var options: IModelDecorationOptions
}

external interface IEditor {
    fun layout(dimension: IDimension? = definedExternally)
}

external interface ICodeEditor : IEditor {
    fun getModel(): ITextModel

    fun onDidChangeModelContent(listener: (IModelContentChangedEvent) -> Unit): IDisposable
    fun deltaDecorations(oldDecorations: Array<String>, newDecorations: Array<IModelDeltaDecoration>): Array<String>
    fun getLineDecorations(lineNum: Int): dynamic
}

external interface IStandaloneCodeEditor : ICodeEditor

external interface IStandaloneEditorConstructionOptions {
    var language: String
    var value: String
    var theme: String
    var wordBasedSuggestions: String
}

external interface IEditorOverrideServices

external interface IStandaloneThemeData {
    var base: Any
    var inherit: Boolean;
    var rules: Array<ITokenThemeRule>
    //val encodedTokensColors: Array<String>?
    var colors: dynamic
}

external interface IModelContentChangedEvent

external interface IDimension

external interface ITextModel {
    val tokenization: dynamic

    fun getValue(eol: EndOfLinePreference? = definedExternally, preserveBOM: Boolean? = definedExternally): String
    fun setValue(newValue: String)

    fun getOffsetAt(position: IPosition): Int

}

external interface ITokenThemeRule {
    val token: String
    val foreground: String?
    val background: String?
    val fontStyle: String?
}

external interface IMarkerData {
    var code: String?
    var severity: MarkerSeverity;
    var message: String
    var source: String?
    var startLineNumber: Int
    var startColumn: Int
    var endLineNumber: Int
    var endColumn: Int
    //val relatedInformation: Array<IRelatedInformation>?
    //val tags: Array<MarkerTag>?
}


