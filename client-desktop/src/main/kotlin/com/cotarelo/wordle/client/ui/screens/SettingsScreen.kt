package com.cotarelo.wordle.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotarelo.wordle.client.settings.AppSettings

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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = settings.timerEnabled,
                onCheckedChange = { onChangeSettings(settings.copy(timerEnabled = it)) }
            )
            Spacer(Modifier.width(8.dp))
            Text("Temporizador por partida")
        }

        if (settings.timerEnabled) {
            Spacer(Modifier.height(8.dp))
            TimerSecondsSelector(
                value = settings.timerSeconds,
                onChange = { onChangeSettings(settings.copy(timerSeconds = it)) }
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
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Longitud palabra", modifier = Modifier.weight(1f))
        Stepper(
            value = value,
            min = 4,
            max = 7,
            onChange = onChange
        )
    }
}

@Composable
private fun SettingMaxAttempts(value: Int, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Intentos máximos", modifier = Modifier.weight(1f))
        Stepper(
            value = value,
            min = 4,
            max = 10,
            onChange = onChange
        )
    }
}

@Composable
private fun TimerSecondsSelector(value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Tiempo (segundos): $value", style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(60, 120, 180, 300).forEach { option ->
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
