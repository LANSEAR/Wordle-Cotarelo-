package com.cotarelo.wordle.server.data

import com.cotarelo.wordle.shared.network.Records
import com.cotarelo.wordle.shared.network.PlayerStats
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Gestor de records del servidor con sincronización thread-safe
 */
class RecordsManager(private val recordsPath: String = "records.json") {
    private val lock = ReentrantReadWriteLock()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private var records: Records = loadFromFile()

    /**
     * Carga records desde el archivo JSON
     */
    private fun loadFromFile(): Records {
        val file = File(recordsPath)
        if (!file.exists()) {
            println("⚠️  Archivo records.json no encontrado, creando uno nuevo...")
            return Records()
        }

        return try {
            val jsonText = file.readText()
            if (jsonText.isBlank()) {
                Records()
            } else {
                json.decodeFromString<Records>(jsonText)
            }
        } catch (e: Exception) {
            println("⚠️  Error al cargar records: ${e.message}")
            println("⚠️  Creando archivo de records limpio...")
            Records()
        }
    }

    /**
     * Guarda records en el archivo JSON
     */
    private fun saveToFile() {
        try {
            val jsonText = json.encodeToString(records)
            File(recordsPath).writeText(jsonText)
        } catch (e: Exception) {
            println("❌ Error al guardar records: ${e.message}")
        }
    }

    /**
     * Obtiene los records actuales (thread-safe)
     */
    fun getRecords(): Records = lock.read {
        records
    }

    /**
     * Obtiene las estadísticas de un jugador
     */
    fun getPlayerStats(playerName: String): PlayerStats? = lock.read {
        records.players[playerName]
    }

    /**
     * Actualiza las estadísticas de un jugador después de una partida
     */
    fun updatePlayerStats(
        playerName: String,
        won: Boolean,
        attempts: Int,
        wordsGuessed: Int,
        totalWords: Int
    ) = lock.write {
        val currentStats = records.players[playerName] ?: PlayerStats()

        val newStats = currentStats.copy(
            gamesWon = currentStats.gamesWon + if (won) 1 else 0,
            gamesLost = currentStats.gamesLost + if (!won) 1 else 0,
            totalGames = currentStats.totalGames + 1,
            currentStreak = if (won) currentStats.currentStreak + 1 else 0,
            maxStreak = if (won) maxOf(currentStats.maxStreak, currentStats.currentStreak + 1) else currentStats.maxStreak,
            averageAttempts = calculateNewAverage(
                currentStats.averageAttempts,
                currentStats.totalGames,
                attempts.toDouble()
            ),
            attemptsDistribution = updateDistribution(currentStats.attemptsDistribution, attempts),
            wordsGuessed = currentStats.wordsGuessed + wordsGuessed,
            totalWords = currentStats.totalWords + totalWords
        )

        records = records.copy(
            players = records.players + (playerName to newStats)
        )

        saveToFile()
    }

    /**
     * Calcula el nuevo promedio de intentos
     */
    private fun calculateNewAverage(currentAverage: Double, count: Int, newValue: Double): Double {
        if (count == 0) return newValue
        return (currentAverage * count + newValue) / (count + 1)
    }

    /**
     * Actualiza la distribución de intentos
     */
    private fun updateDistribution(current: Map<Int, Int>, attempts: Int): Map<Int, Int> {
        val mutable = current.toMutableMap()
        mutable[attempts] = (mutable[attempts] ?: 0) + 1
        return mutable.toMap()
    }

    /**
     * Reinicia todos los records (para testing)
     */
    fun reset() = lock.write {
        records = Records()
        saveToFile()
    }

    /**
     * Imprime estadísticas actuales
     */
    fun printStats() {
        lock.read {
            println("\n📊 RECORDS ACTUALES:")
            println("═══════════════════════════════════════")
            if (records.players.isEmpty()) {
                println("  (No hay jugadores registrados)")
            } else {
                records.players.forEach { (name, stats) ->
                    println("\n👤 $name:")
                    println("   Partidas: ${stats.totalGames} (${stats.gamesWon}G - ${stats.gamesLost}P)")
                    println("   Win Rate: ${"%.1f".format(stats.winRate * 100)}%")
                    println("   Racha actual: ${stats.currentStreak} | Máxima: ${stats.maxStreak}")
                    println("   Promedio intentos: ${"%.2f".format(stats.averageAttempts)}")
                    println("   Palabras adivinadas: ${stats.wordsGuessed}/${stats.totalWords} (${"%.1f".format(stats.guessRate * 100)}%)")
                }
            }
            println("═══════════════════════════════════════\n")
        }
    }
}
