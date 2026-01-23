package com.cotarelo.wordle.client.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cotarelo.wordle.client.data.WordRepository
import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.model.evaluateGuess

class GameController(
    private var resourcePath: String = "words_es_5.txt",
    private var wordLength: Int = 5,
    private var maxAttempts: Int = 6
) {
    var state by mutableStateOf(GameState(rows = maxAttempts, cols = wordLength))
        private set

    private var dictionaryList: List<String> =
        WordRepository.loadWordsFromResource(resourcePath, length = wordLength)

    private var dictionarySet: Set<String> = dictionaryList.toHashSet()

    private var secret: String = dictionaryList.random()

    /**
     * Reinicia la partida respetando (o cambiando) longitud e intentos.
     * - Relee el diccionario si cambia la longitud o el resourcePath
     * - Elige nueva palabra secreta
     * - Resetea el state (tablero + cursor + status)
     */
    fun newGame(
        wordLength: Int = this.wordLength,
        maxAttempts: Int = this.maxAttempts,
        resourcePath: String = this.resourcePath
    ) {
        val wl = wordLength.coerceIn(4, 7)
        val ma = maxAttempts.coerceIn(4, 10)

        val needReload = (wl != this.wordLength) || (resourcePath != this.resourcePath)

        this.wordLength = wl
        this.maxAttempts = ma
        this.resourcePath = resourcePath

        if (needReload) {
            dictionaryList = WordRepository.loadWordsFromResource(resourcePath, length = wl)
            dictionarySet = dictionaryList.toHashSet()
        }

        secret = dictionaryList.random()
        state = GameState(rows = ma, cols = wl)
    }

    fun onLetter(c: Char) {
        if (state.status != GameState.Status.Playing) return
        if (state.currentCol >= state.cols) return

        val row = state.currentRow
        val col = state.currentCol

        state = state.copy(
            letters = state.letters.updateChar(row, col, c.uppercaseChar()),
            currentCol = col + 1,
            message = null
        )
    }

    fun onBackspace() {
        if (state.status != GameState.Status.Playing) return
        val row = state.currentRow
        val col = state.currentCol
        if (col <= 0) return

        val newCol = col - 1
        state = state.copy(
            letters = state.letters.updateChar(row, newCol, ' '),
            currentCol = newCol,
            message = null
        )
    }

    fun onEnter() {
        if (state.status != GameState.Status.Playing) return

        if (state.currentCol < state.cols) {
            state = state.copy(message = "Faltan letras")
            return
        }

        val row = state.currentRow
        val guess = state.letters[row].joinToString("").uppercase()

        if (guess !in dictionarySet) {
            state = state.copy(message = "No está en el diccionario")
            return
        }

        val eval = evaluateGuess(secret, guess)

        val newStates = state.states.updateRow(row, eval)
        val won = eval.all { it == TileState.Correct }
        val lastRow = row == state.rows - 1

        state = state.copy(
            states = newStates,
            currentRow = if (won || lastRow) row else row + 1,
            currentCol = if (won || lastRow) state.cols else 0,
            status = when {
                won -> GameState.Status.Won
                lastRow -> GameState.Status.Lost
                else -> GameState.Status.Playing
            },
            message = when {
                won -> "¡Correcto!"
                lastRow -> "Fin. Era: $secret"
                else -> null
            }
        )
    }

    private fun List<List<Char>>.updateChar(r: Int, c: Int, value: Char): List<List<Char>> =
        mapIndexed { ri, row ->
            if (ri != r) row else row.mapIndexed { ci, ch -> if (ci == c) value else ch }
        }

    private fun List<List<TileState>>.updateRow(r: Int, newRow: List<TileState>): List<List<TileState>> =
        mapIndexed { ri, row -> if (ri == r) newRow else row }

    companion object {
        fun newSinglePlayer(wordLength: Int, maxAttempts: Int): GameController {
            val wl = wordLength.coerceIn(4, 7)
            val ma = maxAttempts.coerceIn(4, 10)
            val resource = "words_es_${wl}.txt" // IMPORTANTE: crea estos ficheros o ajusta si no los tienes

            return GameController(
                resourcePath = resource,
                wordLength = wl,
                maxAttempts = ma
            )
        }
    }
}
