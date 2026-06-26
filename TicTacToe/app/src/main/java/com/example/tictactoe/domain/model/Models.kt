package com.example.tictactoe.domain.model

/** The two marks. X always moves first. */
enum class Player {
    X, O;

    fun opponent(): Player = if (this == X) O else X
}

/** A single board cell. */
sealed interface Cell {
    data object Empty : Cell
    data class Taken(val player: Player) : Cell
}

/** Terminal-or-not result of a board. */
sealed interface GameStatus {
    data object InProgress : GameStatus
    data class Won(val player: Player, val line: List<Int>) : GameStatus
    data object Draw : GameStatus
}

/** Immutable snapshot of a single round. */
data class GameState(
    val board: List<Cell> = List(9) { Cell.Empty },
    val currentPlayer: Player = Player.X,
    val status: GameStatus = GameStatus.InProgress,
)

enum class Difficulty { EASY, MEDIUM, HARD }

/** Offline-only modes. Online is intentionally excluded. */
enum class GameMode { PASS_AND_PLAY, VS_AI }
