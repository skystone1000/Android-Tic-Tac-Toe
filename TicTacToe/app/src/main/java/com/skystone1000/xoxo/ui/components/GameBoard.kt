package com.skystone1000.xoxo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Renders a 3x3 board from a flat list of 9 [TileMark]s.
 *
 * @param marks size-9 list, index 0..8 row-major.
 * @param winningLine indices that form the winning line (highlighted), or empty.
 */
@Composable
fun GameBoard(
    marks: List<TileMark>,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    winningLine: Set<Int> = emptySet(),
    enabled: Boolean = true,
) {
    require(marks.size == 9) { "GameBoard expects 9 marks, got ${marks.size}" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    GameTile(
                        mark = marks[index],
                        onClick = { onCellClick(index) },
                        highlighted = index in winningLine,
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
