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
import kotlin.js.Console
import kotlin.reflect.KFunction1

fun Console.fatal(vararg o: Any?): Unit = console.asDynamic().debug()
fun Console.debug(vararg o: Any?): Unit = console.asDynamic().debug()
fun Console.trace(vararg o: Any?): Unit = console.asDynamic().debug()

class DemoLogger {

    var level = LogLevel.Information

    fun log(lvl: LogLevel, msg: String, t: Throwable?) {
        when {
            lvl <= level -> logAll(lvl, msg, t)
            else -> Unit
        }
    }

    private fun logAll(lvl: LogLevel, msg: String, t: Throwable?) {
        val func = when (lvl) {
            LogLevel.Fatal -> console::fatal
            LogLevel.Error -> console::error
            LogLevel.Warning -> console::warn
            LogLevel.Information -> console::info
            LogLevel.Debug -> console::debug
            LogLevel.Trace -> console::trace
            else -> error("Internal Error: cannot log a message to '$lvl'")
        }
        if (null == t) {
            func(arrayOf(msg))
        } else {
            func(arrayOf(msg))
            t.printStackTrace()
        }
    }
}