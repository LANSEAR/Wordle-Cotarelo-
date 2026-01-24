package com.cotarelo.wordle.shared.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wrapper para mensajes con tipo explícito
 */
@Serializable
data class Message(
    val type: String,
    val data: String
)

/**
 * JSON configurado para el protocolo
 */
val protocolJson = Json {
    prettyPrint = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}
