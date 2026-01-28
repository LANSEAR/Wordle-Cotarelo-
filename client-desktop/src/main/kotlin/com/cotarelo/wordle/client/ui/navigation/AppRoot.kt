package com.cotarelo.wordle.client.ui.navigation

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import com.cotarelo.wordle.client.settings.SettingsRepository
import com.cotarelo.wordle.client.settings.ThemeMode
import com.cotarelo.wordle.client.network.PVPServerConnection
import com.cotarelo.wordle.client.services.RecordsService
import com.cotarelo.wordle.client.state.RecordsViewModel
import com.cotarelo.wordle.client.ui.screens.GameScreen
import com.cotarelo.wordle.client.ui.screens.GamePVEScreen
import com.cotarelo.wordle.client.ui.screens.GamePVPScreen
import com.cotarelo.wordle.client.ui.screens.LobbyScreen
import com.cotarelo.wordle.client.ui.screens.MenuScreen
import com.cotarelo.wordle.client.ui.screens.RecordsScreen
import com.cotarelo.wordle.client.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppRoot(
    onExitRequest: () -> Unit = {}
) {
    val repo = remember { SettingsRepository() }
    var settings by remember { mutableStateOf(repo.load()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var pvpConnection by remember { mutableStateOf<PVPServerConnection?>(null) }

    val scope = rememberCoroutineScope()
    // Recrear ViewModel cuando cambie el playerName
    val recordsViewModel = remember(settings.playerName) { RecordsViewModel(settings.playerName, scope) }
    val recordsService = remember(recordsViewModel) { RecordsService(recordsViewModel, scope) }

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
                onChangeSettings = { newSettings ->
                    settings = newSettings
                    repo.save(newSettings)
                },
                onStartSinglePlayer = { screen = Screen.Game },
                onStartPVE = { screen = Screen.GamePVE },
                onStartPVP = { screen = Screen.Lobby },
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

            Screen.Records -> {
                // Sincronizar records al entrar
                LaunchedEffect(Unit) {
                    scope.launch {
                        recordsService.syncRecords()
                    }
                }
                RecordsScreen(
                    viewModel = recordsViewModel,
                    onBack = { screen = Screen.Menu }
                )
            }

            Screen.Lobby -> LobbyScreen(
                settings = settings,
                onBackToMenu = { screen = Screen.Menu },
                onGameStart = { roomId, opponentName, wordLength, maxAttempts, rounds, difficulty, connection ->
                    pvpConnection = connection
                    screen = Screen.GamePVP(roomId, opponentName, wordLength, maxAttempts, rounds, difficulty)
                }
            )

            is Screen.GamePVP -> {
                val gamePVPScreen = screen as Screen.GamePVP
                pvpConnection?.let { connection ->
                    GamePVPScreen(
                        roomId = gamePVPScreen.roomId,
                        opponentName = gamePVPScreen.opponentName,
                        wordLength = gamePVPScreen.wordLength,
                        maxAttempts = gamePVPScreen.maxAttempts,
                        rounds = gamePVPScreen.rounds,
                        difficulty = gamePVPScreen.difficulty,
                        settings = settings,
                        connection = connection,
                        onBackToMenu = {
                            pvpConnection = null
                            screen = Screen.Menu
                        },
                        onRematch = {
                            pvpConnection = null
                            screen = Screen.Lobby
                        }
                    )
                }
            }
        }
    }
}
