package com.cotarelo.wordle.client.ui.navigation

sealed class Screen {
    data object Menu : Screen()
    data object Game : Screen()
    data object GamePVE : Screen()
    data object Settings : Screen()
    data object Records : Screen()
}
