package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.client.settings.ThemeMode

@Composable
fun MenuScreen(
    settings: AppSettings,
    onToggleTheme: () -> Unit,
    onStartSinglePlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecords: () -> Unit,
    onExit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            // ───── Top bar con botón de tema ─────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector =
                            if (settings.themeMode == ThemeMode.DARK)
                                Icons.Filled.WbSunny
                            else
                                Icons.Filled.DarkMode,
                        contentDescription = "Cambiar tema",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ───── Contenido centrado ─────
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "WORDLE",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onBackground
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
    }
}
