package com.skystone1000.xoxo.domain.engine

import com.skystone1000.xoxo.domain.model.Cell
import com.skystone1000.xoxo.domain.model.GameState
import com.skystone1000.xoxo.domain.model.GameStatus
import com.skystone1000.xoxo.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTest {

    private val engine = GameEngine()

    @Test
    fun `X plays first cell and turn passes to O`() {
        val s = engine.move(GameState(), 0)
        assertEquals(Cell.Taken(Player.X), s.board[0])
        assertEquals(Player.O, s.currentPlayer)
        assertEquals(GameStatus.InProgress, s.status)
    }

    @Test
    fun `move on a taken cell leaves state unchanged`() {
        val s1 = engine.move(GameState(), 0)
        val s2 = engine.move(s1, 0)
        assertEquals(s1, s2)
    }

    @Test
    fun `top row of X is a win`() {
        var s = GameState()
        s = engine.move(s, 0) // X
        s = engine.move(s, 3) // O
        s = engine.move(s, 1) // X
        s = engine.move(s, 4) // O
        s = engine.move(s, 2) // X wins
        assertEquals(GameStatus.Won(Player.X, listOf(0, 1, 2)), s.status)
    }

    @Test
    fun `diagonal win is detected`() {
        var s = GameState()
        listOf(0, 1, 4, 2, 8).forEach { s = engine.move(s, it) } // X:0,4,8  O:1,2
        assertEquals(GameStatus.Won(Player.X, listOf(0, 4, 8)), s.status)
    }

    @Test
    fun `full board with no line is a draw`() {
        // X O X / X O O / O X X
        val order = listOf(0, 1, 2, 4, 3, 5, 7, 6, 8)
        var s = GameState()
        order.forEach { s = engine.move(s, it) }
        assertEquals(GameStatus.Draw, s.status)
    }

    @Test
    fun `no moves accepted after the game is won`() {
        var s = GameState()
        listOf(0, 3, 1, 4, 2).forEach { s = engine.move(s, it) } // X wins
        val after = engine.move(s, 5)
        assertEquals(s, after)
    }

    @Test
    fun `reset returns a fresh in-progress state`() {
        assertEquals(GameState(), engine.reset())
    }
}
