package com.cotarelo.wordle.client.services

import com.cotarelo.wordle.client.network.SimpleServerConnection
import com.cotarelo.wordle.client.network.ServerResponse
import com.cotarelo.wordle.client.state.RecordsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Servicio para gestionar la sincronización de records con el servidor
 */
class RecordsService(
    private val viewModel: RecordsViewModel,
    private val scope: CoroutineScope
) {
    private var connection: SimpleServerConnection? = null

    /**
     * Sincroniza los records con el servidor
     */
    suspend fun syncRecords() {
        viewModel.isLoading = true

        try {
            // Crear conexión temporal
            connection = SimpleServerConnection()

            if (!connection!!.connect()) {
                viewModel.setError("No se pudo conectar al servidor")
                return
            }

            // Escuchar respuestas del servidor
            scope.launch {
                connection!!.serverResponses
                    .filterIsInstance<ServerResponse.RecordsData>()
                    .collect { response ->
                        viewModel.updateRecords(response.data.records)
                        disconnect()
                    }
            }

            // Escuchar errores
            scope.launch {
                connection!!.serverResponses
                    .filterIsInstance<ServerResponse.Error>()
                    .collect { response ->
                        viewModel.setError(response.data.message)
                        disconnect()
                    }
            }

            // Solicitar records
            connection!!.syncRecords()

        } catch (e: Exception) {
            viewModel.setError("Error sincronizando records: ${e.message}")
            disconnect()
        }
    }

    /**
     * Desconecta del servidor
     */
    private suspend fun disconnect() {
        try {
            connection?.disconnect()
            connection = null
        } catch (e: Exception) {
            println("Error al desconectar RecordsService: ${e.message}")
        }
    }
}
