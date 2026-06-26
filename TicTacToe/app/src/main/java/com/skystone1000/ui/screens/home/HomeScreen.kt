package com.skystone1000.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.domain.model.Difficulty
import com.skystone1000.ui.components.Avatar
import com.skystone1000.ui.components.AvatarTone
import com.skystone1000.ui.components.IconBadge
import com.skystone1000.ui.components.SectionLabel
import com.skystone1000.ui.components.SegmentedControl
import com.skystone1000.ui.components.TicButton
import com.skystone1000.ui.components.TicCard
import com.skystone1000.ui.theme.SpaceGrotesk
import com.skystone1000.ui.theme.TicTacTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    contentPadding: PaddingValues,
    onPassAndPlay: () -> Unit,
    onVsAi: (Difficulty) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val colors = TicTacTheme.colors
    var difficulty by remember(state.defaultDifficulty) { mutableStateOf(state.defaultDifficulty) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Welcome back,", style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted)
                Text(
                    state.playerName,
                    fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.ink,
                )
            }
            Box(Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onOpenSettings)) {
                IconBadge(Icons.Rounded.Settings, tint = colors.inkMuted, background = colors.card)
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.clip(RoundedCornerShape(21.dp)).clickable(onClick = onOpenProfile)) {
                Avatar(initial = state.playerName, tone = AvatarTone.Indigo, cornerRadius = 21)
            }
        }

        Spacer(Modifier.height(18.dp))

        // Streak banner (no levels / XP — just a fun streak)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF5B4BE8))))
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFDBA74))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${state.streak}",
                    fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.White,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "win streak",
                    style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionLabel("Game modes")
        Spacer(Modifier.height(12.dp))

        // Pass & Play
        TicCard(onClick = onPassAndPlay, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.Groups, colors.playerX, colors.playerXSoft)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Pass & Play", style = MaterialTheme.typography.titleMedium, color = colors.ink)
                    Text("2 players · one device", style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.inkFaint)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Vs AI
        TicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.SmartToy, colors.playerO, colors.playerOSoft)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Vs AI", style = MaterialTheme.typography.titleMedium, color = colors.ink)
                        Text("Pick your challenge", style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted)
                    }
                }
                Spacer(Modifier.height(12.dp))
                SegmentedControl(
                    options = listOf("Easy", "Medium", "Hard"),
                    selectedIndex = difficulty.ordinal,
                    onSelect = { difficulty = Difficulty.entries[it] },
                )
                Spacer(Modifier.height(12.dp))
                TicButton(text = "Play vs AI", onClick = { onVsAi(difficulty) }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
