package com.cotarelo.wordle.server.network

import com.cotarelo.wordle.server.data.RecordsManager
import com.cotarelo.wordle.server.game.GameSession
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

/**
 * Maneja la comunicación con un cliente conectado usando protocolo simple
 */
class SimpleClientHandler(
    private val clientSocket: Socket,
    private val recordsManager: RecordsManager,
    private val clientId: Int
) {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

    private var currentGame: GameSession? = null
    private var playerName: String = "Player$clientId"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Procesa los mensajes del cliente
     */
    suspend fun handle() = withContext(Dispatchers.IO) {
        try {
            println("✅ Cliente #$clientId conectado desde ${clientSocket.inetAddress.hostAddress}")

            while (!clientSocket.isClosed) {
                val line = reader.readLine() ?: break

                try {
                    // Leer el mensaje wrapper
                    val message = json.decodeFromString<Message>(line)
                    handleMessage(message)
                } catch (e: Exception) {
                    println("⚠️  Error procesando mensaje: ${e.message}")
                    e.printStackTrace()
                    sendError("Error procesando mensaje: ${e.message}")
                }
            }
        } catch (e: SocketException) {
            println("🔌 Cliente #$clientId desconectado")
        } catch (e: Exception) {
            println("❌ Error con cliente #$clientId: ${e.message}")
            e.printStackTrace()
        } finally {
            cleanup()
        }
    }

    private suspend fun handleMessage(message: Message) {
        when (message.type) {
            "START_GAME" -> {
                val request = json.decodeFromString<StartGameRequest>(message.data)
                handleStartGame(request)
            }
            "GUESS" -> {
                val request = json.decodeFromString<GuessRequest>(message.data)
                handleGuess(request)
            }
            "SYNC_RECORDS" -> {
                handleSyncRecords()
            }
            "DISCONNECT" -> {
                clientSocket.close()
            }
            else -> {
                sendError("Tipo de mensaje desconocido: ${message.type}")
            }
        }
    }

    private suspend fun handleStartGame(request: StartGameRequest) {
        println("🎮 Cliente #$clientId inicia partida ${request.mode}")

        // Actualizar playerName si se proporcionó en el request
        if (request.playerName.isNotEmpty()) {
            playerName = request.playerName
        }

        val mode = if (request.mode == "PVE") GameMode.PVE else GameMode.PVP
        val difficulty = when (request.difficulty) {
            "EASY" -> Difficulty.EASY
            "HARD" -> Difficulty.HARD
            "MIXTA" -> Difficulty.MIXTA
            else -> Difficulty.NORMAL
        }

        currentGame = GameSession(
            mode = mode,
            rounds = request.rounds,
            wordLength = request.wordLength,
            maxAttempts = request.maxAttempts,
            difficulty = difficulty,
            playerName = playerName,
            timerSeconds = request.timerSeconds
        )

        val response = GameStartedResponse(
            gameId = currentGame!!.gameId,
            wordLength = request.wordLength,
            maxAttempts = request.maxAttempts,
            rounds = request.rounds
        )

        sendMessage("GAME_STARTED", json.encodeToString(response))
    }

    private suspend fun handleGuess(request: GuessRequest) {
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

        // Si el modo es PVE, la IA hace su turno
        if (game.mode == GameMode.PVE && !game.isRoundOver()) {
            // La IA debe jugar más rápido que el temporizador del jugador
            // Si hay temporizador, usar 70% del tiempo; si no hay, usar 500ms
            val aiDelay = if (game.timerSeconds > 0) {
                (game.timerSeconds * 700L).coerceAtLeast(300L)  // Mínimo 300ms
            } else {
                500L
            }
            delay(aiDelay)
            val aiResult = game.processAITurn()

            if (aiResult != null) {
                val aiResponse = AIMoveResponse(
                    word = aiResult.guess,
                    attemptNumber = aiResult.attempts,
                    result = aiResult.evaluation.map { tileStateToString(it) }
                )
                sendMessage("AI_MOVE", json.encodeToString(aiResponse))
            }
        }

        // Verificar si la ronda terminó
        if (game.isRoundOver()) {
            val roundResult = game.getRoundWinner()

            val roundResponse = RoundWinnerResponse(
                winner = roundResult.winner.toString(),
                attempts = roundResult.playerAttempts,
                solution = roundResult.solution
            )

            sendMessage("ROUND_WINNER", json.encodeToString(roundResponse))

            // Verificar si el juego completo terminó
            if (game.isGameOver()) {
                val gameResult = game.getGameWinner()
                val stats = game.getStats()

                val gameResponse = GameWinnerResponse(
                    winner = gameResult.winner.toString(),
                    playerRounds = gameResult.playerRounds,
                    aiRounds = gameResult.aiRounds
                )

                sendMessage("GAME_WINNER", json.encodeToString(gameResponse))

                // Actualizar records
                recordsManager.updatePlayerStats(
                    playerName = playerName,
                    won = gameResult.winner == Winner.PLAYER,
                    attempts = stats.playerAttempts,
                    wordsGuessed = stats.playerWordsGuessed,
                    totalWords = stats.totalWordsAttempted
                )

                println("🏆 Partida terminada - Ganador: ${gameResult.winner}")
                currentGame = null
            } else {
                // Iniciar siguiente ronda
                delay(1000)
                game.startNewRound()
            }
        }
    }

    private suspend fun handleSyncRecords() {
        val records = recordsManager.getRecords()
        val response = RecordsDataResponse(records)
        sendMessage("RECORDS_DATA", json.encodeToString(response))
        println("📊 Enviando records a cliente #$clientId")
    }

    private suspend fun sendMessage(type: String, data: String) = withContext(Dispatchers.IO) {
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
            scope.cancel()
            reader.close()
            writer.close()
            clientSocket.close()
            println("🧹 Cliente #$clientId desconectado y limpiado")
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
}
