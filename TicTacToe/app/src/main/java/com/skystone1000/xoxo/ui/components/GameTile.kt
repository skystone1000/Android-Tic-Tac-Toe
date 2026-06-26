package com.skystone1000.xoxo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicTacTheme

enum class TileMark { EMPTY, X, O }

@Composable
fun GameTile(
    mark: TileMark,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    cornerRadius: Int = 20,
    markFontSize: Int = 46,
) {
    val colors = TicTacTheme.colors
    val shape = RoundedCornerShape(cornerRadius.dp)

    val markColor: Color = when (mark) {
        TileMark.X -> colors.playerX
        TileMark.O -> colors.playerO
        TileMark.EMPTY -> Color.Transparent
    }
    val background: Color = when {
        highlighted && mark == TileMark.X -> colors.playerXSoft
        highlighted && mark == TileMark.O -> colors.playerOSoft
        mark == TileMark.X -> colors.playerXSoft
        mark == TileMark.O -> colors.playerOSoft
        else -> colors.card
    }

    // Pop-in scale whenever the tile becomes filled.
    val markScale by animateFloatAsState(
        targetValue = if (mark == TileMark.EMPTY) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "markScale",
    )

    val borderColor = when {
        highlighted && mark == TileMark.X -> colors.playerX
        highlighted && mark == TileMark.O -> colors.playerO
        else -> colors.outline
    }
    val borderWidth = if (highlighted) 2.dp else 1.5.dp

    val tileModifier = modifier
        .aspectRatio(1f)
        .then(
            if (highlighted) Modifier.shadow(10.dp, shape, ambientColor = markColor, spotColor = markColor)
            else Modifier
        )
        .clip(shape)
        .background(background, shape)
        .border(borderWidth, borderColor, shape)
        .clickable(
            enabled = enabled && mark == TileMark.EMPTY,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
        .semantics {
            contentDescription = when (mark) {
                TileMark.X -> "X"
                TileMark.O -> "O"
                TileMark.EMPTY -> "empty cell"
            }
        }

    Box(modifier = tileModifier, contentAlignment = Alignment.Center) {
        if (mark != TileMark.EMPTY) {
            Text(
                text = if (mark == TileMark.X) "X" else "O",
                color = markColor,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = markFontSize.sp,
                modifier = Modifier.scale(markScale),
            )
        }
    }
}
