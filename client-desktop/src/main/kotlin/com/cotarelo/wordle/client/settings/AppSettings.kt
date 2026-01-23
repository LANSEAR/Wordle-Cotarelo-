package com.cotarelo.wordle.client.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val wordLength: Int = 5,
    val maxAttempts: Int = 6,
    val roundsBestOf: Int = 1,          // 1 = sin rondas, 3/5/7 = BO3/5/7
    val difficulty: Difficulty = Difficulty.NORMAL,
    val timerEnabled: Boolean = false,
    val timerSeconds: Int = 180
) {
    fun sanitized(): AppSettings {
        val wl = wordLength.coerceIn(4, 7)
        val ma = maxAttempts.coerceIn(4, 10)
        val bo = roundsBestOf.coerceIn(1, 7).let { if (it in setOf(1,3,5,7)) it else 1 }
        val ts = timerSeconds.coerceIn(30, 180) // máximo 3 min según enunciado
        return copy(wordLength = wl, maxAttempts = ma, roundsBestOf = bo, timerSeconds = ts)
    }
}
