package com.cotarelo.wordle.server.network

import com.cotarelo.wordle.server.data.RecordsManager
import com.cotarelo.wordle.server.game.GameSession
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
 * Maneja la comunicación con un cliente conectado
 */
class ClientHandler(
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
            sendMessage(ServerMessage.GameStarted("welcome", 5, 6, 1))

            while (!clientSocket.isClosed) {
                val line = reader.readLine() ?: break

                try {
                    val message = json.decodeFromString<ClientMessage>(line)
                    handleClientMessage(message)
                } catch (e: Exception) {
                    println("⚠️  Error deserializando mensaje del cliente #$clientId: ${e.message}")
                    sendError("Mensaje no válido")
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

    /**
     * Procesa un mensaje específico del cliente
     */
    private suspend fun handleClientMessage(message: ClientMessage) {
        when (message) {
            is ClientMessage.StartGame -> handleStartGame(message)
            is ClientMessage.Guess -> handleGuess(message)
            is ClientMessage.SyncRecords -> handleSyncRecords()
            is ClientMessage.Disconnect -> {
                println("👋 Cliente #$clientId solicitó desconexión")
                clientSocket.close()
            }
        }
    }

    /**
     * Inicia una nueva partida
     */
    private suspend fun handleStartGame(message: ClientMessage.StartGame) {
        println("🎮 Cliente #$clientId inicia partida ${message.mode}")

        currentGame = GameSession(
            mode = message.mode,
            rounds = message.rounds,
            wordLength = message.wordLength,
            maxAttempts = message.maxAttempts,
            difficulty = message.difficulty,
            playerName = playerName
        )

        sendMessage(
            ServerMessage.GameStarted(
                gameId = currentGame!!.gameId,
                wordLength = message.wordLength,
                maxAttempts = message.maxAttempts,
                rounds = message.rounds
            )
        )
    }

    /**
     * Procesa un intento del jugador
     */
    private suspend fun handleGuess(message: ClientMessage.Guess) {
        val game = currentGame
        if (game == null) {
            sendError("No hay partida activa")
            return
        }

        // Procesar intento del jugador
        val playerResult = game.processPlayerGuess(message.word)

        sendMessage(
            ServerMessage.GuessResult(
                word = message.word,
                result = playerResult.evaluation.map { it.toDto() },
                isValid = playerResult.valid,
                message = playerResult.message
            )
        )

        // Si el modo es PVE, la IA hace su turno
        if (game.mode == GameMode.PVE && !game.isRoundOver()) {
            delay(500) // Pequeño delay para simular "pensamiento"
            val aiResult = game.processAITurn()

            if (aiResult != null) {
                sendMessage(
                    ServerMessage.AIMove(
                        word = aiResult.guess,
                        attemptNumber = aiResult.attempts,
                        result = aiResult.evaluation.map { it.toDto() }
                    )
                )
            }
        }

        // Verificar si la ronda terminó
        if (game.isRoundOver()) {
            val roundResult = game.getRoundWinner()

            sendMessage(
                ServerMessage.RoundWinner(
                    winner = roundResult.winner,
                    attempts = roundResult.playerAttempts,
                    solution = roundResult.solution
                )
            )

            // Verificar si el juego completo terminó
            if (game.isGameOver()) {
                val gameResult = game.getGameWinner()
                val stats = game.getStats()

                sendMessage(
                    ServerMessage.GameWinner(
                        winner = gameResult.winner,
                        playerRounds = gameResult.playerRounds,
                        aiRounds = gameResult.aiRounds
                    )
                )

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

    /**
     * Sincroniza los records con el cliente
     */
    private suspend fun handleSyncRecords() {
        val records = recordsManager.getRecords()
        sendMessage(ServerMessage.RecordsData(records))
        println("📊 Enviando records a cliente #$clientId")
    }

    /**
     * Envía un mensaje al cliente
     */
    private suspend fun sendMessage(message: ServerMessage) = withContext(Dispatchers.IO) {
        try {
            val jsonText = json.encodeToString(message)
            writer.write(jsonText)
            writer.newLine()
            writer.flush()
        } catch (e: Exception) {
            println("❌ Error enviando mensaje a cliente #$clientId: ${e.message}")
        }
    }

    /**
     * Envía un mensaje de error
     */
    private suspend fun sendError(errorMessage: String) {
        sendMessage(ServerMessage.Error(errorMessage))
    }

    /**
     * Limpia recursos al desconectar
     */
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
}
