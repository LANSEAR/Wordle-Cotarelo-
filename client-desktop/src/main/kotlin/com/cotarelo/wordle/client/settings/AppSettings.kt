package com.cotarelo.wordle.client.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val wordLength: Int = 5,          // 4..7
    val maxAttempts: Int = 6,         // 4..10 (por ejemplo)
    val timerEnabled: Boolean = false,
    val timerSeconds: Int = 180       // 3 min por defecto
) {
    fun sanitized(): AppSettings {
        val wl = wordLength.coerceIn(4, 7)
        val ma = maxAttempts.coerceIn(4, 10)
        val ts = timerSeconds.coerceIn(30, 3600)
        return copy(wordLength = wl, maxAttempts = ma, timerSeconds = ts)
    }
}
