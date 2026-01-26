package com.cotarelo.wordle.client.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cotarelo.wordle.client.network.ServerConnection
import com.cotarelo.wordle.client.network.ServerResponse
import com.cotarelo.wordle.shared.model.TileState
import com.cotarelo.wordle.shared.network.*
import com.cotarelo.wordle.shared.network.toModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Controlador para partidas online (PVE con servidor)
 */
class OnlineGameController(
    private val wordLength: Int,
    private val maxAttempts: Int,
    private val rounds: Int,
    private val difficulty: Difficulty
) {
    var state by mutableStateOf(GameState(rows = maxAttempts, cols = wordLength))
        private set

    var connectionStatus by mutableStateOf("Desconectado")
        private set

    var currentRound by mutableStateOf(1)
        private set

    var playerRoundsWon by mutableStateOf(0)
        private set

    var aiRoundsWon by mutableStateOf(0)
        private set

    var lastAIMove by mutableStateOf<AIMove?>(null)
        private set

    var roundWinner by mutableStateOf<Winner?>(null)
        private set

    var gameWinner by mutableStateOf<Winner?>(null)
        private set

    var solution by mutableStateOf("")
        private set

    private val connection = ServerConnection()
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        // Escuchar mensajes del servidor
        scope.launch {
            connection.serverMessages.collect { message ->
                handleServerMessage(message)
            }
        }
    }

    /**
     * Conecta al servidor e inicia el juego
     */
    suspend fun connectAndStart(): Boolean {
        connectionStatus = "Conectando..."
        val connected = connection.connect()

        if (!connected) {
            connectionStatus = "Error de conexión"
            state = state.copy(message = "No se pudo conectar al servidor")
            return false
        }

        connectionStatus = "Conectado"

        // Iniciar partida
        connection.startGame(
            mode = GameMode.PVE,
            rounds = rounds,
            wordLength = wordLength,
            maxAttempts = maxAttempts,
            difficulty = difficulty
        )

        return true
    }

    /**
     * Desconecta del servidor
     */
    suspend fun disconnect() {
        connection.disconnect()
        connectionStatus = "Desconectado"
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
            connection.sendGuess(guess, row + 1)
        }
    }

    /**
     * Maneja mensajes del servidor
     */
    private fun handleServerMessage(response: ServerResponse) {
        when (response) {
            is ServerResponse.GameStarted -> {
                println("🎮 Partida iniciada: ${response.data.gameId}")
                state = state.copy(message = "¡Partida iniciada!")
            }

            is ServerResponse.GuessResult -> {
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
                        won -> "¡Correcto!"
                        lastRow -> "Sin intentos"
                        else -> null
                    }
                )
            }

            is ServerResponse.AIMove -> {
                lastAIMove = AIMove(response.data.word, response.data.attemptNumber, response.data.result.map { parseState(it) }.map { it.toDto() })
                println("🤖 IA jugó: ${response.data.word} (intento ${response.data.attemptNumber})")
            }

            is ServerResponse.RoundWinner -> {
                roundWinner = parseWinner(response.data.winner)
                solution = response.data.solution

                when (response.data.winner) {
                    "PLAYER" -> playerRoundsWon++
                    "AI" -> aiRoundsWon++
                    else -> {}
                }

                state = state.copy(
                    message = "Ronda terminada. Solución: ${response.data.solution}",
                    status = if (response.data.winner == "PLAYER") GameState.Status.Won else GameState.Status.Lost
                )

                println("🏁 Ronda terminada - Ganador: ${response.data.winner}")
            }

            is ServerResponse.GameWinner -> {
                gameWinner = parseWinner(response.data.winner)
                println("🏆 Juego terminado - Ganador: ${response.data.winner}")
                println("   Jugador: ${response.data.playerRounds} | IA: ${response.data.aiRounds}")
            }

            is ServerResponse.Error -> {
                state = state.copy(message = "Error: ${response.data.message}")
                println("❌ Error del servidor: ${response.data.message}")
            }

            is ServerResponse.RecordsData -> {
                println("📊 Records recibidos del servidor")
            }

            is ServerResponse.Unknown -> {
                println("⚠️  Tipo de mensaje desconocido: ${response.type}")
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

    private fun parseWinner(winnerStr: String): Winner {
        return when (winnerStr) {
            "PLAYER" -> Winner.PLAYER
            "AI" -> Winner.AI
            "DRAW" -> Winner.DRAW
            else -> Winner.DRAW
        }
    }

    /**
     * Limpia la fila actual
     */
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

    /**
     * Limpia el mensaje
     */
    fun clearMessage() {
        state = state.copy(message = null)
    }

    /**
     * Inicia una nueva ronda
     */
    fun nextRound() {
        currentRound++
        roundWinner = null
        lastAIMove = null
        state = GameState(rows = maxAttempts, cols = wordLength)
    }

    // Utilidades
    private fun List<List<Char>>.updateChar(r: Int, c: Int, value: Char): List<List<Char>> =
        mapIndexed { ri, row ->
            if (ri != r) row else row.mapIndexed { ci, ch -> if (ci == c) value else ch }
        }

    private fun List<List<TileState>>.updateRow(r: Int, newRow: List<TileState>): List<List<TileState>> =
        mapIndexed { ri, row -> if (ri == r) newRow else row }
}

/**
 * Data class para mostrar movimientos de la IA en la UI
 */
data class AIMove(
    val word: String,
    val attemptNumber: Int,
    val result: List<TileStateDto>
)
