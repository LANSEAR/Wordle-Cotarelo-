package com.cotarelo.wordle.client.settings

import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty {
    EASY, NORMAL, HARD
}
