package com.cotarelo.wordle.client.network

import com.cotarelo.wordle.shared.network.*

/**
 * Respuestas del servidor para modo PVE
 */
sealed class ServerResponse {
    data class GameStarted(val data: GameStartedResponse) : ServerResponse()
    data class GuessResult(val data: GuessResultResponse) : ServerResponse()
    data class AIMove(val data: AIMoveResponse) : ServerResponse()
    data class RoundWinner(val data: RoundWinnerResponse) : ServerResponse()
    data class GameWinner(val data: GameWinnerResponse) : ServerResponse()
    data class RecordsData(val data: RecordsDataResponse) : ServerResponse()
    data class Error(val data: ErrorResponse) : ServerResponse()
    data class Unknown(val type: String) : ServerResponse()
}
