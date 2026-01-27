package com.cotarelo.wordle.client.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cotarelo.wordle.client.network.PVPServerConnection
import com.cotarelo.wordle.client.network.PVPServerResponse
import com.cotarelo.wordle.shared.model.TileState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Controlador para partidas PVP
 */
class PVPGameController(
    private val wordLength: Int,
    private val maxAttempts: Int,
    private val connection: PVPServerConnection,
    private val roomId: String
) {
    var state by mutableStateOf(GameState(rows = maxAttempts, cols = wordLength))
        private set

    var opponentAttempts by mutableStateOf(0)
        private set

    var opponentWon by mutableStateOf(false)
        private set

    var roundWinner by mutableStateOf<String?>(null)
        private set

    var gameWinner by mutableStateOf<String?>(null)
        private set

    var youWon by mutableStateOf(false)
        private set

    var solution by mutableStateOf("")
        private set

    var player1Rounds by mutableStateOf(0)
        private set

    var player2Rounds by mutableStateOf(0)
        private set

    var opponentDisconnected by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            connection.serverResponses.collect { response ->
                handleServerResponse(response)
            }
        }
    }

    /**
     * Procesa la entrada de una letra
     */
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

    /**
     * Procesa el borrado
     */
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

    /**
     * Procesa el envío de un intento
     */
    fun onEnter() {
        if (state.status != GameState.Status.Playing) return

        if (state.currentCol < state.cols) {
            state = state.copy(message = "Faltan letras")
            return
        }

        val row = state.currentRow
        val guess = state.letters[row].joinToString("").uppercase()

        // Enviar intento al servidor
        scope.launch {
            connection.sendGuessPVP(guess, row + 1)
        }
    }

    /**
     * Maneja respuestas del servidor
     */
    private fun handleServerResponse(response: PVPServerResponse) {
        when (response) {
            is PVPServerResponse.GuessResult -> {
                if (!response.data.isValid) {
                    state = state.copy(message = response.data.message ?: "Palabra no válida")
                    clearCurrentRow()
                    return
                }

                val row = state.currentRow
                val evaluation = response.data.result.map { parseState(it) }
                val newStates = state.states.updateRow(row, evaluation)

                val won = evaluation.all { it == TileState.Correct }
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
                    message = response.data.message ?: when {
                        won -> "¡Ganaste!"
                        lastRow -> "Sin intentos"
                        else -> null
                    }
                )
            }

            is PVPServerResponse.OpponentProgress -> {
                opponentAttempts = response.data.attempts
                opponentWon = response.data.won
                println("🎮 Oponente: ${response.data.attempts} intentos, ganó: ${response.data.won}")
            }

            is PVPServerResponse.RoundWinnerPVP -> {
                roundWinner = response.data.winner
                youWon = response.data.youWon
                solution = response.data.solution
                player1Rounds = response.data.player1Attempts
                player2Rounds = response.data.player2Attempts

                state = state.copy(
                    message = if (youWon) "¡Ganaste la ronda!" else "Perdiste la ronda",
                    status = if (youWon) GameState.Status.Won else GameState.Status.Lost
                )

                println("🏁 Ronda terminada - ${if (youWon) "Ganaste" else "Perdiste"}")
            }

            is PVPServerResponse.GameWinnerPVP -> {
                gameWinner = response.data.winner
                youWon = response.data.youWon
                player1Rounds = response.data.player1Rounds
                player2Rounds = response.data.player2Rounds

                println("🏆 Juego terminado - ${if (youWon) "¡GANASTE!" else "Perdiste"}")
            }

            is PVPServerResponse.OpponentDisconnected -> {
                opponentDisconnected = true
                state = state.copy(message = "${response.data.opponentName} se desconectó")
                println("👋 Oponente desconectado")
            }

            is PVPServerResponse.Error -> {
                state = state.copy(message = "Error: ${response.data.message}")
                println("❌ Error del servidor: ${response.data.message}")
            }

            else -> {
                println("⚠️  Respuesta no manejada en PVPGameController: $response")
            }
        }
    }

    private fun parseState(stateStr: String): TileState {
        return when (stateStr) {
            "CORRECT" -> TileState.Correct
            "PRESENT" -> TileState.Present
            "ABSENT" -> TileState.Absent
            else -> TileState.Empty
        }
    }

    private fun clearCurrentRow() {
        val row = state.currentRow
        var lettersCleared = state.letters
        for (c in 0 until state.cols) {
            lettersCleared = lettersCleared.updateChar(row, c, ' ')
        }
        state = state.copy(
            letters = lettersCleared,
            currentCol = 0
        )
    }

    fun clearMessage() {
        state = state.copy(message = null)
    }

    fun forceLoseByTimeout() {
        if (state.status != GameState.Status.Playing) return
        if (roundWinner != null) return // Ya terminó la ronda
        if (state.currentRow >= state.rows) return // Ya completó todos los intentos

        val row = state.currentRow

        // Enviar palabra vacía al servidor para que cuente el timeout
        // El servidor responderá con la evaluación y actualizará el estado
        scope.launch {
            val emptyWord = " ".repeat(state.cols)
            connection.sendGuessPVP(emptyWord, row + 1)
        }

        // NO actualizamos el estado local aquí - esperamos la respuesta del servidor
        // Esto evita duplicar la fila vacía
        // El servidor manejará el fin de ronda cuando ambos jugadores hayan terminado
    }

    fun nextRound() {
        roundWinner = null
        state = GameState(rows = maxAttempts, cols = wordLength)
        opponentAttempts = 0
        opponentWon = false
    }

    suspend fun disconnect() {
        connection.leaveRoom()
        connection.disconnect()
    }

    // Utilidades
    private fun List<List<Char>>.updateChar(r: Int, c: Int, value: Char): List<List<Char>> =
        mapIndexed { ri, row ->
            if (ri != r) row else row.mapIndexed { ci, ch -> if (ci == c) value else ch }
        }

    private fun List<List<TileState>>.updateRow(r: Int, newRow: List<TileState>): List<List<TileState>> =
        mapIndexed { ri, row -> if (ri == r) newRow else row }
}
