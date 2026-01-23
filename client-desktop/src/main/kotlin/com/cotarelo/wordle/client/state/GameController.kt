package com.cotarelo.wordle.client.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cotarelo.wordle.client.data.WordRepository
import com.cotarelo.wordle.client.settings.Difficulty
import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.model.evaluateGuess

class GameController(
    private var wordLength: Int = 5,
    private var maxAttempts: Int = 6,
    private var difficulty: Difficulty = Difficulty.NORMAL
) {
    var state by mutableStateOf(GameState(rows = maxAttempts, cols = wordLength))
        private set

    // Exponer solución para UI (diálogo fin de ronda/serie)
    val solution: String
        get() = secret

    // Listas para escoger secret según dificultad
    private var commonList: List<String> = emptyList()
    private var rareList: List<String> = emptyList()
    private var secretList: List<String> = emptyList()

    // Set para validar guesses (unión common+rare)
    private var allowedSet: Set<String> = emptySet()

    private var secret: String = ""

    init {
        reloadDictionaries()
        pickNewSecret()
    }

    fun newGame(
        wordLength: Int = this.wordLength,
        maxAttempts: Int = this.maxAttempts,
        difficulty: Difficulty = this.difficulty
    ) {
        val wl = wordLength.coerceIn(4, 7)
        val ma = maxAttempts.coerceIn(4, 10)

        val needReload =
            wl != this.wordLength ||
                    ma != this.maxAttempts ||
                    difficulty != this.difficulty

        this.wordLength = wl
        this.maxAttempts = ma
        this.difficulty = difficulty

        if (needReload) reloadDictionaries()
        pickNewSecret()

        state = GameState(rows = ma, cols = wl)
    }

    fun forceLoseByTimeout() {
        if (state.status != GameState.Status.Playing) return
        state = state.copy(
            status = GameState.Status.Lost,
            message = "Tiempo agotado. Era: $secret",
            currentCol = state.cols
        )
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

        if (guess !in allowedSet) {
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
            // OJO: este mensaje puede no llegar a verse si tu UI muestra un diálogo y pasa de ronda
            message = when {
                won -> "¡Correcto!"
                lastRow -> "Fin. Era: $secret"
                else -> null
            }
        )
    }

    private fun reloadDictionaries() {
        val wl = wordLength.coerceIn(4, 7)

        val commonPath = "words_es_${wl}_common.txt"
        val rarePath = "words_es_${wl}_rare.txt"

        commonList = WordRepository.loadWordsFromResource(commonPath, length = wl)
        rareList = WordRepository.loadWordsFromResource(rarePath, length = wl)

        // Allowed guesses = unión (si una está vacía, usa la otra)
        allowedSet = (commonList + rareList).toHashSet().ifEmpty {
            // último fallback: permitir lo que haya en la secretList (la rellenamos abajo)
            emptySet()
        }

        // Secret list según dificultad
        secretList = when (difficulty) {
            Difficulty.EASY -> commonList.ifEmpty { rareList }
            Difficulty.HARD -> rareList.ifEmpty { commonList }
            Difficulty.NORMAL -> mixCommonRare(commonList, rareList, commonWeight = 0.7)
        }

        // Si allowedSet estaba vacío pero hay secretList, úsala como allowed también
        if (allowedSet.isEmpty() && secretList.isNotEmpty()) {
            allowedSet = secretList.toHashSet()
        }

        // Si todo está vacío, mejor error claro (así no te quedas “atascado”)
        if (secretList.isEmpty()) {
            error(
                "Diccionarios vacíos para longitud $wl.\n" +
                        "Revisa resources: $commonPath y $rarePath (deben tener palabras de $wl letras)."
            )
        }
    }

    private fun pickNewSecret() {
        secret = secretList.random()
    }

    /**
     * Mezcla common/rare para NORMAL.
     * - 70% common, 30% rare por defecto
     * - Si rare está vacío, usa common
     * - Si common está vacío, usa rare
     */
    private fun mixCommonRare(common: List<String>, rare: List<String>, commonWeight: Double): List<String> {
        if (common.isEmpty()) return rare
        if (rare.isEmpty()) return common

        val total = common.size + rare.size
        val commonTarget = (total * commonWeight).toInt().coerceAtLeast(1)

        val mixed = ArrayList<String>(total)
        mixed.addAll(common.shuffled().take(commonTarget))
        mixed.addAll(rare.shuffled())

        // quitamos duplicados si hubiera
        return mixed.distinct()
    }

    private fun List<List<Char>>.updateChar(r: Int, c: Int, value: Char): List<List<Char>> =
        mapIndexed { ri, row ->
            if (ri != r) row else row.mapIndexed { ci, ch -> if (ci == c) value else ch }
        }

    private fun List<List<TileState>>.updateRow(r: Int, newRow: List<TileState>): List<List<TileState>> =
        mapIndexed { ri, row -> if (ri == r) newRow else row }

    companion object {
        fun newSinglePlayer(
            wordLength: Int,
            maxAttempts: Int,
            difficulty: Difficulty
        ): GameController {
            return GameController(
                wordLength = wordLength.coerceIn(4, 7),
                maxAttempts = maxAttempts.coerceIn(4, 10),
                difficulty = difficulty
            )
        }
    }
}
