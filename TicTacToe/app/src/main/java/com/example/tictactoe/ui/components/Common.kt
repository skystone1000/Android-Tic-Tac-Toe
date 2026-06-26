package com.example.tictactoe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tictactoe.ui.theme.SpaceGrotesk
import com.example.tictactoe.ui.theme.TicTacTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Uppercase faint section label, e.g. "GAME MODES". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TicTacTheme.colors.inkFaint,
        modifier = modifier,
    )
}

/** White rounded card surface (elevation e1). */
@Composable
fun TicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 20,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(TicTacTheme.colors.card, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) { content() }
}

/** Back arrow + title row used at the top of detail screens. */
@Composable
fun TicTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = TicTacTheme.colors
    Row(
        modifier = modifier.padding(horizontal = 22.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .size(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.ink)
            }
        }
        Text(
            title,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = colors.ink,
        )
    }
}

/** Small leading icon tile used in list rows and mode cards. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    size: Int = 48,
    cornerRadius: Int = 15,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.52).dp))
    }
}
