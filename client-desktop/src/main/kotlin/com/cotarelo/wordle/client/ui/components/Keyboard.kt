package com.cotarelo.wordle.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Keyboard(
    onKey: (Char) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit
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

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            SpecialKey(text = "ENVIAR", width = 70.dp, onClick = onEnter)
            KeyRow(keys = row3, onKey = onKey)
            SpecialKey(text = "⌫", width = 56.dp, onClick = onBackspace)
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<Char>,
    onKey: (Char) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Row(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { ch -> Key(text = ch.toString()) { onKey(ch) } }
    }
}

@Composable
private fun Key(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 52.dp)
            .background(Color(0xFF818384))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun SpecialKey(text: String, width: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = width, height = 52.dp)
            .background(Color(0xFF818384))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
