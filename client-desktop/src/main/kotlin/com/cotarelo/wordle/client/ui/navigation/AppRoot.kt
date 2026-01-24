package com.cotarelo.wordle.client.ui.navigation

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import com.cotarelo.wordle.client.settings.SettingsRepository
import com.cotarelo.wordle.client.settings.ThemeMode
import com.cotarelo.wordle.client.ui.screens.GameScreen
import com.cotarelo.wordle.client.ui.screens.GamePVEScreen
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

    val colors = when (settings.themeMode) {
        ThemeMode.DARK -> darkColors()
        ThemeMode.LIGHT -> lightColors()
    }

    fun toggleTheme() {
        val newMode = if (settings.themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        val newSettings = settings.copy(themeMode = newMode)
        settings = newSettings
        repo.save(newSettings)
    }

    MaterialTheme(colors = colors) {
        when (screen) {
            Screen.Menu -> MenuScreen(
                settings = settings,
                onToggleTheme = { toggleTheme() },
                onStartSinglePlayer = { screen = Screen.Game },
                onStartPVE = { screen = Screen.GamePVE },
                onOpenSettings = { screen = Screen.Settings },
                onOpenRecords = { screen = Screen.Records },
                onExit = onExitRequest
            )

            Screen.Game -> GameScreen(
                settings = settings,
                onBackToMenu = { screen = Screen.Menu }
            )

            Screen.GamePVE -> GamePVEScreen(
                settings = settings,
                onBackToMenu = { screen = Screen.Menu }
            )

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
}
