package com.cotarelo.wordle.server.config

import java.io.File
import java.util.Properties

/**
 * Configuración del servidor cargada desde server.properties
 */
data class ServerConfig(
    val host: String,
    val port: Int,
    val maxClients: Int,
    val connectionTimeout: Int,
    val debugEnabled: Boolean
) {
    companion object {
        fun load(propertiesPath: String = "server.properties"): ServerConfig {
            val props = Properties()
            val file = File(propertiesPath)

            if (!file.exists()) {
                println("⚠️  Archivo server.properties no encontrado en: ${file.absolutePath}")
                println("⚠️  Usando configuración por defecto")
                return default()
            }

            file.inputStream().use { props.load(it) }

            return ServerConfig(
                host = props.getProperty("server.host", "localhost"),
                port = props.getProperty("server.port", "5678").toIntOrNull() ?: 5678,
                maxClients = props.getProperty("max.clients", "10").toIntOrNull() ?: 10,
                connectionTimeout = props.getProperty("connection.timeout", "300").toIntOrNull() ?: 300,
                debugEnabled = props.getProperty("debug.enabled", "true").toBoolean()
            )
        }

        private fun default() = ServerConfig(
            host = "localhost",
            port = 5678,
            maxClients = 10,
            connectionTimeout = 300,
            debugEnabled = true
        )
    }

    fun print() {
        println("📋 Configuración del Servidor:")
        println("   Host: $host")
        println("   Puerto: $port")
        println("   Máximo de clientes: $maxClients")
        println("   Timeout: $connectionTimeout segundos")
        println("   Debug: ${if (debugEnabled) "activado" else "desactivado"}")
    }
}
