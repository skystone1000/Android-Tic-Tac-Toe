package com.skystone1000.domain.ai

import com.skystone1000.domain.model.GameState

/** Chooses a board index for the [GameState.currentPlayer] to play. */
interface AiOpponent {
    fun chooseMove(state: GameState): Int
}
