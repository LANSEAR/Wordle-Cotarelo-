package com.cotarelo.wordle.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Text

@Composable
fun Keyboard(
    onKey: (Char) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit,
    enabled: Boolean
) {
    val row1 = "QWERTYUIOP".toList()
    val row2 = "ASDFGHJKLÑ".toList()
    val row3 = "ZXCVBNM".toList()

    Column(
        modifier = Modifier.padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KeyRow(keys = row1, onKey = onKey)

        KeyRow(keys = row2, onKey = onKey, horizontalPadding = 14.dp)

        // Tercera fila: ENTER + letras + BACKSPACE (todo en una sola Row)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpecialKey(text = "ENVIAR", width = 72.dp, onClick = onEnter)

            row3.forEach { ch ->
                Key(text = ch.toString()) { onKey(ch) }
            }

            SpecialKey(text = "⌫", width = 56.dp, onClick = onBackspace)
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<Char>,
    onKey: (Char) -> Unit,
    horizontalPadding: Dp = 0.dp
) {
    Row(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { ch ->
            Key(text = ch.toString()) { onKey(ch) }
        }
    }
}

@Composable
private fun Key(text: String, onClick: () -> Unit) {
    KeyBase(
        text = text,
        width = 40.dp,
        fontSize = 14.sp,
        onClick = onClick
    )
}

@Composable
private fun SpecialKey(text: String, width: Dp, onClick: () -> Unit) {
    KeyBase(
        text = text,
        width = width,
        fontSize = 12.sp,
        onClick = onClick
    )
}

@Composable
private fun KeyBase(
    text: String,
    width: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = width, height = 52.dp)
            .background(Color(0xFF818384))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
