package com.skystone1000.xoxo.domain.ai

import com.skystone1000.xoxo.domain.engine.GameEngine
import com.skystone1000.xoxo.domain.model.Cell
import com.skystone1000.xoxo.domain.model.Difficulty
import com.skystone1000.xoxo.domain.model.GameState
import com.skystone1000.xoxo.domain.model.GameStatus
import com.skystone1000.xoxo.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MinimaxAiTest {

    private val engine = GameEngine()

    private fun state(x: List<Int>, o: List<Int>, toMove: Player): GameState {
        val cells = MutableList<Cell>(9) { Cell.Empty }
        x.forEach { cells[it] = Cell.Taken(Player.X) }
        o.forEach { cells[it] = Cell.Taken(Player.O) }
        return GameState(board = cells, currentPlayer = toMove, status = GameStatus.InProgress)
    }

    @Test
    fun `HARD takes the immediate winning move`() {
        // O to move with O at 0,1 -> must complete the top row at 2.
        val s = state(x = listOf(3, 4, 6), o = listOf(0, 1), toMove = Player.O)
        val ai = MinimaxAi(engine, Difficulty.HARD, Random(0))
        assertEquals(2, ai.chooseMove(s))
    }

    @Test
    fun `HARD blocks the opponent's winning move`() {
        // O to move; X threatens the top row (X at 0,1) -> O must block at 2.
        val s = state(x = listOf(0, 1, 5), o = listOf(3, 4), toMove = Player.O)
        val ai = MinimaxAi(engine, Difficulty.HARD, Random(0))
        assertEquals(2, ai.chooseMove(s))
    }

    @Test
    fun `HARD never loses as the first player against random play`() {
        repeat(30) { seed ->
            val rng = Random(seed.toLong())
            val ai = MinimaxAi(engine, Difficulty.HARD, rng)
            val aiSide = Player.X
            var s = GameState()
            while (s.status == GameStatus.InProgress) {
                val move = if (s.currentPlayer == aiSide) {
                    ai.chooseMove(s)
                } else {
                    s.board.indices.filter { s.board[it] == Cell.Empty }.random(rng)
                }
                s = engine.move(s, move)
            }
            val status = s.status
            if (status is GameStatus.Won) {
                assertNotEquals("AI lost on seed $seed", aiSide.opponent(), status.player)
            }
        }
    }

    @Test
    fun `chooseMove always returns a legal empty cell`() {
        val ai = MinimaxAi(engine, Difficulty.EASY, Random(7))
        val move = ai.chooseMove(GameState())
        assertTrue(move in 0..8)
    }
}
