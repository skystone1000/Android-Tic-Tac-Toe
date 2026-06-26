package com.skystone1000.xoxo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicTacTheme

/** Celebratory end-of-round overlay. [win] shows confetti + trophy; otherwise a calm card. */
@Composable
fun ResultDialog(
    title: String,
    subtitle: String,
    win: Boolean,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (win) {
                Confetti(modifier = Modifier.fillMaxSize())
            }
            ResultCard(title, subtitle, win, onPlayAgain, onHome)
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    subtitle: String,
    win: Boolean,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val colors = TicTacTheme.colors
    var shown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (shown) 1f else 0.7f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale",
    )
    LaunchedEffect(Unit) { shown = true }

    Column(
        modifier = Modifier
            .scale(scale)
            .padding(28.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(colors.card)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(if (win) colors.primaryContainer else colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (win) Icons.Rounded.EmojiEvents else Icons.Rounded.Handshake,
                contentDescription = null,
                tint = if (win) colors.playerO else colors.inkMuted,
                modifier = Modifier.size(42.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = colors.ink)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted)
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TicButton(
                text = "Home",
                onClick = onHome,
                style = TicButtonStyle.Secondary,
                leadingIcon = Icons.Rounded.Home,
                modifier = Modifier.weight(1f),
            )
            TicButton(
                text = "Play again",
                onClick = onPlayAgain,
                leadingIcon = Icons.Rounded.Replay,
                modifier = Modifier.weight(1.3f),
            )
        }
    }
}
