package com.zahar2.andr.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Event(
    val action: Action,
    val from: Application,
    val over: String,
    val to: Application,
    val timestamp: String,
    val errorDescription: String? = null
) {

    override fun toString(): String = Json.encodeToString(this)
}

fun String.toEvent(): Event {
    val event: Event = Json.decodeFromString(this)
    Instant.parse(event.timestamp)
    return event
}