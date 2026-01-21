package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.ui.components.Board
import com.cotarelo.wordle.client.ui.components.Keyboard
import com.cotarelo.wordle.client.ui.components.TopBar

@Composable
fun GameScreen() {
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
                rows = 6,
                cols = 5,
                letters = List(6) { List(5) { ' ' } },
                states = List(6) { List(5) { TileState.Empty } },
            )

            Keyboard(
                onKey = {},
                onEnter = {},
                onBackspace = {}
            )
        }
    }
}

enum class TileState { Empty, Absent, Present, Correct }
