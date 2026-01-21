package com.cotarelo.wordle.shared.model

/**
 * Evalúa guess vs secret con reglas Wordle:
 * - Correct: letra misma posición
 * - Present: letra existe pero en otra posición (respetando repeticiones)
 * - Absent: no existe (o ya consumida)
 */
fun evaluateGuess(secret: String, guess: String): List<TileState> {
    require(secret.length == guess.length)

    val n = secret.length
    val result = MutableList(n) { TileState.Absent }

    val secretChars = secret.toCharArray()
    val guessChars = guess.toCharArray()

    // Cuenta letras restantes (no acertadas)
    val remaining = mutableMapOf<Char, Int>()

    // 1) marcar Correct y contabilizar el resto
    for (i in 0 until n) {
        if (guessChars[i] == secretChars[i]) {
            result[i] = TileState.Correct
        } else {
            remaining[secretChars[i]] = (remaining[secretChars[i]] ?: 0) + 1
        }
    }

    // 2) marcar Present/Absent usando remaining
    for (i in 0 until n) {
        if (result[i] == TileState.Correct) continue
        val c = guessChars[i]
        val count = remaining[c] ?: 0
        if (count > 0) {
            result[i] = TileState.Present
            remaining[c] = count - 1
        } else {
            result[i] = TileState.Absent
        }
    }

    return result
}
