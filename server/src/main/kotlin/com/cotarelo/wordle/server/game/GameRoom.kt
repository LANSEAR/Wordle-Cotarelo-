package com.cotarelo.wordle.server.game

import com.cotarelo.wordle.server.data.WordDictionary
import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.model.evaluateGuess
import java.util.UUID

/**
 * Sala de juego PVP para dos jugadores
 */
class GameRoom(
    val roomId: String = UUID.randomUUID().toString(),
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int,
    val difficulty: String
) {
    // Jugadores
    var player1Id: Int? = null
    var player1Name: String? = null
    var player2Id: Int? = null
    var player2Name: String? = null

    // Estado del juego
    private var currentRound = 0
    private var player1RoundsWon = 0
    private var player2RoundsWon = 0

    // Estado de la ronda actual
    private var secret: String = ""
    private var player1Attempts = 0
    private var player2Attempts = 0
    private var player1Guesses = mutableListOf<Pair<String, List<TileState>>>()
    private var player2Guesses = mutableListOf<Pair<String, List<TileState>>>()
    private var player1Won = false
    private var player2Won = false
    private var roundFinished = false

    var gameStarted = false
        private set

    /**
     * Verifica si la sala está llena
     */
    fun isFull(): Boolean = player1Id != null && player2Id != null

    /**
     * Verifica si la sala está vacía
     */
    fun isEmpty(): Boolean = player1Id == null && player2Id == null

    /**
     * Agrega un jugador a la sala
     */
    fun addPlayer(clientId: Int, playerName: String): Boolean {
        return when {
            player1Id == null -> {
                player1Id = clientId
                player1Name = playerName
                println("✅ Jugador 1 '$playerName' (#$clientId) se unió a sala $roomId")
                true
            }
            player2Id == null -> {
                player2Id = clientId
                player2Name = playerName
                println("✅ Jugador 2 '$playerName' (#$clientId) se unió a sala $roomId")
                true
            }
            else -> {
                println("⚠️  Sala $roomId está llena")
                false
            }
        }
    }

    /**
     * Remueve un jugador de la sala
     */
    fun removePlayer(clientId: Int) {
        when (clientId) {
            player1Id -> {
                println("👋 Jugador 1 '$player1Name' (#$clientId) salió de sala $roomId")
                player1Id = null
                player1Name = null
            }
            player2Id -> {
                println("👋 Jugador 2 '$player2Name' (#$clientId) salió de sala $roomId")
                player2Id = null
                player2Name = null
            }
        }
    }

    /**
     * Inicia el juego cuando hay 2 jugadores
     */
    fun startGame() {
        if (!isFull()) {
            println("⚠️  No se puede iniciar juego, faltan jugadores")
            return
        }

        gameStarted = true
        startNewRound()
        println("🎮 Partida PVP iniciada en sala $roomId")
    }

    /**
     * Inicia una nueva ronda
     */
    private fun startNewRound() {
        currentRound++
        secret = WordDictionary.getRandomWord(wordLength, difficulty)
        player1Attempts = 0
        player2Attempts = 0
        player1Guesses.clear()
        player2Guesses.clear()
        player1Won = false
        player2Won = false
        roundFinished = false

        println("🎮 Sala $roomId - Ronda $currentRound/$rounds - Palabra: $secret")
    }

    /**
     * Procesa un intento de un jugador
     */
    fun processGuess(clientId: Int, guess: String): GuessResult? {
        if (!gameStarted) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = 0,
                message = "El juego no ha iniciado"
            )
        }

        if (roundFinished) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = 0,
                message = "La ronda ya terminó"
            )
        }

        val normalizedGuess = guess.trim().uppercase()

        // Validar longitud
        if (normalizedGuess.length != secret.length) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = 0,
                message = "La palabra debe tener ${secret.length} letras"
            )
        }

        // Validar palabra en diccionario
        if (!WordDictionary.isValidWord(normalizedGuess)) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = 0,
                message = "Palabra no válida"
            )
        }

        // Procesar según jugador
        return when (clientId) {
            player1Id -> {
                if (player1Won) {
                    return GuessResult(false, emptyList(), true, player1Attempts, "Ya ganaste")
                }
                if (player1Attempts >= maxAttempts) {
                    return GuessResult(false, emptyList(), false, player1Attempts, "Sin intentos")
                }

                player1Attempts++
                val evaluation = evaluateGuess(secret, normalizedGuess)
                player1Guesses.add(normalizedGuess to evaluation)
                player1Won = evaluation.all { it == TileState.Correct }

                if (player1Won || player2Won || (player1Attempts >= maxAttempts && player2Attempts >= maxAttempts)) {
                    roundFinished = true
                }

                GuessResult(true, evaluation, player1Won, player1Attempts, null)
            }
            player2Id -> {
                if (player2Won) {
                    return GuessResult(false, emptyList(), true, player2Attempts, "Ya ganaste")
                }
                if (player2Attempts >= maxAttempts) {
                    return GuessResult(false, emptyList(), false, player2Attempts, "Sin intentos")
                }

                player2Attempts++
                val evaluation = evaluateGuess(secret, normalizedGuess)
                player2Guesses.add(normalizedGuess to evaluation)
                player2Won = evaluation.all { it == TileState.Correct }

                if (player1Won || player2Won || (player1Attempts >= maxAttempts && player2Attempts >= maxAttempts)) {
                    roundFinished = true
                }

                GuessResult(true, evaluation, player2Won, player2Attempts, null)
            }
            else -> null
        }
    }

    /**
     * Obtiene el progreso del oponente
     */
    fun getOpponentProgress(clientId: Int): OpponentProgress? {
        return when (clientId) {
            player1Id -> OpponentProgress(player2Attempts, player2Won)
            player2Id -> OpponentProgress(player1Attempts, player1Won)
            else -> null
        }
    }

    /**
     * Verifica si la ronda terminó
     */
    fun isRoundOver(): Boolean = roundFinished

    /**
     * Obtiene el ganador de la ronda
     */
    fun getRoundWinner(): RoundWinner {
        val winner = when {
            player1Won && !player2Won -> "PLAYER1"
            player2Won && !player1Won -> "PLAYER2"
            player1Won && player2Won -> {
                if (player1Attempts < player2Attempts) "PLAYER1"
                else if (player2Attempts < player1Attempts) "PLAYER2"
                else "DRAW"
            }
            else -> "DRAW"
        }

        when (winner) {
            "PLAYER1" -> player1RoundsWon++
            "PLAYER2" -> player2RoundsWon++
        }

        return RoundWinner(
            winner = winner,
            player1Attempts = player1Attempts,
            player2Attempts = player2Attempts,
            solution = secret
        )
    }

    /**
     * Verifica si el juego completo terminó
     */
    fun isGameOver(): Boolean {
        val roundsToWin = (rounds / 2) + 1
        return player1RoundsWon >= roundsToWin ||
               player2RoundsWon >= roundsToWin ||
               currentRound >= rounds
    }

    /**
     * Obtiene el ganador del juego
     */
    fun getGameWinner(): GameWinner {
        val winner = when {
            player1RoundsWon > player2RoundsWon -> "PLAYER1"
            player2RoundsWon > player1RoundsWon -> "PLAYER2"
            else -> "DRAW"
        }

        return GameWinner(
            winner = winner,
            player1Rounds = player1RoundsWon,
            player2Rounds = player2RoundsWon,
            player1Name = player1Name ?: "Player1",
            player2Name = player2Name ?: "Player2"
        )
    }

    /**
     * Continúa a la siguiente ronda
     */
    fun nextRound() {
        if (!isGameOver()) {
            startNewRound()
        }
    }
}

// Clases de resultado (GuessResult está en GameSession.kt)
data class OpponentProgress(
    val attempts: Int,
    val won: Boolean
)

data class RoundWinner(
    val winner: String,
    val player1Attempts: Int,
    val player2Attempts: Int,
    val solution: String
)

data class GameWinner(
    val winner: String,
    val player1Rounds: Int,
    val player2Rounds: Int,
    val player1Name: String,
    val player2Name: String
)
