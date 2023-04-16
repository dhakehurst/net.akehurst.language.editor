/**
 * Copyright (C) 2023 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.language.editor.application.client.web

import net.akehurst.language.editor.api.LogLevel

class DemoLogger(
    var level: LogLevel
) {

    fun logFatal(msg: String?) {
        console.error("Fatal: $msg")
    }

    fun logError(msg: String?) {
        console.error("Error: $msg")
    }

    fun logWarn(msg: String?) {
        console.warn("Warn: $msg")
    }

    fun logInfo(msg: String?) {
        console.info("Info: $msg")
    }

    fun logDebug(msg: String?) {
        console.asDynamic().debug("Debug: $msg")
    }

    fun logTrace(msg: String?) {
        console.asDynamic().debug("Trace: $msg")
    }

    fun log(lvl: LogLevel, msg: String, t: Throwable?) {
        when {
            lvl <= level -> logAll(lvl, msg, t)
            else -> Unit
        }
    }

    private fun logAll(lvl: LogLevel, msg: String, t: Throwable?) {
        val func = when (lvl) {
            LogLevel.Fatal -> this::logFatal
            LogLevel.Error -> this::logError
            LogLevel.Warning -> this::logWarn
            LogLevel.Information -> this::logInfo
            LogLevel.Debug -> this::logDebug
            LogLevel.Trace -> this::logTrace
            else -> error("Internal Error: cannot log a message to '$lvl'")
        }
        if (null == t) {
            func(msg)
        } else {
            func(msg)
            t.printStackTrace()
        }
    }
}