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

import korlibs.time.DateTime
import net.akehurst.language.editor.api.LogLevel
import kotlin.js.Date
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class DemoLogger(
    var level: LogLevel
) {

    fun logFatal(msg: String?) = log(LogLevel.Fatal, msg, null)
    fun logError(msg: String?) = log(LogLevel.Error, msg, null)
    fun logWarn(msg: String?) = log(LogLevel.Warning, msg, null)
    fun logInfo(msg: String?) = log(LogLevel.Information, msg, null)
    fun logDebug(msg: String?) = log(LogLevel.Debug, msg, null)
    fun logTrace(msg: String?) = log(LogLevel.Trace, msg, null)

    fun log(lvl: LogLevel, msg: String?, t: Throwable?) {
        when {
            lvl <= level -> logAll(lvl, msg, t)
            else -> Unit
        }
    }

    private fun logAll(lvl: LogLevel, msg: String?, t: Throwable?) {
        val func = when (lvl) {
            LogLevel.Fatal -> this::consoleFatal
            LogLevel.Error -> this::consoleError
            LogLevel.Warning -> this::consoleWarn
            LogLevel.Information -> this::consoleInfo
            LogLevel.Debug -> this::consoleDebug
            LogLevel.Trace -> this::consoleTrace
            else -> error("Internal Error: cannot log a message to '$lvl'")
        }
        if (null == t) {
            func(msg)
        } else {
            func(msg)
            t.printStackTrace()
        }
    }

    private fun consoleFatal(msg: String?) = console.error(duration("Fatal","$msg"))
    private fun consoleError(msg: String?) = console.error(duration("Error","$msg"))
    private fun consoleWarn(msg: String?) = console.warn(duration("Warn","$msg"))
    private fun consoleInfo(msg: String?) = console.info(duration("Info","$msg"))
    private fun consoleDebug(msg: String?) = console.asDynamic().debug(duration("Debug","$msg"))
    private fun consoleTrace(msg: String?) = console.asDynamic().debug(duration("Trace","$msg"))

    private var lastTimeMark: TimeSource.Monotonic.ValueTimeMark? = null
    private fun duration(lev: String, msg: String): String {
        return if (msg.startsWith("Send") || msg.startsWith("Receiv")) {
            val prev = lastTimeMark
            val now = TimeSource.Monotonic.markNow()
            lastTimeMark = now
            val durStr = prev?.let {
                val dur = now - it
                dur.toString(DurationUnit.MILLISECONDS)
            } ?: "0"
             "$lev ($durStr): $msg"
        } else {
            "$lev: $msg"
        }
    }
}