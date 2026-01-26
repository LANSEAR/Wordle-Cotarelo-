package com.cotarelo.wordle.server.network

import com.cotarelo.wordle.server.data.RecordsManager
import com.cotarelo.wordle.server.game.GameSession
import com.cotarelo.wordle.server.game.RoomManager
import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.network.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

/**
 * Maneja la comunicación con clientes en modo PVP
 */
class PVPClientHandler(
    private val clientSocket: Socket,
    private val recordsManager: RecordsManager,
    val clientId: Int
) {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Para modo PVE
    private var currentGame: GameSession? = null
    private var playerName: String = "Player$clientId"

    // Registro estático de todos los handlers activos para notificaciones
    companion object {
        private val activeHandlers = ConcurrentHashMap<Int, PVPClientHandler>()

        fun register(handler: PVPClientHandler) {
            activeHandlers[handler.clientId] = handler
        }

        fun unregister(clientId: Int) {
            activeHandlers.remove(clientId)
        }

        suspend fun sendToClient(clientId: Int, type: String, data: String) {
            activeHandlers[clientId]?.sendMessage(type, data)
        }
    }

    /**
     * Procesa los mensajes del cliente
     */
    suspend fun handle() = withContext(Dispatchers.IO) {
        register(this@PVPClientHandler)

        try {
            println("✅ Cliente PVP #$clientId conectado desde ${clientSocket.inetAddress.hostAddress}")

            while (!clientSocket.isClosed) {
                val line = reader.readLine() ?: break

                try {
                    val message = json.decodeFromString<Message>(line)
                    handleMessage(message)
                } catch (e: Exception) {
                    println("⚠️  Error procesando mensaje: ${e.message}")
                    e.printStackTrace()
                    sendError("Error procesando mensaje: ${e.message}")
                }
            }
        } catch (e: SocketException) {
            println("🔌 Cliente PVP #$clientId desconectado")
        } catch (e: Exception) {
            println("❌ Error con cliente #$clientId: ${e.message}")
            e.printStackTrace()
        } finally {
            cleanup()
        }
    }

    private suspend fun handleMessage(message: Message) {
        when (message.type) {
            "CREATE_ROOM" -> {
                val request = json.decodeFromString<CreateRoomRequest>(message.data)
                handleCreateRoom(request)
            }
            "LIST_ROOMS" -> {
                handleListRooms()
            }
            "JOIN_ROOM" -> {
                val request = json.decodeFromString<JoinRoomRequest>(message.data)
                handleJoinRoom(request)
            }
            "GUESS_PVP" -> {
                val request = json.decodeFromString<GuessRequest>(message.data)
                handleGuessPVP(request)
            }
            "LEAVE_ROOM" -> {
                handleLeaveRoom()
            }
            "DISCONNECT" -> {
                clientSocket.close()
            }
            // Mensajes PVE (modo VS IA)
            "START_GAME" -> {
                val request = json.decodeFromString<StartGameRequest>(message.data)
                handleStartGamePVE(request)
            }
            "GUESS" -> {
                val request = json.decodeFromString<GuessRequest>(message.data)
                handleGuessPVE(request)
            }
            "SYNC_RECORDS" -> {
                handleSyncRecords()
            }
            else -> {
                sendError("Tipo de mensaje desconocido: ${message.type}")
            }
        }
    }

    private suspend fun handleCreateRoom(request: CreateRoomRequest) {
        try {
            val room = RoomManager.createRoom(
                wordLength = request.wordLength,
                maxAttempts = request.maxAttempts,
                rounds = request.rounds,
                difficulty = request.difficulty,
                creatorId = clientId,
                creatorName = request.playerName
            )

            val response = RoomCreatedResponse(
                roomId = room.roomId,
                wordLength = room.wordLength,
                maxAttempts = room.maxAttempts,
                rounds = room.rounds,
                difficulty = room.difficulty
            )

            sendMessage("ROOM_CREATED", json.encodeToString(response))
            println("🏠 Cliente #$clientId creó sala ${room.roomId}")
        } catch (e: Exception) {
            sendError("Error creando sala: ${e.message}")
        }
    }

    private suspend fun handleListRooms() {
        val rooms = RoomManager.getAvailableRooms()
        val roomDtos = rooms.map { room ->
            RoomInfoDto(
                roomId = room.roomId,
                wordLength = room.wordLength,
                maxAttempts = room.maxAttempts,
                rounds = room.rounds,
                difficulty = room.difficulty,
                playerCount = room.playerCount,
                creatorName = room.creatorName
            )
        }

        val response = RoomListResponse(roomDtos)
        sendMessage("ROOM_LIST", json.encodeToString(response))
    }

    private suspend fun handleJoinRoom(request: JoinRoomRequest) {
        val room = RoomManager.joinRoom(request.roomId, clientId, request.playerName)

        if (room == null) {
            sendError("No se pudo unir a la sala")
            return
        }

        val isPlayer1 = room.player1Id == clientId
        val opponentName = if (isPlayer1) room.player2Name else room.player1Name

        val response = RoomJoinedResponse(
            roomId = room.roomId,
            opponentName = opponentName ?: "Esperando...",
            isPlayer1 = isPlayer1,
            wordLength = room.wordLength,
            maxAttempts = room.maxAttempts,
            rounds = room.rounds,
            difficulty = room.difficulty
        )

        sendMessage("ROOM_JOINED", json.encodeToString(response))

        // Si la sala está llena, notificar a ambos jugadores que el juego comienza
        if (room.isFull() && room.gameStarted) {
            notifyGameStart(room.roomId)
        }
    }

    private suspend fun notifyGameStart(roomId: String) {
        val room = RoomManager.getPlayerRoom(clientId) ?: return

        // Notificar al jugador 1
        room.player1Id?.let { p1Id ->
            val response = GameStartedPVPResponse(
                roomId = roomId,
                opponentName = room.player2Name ?: "Jugador 2"
            )
            sendToClient(p1Id, "GAME_STARTED_PVP", json.encodeToString(response))
        }

        // Notificar al jugador 2
        room.player2Id?.let { p2Id ->
            val response = GameStartedPVPResponse(
                roomId = roomId,
                opponentName = room.player1Name ?: "Jugador 1"
            )
            sendToClient(p2Id, "GAME_STARTED_PVP", json.encodeToString(response))
        }

        println("🎮 Juego PVP iniciado en sala $roomId")
    }

    private suspend fun handleGuessPVP(request: GuessRequest) {
        val room = RoomManager.getPlayerRoom(clientId)
        if (room == null) {
            sendError("No estás en ninguna sala")
            return
        }

        val result = room.processGuess(clientId, request.word)
        if (result == null) {
            sendError("Error procesando intento")
            return
        }

        // Enviar resultado al jugador
        val response = GuessResultResponse(
            word = request.word,
            result = result.evaluation.map { tileStateToString(it) },
            isValid = result.valid,
            message = result.message
        )
        sendMessage("GUESS_RESULT", json.encodeToString(response))

        // Notificar progreso al oponente
        val opponentId = RoomManager.getOpponentId(clientId)
        if (opponentId != null) {
            val opponentProgress = room.getOpponentProgress(opponentId)
            if (opponentProgress != null) {
                val progressResponse = OpponentProgressResponse(
                    attempts = opponentProgress.attempts,
                    won = opponentProgress.won
                )
                sendToClient(opponentId, "OPPONENT_PROGRESS", json.encodeToString(progressResponse))
            }
        }

        // Verificar si la ronda terminó
        if (room.isRoundOver()) {
            delay(1000) // Pequeña pausa para que se vea el último intento
            notifyRoundEnd(room)

            if (room.isGameOver()) {
                delay(2000)
                notifyGameEnd(room)
            } else {
                delay(2000)
                room.nextRound()
            }
        }
    }

    private suspend fun notifyRoundEnd(room: com.cotarelo.wordle.server.game.GameRoom) {
        val roundWinner = room.getRoundWinner()

        // Notificar al jugador 1
        room.player1Id?.let { p1Id ->
            val youWon = roundWinner.winner == "PLAYER1" ||
                        (roundWinner.winner == "DRAW" && roundWinner.player1Attempts < roundWinner.player2Attempts)
            val response = RoundWinnerPVPResponse(
                winner = roundWinner.winner,
                player1Attempts = roundWinner.player1Attempts,
                player2Attempts = roundWinner.player2Attempts,
                solution = roundWinner.solution,
                youWon = youWon
            )
            sendToClient(p1Id, "ROUND_WINNER_PVP", json.encodeToString(response))
        }

        // Notificar al jugador 2
        room.player2Id?.let { p2Id ->
            val youWon = roundWinner.winner == "PLAYER2" ||
                        (roundWinner.winner == "DRAW" && roundWinner.player2Attempts < roundWinner.player1Attempts)
            val response = RoundWinnerPVPResponse(
                winner = roundWinner.winner,
                player1Attempts = roundWinner.player1Attempts,
                player2Attempts = roundWinner.player2Attempts,
                solution = roundWinner.solution,
                youWon = youWon
            )
            sendToClient(p2Id, "ROUND_WINNER_PVP", json.encodeToString(response))
        }
    }

    private suspend fun notifyGameEnd(room: com.cotarelo.wordle.server.game.GameRoom) {
        val gameWinner = room.getGameWinner()

        // Notificar al jugador 1
        room.player1Id?.let { p1Id ->
            val youWon = gameWinner.winner == "PLAYER1"
            val response = GameWinnerPVPResponse(
                winner = gameWinner.winner,
                player1Rounds = gameWinner.player1Rounds,
                player2Rounds = gameWinner.player2Rounds,
                player1Name = gameWinner.player1Name,
                player2Name = gameWinner.player2Name,
                youWon = youWon
            )
            sendToClient(p1Id, "GAME_WINNER_PVP", json.encodeToString(response))

            // Actualizar records
            recordsManager.updatePlayerStats(
                playerName = gameWinner.player1Name,
                won = youWon,
                attempts = 0,
                wordsGuessed = 0,
                totalWords = 0
            )
        }

        // Notificar al jugador 2
        room.player2Id?.let { p2Id ->
            val youWon = gameWinner.winner == "PLAYER2"
            val response = GameWinnerPVPResponse(
                winner = gameWinner.winner,
                player1Rounds = gameWinner.player1Rounds,
                player2Rounds = gameWinner.player2Rounds,
                player1Name = gameWinner.player1Name,
                player2Name = gameWinner.player2Name,
                youWon = youWon
            )
            sendToClient(p2Id, "GAME_WINNER_PVP", json.encodeToString(response))

            // Actualizar records
            recordsManager.updatePlayerStats(
                playerName = gameWinner.player2Name,
                won = youWon,
                attempts = 0,
                wordsGuessed = 0,
                totalWords = 0
            )
        }
    }

    private suspend fun handleLeaveRoom() {
        // Obtener información de la sala ANTES de remover al jugador
        val room = RoomManager.getPlayerRoom(clientId)
        if (room != null) {
            val opponentId = if (room.player1Id == clientId) room.player2Id else room.player1Id
            val leavingPlayerName = if (room.player1Id == clientId) room.player1Name else room.player2Name
            val gameWasStarted = room.gameStarted
            val gameWasOver = room.isGameOver()

            // Guardar información del juego antes de remover
            val player1Name = room.player1Name
            val player2Name = room.player2Name
            val currentGameState = if (gameWasStarted) room.getGameWinner() else null

            // Ahora sí remover al jugador
            RoomManager.removePlayer(clientId)

            if (opponentId != null) {
                // Si el juego ha comenzado y no ha terminado, declarar victoria automática para el oponente
                if (gameWasStarted && !gameWasOver) {
                    // El jugador que abandonó pierde, el que se queda gana
                    val winner = if (room.player1Id == clientId) "PLAYER2" else "PLAYER1"

                    // Crear respuesta con el jugador restante como ganador absoluto
                    val gameWinnerResponse = GameWinnerPVPResponse(
                        winner = winner,
                        player1Rounds = currentGameState?.player1Rounds ?: 0,
                        player2Rounds = currentGameState?.player2Rounds ?: 0,
                        player1Name = player1Name ?: "Jugador 1",
                        player2Name = player2Name ?: "Jugador 2",
                        youWon = true // El jugador que recibe este mensaje siempre gana
                    )
                    sendToClient(opponentId, "GAME_WINNER_PVP", json.encodeToString(gameWinnerResponse))
                    println("🏆 Victoria por abandono para jugador #$opponentId en sala ${room.roomId}")
                } else {
                    // Solo notificar desconexión si el juego no había empezado o ya terminó
                    val response = OpponentDisconnectedResponse(leavingPlayerName ?: "Oponente")
                    sendToClient(opponentId, "OPPONENT_DISCONNECTED", json.encodeToString(response))
                }
            }
        }
    }

    suspend fun sendMessage(type: String, data: String) = withContext(Dispatchers.IO) {
        try {
            val message = Message(type, data)
            val jsonText = json.encodeToString(message)
            writer.write(jsonText)
            writer.newLine()
            writer.flush()
        } catch (e: Exception) {
            println("❌ Error enviando mensaje a cliente #$clientId: ${e.message}")
        }
    }

    private suspend fun sendError(errorMessage: String) {
        val error = ErrorResponse(errorMessage)
        sendMessage("ERROR", json.encodeToString(error))
    }

    private fun cleanup() {
        try {
            // Remover jugador de cualquier sala
            RoomManager.removePlayer(clientId)

            // Limpiar juego PVE si está activo
            currentGame = null

            unregister(clientId)
            scope.cancel()
            reader.close()
            writer.close()
            clientSocket.close()
            println("🧹 Cliente PVP #$clientId desconectado y limpiado")
        } catch (e: Exception) {
            println("⚠️  Error limpiando cliente #$clientId: ${e.message}")
        }
    }

    private fun tileStateToString(state: TileState): String {
        return when (state) {
            TileState.Empty -> "EMPTY"
            TileState.Absent -> "ABSENT"
            TileState.Present -> "PRESENT"
            TileState.Correct -> "CORRECT"
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS PARA MODO PVE (VS IA)
    // ═══════════════════════════════════════════════════════════

    private suspend fun handleStartGamePVE(request: StartGameRequest) {
        println("🎮 Cliente #$clientId inicia partida PVE ${request.difficulty}")

        val mode = when (request.mode) {
            "PVE" -> GameMode.PVE
            "PVP" -> GameMode.PVP
            else -> GameMode.PVE
        }

        val difficulty = when (request.difficulty) {
            "EASY" -> Difficulty.EASY
            "NORMAL" -> Difficulty.NORMAL
            "HARD" -> Difficulty.HARD
            else -> Difficulty.NORMAL
        }

        currentGame = GameSession(
            mode = mode,
            rounds = request.rounds,
            wordLength = request.wordLength,
            maxAttempts = request.maxAttempts,
            difficulty = difficulty,
            playerName = playerName
        )

        val response = GameStartedResponse(
            gameId = currentGame!!.gameId,
            wordLength = request.wordLength,
            maxAttempts = request.maxAttempts,
            rounds = request.rounds
        )

        sendMessage("GAME_STARTED", json.encodeToString(response))
    }

    private suspend fun handleGuessPVE(request: GuessRequest) {
        val game = currentGame
        if (game == null) {
            sendError("No hay partida activa")
            return
        }

        // Procesar intento del jugador
        val playerResult = game.processPlayerGuess(request.word)

        val response = GuessResultResponse(
            word = request.word,
            result = playerResult.evaluation.map { tileStateToString(it) },
            isValid = playerResult.valid,
            message = playerResult.message
        )
        sendMessage("GUESS_RESULT", json.encodeToString(response))

        // Verificar si la ronda terminó después del intento del jugador
        if (game.isRoundOver()) {
            handleRoundEnd()
            return
        }

        // Turno de la IA después del jugador (sistema de turnos)
        if (game.mode == GameMode.PVE) {
            delay(1000) // Pequeño delay para que se vea el resultado del jugador

            val aiResult = game.processAITurn()
            if (aiResult != null) {
                val aiMoveResponse = AIMoveResponse(
                    word = aiResult.guess,
                    attemptNumber = aiResult.attempts,
                    result = aiResult.evaluation.map { tileStateToString(it) }
                )
                sendMessage("AI_MOVE", json.encodeToString(aiMoveResponse))

                // Verificar si la ronda terminó después del turno de la IA
                if (game.isRoundOver()) {
                    handleRoundEnd()
                }
            }
        }
    }

    /**
     * Maneja el fin de una ronda
     */
    private suspend fun handleRoundEnd() {
        val game = currentGame ?: return
        val roundResult = game.getRoundWinner()

        val roundWinnerResponse = RoundWinnerResponse(
            winner = roundResult.winner.name,
            attempts = roundResult.playerAttempts,
            solution = roundResult.solution
        )
        sendMessage("ROUND_WINNER", json.encodeToString(roundWinnerResponse))

        // Verificar si el juego completo terminó
        if (game.isGameOver()) {
            val gameResult = game.getGameWinner()
            val stats = game.getStats()

            val gameWinnerResponse = GameWinnerResponse(
                winner = gameResult.winner.name,
                playerRounds = gameResult.playerRounds,
                aiRounds = gameResult.aiRounds
            )
            sendMessage("GAME_WINNER", json.encodeToString(gameWinnerResponse))

            // Actualizar records
            recordsManager.updatePlayerStats(
                playerName = playerName,
                won = gameResult.winner == Winner.PLAYER,
                attempts = stats.playerAttempts,
                wordsGuessed = stats.playerWordsGuessed,
                totalWords = stats.totalWordsAttempted
            )

            println("🏆 Partida PVE terminada - Ganador: ${gameResult.winner}")
            currentGame = null
        } else {
            // Iniciar siguiente ronda
            delay(1000)
            game.startNewRound()
        }
    }

    private suspend fun handleSyncRecords() {
        val records = recordsManager.getRecords()
        val recordsResponse = RecordsDataResponse(records)
        sendMessage("RECORDS_DATA", json.encodeToString(recordsResponse))
        println("📊 Enviando records a cliente #$clientId")
    }
}
