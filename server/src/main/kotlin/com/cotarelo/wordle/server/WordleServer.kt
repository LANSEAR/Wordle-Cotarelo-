package com.cotarelo.wordle.server

import com.cotarelo.wordle.server.config.ServerConfig
import com.cotarelo.wordle.server.data.RecordsManager
import com.cotarelo.wordle.server.network.SimpleClientHandler
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Servidor principal de Wordle Multijugador
 */
class WordleServer(private val config: ServerConfig) {

    private val recordsManager = RecordsManager()
    private val clientCounter = AtomicInteger(0)
    private val activeClients = mutableListOf<Job>()

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    /**
     * Inicia el servidor
     */
    suspend fun start() = coroutineScope {
        try {
            serverSocket = ServerSocket(config.port)
            isRunning = true

            println("╔═══════════════════════════════════════════════════════╗")
            println("║     🎮 SERVIDOR WORDLE MULTIJUGADOR INICIADO 🎮      ║")
            println("╚═══════════════════════════════════════════════════════╝")
            println()
            config.print()
            println()
            recordsManager.printStats()
            println("🚀 Servidor escuchando en ${config.host}:${config.port}")
            println("⏳ Esperando conexiones de clientes...")
            println("   (Presiona Ctrl+C para detener el servidor)")
            println()

            while (isRunning) {
                try {
                    val clientSocket = serverSocket?.accept() ?: break

                    // Verificar límite de clientes
                    if (activeClients.size >= config.maxClients) {
                        println("⚠️  Máximo de clientes alcanzado, rechazando conexión")
                        clientSocket.close()
                        continue
                    }

                    val clientId = clientCounter.incrementAndGet()
                    val handler = SimpleClientHandler(clientSocket, recordsManager, clientId)

                    // Lanzar handler en una corrutina
                    val job = launch(Dispatchers.IO) {
                        try {
                            handler.handle()
                        } finally {
                            activeClients.remove(coroutineContext[Job])
                            println("📊 Clientes activos: ${activeClients.size}/${config.maxClients}")
                        }
                    }

                    activeClients.add(job)
                    println("📊 Clientes activos: ${activeClients.size}/${config.maxClients}")

                } catch (e: SocketException) {
                    if (isRunning) {
                        println("❌ Error de socket: ${e.message}")
                    }
                } catch (e: Exception) {
                    println("❌ Error aceptando cliente: ${e.message}")
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            println("❌ Error fatal del servidor: ${e.message}")
            e.printStackTrace()
        } finally {
            stop()
        }
    }

    /**
     * Detiene el servidor
     */
    fun stop() {
        println("\n🛑 Deteniendo servidor...")
        isRunning = false

        // Cancelar todos los clientes activos
        activeClients.forEach { it.cancel() }
        activeClients.clear()

        // Cerrar socket
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            println("⚠️  Error cerrando socket: ${e.message}")
        }

        println("✅ Servidor detenido correctamente")
        println()
        recordsManager.printStats()
    }
}
