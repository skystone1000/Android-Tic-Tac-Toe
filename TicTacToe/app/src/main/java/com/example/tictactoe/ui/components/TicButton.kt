package com.example.tictactoe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tictactoe.ui.theme.TicTacTheme

enum class TicButtonStyle { Primary, Secondary, Outlined }

@Composable
fun TicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TicButtonStyle = TicButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = TicTacTheme.colors
    val shape = RoundedCornerShape(16.dp)

    val container: Color
    val content: Color
    val border: BorderStroke?
    when {
        !enabled -> {
            container = colors.surfaceVariant; content = colors.inkFaint; border = null
        }
        style == TicButtonStyle.Primary -> {
            container = colors.primary; content = colors.onPrimary; border = null
        }
        style == TicButtonStyle.Secondary -> {
            container = colors.primaryContainer; content = colors.onPrimaryContainer; border = null
        }
        else -> {
            container = Color.Transparent; content = colors.primary
            border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.35f))
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(container, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

/** Square icon button (48dp) used for board controls and quick actions. */
@Composable
fun TicIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TicButtonStyle = TicButtonStyle.Secondary,
    enabled: Boolean = true,
) {
    val colors = TicTacTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val container = when {
        !enabled -> colors.surfaceVariant
        style == TicButtonStyle.Primary -> colors.primary
        else -> colors.primaryContainer
    }
    val tint = when {
        !enabled -> colors.inkFaint
        style == TicButtonStyle.Primary -> colors.onPrimary
        else -> colors.onPrimaryContainer
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .background(container, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}
