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
    onChangeSettings: (AppSettings) -> Unit,
    onStartSinglePlayer: () -> Unit,
    onStartPVE: () -> Unit,
    onStartPVP: () -> Unit,
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

                // Temporizador en el menú principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "⏱ Temporizador",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onSurface
                            )
                            if (settings.timerEnabled) {
                                val timeText = if (settings.timerSeconds < 60) {
                                    "${settings.timerSeconds} s por palabra"
                                } else {
                                    "${settings.timerSeconds / 60} min por palabra"
                                }
                                Text(
                                    timeText,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    "Desactivado",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Switch(
                            checked = settings.timerEnabled,
                            onCheckedChange = { enabled ->
                                onChangeSettings(settings.copy(timerEnabled = enabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colors.primary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onStartSinglePlayer,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Nueva partida (Local)") }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onStartPVE,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Nueva partida PVE (vs IA)") }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onStartPVP,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Nueva partida PVP (Online)") }

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
