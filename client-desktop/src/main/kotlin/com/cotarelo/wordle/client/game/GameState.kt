package com.cotarelo.wordle.client.game

import com.cotarelo.wordle.shared.model.TileState

data class GameState(
    val rows: Int,
    val cols: Int,
    val grid: List<MutableList<Char?>>,
    val tileStates: List<MutableList<TileState>>,
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val isFinished: Boolean = false,
    val message: String? = null
) {
    companion object {
        fun createEmpty(rows: Int, cols: Int): GameState {
            val r = rows.coerceIn(1, 20)
            val c = cols.coerceIn(1, 12)
            val grid = List(r) { MutableList<Char?>(c) { null } }
            val states = List(r) { MutableList(c) { TileState.Empty } }
            return GameState(
                rows = r,
                cols = c,
                grid = grid,
                tileStates = states
            )
        }
    }
}
