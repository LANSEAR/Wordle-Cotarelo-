package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.background
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
import com.cotarelo.wordle.client.state.GameController
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard

@Composable
fun GameScreen(
    settings: AppSettings,
    onBackToMenu: () -> Unit
) {
    // Controller con tamaño según settings
    val controller = remember(settings.wordLength, settings.maxAttempts) {
        GameController.newSinglePlayer(
            wordLength = settings.wordLength,
            maxAttempts = settings.maxAttempts
        )
    }

    val state = controller.state

    // Foco para teclado físico
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(settings.wordLength, settings.maxAttempts) {
        // cuando cambian settings o entras a la pantalla, pide foco
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (event.key) {
                    Key.Enter -> {
                        controller.onEnter()
                        true
                    }
                    Key.Backspace, Key.Delete -> {
                        controller.onBackspace()
                        true
                    }
                    else -> {
                        val ch = event.utf16CodePoint.toCharOrNull()
                        if (ch != null) {
                            val up = ch.uppercaseChar()
                            if ((up in 'A'..'Z') || up == 'Ñ') {
                                controller.onLetter(up)
                                true
                            } else false
                        } else false
                    }
                }
            }
            .padding(12.dp)
    ) {
        // Barra superior con botones
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackToMenu) { Text("Menú") }

            OutlinedButton(
                onClick = {
                    controller.newGame(
                        wordLength = settings.wordLength,
                        maxAttempts = settings.maxAttempts,
                        resourcePath = "words_es_${settings.wordLength}.txt"
                    )
                    focusRequester.requestFocus()
                }
            ) { Text("Nueva partida") }
        }

        Spacer(Modifier.height(12.dp))

        // Tablero + mensaje + teclado en pantalla
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Board(
                rows = state.rows,
                cols = state.cols,
                letters = state.letters,
                states = state.states
            )

            state.message?.let { msg ->
                Text(
                    text = msg,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colors.onBackground
                )
            }

            Keyboard(
                onKey = controller::onLetter,
                onEnter = controller::onEnter,
                onBackspace = controller::onBackspace
            )
        }
    }
}

/**
 * Convierte utf16CodePoint en Char si existe.
 * En Compose Desktop, cuando no hay carácter "real" (teclas especiales), puede venir null/0.
 */
private fun Int.toCharOrNull(): Char? {
    if (this == 0) return null
    return try {
        Character.toChars(this).concatToString().firstOrNull()
    } catch (_: IllegalArgumentException) {
        null
    }
}
