package com.zahar2.andr.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class Action {
    SEND, RECEIVE;

    override fun toString(): String = Json.encodeToString(this)
}

fun String.toAction(): Action = Json.decodeFromString(this)