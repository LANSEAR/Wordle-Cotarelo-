package com.cotarelo.wordle.client.ui.navigation

sealed class Screen {
    data object Menu : Screen()
    data object Game : Screen()
    data object GamePVE : Screen()
    data object Lobby : Screen()
    data class GamePVP(
        val roomId: String,
        val opponentName: String,
        val wordLength: Int,
        val maxAttempts: Int,
        val rounds: Int,
        val difficulty: String
    ) : Screen()
    data object Settings : Screen()
    data object Records : Screen()
}
