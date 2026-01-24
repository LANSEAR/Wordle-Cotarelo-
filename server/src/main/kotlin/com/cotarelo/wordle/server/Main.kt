package com.cotarelo.wordle.server

import com.cotarelo.wordle.server.config.ServerConfig
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println()
    println("═══════════════════════════════════════════════════════")
    println("    Wordle Multiplayer Server - Kotlin Multiplatform")
    println("═══════════════════════════════════════════════════════")
    println()

    // Cargar configuración
    val config = ServerConfig.load()

    // Crear e iniciar servidor
    val server = WordleServer(config)

    // Manejar señal de salida
    Runtime.getRuntime().addShutdownHook(Thread {
        println("\n\n⚠️  Señal de interrupción recibida...")
        server.stop()
    })

    // Iniciar servidor
    server.start()
}
