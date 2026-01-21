package com.cotarelo.wordle.client.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotarelo.wordle.client.ui.screens.TileState

@Composable
fun Tile(letter: Char, state: TileState) {

    val (bg, border, fg) = when (state) {
        TileState.Empty -> Triple(
            Color.Transparent,
            Color(0xFF3A3A3C),
            MaterialTheme.colors.onBackground
        )
        TileState.Absent -> Triple(
            Color(0xFF3A3A3C),
            Color(0xFF3A3A3C),
            Color.White
        )
        TileState.Present -> Triple(
            Color(0xFFB59F3B),
            Color(0xFFB59F3B),
            Color.White
        )
        TileState.Correct -> Triple(
            Color(0xFF538D4E),
            Color(0xFF538D4E),
            Color.White
        )
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .background(bg)
            .border(BorderStroke(2.dp, border)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (letter == ' ') "" else letter.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = fg
        )
    }
}
