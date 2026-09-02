package com.skystone1000.xoxo.ui.layout

/**
 * Pure layout maths shared by the board and the screen containers. Deliberately expressed in bare
 * `Float` dp/sp values rather than `Dp`/`TextUnit` so it unit-tests on the JVM without Robolectric.
 */

/** The board never grows past this on a compact (phone-width) window. */
const val MAX_BOARD_SIDE_DP: Float = 420f

/**
 * The cap on medium/expanded windows. A 420dp board is correct on a phone but leaves an 800x1280dp
 * tablet looking sparse, so larger windows get a proportionally larger board — still capped, so it
 * never balloons to fill the screen the way the old fillMaxWidth board did.
 */
const val MAX_BOARD_SIDE_LARGE_DP: Float = 520f

/** Reading-comfort cap for single-column screen content on wide windows. */
const val MAX_CONTENT_WIDTH_DP: Float = 560f

/**
 * The largest square that fits in the given space, capped at [maxSideDp].
 *
 * An infinite [availableHeightDp] (an unbounded parent, e.g. inside a scroll container) falls back
 * to the width so the board still gets a finite size instead of crashing the measure pass.
 */
fun boardSideDp(
    availableWidthDp: Float,
    availableHeightDp: Float,
    maxSideDp: Float = MAX_BOARD_SIDE_DP,
): Float {
    val height = if (availableHeightDp.isFinite()) availableHeightDp else availableWidthDp
    return minOf(availableWidthDp, height, maxSideDp).coerceAtLeast(0f)
}

/** Gap between tiles: 3% of the board side, clamped to a sane 6..14 dp. */
fun boardGapDp(sideDp: Float): Float = (sideDp * 0.03f).coerceIn(6f, 14f)

/** Mark glyph size: 46% of the tile side, so an "X" always fills its cell the same way. */
fun tileMarkSp(tileSideDp: Float): Float = tileSideDp * 0.46f

/** Tile corner radius: 22% of the tile side, clamped to 12..28 dp. */
fun tileCornerDp(tileSideDp: Float): Float = (tileSideDp * 0.22f).coerceIn(12f, 28f)
