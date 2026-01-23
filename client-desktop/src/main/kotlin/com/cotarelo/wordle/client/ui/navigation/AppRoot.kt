package com.cotarelo.wordle.client.ui.navigation

import androidx.compose.runtime.*
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.client.settings.SettingsRepository
import com.cotarelo.wordle.client.ui.screens.GameScreen
import com.cotarelo.wordle.client.ui.screens.MenuScreen
import com.cotarelo.wordle.client.ui.screens.RecordsScreen
import com.cotarelo.wordle.client.ui.screens.SettingsScreen

@Composable
fun AppRoot(
    onExitRequest: () -> Unit = {}
) {
    val repo = remember { SettingsRepository() }
    var settings by remember { mutableStateOf(repo.load()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Menu) }

    when (screen) {
        Screen.Menu -> MenuScreen(
            onStartSinglePlayer = { screen = Screen.Game },
            onOpenSettings = { screen = Screen.Settings },
            onOpenRecords = { screen = Screen.Records },
            onExit = onExitRequest
        )

        // Paso 3: aquí pasaremos settings al GameScreen para que adapte tablero.
        Screen.Game -> GameScreen(settings = settings, onBackToMenu = { screen = Screen.Menu })


        Screen.Settings -> SettingsScreen(
            settings = settings,
            onChangeSettings = { newSettings ->
                settings = newSettings
                repo.save(newSettings)
            },
            onBack = { screen = Screen.Menu }
        )

        Screen.Records -> RecordsScreen(onBack = { screen = Screen.Menu })
    }
}
