package com.cotarelo.wordle.client.ui.navigation

import androidx.compose.runtime.*
import com.cotarelo.wordle.client.ui.screens.GameScreen
import com.cotarelo.wordle.client.ui.screens.MenuScreen
import com.cotarelo.wordle.client.ui.screens.RecordsScreen
import com.cotarelo.wordle.client.ui.screens.SettingsScreen

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf<Screen>(Screen.Menu) }

    when (screen) {
        Screen.Menu -> MenuScreen(
            onStartSinglePlayer = { screen = Screen.Game },
            onOpenSettings = { screen = Screen.Settings },
            onOpenRecords = { screen = Screen.Records },
            onExit = { /* lo conectaremos desde Main.kt */ }
        )

        Screen.Game -> GameScreen()

        Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Menu })

        Screen.Records -> RecordsScreen(onBack = { screen = Screen.Menu })
    }
}
