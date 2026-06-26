package com.skystone1000.xoxo.domain.ai

import com.skystone1000.xoxo.domain.model.GameState

/** Chooses a board index for the [GameState.currentPlayer] to play. */
interface AiOpponent {
    fun chooseMove(state: GameState): Int
}
