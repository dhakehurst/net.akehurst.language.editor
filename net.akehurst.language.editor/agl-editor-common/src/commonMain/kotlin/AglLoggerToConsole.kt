package net.akehurst.language.editor.common

import net.akehurst.language.editor.api.AglEditorLogger
import net.akehurst.language.editor.api.LogLevel

class AglLoggerToConsole(
    var level: LogLevel
) {

        fun logFatal(msg: String?) = println("Fatal: $msg")
        fun logError(msg: String?) = println("Error: $msg")
        fun logWarn(msg: String?) = println("Warn: $msg")
        fun logInfo(msg: String?) = println("Info: $msg")
        fun logDebug(msg: String?) = println("Debug: $msg")
        fun logTrace(msg: String?) = println("Trace: $msg")

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