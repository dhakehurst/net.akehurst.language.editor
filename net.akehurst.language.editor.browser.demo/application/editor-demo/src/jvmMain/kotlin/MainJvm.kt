package net.akehurst.language.editor.demo

import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.kotlinx.logging.common.LoggingByConsole
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution
import java.io.File

suspend fun main() {
    println("PWD: ${File(".").absolutePath}")
    LoggingManager.use(LoggingByConsole)
    val logFunction: LogFunction = { logLevel, prefix, t, msg ->
        when {
            logLevel <= LogLevel.All -> {
                println("$logLevel: $prefix - ${msg.invoke()}")
                t?.let { println("$logLevel: $t") }
            }
        }
    }
    val languageService = LanguageServiceDirectExecution(logFunction)
//    val languageService = LanguageServiceByCoroutine(
//        CoroutineScope(Dispatchers.Default), logFunction
//    )
    val app = EditorDemoApplication(languageService)
    app.start({gui ->
        CoroutineScope(Dispatchers.Default).async {
            gui.doStart()
            singleWindowApplication(
                title = "AGL Editor",
            ) {
                gui.content()
            }
        }
    })
}