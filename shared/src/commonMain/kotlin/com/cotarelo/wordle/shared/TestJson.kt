package com.cotarelo.wordle.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Ping(val msg: String)

object TestJson {
    private val json = Json { prettyPrint = true }

    fun encode(): String = json.encodeToString(Ping.serializer(), Ping("pong"))
}
