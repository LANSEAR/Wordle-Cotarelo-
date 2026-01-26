package com.cotarelo.wordle.shared.network

import com.cotarelo.wordle.shared.model.TileState
import kotlinx.serialization.Serializable

/**
 * Mensajes del protocolo cliente-servidor para Wordle multijugador
 */

// ═══════════════════════════════════════════════════════════
// MENSAJES CLIENTE → SERVIDOR
// ═══════════════════════════════════════════════════════════

@Serializable
sealed class ClientMessage {
    /**
     * Inicia una nueva partida
     */
    @Serializable
    data class StartGame(
        val mode: GameMode,
        val rounds: Int,
        val wordLength: Int,
        val maxAttempts: Int,
        val difficulty: Difficulty
    ) : ClientMessage()

    /**
     * Envía un intento de palabra
     */
    @Serializable
    data class Guess(
        val word: String,
        val attemptNumber: Int
    ) : ClientMessage()

    /**
     * Solicita sincronización de records
     */
    @Serializable
    data object SyncRecords : ClientMessage()

    /**
     * Desconexión del cliente
     */
    @Serializable
    data object Disconnect : ClientMessage()
}

// ═══════════════════════════════════════════════════════════
// MENSAJES SERVIDOR → CLIENTE
// ═══════════════════════════════════════════════════════════

@Serializable
sealed class ServerMessage {
    /**
     * Confirmación de inicio de partida
     */
    @Serializable
    data class GameStarted(
        val gameId: String,
        val wordLength: Int,
        val maxAttempts: Int,
        val rounds: Int
    ) : ServerMessage()

    /**
     * Resultado de un intento
     */
    @Serializable
    data class GuessResult(
        val word: String,
        val result: List<TileStateDto>,
        val isValid: Boolean,
        val message: String? = null
    ) : ServerMessage()

    /**
     * Movimiento de la IA (en modo PVE)
     */
    @Serializable
    data class AIMove(
        val word: String,
        val attemptNumber: Int,
        val result: List<TileStateDto>
    ) : ServerMessage()

    /**
     * Ganador de una ronda
     */
    @Serializable
    data class RoundWinner(
        val winner: Winner,
        val attempts: Int,
        val solution: String
    ) : ServerMessage()

    /**
     * Ganador de la partida (serie de rondas)
     */
    @Serializable
    data class GameWinner(
        val winner: Winner,
        val playerRounds: Int,
        val aiRounds: Int
    ) : ServerMessage()

    /**
     * Error del servidor
     */
    @Serializable
    data class Error(
        val message: String
    ) : ServerMessage()

    /**
     * Datos de records
     */
    @Serializable
    data class RecordsData(
        val records: Records
    ) : ServerMessage()
}

// ═══════════════════════════════════════════════════════════
// ENUMS Y DATOS
// ═══════════════════════════════════════════════════════════

@Serializable
enum class GameMode {
    PVP,  // Player vs Player
    PVE   // Player vs Environment (IA)
}

@Serializable
enum class Difficulty {
    EASY,
    NORMAL,
    HARD,
    MIXTA
}

@Serializable
enum class Winner {
    PLAYER,
    AI,
    OPPONENT,
    DRAW
}

/**
 * DTO para TileState (serializable)
 */
@Serializable
enum class TileStateDto {
    EMPTY,
    ABSENT,
    PRESENT,
    CORRECT
}

// Conversiones entre TileState y TileStateDto
fun TileState.toDto(): TileStateDto = when (this) {
    TileState.Empty -> TileStateDto.EMPTY
    TileState.Absent -> TileStateDto.ABSENT
    TileState.Present -> TileStateDto.PRESENT
    TileState.Correct -> TileStateDto.CORRECT
}

fun TileStateDto.toModel(): TileState = when (this) {
    TileStateDto.EMPTY -> TileState.Empty
    TileStateDto.ABSENT -> TileState.Absent
    TileStateDto.PRESENT -> TileState.Present
    TileStateDto.CORRECT -> TileState.Correct
}

// ═══════════════════════════════════════════════════════════
// RECORDS
// ═══════════════════════════════════════════════════════════

@Serializable
data class Records(
    val players: Map<String, PlayerStats> = emptyMap()
)

@Serializable
data class PlayerStats(
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val totalGames: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val averageAttempts: Double = 0.0,
    val attemptsDistribution: Map<Int, Int> = emptyMap(), // intentos -> cantidad
    val wordsGuessed: Int = 0,
    val totalWords: Int = 0
) {
    val winRate: Double
        get() = if (totalGames > 0) gamesWon.toDouble() / totalGames else 0.0

    val guessRate: Double
        get() = if (totalWords > 0) wordsGuessed.toDouble() / totalWords else 0.0
}
