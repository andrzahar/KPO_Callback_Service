package com.zahar2.andr.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Application(
    val author: String,
    val language: String
) {

    override fun toString(): String = Json.encodeToString(this)
}

fun String.toApplication(): Application = Json.decodeFromString(this)
