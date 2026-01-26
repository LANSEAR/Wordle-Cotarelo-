package com.cotarelo.wordle.server.game

import com.cotarelo.wordle.server.ai.WordleAI
import com.cotarelo.wordle.server.data.WordDictionary
import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.model.evaluateGuess
import com.cotarelo.wordle.shared.network.Difficulty
import com.cotarelo.wordle.shared.network.GameMode
import com.cotarelo.wordle.shared.network.Winner
import java.util.UUID

/**
 * Sesión de juego que gestiona una partida PVE
 */
class GameSession(
    val gameId: String = UUID.randomUUID().toString(),
    val mode: GameMode,
    val rounds: Int,
    val wordLength: Int,
    val maxAttempts: Int,
    val difficulty: Difficulty,
    val playerName: String = "Player"
) {
    // Estado de la partida
    private var currentRound = 0
    private var playerRoundsWon = 0
    private var aiRoundsWon = 0

    // Estado de la ronda actual
    private var secret: String = ""
    private var playerAttempts = 0
    private var aiAttempts = 0
    private var playerGuesses = mutableListOf<String>()
    private var aiGuesses = mutableListOf<String>()
    private var playerWon = false
    private var aiWon = false

    // IA
    private val ai: WordleAI? = if (mode == GameMode.PVE) {
        val dictionary = WordDictionary.getWords(wordLength)
        WordleAI(dictionary, difficulty)
    } else null

    init {
        startNewRound()
    }

    /**
     * Inicia una nueva ronda
     */
    fun startNewRound() {
        currentRound++
        secret = WordDictionary.getRandomWord(wordLength, difficulty.toString())
        playerAttempts = 0
        aiAttempts = 0
        playerGuesses.clear()
        aiGuesses.clear()
        playerWon = false
        aiWon = false
        ai?.reset()

        println("🎮 Nueva ronda $currentRound/$rounds - Palabra secreta: $secret")
    }

    /**
     * Procesa un intento del jugador
     */
    fun processPlayerGuess(guess: String): GuessResult {
        if (playerWon) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = true,
                attempts = playerAttempts,
                message = "Ya ganaste esta ronda"
            )
        }

        if (playerAttempts >= maxAttempts) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = playerAttempts,
                message = "Se acabaron los intentos"
            )
        }

        // Validar longitud
        val normalizedGuess = guess.trim().uppercase()
        if (normalizedGuess.length != secret.length) {
            println("❌ Longitud incorrecta: guess='$normalizedGuess' (${normalizedGuess.length}) vs secret='$secret' (${secret.length})")
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = playerAttempts,
                message = "La palabra debe tener ${secret.length} letras"
            )
        }

        if (!WordDictionary.isValidWord(normalizedGuess)) {
            return GuessResult(
                valid = false,
                evaluation = emptyList(),
                won = false,
                attempts = playerAttempts,
                message = "Palabra no válida"
            )
        }

        playerAttempts++
        playerGuesses.add(normalizedGuess)
        println("✅ Evaluando: secret='$secret' vs guess='$normalizedGuess'")
        val evaluation = evaluateGuess(secret, normalizedGuess)
        playerWon = evaluation.all { it == TileState.Correct }

        return GuessResult(
            valid = true,
            evaluation = evaluation,
            won = playerWon,
            attempts = playerAttempts,
            message = if (playerWon) "¡Ganaste!" else null
        )
    }

    /**
     * Procesa un turno de la IA (se llama después del turno del jugador)
     */
    fun processAITurn(): AITurnResult? {
        if (mode != GameMode.PVE || ai == null) return null
        if (aiWon || aiAttempts >= maxAttempts) return null

        val guess = ai.getNextGuess()
        aiAttempts++
        aiGuesses.add(guess)

        val evaluation = evaluateGuess(secret, guess.uppercase())
        aiWon = evaluation.all { it == TileState.Correct }

        // Actualizar IA con resultado
        ai.updateWithResult(guess.uppercase(), secret)

        println("🤖 IA intento $aiAttempts: $guess -> ${if (aiWon) "¡GANÓ!" else "continúa"}")

        return AITurnResult(
            guess = guess,
            evaluation = evaluation,
            won = aiWon,
            attempts = aiAttempts
        )
    }

    /**
     * Verifica si la ronda ha terminado
     */
    fun isRoundOver(): Boolean {
        return playerWon || aiWon ||
               (playerAttempts >= maxAttempts && aiAttempts >= maxAttempts)
    }

    /**
     * Obtiene el ganador de la ronda
     */
    fun getRoundWinner(): RoundResult {
        val winner = when {
            playerWon && !aiWon -> Winner.PLAYER
            aiWon && !playerWon -> Winner.AI
            playerWon && aiWon -> {
                // Ambos ganaron, gana quien usó menos intentos
                if (playerAttempts < aiAttempts) Winner.PLAYER
                else if (aiAttempts < playerAttempts) Winner.AI
                else Winner.DRAW
            }
            else -> Winner.DRAW // Ninguno ganó
        }

        if (winner == Winner.PLAYER) playerRoundsWon++
        else if (winner == Winner.AI) aiRoundsWon++

        return RoundResult(
            winner = winner,
            playerAttempts = playerAttempts,
            aiAttempts = aiAttempts,
            solution = secret
        )
    }

    /**
     * Verifica si el juego completo ha terminado
     */
    fun isGameOver(): Boolean {
        // El juego termina cuando alguien gana la mayoría de rondas
        val roundsToWin = (rounds / 2) + 1
        return playerRoundsWon >= roundsToWin ||
               aiRoundsWon >= roundsToWin ||
               currentRound >= rounds
    }

    /**
     * Obtiene el ganador del juego completo
     */
    fun getGameWinner(): GameResult {
        val winner = when {
            playerRoundsWon > aiRoundsWon -> Winner.PLAYER
            aiRoundsWon > playerRoundsWon -> Winner.AI
            else -> Winner.DRAW
        }

        return GameResult(
            winner = winner,
            playerRounds = playerRoundsWon,
            aiRounds = aiRoundsWon,
            totalRounds = currentRound
        )
    }

    /**
     * Obtiene estadísticas de la sesión
     */
    fun getStats(): GameStats {
        return GameStats(
            currentRound = currentRound,
            totalRounds = rounds,
            playerRoundsWon = playerRoundsWon,
            aiRoundsWon = aiRoundsWon,
            playerAttempts = playerAttempts,
            aiAttempts = aiAttempts,
            playerWordsGuessed = playerGuesses.size,
            totalWordsAttempted = currentRound
        )
    }
}

// ═══════════════════════════════════════════════════════════
// CLASES DE RESULTADO
// ═══════════════════════════════════════════════════════════

data class GuessResult(
    val valid: Boolean,
    val evaluation: List<TileState>,
    val won: Boolean,
    val attempts: Int,
    val message: String? = null
)

data class AITurnResult(
    val guess: String,
    val evaluation: List<TileState>,
    val won: Boolean,
    val attempts: Int
)

data class RoundResult(
    val winner: Winner,
    val playerAttempts: Int,
    val aiAttempts: Int,
    val solution: String
)

data class GameResult(
    val winner: Winner,
    val playerRounds: Int,
    val aiRounds: Int,
    val totalRounds: Int
)

data class GameStats(
    val currentRound: Int,
    val totalRounds: Int,
    val playerRoundsWon: Int,
    val aiRoundsWon: Int,
    val playerAttempts: Int,
    val aiAttempts: Int,
    val playerWordsGuessed: Int,
    val totalWordsAttempted: Int
)
