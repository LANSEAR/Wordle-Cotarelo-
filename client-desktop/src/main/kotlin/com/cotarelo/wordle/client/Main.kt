package com.cotarelo.wordle.client

import androidx.compose.material.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cotarelo.wordle.shared.TestShared

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Wordle - Smoke Test"
    ) {
        Text(TestShared.greeting("CLIENT"))
    }
}
