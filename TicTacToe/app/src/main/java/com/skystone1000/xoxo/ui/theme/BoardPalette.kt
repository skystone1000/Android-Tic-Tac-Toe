package com.skystone1000.xoxo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.skystone1000.xoxo.data.settings.BoardTheme

/**
 * The six colors the game screen needs. Kept separate from [TicColors] so the user's [BoardTheme]
 * choice can retint the board, score cards and turn pill without touching the rest of the app.
 */
@Immutable
data class BoardPalette(
    val markX: Color,
    val markO: Color,
    val softX: Color,
    val softO: Color,
    val tile: Color,
    val outline: Color,
)

/**
 * Resolves the user's [BoardTheme] choice into concrete colors. CLASSIC follows the app palette
 * (and therefore light/dark); MIDNIGHT and AURORA are fixed looks that read the same either way.
 */
@Composable
@ReadOnlyComposable
fun boardPaletteFor(theme: BoardTheme): BoardPalette {
    val c = LocalTicColors.current
    return when (theme) {
        BoardTheme.CLASSIC -> BoardPalette(
            markX = c.playerX,
            markO = c.playerO,
            softX = c.playerXSoft,
            softO = c.playerOSoft,
            tile = c.card,
            outline = c.outline,
        )
        BoardTheme.MIDNIGHT -> BoardPalette(
            markX = Color(0xFF5EEAD4),
            markO = Color(0xFFFDBA74),
            softX = Color(0xFF14313A),
            softO = Color(0xFF3A2A18),
            tile = Color(0xFF16162A),
            outline = Color(0xFF2B2B42),
        )
        BoardTheme.AURORA -> BoardPalette(
            markX = Color(0xFF7C6CF5),
            markO = Color(0xFFE879A6),
            softX = Color(0xFFE9E6FF),
            softO = Color(0xFFFDE7F0),
            tile = Color(0xFFF7F5FF),
            outline = Color(0xFFDCD6F7),
        )
    }
}
