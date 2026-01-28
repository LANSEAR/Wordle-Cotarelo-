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
 * Respuestas PVP del servidor
 */
sealed class PVPServerResponse {
    data class RoomCreated(val data: RoomCreatedResponse) : PVPServerResponse()
    data class RoomList(val data: RoomListResponse) : PVPServerResponse()
    data class RoomJoined(val data: RoomJoinedResponse) : PVPServerResponse()
    data class GameStartedPVP(val data: GameStartedPVPResponse) : PVPServerResponse()
    data class GuessResult(val data: GuessResultResponse) : PVPServerResponse()
    data class OpponentProgress(val data: OpponentProgressResponse) : PVPServerResponse()
    data class RoundWinnerPVP(val data: RoundWinnerPVPResponse) : PVPServerResponse()
    data class GameWinnerPVP(val data: GameWinnerPVPResponse) : PVPServerResponse()
    data class OpponentDisconnected(val data: OpponentDisconnectedResponse) : PVPServerResponse()
    data class RecordsData(val data: RecordsDataResponse) : PVPServerResponse()
    data class Error(val data: ErrorResponse) : PVPServerResponse()
    data class Unknown(val type: String) : PVPServerResponse()
}

/**
 * Cliente de conexión PVP al servidor
 */
class PVPServerConnection(
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

    private val _serverResponses = MutableSharedFlow<PVPServerResponse>(replay = 0)
    val serverResponses = _serverResponses.asSharedFlow()

    var isConnected = false
        private set

    /**
     * Conecta al servidor
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            println("🔌 Conectando a servidor PVP $host:$port...")
            socket = Socket(host, port)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            isConnected = true

            startListening()

            println("✅ Conectado al servidor PVP")
            true
        } catch (e: Exception) {
            println("❌ Error conectando al servidor PVP: ${e.message}")
            e.printStackTrace()
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
                sendMessage("DISCONNECT", "{}")
                delay(100)
            }
        } catch (e: Exception) {
            println("⚠️  Error al desconectar: ${e.message}")
        } finally {
            cleanup()
        }
    }

    /**
     * Crea una nueva sala
     */
    suspend fun createRoom(
        wordLength: Int,
        maxAttempts: Int,
        rounds: Int,
        difficulty: String,
        playerName: String
    ) {
        val request = CreateRoomRequest(
            wordLength = wordLength,
            maxAttempts = maxAttempts,
            rounds = rounds,
            difficulty = difficulty,
            playerName = playerName
        )
        sendMessage("CREATE_ROOM", json.encodeToString(request))
    }

    /**
     * Solicita la lista de salas disponibles
     */
    suspend fun listRooms() {
        sendMessage("LIST_ROOMS", "{}")
    }

    /**
     * Une a una sala existente
     */
    suspend fun joinRoom(roomId: String, playerName: String) {
        val request = JoinRoomRequest(
            roomId = roomId,
            playerName = playerName
        )
        sendMessage("JOIN_ROOM", json.encodeToString(request))
    }

    /**
     * Envía un intento en modo PVP
     */
    suspend fun sendGuessPVP(word: String, attemptNumber: Int) {
        val request = GuessRequest(
            word = word,
            attemptNumber = attemptNumber
        )
        sendMessage("GUESS_PVP", json.encodeToString(request))
    }

    /**
     * Solicita sincronización de records
     */
    suspend fun syncRecords() {
        sendMessage("SYNC_RECORDS", "{}")
    }

    /**
     * Sale de la sala actual
     */
    suspend fun leaveRoom() {
        sendMessage("LEAVE_ROOM", "{}")
    }

    private suspend fun sendMessage(type: String, data: String) = withContext(Dispatchers.IO) {
        try {
            val message = Message(type, data)
            val jsonText = json.encodeToString(message)
            println("📤 Enviando PVP: $type")
            writer?.write(jsonText)
            writer?.newLine()
            writer?.flush()
        } catch (e: Exception) {
            println("❌ Error enviando mensaje PVP: ${e.message}")
            e.printStackTrace()
            isConnected = false
        }
    }

    private fun startListening() {
        scope.launch(Dispatchers.IO) {
            try {
                while (isConnected && socket?.isConnected == true) {
                    val line = reader?.readLine()
                    if (line == null) {
                        println("🔌 Servidor PVP cerró la conexión")
                        isConnected = false
                        break
                    }

                    try {
                        println("📥 Recibido PVP: $line")
                        val message = json.decodeFromString<Message>(line)
                        val response = parseResponse(message)
                        _serverResponses.emit(response)
                    } catch (e: Exception) {
                        println("⚠️  Error deserializando mensaje PVP: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("❌ Error en escucha de mensajes PVP: ${e.message}")
                e.printStackTrace()
                isConnected = false
            }
        }
    }

    private fun parseResponse(message: Message): PVPServerResponse {
        return try {
            when (message.type) {
                "ROOM_CREATED" -> {
                    val data = json.decodeFromString<RoomCreatedResponse>(message.data)
                    PVPServerResponse.RoomCreated(data)
                }
                "ROOM_LIST" -> {
                    val data = json.decodeFromString<RoomListResponse>(message.data)
                    PVPServerResponse.RoomList(data)
                }
                "ROOM_JOINED" -> {
                    val data = json.decodeFromString<RoomJoinedResponse>(message.data)
                    PVPServerResponse.RoomJoined(data)
                }
                "GAME_STARTED_PVP" -> {
                    val data = json.decodeFromString<GameStartedPVPResponse>(message.data)
                    PVPServerResponse.GameStartedPVP(data)
                }
                "GUESS_RESULT" -> {
                    val data = json.decodeFromString<GuessResultResponse>(message.data)
                    PVPServerResponse.GuessResult(data)
                }
                "OPPONENT_PROGRESS" -> {
                    val data = json.decodeFromString<OpponentProgressResponse>(message.data)
                    PVPServerResponse.OpponentProgress(data)
                }
                "ROUND_WINNER_PVP" -> {
                    val data = json.decodeFromString<RoundWinnerPVPResponse>(message.data)
                    PVPServerResponse.RoundWinnerPVP(data)
                }
                "GAME_WINNER_PVP" -> {
                    val data = json.decodeFromString<GameWinnerPVPResponse>(message.data)
                    PVPServerResponse.GameWinnerPVP(data)
                }
                "OPPONENT_DISCONNECTED" -> {
                    val data = json.decodeFromString<OpponentDisconnectedResponse>(message.data)
                    PVPServerResponse.OpponentDisconnected(data)
                }
                "RECORDS_DATA" -> {
                    val data = json.decodeFromString<RecordsDataResponse>(message.data)
                    PVPServerResponse.RecordsData(data)
                }
                "ERROR" -> {
                    val data = json.decodeFromString<ErrorResponse>(message.data)
                    PVPServerResponse.Error(data)
                }
                else -> PVPServerResponse.Unknown(message.type)
            }
        } catch (e: Exception) {
            println("Error parseando respuesta PVP ${message.type}: ${e.message}")
            e.printStackTrace()
            PVPServerResponse.Error(ErrorResponse("Error parseando respuesta: ${e.message}"))
        }
    }

    private fun cleanup() {
        try {
            isConnected = false
            scope.cancel()
            reader?.close()
            writer?.close()
            socket?.close()
            println("🧹 Conexión PVP limpiada")
        } catch (e: Exception) {
            println("⚠️  Error limpiando conexión PVP: ${e.message}")
        }
    }
}
