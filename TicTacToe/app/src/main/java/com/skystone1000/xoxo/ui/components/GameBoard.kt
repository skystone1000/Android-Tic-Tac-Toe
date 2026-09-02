package com.skystone1000.xoxo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skystone1000.xoxo.ui.layout.MAX_BOARD_SIDE_DP
import com.skystone1000.xoxo.ui.layout.boardGapDp
import com.skystone1000.xoxo.ui.layout.boardSideDp
import com.skystone1000.xoxo.ui.theme.BoardPalette

/**
 * Renders a 3x3 board from a flat list of 9 [TileMark]s.
 *
 * The board is always a **square that fits the box it is given** — never taller than the space
 * available — and is capped at [maxSide] so it does not balloon on tablets. Give it a bounded box
 * (e.g. `Modifier.weight(1f).fillMaxWidth()`); an unbounded height falls back to the width.
 *
 * @param marks size-9 list, index 0..8 row-major.
 * @param winningLine indices that form the winning line (highlighted), or empty.
 */
@Composable
fun GameBoard(
    marks: List<TileMark>,
    onCellClick: (Int) -> Unit,
    palette: BoardPalette,
    modifier: Modifier = Modifier,
    winningLine: Set<Int> = emptySet(),
    enabled: Boolean = true,
    maxSide: Dp = MAX_BOARD_SIDE_DP.dp,
) {
    require(marks.size == 9) { "GameBoard expects 9 marks, got ${marks.size}" }
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val heightDp = if (constraints.hasBoundedHeight) maxHeight.value else Float.POSITIVE_INFINITY
        val side = boardSideDp(maxWidth.value, heightDp, maxSide.value).dp
        val gap = boardGapDp(side.value).dp

        Column(
            modifier = Modifier.size(side),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        GameTile(
                            mark = marks[index],
                            onClick = { onCellClick(index) },
                            positionLabel = "row ${row + 1}, column ${col + 1}",
                            palette = palette,
                            highlighted = index in winningLine,
                            enabled = enabled,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}
