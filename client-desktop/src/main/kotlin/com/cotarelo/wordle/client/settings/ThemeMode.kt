package com.cotarelo.wordle.client.settings

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
    LIGHT, DARK
    // Si quieres, luego añadimos SYSTEM
}
