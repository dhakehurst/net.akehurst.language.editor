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

package net.akehurst.language.editor.web.server

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.generateNonce
import java.io.File

fun main(args: Array<String>) {
    println("PWD: " + File(".").absolutePath)
    val application = EditorApplication
    application.start()
}

object EditorApplication {

    val server = Server("0.0.0.0", 9999)

    fun start() {
        server.start()
    }

}

class Server(
        val host:String,
        val port:Int
) {

    fun start() {
        val server = embeddedServer(Jetty, port = port, host = host) {
            install(DefaultHeaders)
            install(CallLogging)
            install(Routing)
            install(Sessions) {
                cookie<String>("SESSION_ID")
            }
            intercept(ApplicationCallPipeline.Features) {
                call.sessions.set<String>(generateNonce())
            }
            install(SinglePageApplication) {
                defaultPage = "index.html"
                folderPath = "/"
                spaRoute = ""
                useFiles = false
            }
        }

        server.start(true)
    }


}