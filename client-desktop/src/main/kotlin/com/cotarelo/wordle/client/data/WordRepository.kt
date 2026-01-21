package com.cotarelo.wordle.client.data

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object WordRepository {

    fun loadWordsFromResource(resourcePath: String, length: Int = 5): List<String> {
        val cl = Thread.currentThread().contextClassLoader
            ?: WordRepository::class.java.classLoader

        val normalized = resourcePath.removePrefix("/")

        val stream = cl.getResourceAsStream(normalized)
            ?: error(
                "No se encontró el recurso: $normalized\n" +
                        "Comprueba que existe en: client-desktop/src/main/resources/$normalized\n" +
                        "URL encontrada: ${cl.getResource(normalized)}"
            )

        stream.use { input ->
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).useLines { lines ->
                return lines
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { it.uppercase() }
                    .filter { it.length == length }
                    .distinct()
                    .toList()
            }
        }
    }
}
