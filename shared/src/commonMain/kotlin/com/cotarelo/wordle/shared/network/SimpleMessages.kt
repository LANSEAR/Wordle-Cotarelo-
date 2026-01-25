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

// ═══════════════════════════════════════════════════════════
// MENSAJES PVP
// ═══════════════════════════════════════════════════════════

@Serializable
data class CreateRoomRequest(
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int,
    val difficulty: String,
    val playerName: String
)

@Serializable
data class JoinRoomRequest(
    val roomId: String,
    val playerName: String
)

@Serializable
data class RoomCreatedResponse(
    val roomId: String,
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int,
    val difficulty: String
)

@Serializable
data class RoomListResponse(
    val rooms: List<RoomInfoDto>
)

@Serializable
data class RoomInfoDto(
    val roomId: String,
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int,
    val difficulty: String,
    val playerCount: Int,
    val creatorName: String
)

@Serializable
data class RoomJoinedResponse(
    val roomId: String,
    val opponentName: String,
    val isPlayer1: Boolean,
    val wordLength: Int,
    val maxAttempts: Int,
    val rounds: Int,
    val difficulty: String
)

@Serializable
data class GameStartedPVPResponse(
    val roomId: String,
    val opponentName: String
)

@Serializable
data class OpponentProgressResponse(
    val attempts: Int,
    val won: Boolean
)

@Serializable
data class OpponentDisconnectedResponse(
    val opponentName: String
)

@Serializable
data class RoundWinnerPVPResponse(
    val winner: String,  // "PLAYER1", "PLAYER2", "DRAW"
    val player1Attempts: Int,
    val player2Attempts: Int,
    val solution: String,
    val youWon: Boolean
)

@Serializable
data class GameWinnerPVPResponse(
    val winner: String,
    val player1Rounds: Int,
    val player2Rounds: Int,
    val player1Name: String,
    val player2Name: String,
    val youWon: Boolean
)
