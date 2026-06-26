package com.example.tictactoe.domain.engine

import com.example.tictactoe.domain.model.Cell
import com.example.tictactoe.domain.model.GameState
import com.example.tictactoe.domain.model.GameStatus

/** Pure tic-tac-toe rules. No Android, no state held — every call returns a new [GameState]. */
class GameEngine {

    private val lines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
        listOf(0, 4, 8), listOf(2, 4, 6),                  // diagonals
    )

    fun reset(): GameState = GameState()

    /** Applies a move at [index]. Illegal moves (occupied cell, finished game) return [state] unchanged. */
    fun move(state: GameState, index: Int): GameState {
        if (state.status != GameStatus.InProgress) return state
        if (state.board[index] != Cell.Empty) return state

        val board = state.board.toMutableList()
        board[index] = Cell.Taken(state.currentPlayer)
        val status = evaluate(board)
        return state.copy(
            board = board,
            currentPlayer = if (status == GameStatus.InProgress) {
                state.currentPlayer.opponent()
            } else {
                state.currentPlayer
            },
            status = status,
        )
    }

    private fun evaluate(board: List<Cell>): GameStatus {
        for (line in lines) {
            val (a, b, c) = line
            val first = board[a]
            if (first is Cell.Taken && first == board[b] && first == board[c]) {
                return GameStatus.Won(first.player, line)
            }
        }
        return if (board.none { it == Cell.Empty }) GameStatus.Draw else GameStatus.InProgress
    }
}
