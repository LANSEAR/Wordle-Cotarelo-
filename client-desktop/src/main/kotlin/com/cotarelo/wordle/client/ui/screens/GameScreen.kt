package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.state.GameController
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard
import com.cotarelo.wordle.client.ui.components.TopBar

@Composable
fun GameScreen() {
    val controller = remember { GameController() }
    val s = controller.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
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
                rows = s.rows,
                cols = s.cols,
                letters = s.letters,
                states = s.states
            )

            // Mensaje simple (faltan letras / correcto / fin...)
            if (s.message != null) {
                Text(
                    text = s.message,
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }

            Keyboard(
                onKey = { controller.onLetter(it) },
                onEnter = { controller.onEnter() },
                onBackspace = { controller.onBackspace() }
            )
        }
    }
}
