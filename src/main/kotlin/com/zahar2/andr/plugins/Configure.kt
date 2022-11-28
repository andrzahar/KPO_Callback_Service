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
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.respond(status: HttpStatusCode, message: String, debug: Boolean) {
    if (debug) {
        call.respond(status, message)
    } else {
        call.respond(status)
    }
}

fun logText(path: String, message: String?) = "$path: - $message"

fun ApplicationCall.logInfo(path: String, message: String?) = application.log.info(logText(path, message))

fun ApplicationCall.logWarn(path: String, message: String?) = application.log.warn(logText(path, message))

fun ApplicationCall.logError(path: String, message: String?) = application.log.error(logText(path, message))

fun <D> Routing.simpleWebSocket(path: String, connection: Connection<D>, closeForInput: Boolean = false) {
    webSocket(path) {
        call.logInfo(path, "Adding session!")
        connection.addConnection(this)
        try {
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                if (!closeForInput) connection.update(receivedText)
            }
        } catch (e: Exception) {
            call.logError(path, e.cause?.localizedMessage)
        } finally {
            call.logWarn(path, "Removing session!")
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
        val debug = call.parameters["debug"].toBoolean()
        try {
            val stringData = call.receiveText()
            val data = stringData.toObject()

            val message = "Post successful!\nData: $data"
            call.respond(HttpStatusCode.OK)
            call.logInfo(path, message)

            onSuccess(data)
        } catch (e: Exception) {
            val message =
                "Post error! Because: ${e.localizedMessage}. First of all, check that you receive correct JSON and timestamp in correct format (RFC3339)."
            call.logError(path, message)
            respond(HttpStatusCode.BadRequest, message, debug)
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
