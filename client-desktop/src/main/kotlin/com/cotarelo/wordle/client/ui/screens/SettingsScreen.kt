package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.settings.AppSettings
import com.cotarelo.wordle.client.settings.Difficulty

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onChangeSettings: (AppSettings) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Configuración", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))

        SettingWordLength(
            value = settings.wordLength,
            onChange = { onChangeSettings(settings.copy(wordLength = it)) }
        )

        Spacer(Modifier.height(16.dp))

        SettingMaxAttempts(
            value = settings.maxAttempts,
            onChange = { onChangeSettings(settings.copy(maxAttempts = it)) }
        )

        Spacer(Modifier.height(16.dp))

        SettingRoundsBestOf(
            value = settings.roundsBestOf,
            onChange = { onChangeSettings(settings.copy(roundsBestOf = it)) }
        )

        Spacer(Modifier.height(16.dp))

        SettingDifficulty(
            value = settings.difficulty,
            onChange = { onChangeSettings(settings.copy(difficulty = it)) }
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = settings.timerEnabled,
                onCheckedChange = { onChangeSettings(settings.copy(timerEnabled = it)) }
            )
            Spacer(Modifier.width(8.dp))
            Text("Tiempo por palabra (opcional)")
        }

        if (settings.timerEnabled) {
            Spacer(Modifier.height(8.dp))
            TimerSecondsSelector(
                value = settings.timerSeconds,
                onChange = { onChangeSettings(settings.copy(timerSeconds = it)) }
            )
            Text(
                text = "Máximo 3 minutos",
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onBack) { Text("Volver") }
            Text(
                text = "Se guarda automáticamente",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
private fun SettingWordLength(value: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("Longitud palabra", style = MaterialTheme.typography.subtitle1)
        Spacer(Modifier.height(8.dp))
        Stepper(value = value, min = 4, max = 7, onChange = onChange)
    }
}

@Composable
private fun SettingMaxAttempts(value: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("Intentos máximos", style = MaterialTheme.typography.subtitle1)
        Spacer(Modifier.height(8.dp))
        Stepper(value = value, min = 4, max = 10, onChange = onChange)
    }
}

@Composable
private fun SettingRoundsBestOf(value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Rondas", style = MaterialTheme.typography.subtitle1)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 3, 5, 7).forEach { bo ->
                val selected = value == bo
                Button(
                    onClick = { onChange(bo) },
                    colors = if (selected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(if (bo == 1) "1" else "BO$bo")
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = if (value == 1) "Sin rondas (una palabra por partida)"
            else "Mejor de $value palabras",
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
private fun SettingDifficulty(value: Difficulty, onChange: (Difficulty) -> Unit) {
    Column {
        Text("Dificultad (selección de palabra)", style = MaterialTheme.typography.subtitle1)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Difficulty.values().forEach { d ->
                val selected = value == d
                Button(
                    onClick = { onChange(d) },
                    colors = if (selected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        when (d) {
                            Difficulty.EASY -> "Fácil"
                            Difficulty.NORMAL -> "Media"
                            Difficulty.HARD -> "Difícil"
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = when (value) {
                Difficulty.EASY -> "Palabras comunes"
                Difficulty.NORMAL -> "Comunes (mezcla ligera)"
                Difficulty.HARD -> "Palabras raras"
            },
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
private fun TimerSecondsSelector(value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Tiempo máximo: $value s", style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(60, 120, 180).forEach { option ->
                val selected = option == value
                Button(
                    onClick = { onChange(option) },
                    colors = if (selected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("${option / 60} min")
                }
            }
        }
    }
}

@Composable
private fun Stepper(
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { onChange((value - 1).coerceAtLeast(min)) },
            enabled = value > min,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) { Text("-") }

        Spacer(Modifier.width(12.dp))
        Text(value.toString(), style = MaterialTheme.typography.h6)
        Spacer(Modifier.width(12.dp))

        OutlinedButton(
            onClick = { onChange((value + 1).coerceAtMost(max)) },
            enabled = value < max,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) { Text("+") }
    }
}
