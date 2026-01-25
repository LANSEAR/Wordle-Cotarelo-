package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.state.GameController
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard

@Composable
fun GameContent(
    controller: GameController,
    modifier: Modifier = Modifier
) {
    val state = controller.state

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tablero (TU Board usa rows/cols/letters/states)
        Board(
            rows = state.rows,
            cols = state.cols,
            letters = state.letters,
            states = state.states,
            modifier = Modifier.fillMaxWidth(),
            currentRow = state.currentRow,
            currentCol = state.currentCol
        )

        Spacer(Modifier.height(16.dp))

        // Teclado (TU Keyboard usa onKey/onEnter/onBackspace)
        Keyboard(
            onKey = { ch -> controller.onLetter(ch) },
            onEnter = { controller.onEnter() },
            onBackspace = { controller.onBackspace() },
            enabled = true
        )
    }
}
