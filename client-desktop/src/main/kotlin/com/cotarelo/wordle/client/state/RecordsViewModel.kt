package com.cotarelo.wordle.client.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cotarelo.wordle.shared.network.Records
import com.cotarelo.wordle.shared.network.PlayerStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar los records del jugador
 */
class RecordsViewModel(
    val playerName: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    var records by mutableStateOf<Records?>(null)

    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Obtiene las estadísticas del jugador actual
     */
    val playerStats: PlayerStats?
        get() = records?.players?.get(playerName)

    /**
     * Obtiene el top de jugadores ordenados por win rate
     */
    val topPlayers: List<Pair<String, PlayerStats>>
        get() = records?.players?.entries
            ?.map { it.key to it.value }
            ?.filter { it.second.totalGames >= 5 } // Mínimo 5 partidas para ranking
            ?.sortedByDescending { it.second.winRate }
            ?.take(10)
            ?: emptyList()

    /**
     * Actualiza los records desde el servidor
     */
    fun updateRecords(newRecords: Records) {
        records = newRecords
        errorMessage = null
        isLoading = false
    }

    /**
     * Establece un mensaje de error
     */
    fun setError(message: String) {
        errorMessage = message
        isLoading = false
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        errorMessage = null
    }
}
