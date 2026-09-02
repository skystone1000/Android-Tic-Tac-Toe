package com.skystone1000.xoxo.ui.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowSizeTest {

    @Test
    fun `phone portrait is compact width and medium height`() {
        val size = windowSizeOf(411f, 891f)
        assertEquals(WidthClass.COMPACT, size.width)
        assertEquals(HeightClass.MEDIUM, size.height)
    }

    @Test
    fun `phone landscape is expanded width and compact height`() {
        // A modern phone is ~891dp wide on its side, which clears the 840dp expanded breakpoint.
        // The height is what matters here: 411dp is compact, so the game goes two-pane.
        val size = windowSizeOf(891f, 411f)
        assertEquals(WidthClass.EXPANDED, size.width)
        assertEquals(HeightClass.COMPACT, size.height)
    }

    @Test
    fun `small phone landscape is medium width and compact height`() {
        val size = windowSizeOf(720f, 360f)
        assertEquals(WidthClass.MEDIUM, size.width)
        assertEquals(HeightClass.COMPACT, size.height)
    }

    @Test
    fun `tablet portrait is medium width and expanded height`() {
        val size = windowSizeOf(800f, 1280f)
        assertEquals(WidthClass.MEDIUM, size.width)
        assertEquals(HeightClass.EXPANDED, size.height)
    }

    @Test
    fun `tablet landscape is expanded width`() {
        val size = windowSizeOf(1280f, 800f)
        assertEquals(WidthClass.EXPANDED, size.width)
        assertEquals(HeightClass.MEDIUM, size.height)
    }

    @Test
    fun `breakpoints are inclusive at the lower bound`() {
        assertEquals(WidthClass.MEDIUM, windowSizeOf(600f, 800f).width)
        assertEquals(WidthClass.EXPANDED, windowSizeOf(840f, 800f).width)
        assertEquals(HeightClass.MEDIUM, windowSizeOf(600f, 480f).height)
        assertEquals(HeightClass.EXPANDED, windowSizeOf(600f, 900f).height)
    }

    @Test
    fun `two pane game is used in landscape and on expanded width`() {
        assertTrue(windowSizeOf(891f, 411f).useTwoPaneGame)
        assertTrue(windowSizeOf(1280f, 800f).useTwoPaneGame)
        assertFalse(windowSizeOf(411f, 891f).useTwoPaneGame)
        assertFalse(windowSizeOf(800f, 1280f).useTwoPaneGame)
    }
}
