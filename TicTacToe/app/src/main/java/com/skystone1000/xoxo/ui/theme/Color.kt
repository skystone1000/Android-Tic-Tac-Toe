package com.skystone1000.xoxo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color palette for TicTac (design "Option A" — Indigo · Teal · Orange).
 * Material's [androidx.compose.material3.ColorScheme] only covers the brand/surface
 * basics, so game-specific colors (player marks, soft tile backgrounds) live here and
 * are exposed through [LocalTicColors].
 */
@Immutable
data class TicColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,   // secondary button / soft indigo chip
    val onPrimaryContainer: Color,

    val playerX: Color,
    val onPlayerX: Color,          // readable text on the soft-X background
    val playerXSoft: Color,        // soft tile / chip background for X

    val playerO: Color,
    val onPlayerO: Color,
    val playerOSoft: Color,

    val background: Color,         // screen background
    val card: Color,              // raised card / tile surface
    val surfaceVariant: Color,     // muted fills (segmented track, disabled)

    val ink: Color,               // primary text
    val inkMuted: Color,          // secondary text
    val inkFaint: Color,          // tertiary / labels
    val outline: Color,           // hairline borders / tile stroke

    val isDark: Boolean,
)

val LightTicColors = TicColors(
    primary = Color(0xFF4338CA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEEDFB),
    onPrimaryContainer = Color(0xFF4338CA),

    playerX = Color(0xFF14B8A6),
    onPlayerX = Color(0xFF0F766E),
    playerXSoft = Color(0xFFE2F7F4),

    playerO = Color(0xFFF97316),
    onPlayerO = Color(0xFFC2410C),
    playerOSoft = Color(0xFFFEEEE2),

    background = Color(0xFFF4F4F8),
    card = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F1F6),

    ink = Color(0xFF16162A),
    inkMuted = Color(0xFF75758A),
    inkFaint = Color(0xFF9A9AAE),
    outline = Color(0xFFEAEAF2),

    isDark = false,
)

val DarkTicColors = TicColors(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF2A2747),
    onPrimaryContainer = Color(0xFFC7CBFF),

    playerX = Color(0xFF2DD4BF),
    onPlayerX = Color(0xFF99F6E4),
    playerXSoft = Color(0xFF123833),

    playerO = Color(0xFFFB923C),
    onPlayerO = Color(0xFFFED7AA),
    playerOSoft = Color(0xFF3A2415),

    background = Color(0xFF0E0E14),
    card = Color(0xFF23232E),
    surfaceVariant = Color(0xFF1A1A22),

    ink = Color(0xFFECECF3),
    inkMuted = Color(0xFF9A9AAE),
    inkFaint = Color(0xFF75758A),
    outline = Color(0xFF33333F),

    isDark = true,
)

val LocalTicColors = staticCompositionLocalOf { LightTicColors }
