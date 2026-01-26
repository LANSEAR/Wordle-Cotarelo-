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
    private val _serverMessages = MutableSharedFlow<ServerResponse>(replay = 0)
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
                sendRawMessage("DISCONNECT", "{}")
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
        val request = StartGameRequest(
            mode = mode.name,
            rounds = rounds,
            wordLength = wordLength,
            maxAttempts = maxAttempts,
            difficulty = difficulty.name
        )
        sendRawMessage("START_GAME", json.encodeToString(request))
    }

    /**
     * Envía un intento de palabra
     */
    suspend fun sendGuess(word: String, attemptNumber: Int) {
        val request = GuessRequest(
            word = word,
            attemptNumber = attemptNumber
        )
        sendRawMessage("GUESS", json.encodeToString(request))
    }

    /**
     * Solicita sincronización de records
     */
    suspend fun syncRecords() {
        sendRawMessage("SYNC_RECORDS", "{}")
    }

    /**
     * Envía un mensaje con formato Message(type, data)
     */
    private suspend fun sendRawMessage(type: String, data: String) = withContext(Dispatchers.IO) {
        try {
            val message = Message(type, data)
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
                        println("📥 Recibido PVE: $line")
                        val message = json.decodeFromString<Message>(line)
                        val response = parseResponse(message)
                        _serverMessages.emit(response)
                    } catch (e: Exception) {
                        println("⚠️  Error deserializando mensaje: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("❌ Error en escucha de mensajes: ${e.message}")
                isConnected = false
            }
        }
    }

    private fun parseResponse(message: Message): ServerResponse {
        return try {
            when (message.type) {
                "GAME_STARTED" -> {
                    val data = json.decodeFromString<GameStartedResponse>(message.data)
                    ServerResponse.GameStarted(data)
                }
                "GUESS_RESULT" -> {
                    val data = json.decodeFromString<GuessResultResponse>(message.data)
                    ServerResponse.GuessResult(data)
                }
                "AI_MOVE" -> {
                    val data = json.decodeFromString<AIMoveResponse>(message.data)
                    ServerResponse.AIMove(data)
                }
                "ROUND_WINNER" -> {
                    val data = json.decodeFromString<RoundWinnerResponse>(message.data)
                    ServerResponse.RoundWinner(data)
                }
                "GAME_WINNER" -> {
                    val data = json.decodeFromString<GameWinnerResponse>(message.data)
                    ServerResponse.GameWinner(data)
                }
                "RECORDS_DATA" -> {
                    val data = json.decodeFromString<RecordsDataResponse>(message.data)
                    ServerResponse.RecordsData(data)
                }
                "ERROR" -> {
                    val data = json.decodeFromString<ErrorResponse>(message.data)
                    ServerResponse.Error(data)
                }
                else -> ServerResponse.Unknown(message.type)
            }
        } catch (e: Exception) {
            println("Error parseando respuesta ${message.type}: ${e.message}")
            e.printStackTrace()
            ServerResponse.Error(ErrorResponse("Error parseando respuesta: ${e.message}"))
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
