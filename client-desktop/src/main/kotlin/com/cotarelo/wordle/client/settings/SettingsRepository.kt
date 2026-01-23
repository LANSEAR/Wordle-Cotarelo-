package com.cotarelo.wordle.client.settings

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class SettingsRepository(
    private val baseDir: Path = defaultBaseDir()
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val settingsFile: Path = baseDir.resolve("settings.json")

    fun load(): AppSettings {
        return try {
            if (!settingsFile.exists()) {
                val defaults = AppSettings().sanitized()
                save(defaults)
                defaults
            } else {
                val text = settingsFile.readText()
                json.decodeFromString(AppSettings.serializer(), text).sanitized()
            }
        } catch (_: Exception) {
            // Si hay corrupción o formato raro, volvemos a defaults
            val defaults = AppSettings().sanitized()
            runCatching { save(defaults) }
            defaults
        }
    }

    fun save(settings: AppSettings) {
        val safe = settings.sanitized()
        if (!Files.exists(baseDir)) baseDir.createDirectories()
        val text = json.encodeToString(AppSettings.serializer(), safe)
        settingsFile.writeText(text)
    }

    companion object {
        fun defaultBaseDir(): Path {
            val home = System.getProperty("user.home") ?: "."
            return Path.of(home, ".wordle-cotarelo")
        }
    }
}
