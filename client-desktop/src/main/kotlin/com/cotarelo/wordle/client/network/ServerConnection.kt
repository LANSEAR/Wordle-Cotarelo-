package com.cotarelo.wordle.client.network

import com.cotarelo.wordle.shared.network.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

/**
 * Cliente de conexión al servidor Wordle
 */
class ServerConnection(
    private val host: String = "localhost",
    private val port: Int = 5678
) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Flow para emitir mensajes recibidos del servidor
    private val _serverMessages = MutableSharedFlow<ServerMessage>(replay = 0)
    val serverMessages = _serverMessages.asSharedFlow()

    var isConnected = false
        private set

    /**
     * Conecta al servidor
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            println("🔌 Conectando a $host:$port...")
            socket = Socket(host, port)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            isConnected = true

            // Iniciar escucha de mensajes
            startListening()

            println("✅ Conectado al servidor")
            true
        } catch (e: Exception) {
            println("❌ Error conectando al servidor: ${e.message}")
            isConnected = false
            false
        }
    }

    /**
     * Desconecta del servidor
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            if (isConnected) {
                sendMessage(ClientMessage.Disconnect)
                delay(100) // Dar tiempo para enviar mensaje
            }
        } catch (e: Exception) {
            println("⚠️  Error al desconectar: ${e.message}")
        } finally {
            cleanup()
        }
    }

    /**
     * Inicia una nueva partida
     */
    suspend fun startGame(
        mode: GameMode,
        rounds: Int,
        wordLength: Int,
        maxAttempts: Int,
        difficulty: Difficulty
    ) {
        sendMessage(
            ClientMessage.StartGame(
                mode = mode,
                rounds = rounds,
                wordLength = wordLength,
                maxAttempts = maxAttempts,
                difficulty = difficulty
            )
        )
    }

    /**
     * Envía un intento de palabra
     */
    suspend fun sendGuess(word: String, attemptNumber: Int) {
        sendMessage(
            ClientMessage.Guess(
                word = word,
                attemptNumber = attemptNumber
            )
        )
    }

    /**
     * Solicita sincronización de records
     */
    suspend fun syncRecords() {
        sendMessage(ClientMessage.SyncRecords)
    }

    /**
     * Envía un mensaje al servidor
     */
    private suspend fun sendMessage(message: ClientMessage) = withContext(Dispatchers.IO) {
        try {
            val jsonText = json.encodeToString(message)
            writer?.write(jsonText)
            writer?.newLine()
            writer?.flush()
        } catch (e: Exception) {
            println("❌ Error enviando mensaje: ${e.message}")
            isConnected = false
        }
    }

    /**
     * Inicia la escucha de mensajes del servidor
     */
    private fun startListening() {
        scope.launch(Dispatchers.IO) {
            try {
                while (isConnected && socket?.isConnected == true) {
                    val line = reader?.readLine()
                    if (line == null) {
                        println("🔌 Servidor cerró la conexión")
                        isConnected = false
                        break
                    }

                    try {
                        val message = json.decodeFromString<ServerMessage>(line)
                        _serverMessages.emit(message)
                    } catch (e: Exception) {
                        println("⚠️  Error deserializando mensaje: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error en escucha de mensajes: ${e.message}")
                isConnected = false
            }
        }
    }

    /**
     * Limpia recursos
     */
    private fun cleanup() {
        try {
            isConnected = false
            scope.cancel()
            reader?.close()
            writer?.close()
            socket?.close()
            println("🧹 Conexión limpiada")
        } catch (e: Exception) {
            println("⚠️  Error limpiando conexión: ${e.message}")
        }
    }
}
