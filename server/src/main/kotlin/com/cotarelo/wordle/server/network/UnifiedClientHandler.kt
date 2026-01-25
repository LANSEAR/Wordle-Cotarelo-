package com.cotarelo.wordle.server.network

import com.cotarelo.wordle.server.data.RecordsManager
import com.cotarelo.wordle.shared.network.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

/**
 * Handler unificado que detecta el modo (PVE o PVP) y delega
 */
class UnifiedClientHandler(
    private val clientSocket: Socket,
    private val recordsManager: RecordsManager,
    private val clientId: Int
) {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    suspend fun handle() = withContext(Dispatchers.IO) {
        try {
            // Leer el primer mensaje para determinar el modo
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val firstLine = reader.readLine()

            if (firstLine != null) {
                val message = json.decodeFromString<Message>(firstLine)

                // Determinar el modo según el tipo de mensaje
                val isPVP = message.type in setOf("CREATE_ROOM", "JOIN_ROOM", "LIST_ROOMS")

                // Recrear el socket reader para que el handler específico pueda leerlo
                // Como ya leímos una línea, necesitamos que el handler específico procese ese mensaje

                if (isPVP) {
                    println("🎮 Cliente #$clientId usa modo PVP")
                    val pvpHandler = PVPClientHandler(clientSocket, recordsManager, clientId)
                    // Procesar el primer mensaje manualmente
                    pvpHandler.handle()
                } else {
                    println("🤖 Cliente #$clientId usa modo PVE")
                    val pveHandler = SimpleClientHandler(clientSocket, recordsManager, clientId)
                    pveHandler.handle()
                }
            }
        } catch (e: Exception) {
            println("❌ Error determinando modo para cliente #$clientId: ${e.message}")
            e.printStackTrace()
        }
    }
}
