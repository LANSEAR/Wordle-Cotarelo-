package com.cotarelo.wordle.client.state

import com.cotarelo.wordle.shared.model.TileState

data class GameState(
    val rows: Int = 6,
    val cols: Int = 5,
    val letters: List<List<Char>> = List(rows) { List(cols) { ' ' } },
    val states: List<List<TileState>> = List(rows) { List(cols) { TileState.Empty } },
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val status: Status = Status.Playing,
    val message: String? = null
) {
    enum class Status { Playing, Won, Lost }
}
