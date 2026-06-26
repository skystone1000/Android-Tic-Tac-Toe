package com.example.tictactoe.domain.ai

import com.example.tictactoe.domain.model.GameState

/** Chooses a board index for the [GameState.currentPlayer] to play. */
interface AiOpponent {
    fun chooseMove(state: GameState): Int
}
