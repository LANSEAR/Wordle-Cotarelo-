package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.client.state.GameController
import com.cotarelo.wordle.client.state.GameState
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    settings: AppSettings,
    onBackToMenu: () -> Unit
) {
    val bestOf = settings.roundsBestOf.takeIf { it in setOf(1, 3, 5, 7) } ?: 1

    var roundIndex by remember(bestOf, settings.wordLength, settings.maxAttempts, settings.difficulty) { mutableStateOf(1) }
    var wins by remember(bestOf, settings.wordLength, settings.maxAttempts, settings.difficulty) { mutableStateOf(0) }
    var losses by remember(bestOf, settings.wordLength, settings.maxAttempts, settings.difficulty) { mutableStateOf(0) }

    val controller = remember(settings.wordLength, settings.maxAttempts, settings.difficulty) {
        GameController.newSinglePlayer(
            wordLength = settings.wordLength,
            maxAttempts = settings.maxAttempts,
            difficulty = settings.difficulty
        )
    }

    // Timer
    val timerMax = settings.timerSeconds.coerceIn(10, 180)
    var secondsLeft by remember(settings.timerEnabled, timerMax, roundIndex) {
        mutableStateOf(if (settings.timerEnabled) timerMax else 0)
    }

    // Dialog fin de ronda/serie
    var showEndDialog by remember { mutableStateOf(false) }
    var endTitle by remember { mutableStateOf("") }
    var endText by remember { mutableStateOf("") }
    var endIsSeriesEnd by remember { mutableStateOf(false) }

    // Foco teclado físico
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Auto-ocultar mensajes (diccionario, faltan letras, etc.)
    LaunchedEffect(controller.state.message) {
        if (!controller.state.message.isNullOrBlank()) {
            delay(1500)
            controller.clearMessage()
        }
    }

    // Reiniciar temporizador cuando cambia el intento actual (nueva palabra jugada o timeout)
    LaunchedEffect(controller.state.currentRow) {
        // Solo reiniciar si aún quedan intentos y el juego está activo
        if (settings.timerEnabled &&
            controller.state.status == GameState.Status.Playing &&
            controller.state.currentRow < controller.state.rows) {
            secondsLeft = timerMax
        } else if (controller.state.currentRow >= controller.state.rows) {
            // Detener temporizador si el jugador terminó
            secondsLeft = 0
        }
    }

    // Cuenta atrás (solo si se está jugando y NO hay diálogo)
    LaunchedEffect(settings.timerEnabled, timerMax, roundIndex, controller.state.status, showEndDialog, controller.state.currentRow) {
        if (!settings.timerEnabled) return@LaunchedEffect
        if (showEndDialog) return@LaunchedEffect
        if (controller.state.status != GameState.Status.Playing) return@LaunchedEffect

        // Detener temporizador si el jugador ya completó todos sus intentos
        val playerFinished = controller.state.currentRow >= controller.state.rows

        if (playerFinished) {
            secondsLeft = 0
            return@LaunchedEffect
        }

        while (secondsLeft > 0 &&
               controller.state.status == GameState.Status.Playing &&
               !showEndDialog &&
               controller.state.currentRow < controller.state.rows) {
            delay(1000)
            secondsLeft -= 1
        }

        if (secondsLeft <= 0 &&
            controller.state.status == GameState.Status.Playing &&
            controller.state.currentRow < controller.state.rows) {
            controller.forceLoseByTimeout()
        }
    }

    // Cuando termina una ronda, mostramos diálogo (NO auto-advance)
    LaunchedEffect(controller.state.status) {
        val st = controller.state.status
        if (st == GameState.Status.Won || st == GameState.Status.Lost) {
            val won = st == GameState.Status.Won
            val nextRound = roundIndex + 1
            val seriesEnd = nextRound > bestOf

            // Actualiza marcador SOLO una vez por fin de ronda
            if (won) wins += 1 else losses += 1

            endTitle = if (won) "✅ Ronda ganada" else "❌ Ronda perdida"
            endText = buildString {
                append("La palabra era: ${controller.solution}\n")
                if (bestOf > 1) append("Marcador: $wins - $losses\n")
                append(if (seriesEnd) "Serie terminada." else "¿Pasar a la siguiente ronda?")
            }
            endIsSeriesEnd = seriesEnd
            showEndDialog = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { focusRequester.requestFocus() }
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (showEndDialog) return@onPreviewKeyEvent true

                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Enter -> { controller.onEnter(); true }
                        Key.Backspace, Key.Delete -> { controller.onBackspace(); true }
                        else -> {
                            val ch = event.utf16CodePoint.toCharOrNull() ?: return@onPreviewKeyEvent false
                            val up = ch.uppercaseChar()
                            if ((up in 'A'..'Z') || up == 'Ñ') { controller.onLetter(up); true } else false
                        }
                    }
                }
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBackToMenu) { Text("Menú") }

                OutlinedButton(
                    onClick = {
                        roundIndex = 1
                        wins = 0
                        losses = 0
                        showEndDialog = false
                        controller.newGame(
                            wordLength = settings.wordLength,
                            maxAttempts = settings.maxAttempts,
                            difficulty = settings.difficulty
                        )
                        if (settings.timerEnabled) secondsLeft = timerMax
                        focusRequester.requestFocus()
                    }
                ) { Text("Reiniciar") }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val roundsText =
                    if (bestOf == 1) "Ronda: 1/1"
                    else "Ronda: $roundIndex/$bestOf  (W:$wins L:$losses)"
                Text(roundsText)

                if (settings.timerEnabled) Text("⏱ ${formatSeconds(secondsLeft)}")
            }

            Spacer(Modifier.height(12.dp))

            // Tablero + teclado en pantalla
            GameContent(controller = controller)

            // ✅ Mensaje debajo del tablero (p. ej. "No está en el diccionario")
            val msg = controller.state.message
            if (!msg.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body2
                )
            }
        }

        if (showEndDialog) {
            AlertDialog(
                onDismissRequest = { /* no cerrar click fuera */ },
                title = { Text(endTitle) },
                text = { Text(endText) },
                confirmButton = {
                    Button(onClick = {
                        showEndDialog = false

                        if (endIsSeriesEnd) {
                            onBackToMenu()
                        } else {
                            roundIndex += 1
                            controller.newGame(
                                wordLength = settings.wordLength,
                                maxAttempts = settings.maxAttempts,
                                difficulty = settings.difficulty
                            )
                            if (settings.timerEnabled) secondsLeft = timerMax
                            focusRequester.requestFocus()
                        }
                    }) {
                        Text(if (endIsSeriesEnd) "Terminar" else "Siguiente ronda")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showEndDialog = false
                        focusRequester.requestFocus()
                    }) { Text("Cerrar") }
                }
            )
        }
    }
}

private fun Int.toCharOrNull(): Char? {
    if (this == 0) return null
    return try {
        Character.toChars(this).concatToString().firstOrNull()
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun formatSeconds(total: Int): String {
    val t = total.coerceAtLeast(0)
    val m = t / 60
    val s = t % 60
    return "%d:%02d".format(m, s)
}
