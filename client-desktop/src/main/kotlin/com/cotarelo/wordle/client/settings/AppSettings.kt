package com.cotarelo.wordle.client.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val wordLength: Int = 5,
    val maxAttempts: Int = 6,
    val roundsBestOf: Int = 1,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val timerEnabled: Boolean = false,
    val timerSeconds: Int = 10,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val playerName: String = ""
) {
    fun sanitized(): AppSettings {
        val wl = wordLength.coerceIn(4, 7)
        val ma = maxAttempts.coerceIn(4, 10)
        val bo = roundsBestOf.coerceIn(1, 7).let { if (it in setOf(1,3,5,7)) it else 1 }
        val ts = timerSeconds.coerceIn(10, 180) // Para pruebas: 10 segundos mínimo

        // Validar playerName: máximo 20 caracteres, solo alfanuméricos
        val pn = playerName
            .filter { it.isLetterOrDigit() }
            .take(20)
            .ifEmpty { "" } // Mantener vacío si no es válido, se generará uno nuevo al cargar

        return copy(wordLength = wl, maxAttempts = ma, roundsBestOf = bo, timerSeconds = ts, playerName = pn)
    }
}
