package com.cotarelo.wordle.server.ai

import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.model.evaluateGuess
import com.cotarelo.wordle.shared.network.Difficulty

/**
 * IA para jugar Wordle con diferentes niveles de dificultad
 */
class WordleAI(
    private val dictionary: List<String>,
    private val difficulty: Difficulty
) {
    private val possibleWords = dictionary.toMutableList()
    private val attemptHistory = mutableListOf<String>()
    private val forbiddenLetters = mutableSetOf<Char>()
    private val confirmedLetters = mutableMapOf<Int, Char>() // posición -> letra
    private val presentLetters = mutableSetOf<Char>() // letras que están pero no sabemos dónde

    /**
     * Obtiene el siguiente intento de la IA
     */
    fun getNextGuess(): String {
        return when (difficulty) {
            Difficulty.EASY -> getEasyGuess()
            Difficulty.NORMAL -> getNormalGuess()
            Difficulty.HARD -> getHardGuess()
        }
    }

    /**
     * Actualiza el estado de la IA con el resultado del último intento
     */
    fun updateWithResult(guess: String, secret: String) {
        attemptHistory.add(guess)
        val result = evaluateGuess(secret, guess)

        // Analizar resultado para actualizar conocimiento
        guess.forEachIndexed { index, char ->
            when (result[index]) {
                TileState.Correct -> {
                    confirmedLetters[index] = char
                    presentLetters.remove(char)
                }
                TileState.Present -> {
                    presentLetters.add(char)
                }
                TileState.Absent -> {
                    // Solo marcar como prohibida si no está confirmada en otra posición
                    if (!confirmedLetters.values.contains(char) && char !in presentLetters) {
                        forbiddenLetters.add(char)
                    }
                }
                else -> {}
            }
        }

        // Filtrar palabras imposibles (solo para NORMAL y HARD)
        if (difficulty != Difficulty.EASY) {
            filterImpossibleWords(guess, result)
        }
    }

    /**
     * Nivel FÁCIL: Intento aleatorio del diccionario
     */
    private fun getEasyGuess(): String {
        val available = dictionary.filter { it !in attemptHistory }
        return if (available.isNotEmpty()) {
            available.random()
        } else {
            dictionary.random()
        }
    }

    /**
     * Nivel NORMAL: Elimina palabras imposibles y prefiere letras frecuentes
     */
    private fun getNormalGuess(): String {
        if (possibleWords.isEmpty()) {
            return dictionary.random()
        }

        // Si tenemos pocas opciones, elegir aleatoriamente de ellas
        if (possibleWords.size <= 5) {
            return possibleWords.random()
        }

        // Ordenar por frecuencia de letras comunes en español
        val scored = possibleWords.map { word ->
            word to scoreWordFrequency(word)
        }.sortedByDescending { it.second }

        return scored.first().first
    }

    /**
     * Nivel DIFÍCIL: Optimiza usando información conocida
     */
    private fun getHardGuess(): String {
        if (possibleWords.isEmpty()) {
            return dictionary.random()
        }

        // Si solo queda una palabra, es la respuesta
        if (possibleWords.size == 1) {
            return possibleWords.first()
        }

        // Estrategia: maximizar información ganada
        // 1. Priorizar palabras que usan letras confirmadas
        // 2. Incluir letras presentes en posiciones diferentes
        // 3. Maximizar eliminación de candidatos

        val scored = possibleWords.map { word ->
            var score = 0.0

            // Bonus por letras confirmadas en posición correcta
            confirmedLetters.forEach { (pos, char) ->
                if (word.getOrNull(pos) == char) {
                    score += 100.0
                }
            }

            // Bonus por incluir letras presentes
            presentLetters.forEach { char ->
                if (char in word) {
                    score += 50.0
                }
            }

            // Bonus por diversidad de letras (explorar más información)
            score += word.toSet().size * 5.0

            // Bonus por frecuencia de letras
            score += scoreWordFrequency(word)

            word to score
        }.sortedByDescending { it.second }

        return scored.first().first
    }

    /**
     * Puntúa una palabra según la frecuencia de letras en español
     */
    private fun scoreWordFrequency(word: String): Double {
        // Frecuencia aproximada de letras en español (más común = mayor valor)
        val letterFrequency = mapOf(
            'e' to 13.7, 'a' to 12.5, 'o' to 8.7, 's' to 8.0, 'r' to 6.9,
            'n' to 6.7, 'i' to 6.2, 'l' to 5.0, 'd' to 5.8, 'c' to 4.7,
            't' to 4.6, 'u' to 3.9, 'm' to 3.1, 'p' to 2.5, 'b' to 1.4,
            'g' to 1.0, 'v' to 0.9, 'y' to 0.9, 'q' to 0.9, 'h' to 0.7,
            'f' to 0.7, 'z' to 0.5, 'j' to 0.4, 'ñ' to 0.3, 'x' to 0.2,
            'w' to 0.02, 'k' to 0.01
        )

        return word.sumOf { letterFrequency[it.lowercaseChar()] ?: 0.0 }
    }

    /**
     * Filtra palabras que son imposibles según el resultado
     */
    private fun filterImpossibleWords(guess: String, result: List<TileState>) {
        possibleWords.removeAll { candidate ->
            !isWordPossible(candidate, guess, result)
        }
    }

    /**
     * Verifica si una palabra es posible dada la información conocida
     */
    private fun isWordPossible(candidate: String, guess: String, result: List<TileState>): Boolean {
        // Verificar letras confirmadas
        confirmedLetters.forEach { (pos, char) ->
            if (candidate.getOrNull(pos) != char) {
                return false
            }
        }

        // Verificar letras presentes (deben estar pero no en la posición del intento)
        presentLetters.forEach { char ->
            if (char !in candidate) {
                return false
            }
        }

        // Verificar letras prohibidas
        forbiddenLetters.forEach { char ->
            if (char in candidate) {
                return false
            }
        }

        // Verificar que el patrón del intento coincida
        guess.forEachIndexed { index, char ->
            when (result[index]) {
                TileState.Correct -> {
                    if (candidate[index] != char) return false
                }
                TileState.Present -> {
                    // La letra debe estar pero NO en esta posición
                    if (candidate[index] == char) return false
                    if (char !in candidate) return false
                }
                TileState.Absent -> {
                    // La letra no debe estar (a menos que esté confirmada en otra posición)
                    if (char in candidate && char !in confirmedLetters.values) {
                        return false
                    }
                }
                else -> {}
            }
        }

        return true
    }

    /**
     * Reinicia el estado de la IA para una nueva ronda
     */
    fun reset() {
        possibleWords.clear()
        possibleWords.addAll(dictionary)
        attemptHistory.clear()
        forbiddenLetters.clear()
        confirmedLetters.clear()
        presentLetters.clear()
    }

    /**
     * Obtiene estadísticas de depuración
     */
    fun getDebugInfo(): String {
        return buildString {
            appendLine("🤖 Estado de la IA ($difficulty):")
            appendLine("   Palabras posibles: ${possibleWords.size}")
            appendLine("   Intentos realizados: ${attemptHistory.size}")
            appendLine("   Letras confirmadas: $confirmedLetters")
            appendLine("   Letras presentes: $presentLetters")
            appendLine("   Letras prohibidas: $forbiddenLetters")
        }
    }
}
