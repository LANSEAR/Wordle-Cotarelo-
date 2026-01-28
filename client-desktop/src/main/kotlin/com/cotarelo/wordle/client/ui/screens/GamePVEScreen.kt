package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.client.settings.Difficulty
import com.cotarelo.wordle.client.state.SimpleOnlineGameController
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GamePVEScreen(
    settings: AppSettings,
    onBackToMenu: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val controller = remember {
        SimpleOnlineGameController(
            wordLength = settings.wordLength,
            maxAttempts = settings.maxAttempts,
            rounds = settings.roundsBestOf,
            difficulty = when (settings.difficulty) {
                Difficulty.EASY -> "EASY"
                Difficulty.NORMAL -> "NORMAL"
                Difficulty.HARD -> "HARD"
                Difficulty.MIXTA -> "MIXTA"
            },
            timerSeconds = if (settings.timerEnabled) settings.timerSeconds else 0,
            playerName = settings.playerName
        )
    }

    var showConnectionError by remember { mutableStateOf(false) }
    var showRoundDialog by remember { mutableStateOf(false) }
    var showGameDialog by remember { mutableStateOf(false) }

    // Temporizador
    val timerMax = settings.timerSeconds.coerceIn(10, 180)
    var secondsLeft by remember(settings.timerEnabled, timerMax, controller.currentRound) {
        mutableStateOf(if (settings.timerEnabled) timerMax else 0)
    }

    // Foco para teclado físico
    val focusRequester = remember { FocusRequester() }

    // Conectar al servidor al iniciar
    LaunchedEffect(Unit) {
        val connected = controller.connectAndStart()
        if (!connected) {
            showConnectionError = true
        }
    }

    // Desconectar al salir
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                controller.disconnect()
            }
        }
    }

    // Detectar fin de ronda
    LaunchedEffect(controller.roundWinner) {
        if (controller.roundWinner != null && controller.gameWinner == null) {
            showRoundDialog = true
        }
    }

    // Detectar fin de juego
    LaunchedEffect(controller.gameWinner) {
        if (controller.gameWinner != null) {
            showGameDialog = true
        }
    }

    // Enfocar para capturar teclado físico
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Reiniciar temporizador cuando cambia el intento actual (nueva palabra jugada o timeout)
    LaunchedEffect(controller.state.currentRow) {
        // Solo reiniciar si aún quedan intentos y el juego está activo
        if (settings.timerEnabled &&
            controller.roundWinner == null &&
            controller.gameWinner == null &&
            controller.state.currentRow < controller.state.rows &&
            controller.state.status == com.cotarelo.wordle.client.state.GameState.Status.Playing) {
            secondsLeft = timerMax
        } else if (controller.state.currentRow >= controller.state.rows ||
                   controller.state.status != com.cotarelo.wordle.client.state.GameState.Status.Playing) {
            // Detener temporizador si el jugador terminó
            secondsLeft = 0
        }
    }

    // Resetear temporizador al cambiar de ronda
    LaunchedEffect(controller.currentRound) {
        if (settings.timerEnabled) {
            secondsLeft = timerMax
        }
    }

    // Cuenta regresiva del temporizador
    LaunchedEffect(settings.timerEnabled, timerMax, controller.currentRound, showRoundDialog, showGameDialog, controller.state.currentRow, controller.state.status) {
        if (!settings.timerEnabled) return@LaunchedEffect
        if (showRoundDialog || showGameDialog) return@LaunchedEffect
        if (controller.roundWinner != null || controller.gameWinner != null) return@LaunchedEffect

        // Detener temporizador si el jugador ya completó todos sus intentos
        val playerFinished = controller.state.currentRow >= controller.state.rows ||
                           controller.state.status != com.cotarelo.wordle.client.state.GameState.Status.Playing

        if (playerFinished) {
            secondsLeft = 0
            return@LaunchedEffect
        }

        while (secondsLeft > 0 &&
               controller.roundWinner == null &&
               controller.gameWinner == null &&
               !showRoundDialog &&
               !showGameDialog &&
               controller.state.status == com.cotarelo.wordle.client.state.GameState.Status.Playing &&
               controller.state.currentRow < controller.state.rows) {
            delay(1000)
            secondsLeft -= 1
        }

        if (secondsLeft <= 0 &&
            controller.roundWinner == null &&
            controller.gameWinner == null &&
            controller.state.status == com.cotarelo.wordle.client.state.GameState.Status.Playing) {
            // Tiempo agotado - el jugador pierde este intento
            controller.forceLoseByTimeout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Wordle PVE - Ronda ${controller.currentRound}/${settings.roundsBestOf}")
                                Text(
                                    "Jugador ${controller.playerRoundsWon} - ${controller.aiRoundsWon} IA",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                            if (settings.timerEnabled) {
                                Text(
                                    "⏱ ${formatSeconds(secondsLeft)}",
                                    style = MaterialTheme.typography.h6
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Text("←")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.Backspace -> {
                                controller.onBackspace()
                                true
                            }
                            Key.Enter -> {
                                controller.onEnter()
                                true
                            }
                            else -> {
                                val char = event.utf16CodePoint.toChar()
                                if (char.isLetter()) {
                                    controller.onLetter(char)
                                    true
                                } else false
                            }
                        }
                    } else false
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Estado de conexión
            Text(
                text = "Estado: ${controller.connectionStatus}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onBackground
            )

            Spacer(Modifier.height(8.dp))

            // Último movimiento de la IA
            controller.lastAIMove?.let { aiMove ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "🤖 IA jugó: ${aiMove.uppercase()}",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tablero
            Board(
                rows = controller.state.rows,
                cols = controller.state.cols,
                letters = controller.state.letters,
                states = controller.state.states,
                modifier = Modifier.weight(1f),
                currentRow = controller.state.currentRow,
                currentCol = controller.state.currentCol
            )

            // Mensaje
            controller.state.message?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Teclado
            Keyboard(
                onKey = { ch -> controller.onLetter(ch) },
                onEnter = { controller.onEnter() },
                onBackspace = { controller.onBackspace() },
                enabled = true
            )
        }

        // Diálogo de error de conexión
        if (showConnectionError) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Error de Conexión") },
                text = { Text("No se pudo conectar al servidor. Asegúrate de que el servidor esté ejecutándose en localhost:5678.") },
                confirmButton = {
                    Button(onClick = {
                        showConnectionError = false
                        onBackToMenu()
                    }) {
                        Text("Volver al Menú")
                    }
                }
            )
        }

        // Diálogo de fin de ronda
        if (showRoundDialog && controller.roundWinner != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Ronda ${controller.currentRound} Terminada") },
                text = {
                    Column {
                        Text(
                            when (controller.roundWinner) {
                                "PLAYER" -> "¡Ganaste esta ronda! 🎉"
                                "AI" -> "La IA ganó esta ronda 🤖"
                                "DRAW" -> "Empate"
                                else -> ""
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("La palabra era: ${controller.solution.uppercase()}")
                        Spacer(Modifier.height(8.dp))
                        Text("Marcador: Jugador ${controller.playerRoundsWon} - ${controller.aiRoundsWon} IA")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showRoundDialog = false
                        if (controller.currentRound < settings.roundsBestOf) {
                            controller.nextRound()
                        }
                    }) {
                        Text(if (controller.currentRound < settings.roundsBestOf) "Siguiente Ronda" else "Ver Resultado")
                    }
                }
            )
        }

        // Diálogo de fin de juego
        if (showGameDialog && controller.gameWinner != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Juego Terminado!") },
                text = {
                    Column {
                        Text(
                            when (controller.gameWinner) {
                                "PLAYER" -> "🏆 ¡GANASTE LA PARTIDA! 🏆"
                                "AI" -> "🤖 La IA ganó la partida"
                                "DRAW" -> "⚖️ Empate"
                                else -> ""
                            },
                            style = MaterialTheme.typography.h6
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Resultado Final:")
                        Text("Jugador: ${controller.playerRoundsWon} rondas")
                        Text("IA: ${controller.aiRoundsWon} rondas")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showGameDialog = false
                        onBackToMenu()
                    }) {
                        Text("Volver al Menú")
                    }
                }
            )
        }
    }
}

private fun formatSeconds(total: Int): String {
    val t = total.coerceAtLeast(0)
    val m = t / 60
    val s = t % 60
    return "%d:%02d".format(m, s)
}
