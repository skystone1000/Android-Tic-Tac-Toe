package com.skystone1000.xoxo.ui.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class SizingTest {

    @Test
    fun `board fits the narrower dimension on a phone`() {
        assertEquals(359f, boardSideDp(359f, 620f), 0.01f)
    }

    @Test
    fun `board is capped so it never balloons on a tablet`() {
        assertEquals(MAX_BOARD_SIDE_DP, boardSideDp(748f, 900f), 0.01f)
    }

    @Test
    fun `larger windows get a larger but still capped board`() {
        assertEquals(MAX_BOARD_SIDE_LARGE_DP, boardSideDp(748f, 900f, MAX_BOARD_SIDE_LARGE_DP), 0.01f)
        // Still capped — it must never simply fill the window.
        assertEquals(MAX_BOARD_SIDE_LARGE_DP, boardSideDp(1280f, 1280f, MAX_BOARD_SIDE_LARGE_DP), 0.01f)
    }

    @Test
    fun `board is limited by height in landscape`() {
        assertEquals(300f, boardSideDp(1200f, 300f), 0.01f)
    }

    @Test
    fun `board never goes negative when there is no room`() {
        assertEquals(0f, boardSideDp(-40f, 200f), 0.01f)
    }

    @Test
    fun `unbounded height falls back to width`() {
        assertEquals(MAX_BOARD_SIDE_DP, boardSideDp(500f, Float.POSITIVE_INFINITY), 0.01f)
    }

    @Test
    fun `gap scales with the board but stays within bounds`() {
        assertEquals(6f, boardGapDp(100f), 0.01f)     // clamped low
        assertEquals(10.5f, boardGapDp(350f), 0.01f)  // 3% of 350
        assertEquals(14f, boardGapDp(900f), 0.01f)    // clamped high
    }

    @Test
    fun `mark size is a fixed fraction of the tile`() {
        assertEquals(46f, tileMarkSp(100f), 0.01f)
        assertEquals(23f, tileMarkSp(50f), 0.01f)
    }

    @Test
    fun `tile corner scales and clamps`() {
        assertEquals(12f, tileCornerDp(40f), 0.01f)   // clamped low
        assertEquals(22f, tileCornerDp(100f), 0.01f)  // 22% of 100
        assertEquals(28f, tileCornerDp(300f), 0.01f)  // clamped high
    }
}
