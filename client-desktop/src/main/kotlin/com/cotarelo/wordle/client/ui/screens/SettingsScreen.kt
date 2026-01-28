package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Configuración", style = MaterialTheme.typography.h5, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(16.dp))

        SettingPlayerName(
            value = settings.playerName,
            onChange = { onChangeSettings(settings.copy(playerName = it)) }
        )

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

        Column(Modifier.fillMaxWidth()) {
            Text("Temporizador", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onBackground)
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = settings.timerEnabled,
                    onCheckedChange = { onChangeSettings(settings.copy(timerEnabled = it)) }
                )
                Spacer(Modifier.width(8.dp))
                Text("Activar temporizador por palabra", color = MaterialTheme.colors.onBackground)
            }

            if (settings.timerEnabled) {
                Spacer(Modifier.height(12.dp))
                TimerSecondsSelector(
                    value = settings.timerSeconds,
                    onChange = { onChangeSettings(settings.copy(timerSeconds = it)) }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Al agotar el tiempo, pierdes automáticamente",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBack) { Text("Volver") }
                Text(
                    text = "Se guarda automáticamente",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SettingPlayerName(value: String, onChange: (String) -> Unit) {
    var textValue by remember { mutableStateOf(value) }

    Column(Modifier.fillMaxWidth()) {
        Text("Nombre de jugador", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                // Limitar a 20 caracteres y solo alfanuméricos
                val filtered = newValue.filter { it.isLetterOrDigit() }.take(20)
                textValue = filtered
                if (filtered.isNotEmpty()) {
                    onChange(filtered)
                }
            },
            modifier = Modifier.fillMaxWidth(0.5f),
            singleLine = true,
            placeholder = { Text("Introduce tu nombre") }
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = "Máximo 20 caracteres (solo letras y números)",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SettingWordLength(value: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("Longitud palabra", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(8.dp))
        Stepper(value = value, min = 4, max = 7, onChange = onChange)
    }
}

@Composable
private fun SettingMaxAttempts(value: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("Intentos máximos", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(8.dp))
        Stepper(value = value, min = 4, max = 10, onChange = onChange)
    }
}

@Composable
private fun SettingRoundsBestOf(value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Rondas", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onBackground)
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
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SettingDifficulty(value: Difficulty, onChange: (Difficulty) -> Unit) {
    Column {
        Text("Dificultad (selección de palabra)", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onBackground)
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
                            Difficulty.MIXTA -> "Mixta"
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = when (value) {
                Difficulty.EASY -> "Palabras muy comunes del día a día"
                Difficulty.NORMAL -> "Palabras comunes pero menos frecuentes"
                Difficulty.HARD -> "Palabras poco comunes o técnicas"
                Difficulty.MIXTA -> "Todas las categorías mezcladas"
            },
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TimerSecondsSelector(value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Tiempo máximo: $value s", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 60, 120, 180).forEach { option ->
                val selected = option == value
                Button(
                    onClick = { onChange(option) },
                    colors = if (selected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(if (option < 60) "$option s" else "${option / 60} min")
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
        Text(value.toString(), style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.width(12.dp))

        OutlinedButton(
            onClick = { onChange((value + 1).coerceAtMost(max)) },
            enabled = value < max,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) { Text("+") }
    }
}
