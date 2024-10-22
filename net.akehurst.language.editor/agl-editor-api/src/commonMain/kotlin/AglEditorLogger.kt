/**
 * Copyright (C) 2021 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.api

typealias LogFunction = (level: LogLevel, prefix:String, message: String,t:Throwable?) -> Unit

class AglEditorLogger(
    val prefix: String,
    var bind: LogFunction?
) {

    fun log(level: LogLevel, message: String, t:Throwable? = null) = this.bind?.also {
        it.invoke(level, prefix, message, t)
    }

    fun logFatal(message: String, t:Throwable? = null) = log(LogLevel.Fatal, message, t)
    fun logError(message: String, t:Throwable? = null) = log(LogLevel.Error, message, t)
    fun logWarning(message: String, t:Throwable? = null) = log(LogLevel.Warning, message, t)
    fun logInformation(message: String, t:Throwable? = null) = log(LogLevel.Information, message, t)
    fun logDebug(message: String, t:Throwable? = null) = log(LogLevel.Debug, message, t)
    fun logTrace(message: String, t:Throwable? = null) = log(LogLevel.Trace, message, t)

}