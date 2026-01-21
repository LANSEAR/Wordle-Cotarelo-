package com.cotarelo.wordle.client.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.ui.screens.TileState

@Composable
fun Board(
    rows: Int,
    cols: Int,
    letters: List<List<Char>>,
    states: List<List<TileState>>,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until cols) {
                    Tile(
                        letter = letters[r][c],
                        state = states[r][c]
                    )
                }
            }
        }
    }
}
