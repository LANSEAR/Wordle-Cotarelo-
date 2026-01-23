package com.cotarelo.wordle.client.data

import com.cotarelo.wordle.client.settings.Difficulty

object WordResources {
    fun secretListResource(wordLength: Int, difficulty: Difficulty): String {
        val wl = wordLength.coerceIn(4, 7)
        return when (difficulty) {
            Difficulty.EASY -> "words_es_${wl}_common.txt"
            Difficulty.NORMAL -> "words_es_${wl}_common.txt" // puedes mezclar luego
            Difficulty.HARD -> "words_es_${wl}_rare.txt"
        }
    }

    // Si quieres validar guesses contra una lista grande en el futuro:
    fun allowedListResource(wordLength: Int): String {
        val wl = wordLength.coerceIn(4, 7)
        return "words_es_${wl}.txt" // por ahora usamos el general (o unión common+rare)
    }
}
