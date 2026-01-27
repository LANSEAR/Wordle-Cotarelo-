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
import com.cotarelo.wordle.client.network.PVPServerConnection
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.client.state.PVPGameController
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GamePVPScreen(
    roomId: String,
    opponentName: String,
    wordLength: Int,
    maxAttempts: Int,
    rounds: Int,
    difficulty: String,
    settings: AppSettings,
    connection: PVPServerConnection,
    onBackToMenu: () -> Unit,
    onRematch: () -> Unit = onBackToMenu
) {
    val controller = remember {
        PVPGameController(
            wordLength = wordLength,
            maxAttempts = maxAttempts,
            connection = connection,
            roomId = roomId
        )
    }

    val focusRequester = remember { FocusRequester() }

    // Temporizador
    val timerMax = settings.timerSeconds.coerceIn(10, 180)
    var secondsLeft by remember(settings.timerEnabled, timerMax, controller.roundWinner) {
        mutableStateOf(if (settings.timerEnabled) timerMax else 0)
    }

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

    // Resetear temporizador cuando cambia roundWinner (nueva ronda)
    LaunchedEffect(controller.roundWinner) {
        if (controller.roundWinner == null && settings.timerEnabled) {
            secondsLeft = timerMax
        }
    }

    // Cuenta regresiva del temporizador
    LaunchedEffect(settings.timerEnabled, timerMax, controller.roundWinner, controller.gameWinner, controller.state.currentRow, controller.state.status) {
        if (!settings.timerEnabled) return@LaunchedEffect
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

    DisposableEffect(Unit) {
        onDispose {
            kotlinx.coroutines.MainScope().launch {
                controller.disconnect()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        controller.roundWinner == null &&
                        controller.gameWinner == null) {
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
                                } else {
                                    false
                                }
                            }
                        }
                    } else {
                        false
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con información de la partida
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Partida PVP",
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.onSurface
                        )
                        if (settings.timerEnabled) {
                            Text(
                                "⏱ ${formatSeconds(secondsLeft)}",
                                style = MaterialTheme.typography.h6,
                                color = if (secondsLeft <= 30) MaterialTheme.colors.error else MaterialTheme.colors.onSurface
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Tú",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.primary
                            )
                            Text(
                                "Rondas: ${controller.player1Rounds}",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                opponentName,
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.secondary
                            )
                            Text(
                                "Rondas: ${controller.player2Rounds}",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progreso del oponente
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$opponentName:",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        when {
                            controller.opponentWon -> "✓ Completó"
                            controller.opponentAttempts > 0 -> "${controller.opponentAttempts} intentos"
                            else -> "Esperando..."
                        },
                        style = MaterialTheme.typography.body1,
                        color = when {
                            controller.opponentWon -> MaterialTheme.colors.primary
                            else -> MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tablero
            Board(
                rows = controller.state.rows,
                cols = controller.state.cols,
                letters = controller.state.letters,
                states = controller.state.states,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                currentRow = controller.state.currentRow,
                currentCol = controller.state.currentCol
            )

            Spacer(Modifier.height(16.dp))

            // Mensaje de estado
            controller.state.message?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (message.contains("Error") || message.contains("inválid"))
                        MaterialTheme.colors.error
                    else
                        MaterialTheme.colors.primary,
                    elevation = 2.dp
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.body2
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Teclado virtual
            Keyboard(
                onKey = { ch -> controller.onLetter(ch) },
                onEnter = { controller.onEnter() },
                onBackspace = { controller.onBackspace() },
                enabled = controller.roundWinner == null && controller.gameWinner == null
            )

            Spacer(Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón salir de la sala (visible durante la partida)
                if (controller.gameWinner == null && !controller.opponentDisconnected) {
                    val scope = rememberCoroutineScope()
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                connection.leaveRoom()
                                onBackToMenu()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colors.error
                        )
                    ) {
                        Text("Abandonar Sala")
                    }
                }

                // Botón volver (solo visible si el juego terminó)
                if (controller.gameWinner != null || controller.opponentDisconnected) {
                    OutlinedButton(
                        onClick = onBackToMenu,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("← Volver al Menú")
                    }
                }
            }
        }

        // Diálogo ganador de ronda
        if (controller.roundWinner != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Ronda Terminada!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (controller.youWon) "¡Ganaste esta ronda!" else "Perdiste esta ronda",
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Palabra: ${controller.solution}",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        controller.nextRound()
                    }) {
                        Text("Continuar")
                    }
                }
            )
        }

        // Diálogo ganador del juego
        if (controller.gameWinner != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Juego Terminado!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (controller.youWon) "¡GANASTE EL JUEGO!" else "Perdiste el juego",
                            style = MaterialTheme.typography.h6,
                            color = if (controller.youWon)
                                MaterialTheme.colors.primary
                            else
                                MaterialTheme.colors.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Resultado Final:",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            "${controller.player1Rounds} - ${controller.player2Rounds}",
                            style = MaterialTheme.typography.h4,
                            color = MaterialTheme.colors.primary
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onBackToMenu) {
                        Text("Volver al Menú")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onRematch) {
                        Text("Revancha")
                    }
                }
            )
        }

        // Diálogo oponente desconectado (solo si el juego NO terminó normalmente)
        if (controller.opponentDisconnected && controller.gameWinner == null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Oponente Desconectado") },
                text = {
                    Text("$opponentName se ha desconectado. Has ganado la partida por abandono.")
                },
                confirmButton = {
                    Button(onClick = onBackToMenu) {
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
