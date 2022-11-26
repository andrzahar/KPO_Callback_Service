package com.zahar2.andr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.zahar2.andr.plugins.*

fun main() {
    embeddedServer(Netty, port = 23052, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configure()
}
