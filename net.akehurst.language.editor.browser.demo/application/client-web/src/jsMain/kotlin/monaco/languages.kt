//@file:JsModule("monaco-editor/esm/vs/editor/editor.api.js")
//@file:JsNonModule
//
//@file:JsQualifier("languages")

package monaco.languages

import monaco.CancellationToken
import monaco.IDisposable
import monaco.IPosition
import monaco.editor.ITextModel

external fun register(language: ILanguageExtensionPoint)
external fun setTokensProvider(languageId: String, provider: TokensProvider): IDisposable
external fun registerCompletionItemProvider(languageId: String, provider: CompletionItemProvider): IDisposable

external enum class CompletionItemKind {
    Text, Snippet
}