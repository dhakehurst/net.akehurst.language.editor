import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.akehurst.kotlinx.logging.api.LogFunction
import net.akehurst.kotlinx.logging.api.LogLevel
import net.akehurst.kotlinx.logging.api.LoggingManager
import net.akehurst.kotlinx.logging.common.LoggingByConsole
import net.akehurst.language.editor.demo.EditorDemoApplication
import net.akehurst.language.editor.language.service.LanguageServiceDirectExecution

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
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
            ComposeViewport(viewportContainerId = "ComposeTarget") {
                LaunchedEffect(Unit) {
                    document.getElementById("loading-indicator")?.remove()
                }
                gui.content()
            }
        }
    })
}