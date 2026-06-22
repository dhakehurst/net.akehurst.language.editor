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
    var endColumn: Int
    var endLineNumber: Int
    var startColumn: Int
    var startLineNumber: Int

}
