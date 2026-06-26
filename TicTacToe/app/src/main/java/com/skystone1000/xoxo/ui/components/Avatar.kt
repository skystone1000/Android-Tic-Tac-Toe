package com.skystone1000.xoxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk

enum class AvatarTone { Indigo, Teal, Orange }

private fun gradientFor(tone: AvatarTone): Brush = when (tone) {
    AvatarTone.Indigo -> Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF6D5DF0)))
    AvatarTone.Teal -> Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0E9488)))
    AvatarTone.Orange -> Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFFB923C)))
}

/** Rounded-square avatar showing an initial or an icon over a tonal gradient. */
@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    initial: String? = null,
    icon: ImageVector? = null,
    tone: AvatarTone = AvatarTone.Indigo,
    size: Int = 42,
    cornerRadius: Int = 14,
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(shape)
            .background(gradientFor(tone), shape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            icon != null -> Icon(
                icon, contentDescription = null, tint = Color.White,
                modifier = Modifier.size((size * 0.55).dp),
            )
            initial != null -> Text(
                initial.take(1).uppercase(),
                color = Color.White,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.4).sp,
            )
        }
    }
}
