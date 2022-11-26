package com.zahar2.andr.plugins

import com.zahar2.andr.connections.Connection
import com.zahar2.andr.connections.LoggerConnection
import com.zahar2.andr.data.Event
import com.zahar2.andr.data.toEvent
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun <D> Routing.simpleWebSocket(path: String, connection: Connection<D>, closeForInput: Boolean = false) {
    webSocket(path) {
        call.application.log.info("$path: Adding session!")
        connection.addConnection(this)
        try {
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                if (!closeForInput) connection.update(receivedText)
            }
        } catch (e: Exception) {
            call.application.log.error("$path - ${e.cause}")
        } finally {
            call.application.log.warn("$path: Removing!")
            connection.removingConnection(this)
        }
    }
}

inline fun <reified D : Any> Routing.simplePost(
    path: String,
    crossinline toObject: String.() -> D,
    crossinline onSuccess: suspend (D) -> Unit
) {
    post(path) {
        try {
            val stringData = call.receiveText()
            val data = stringData.toObject()
            call.respond(HttpStatusCode.OK)
            call.application.log.info("$path - Post successful!\nData: $data")
            onSuccess(data)
        } catch (e: Exception) {
            call.application.log.error("$path - ${e.cause?: e.localizedMessage}")
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}

fun Application.configure() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        json()
    }

    routing {
        val loggerConnection = LoggerConnection()
        simpleWebSocket("/logger", loggerConnection, true)

        val onSuccess: suspend (Event) -> Unit = { event ->
            loggerConnection.update(event)
        }

        simplePost("/callback", String::toEvent, onSuccess)
    }
}
