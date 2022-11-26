package com.zahar2.andr.connections

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet

abstract class Connection<T> {

    private val connections = Collections.synchronizedSet<DefaultWebSocketServerSession>(LinkedHashSet())

    protected abstract suspend fun onNewConnection(session: DefaultWebSocketServerSession)

    protected abstract fun String.toType(): T

    abstract suspend fun update(data: T)

    suspend fun update(data: String) = update(data.toType())

    suspend fun addConnection(session: DefaultWebSocketServerSession) {
        connections += session
        onNewConnection(session)
    }

    protected suspend fun broadcast(data: T) {
        connections.forEach {
            it.send(data)
        }
    }

    fun removingConnection(session: DefaultWebSocketServerSession) {
        connections.remove(session)
    }

    protected suspend fun DefaultWebSocketServerSession.send(data: T) {
        send(data.toString())
    }
}