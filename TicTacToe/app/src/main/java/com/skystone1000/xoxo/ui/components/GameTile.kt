package com.skystone1000.xoxo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.ui.layout.tileCornerDp
import com.skystone1000.xoxo.ui.layout.tileMarkSp
import com.skystone1000.xoxo.ui.theme.BoardPalette
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk

enum class TileMark { EMPTY, X, O }

/**
 * One board cell.
 *
 * It fills whatever box the caller gives it and derives its corner radius and glyph size from that
 * measured size, so it looks right at every board scale — a fixed 46sp mark looked lost inside the
 * huge tiles a tablet used to produce.
 *
 * @param positionLabel human-readable position, e.g. "row 1, column 2", used for TalkBack so the
 *   nine cells are distinguishable.
 */
@Composable
fun GameTile(
    mark: TileMark,
    onClick: () -> Unit,
    positionLabel: String,
    palette: BoardPalette,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
) {
    val markColor: Color = when (mark) {
        TileMark.X -> palette.markX
        TileMark.O -> palette.markO
        TileMark.EMPTY -> Color.Transparent
    }
    val background: Color = when (mark) {
        TileMark.X -> palette.softX
        TileMark.O -> palette.softO
        TileMark.EMPTY -> palette.tile
    }
    val borderColor = when {
        highlighted && mark == TileMark.X -> palette.markX
        highlighted && mark == TileMark.O -> palette.markO
        else -> palette.outline
    }
    val borderWidth = if (highlighted) 2.dp else 1.5.dp

    // Pop-in scale whenever the tile becomes filled.
    val markScale by animateFloatAsState(
        targetValue = if (mark == TileMark.EMPTY) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "markScale",
    )

    val description = when (mark) {
        TileMark.X -> "$positionLabel, X"
        TileMark.O -> "$positionLabel, O"
        TileMark.EMPTY -> "$positionLabel, empty"
    }

    BoxWithConstraints(
        modifier = modifier
            .clickable(
                enabled = enabled && mark == TileMark.EMPTY,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        val tileSide = minOf(maxWidth, maxHeight)
        val shape = RoundedCornerShape(tileCornerDp(tileSide.value).dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (highlighted) Modifier.shadow(10.dp, shape, ambientColor = markColor, spotColor = markColor)
                    else Modifier
                )
                .clip(shape)
                .background(background, shape)
                .border(borderWidth, borderColor, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (mark != TileMark.EMPTY) {
                Text(
                    text = if (mark == TileMark.X) "X" else "O",
                    color = markColor,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = tileMarkSp(tileSide.value).sp,
                    modifier = Modifier.scale(markScale),
                )
            }
        }
    }
}
