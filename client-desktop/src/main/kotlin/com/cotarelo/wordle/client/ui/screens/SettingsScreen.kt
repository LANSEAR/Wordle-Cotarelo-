package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Configuración (pendiente)")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Volver") }
    }
}
