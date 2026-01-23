package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onStartSinglePlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecords: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WORDLE",
            style = MaterialTheme.typography.h4
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onStartSinglePlayer,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Nueva partida (1 jugador)") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Configuración") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenRecords,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Records") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Salir") }
    }
}
