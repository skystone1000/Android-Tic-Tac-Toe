package com.skystone1000.domain.ai

import com.skystone1000.domain.engine.GameEngine
import com.skystone1000.domain.model.Cell
import com.skystone1000.domain.model.Difficulty
import com.skystone1000.domain.model.GameState
import com.skystone1000.domain.model.GameStatus
import com.skystone1000.domain.model.Player
import kotlin.random.Random

/**
 * Minimax AI with a difficulty knob.
 *
 * HARD plays optimally (unbeatable). MEDIUM and EASY mix in random moves so they
 * are beatable and feel more human. [random] is injectable for deterministic tests.
 */
class MinimaxAi(
    private val engine: GameEngine,
    private val difficulty: Difficulty,
    private val random: Random = Random.Default,
) : AiOpponent {

    private val randomChance: Double = when (difficulty) {
        Difficulty.EASY -> 0.8
        Difficulty.MEDIUM -> 0.3
        Difficulty.HARD -> 0.0
    }

    override fun chooseMove(state: GameState): Int {
        val empties = state.board.indices.filter { state.board[it] == Cell.Empty }
        require(empties.isNotEmpty()) { "No moves available" }
        if (random.nextDouble() < randomChance) return empties.random(random)
        return bestMove(state, empties)
    }

    private fun bestMove(state: GameState, empties: List<Int>): Int {
        val me = state.currentPlayer
        var bestScore = Int.MIN_VALUE
        var best = empties.first()
        for (i in empties) {
            val score = minimax(engine.move(state, i), me, depth = 1)
            if (score > bestScore) {
                bestScore = score
                best = i
            }
        }
        return best
    }

    // Score from `me`'s perspective. The depth term prefers faster wins and slower losses.
    private fun minimax(state: GameState, me: Player, depth: Int): Int {
        when (val st = state.status) {
            is GameStatus.Won -> return if (st.player == me) 10 - depth else depth - 10
            GameStatus.Draw -> return 0
            GameStatus.InProgress -> Unit
        }
        val empties = state.board.indices.filter { state.board[it] == Cell.Empty }
        val maximizing = state.currentPlayer == me
        var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
        for (i in empties) {
            val score = minimax(engine.move(state, i), me, depth + 1)
            best = if (maximizing) maxOf(best, score) else minOf(best, score)
        }
        return best
    }
}
