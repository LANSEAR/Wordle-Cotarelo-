package com.cotarelo.wordle.shared.network

import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════════
// MENSAJES SIMPLES (Sin sealed classes para evitar problemas de serialización)
// ═══════════════════════════════════════════════════════════

@Serializable
data class StartGameRequest(
    val mode: String,  // "PVE" o "PVP"
    val rounds: Int,
    val wordLength: Int,
    val maxAttempts: Int,
    val difficulty: String  // "EASY", "NORMAL", "HARD"
)

@Serializable
data class GuessRequest(
    val word: String,
    val attemptNumber: Int
)

@Serializable
data class GameStartedResponse(
    val gameId: String,
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int
)

@Serializable
data class GuessResultResponse(
    val word: String,
    val result: List<String>,  // "EMPTY", "ABSENT", "PRESENT", "CORRECT"
    val isValid: Boolean,
    val message: String? = null
)

@Serializable
data class AIMoveResponse(
    val word: String,
    val attemptNumber: Int,
    val result: List<String>
)

@Serializable
data class RoundWinnerResponse(
    val winner: String,  // "PLAYER", "AI", "DRAW"
    val attempts: Int,
    val solution: String
)

@Serializable
data class GameWinnerResponse(
    val winner: String,
    val playerRounds: Int,
    val aiRounds: Int
)

@Serializable
data class ErrorResponse(
    val message: String
)
