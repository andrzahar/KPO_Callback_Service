package com.zahar2.andr.connections

import com.zahar2.andr.data.Event
import com.zahar2.andr.data.toEvent
import io.ktor.server.websocket.*

class LoggerConnection: Connection<Event>() {

    override suspend fun onNewConnection(session: DefaultWebSocketServerSession) {

    }

    override fun String.toType(): Event = this.toEvent()

    override suspend fun update(data: Event) {
        broadcast(data)
    }
}