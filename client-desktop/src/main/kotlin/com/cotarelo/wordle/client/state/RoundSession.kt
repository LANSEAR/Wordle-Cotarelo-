package com.cotarelo.wordle.client.state

data class RoundSession(
    val bestOf: Int = 1,
    val roundIndex: Int = 1,
    val wins: Int = 0,
    val losses: Int = 0
) {
    val totalRounds: Int = bestOf
    val isSeries: Boolean = bestOf > 1

    fun isFinished(): Boolean = isSeries && roundIndex > totalRounds
}
