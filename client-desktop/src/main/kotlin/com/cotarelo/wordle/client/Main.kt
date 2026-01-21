package com.cotarelo.wordle.client

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cotarelo.wordle.client.ui.screens.GameScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Wordle"
    ) {
        MaterialTheme(
            colors = darkColors()
        ) {
            GameScreen()
        }
    }
}
