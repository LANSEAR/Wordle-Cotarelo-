package com.cotarelo.wordle.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.shared.model.TileState

@Composable
fun Board(
    rows: Int,
    cols: Int,
    letters: List<List<Char>>,
    states: List<List<TileState>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until cols) {
                    val letter = letters.getOrNull(r)?.getOrNull(c) ?: ' '
                    val state = states.getOrNull(r)?.getOrNull(c) ?: TileState.Empty

                    Tile(
                        letter = letter,
                        state = state
                    )
                }
            }
        }
    }
}
