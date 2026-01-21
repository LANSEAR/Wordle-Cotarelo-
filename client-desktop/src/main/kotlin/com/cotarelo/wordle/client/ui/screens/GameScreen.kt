package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.state.GameController
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard
import com.cotarelo.wordle.client.ui.components.TopBar

@Composable
fun GameScreen() {
    val controller = remember { GameController() }
    val state = controller.state

    // ✅ Necesario para recibir teclas del teclado físico
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
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
                            } else {
                                false
                            }
                        } else false
                    }
                }
            }
    ) {
        TopBar(title = "WORDLE ESPAÑOL")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))

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
