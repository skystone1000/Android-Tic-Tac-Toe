package com.skystone1000.xoxo.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.data.stats.MatchEntity
import com.skystone1000.xoxo.data.stats.MatchResult
import com.skystone1000.xoxo.data.stats.StatsSummary
import com.skystone1000.xoxo.domain.model.GameMode
import com.skystone1000.xoxo.ui.components.IconBadge
import com.skystone1000.xoxo.ui.components.SectionLabel
import com.skystone1000.xoxo.ui.components.TicCard
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicTacTheme

@Composable
fun StatsScreen(summary: StatsSummary, contentPadding: PaddingValues) {
    val colors = TicTacTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp),
    ) {
        Text("Your stats", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.ink)
        Spacer(Modifier.height(16.dp))

        // Win rate + streak
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.primary)
                    .padding(14.dp),
            ) {
                Text("${summary.winRatePercent}%", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                Text("Win rate", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
            TicCard(cornerRadius = 18, modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = colors.playerO, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("${summary.currentStreak}", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colors.ink)
                    }
                    Text("Streak", style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // W / L / D
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat("Wins", summary.wins, colors.playerX, Modifier.weight(1f))
            MiniStat("Losses", summary.losses, colors.playerO, Modifier.weight(1f))
            MiniStat("Draws", summary.draws, colors.inkFaint, Modifier.weight(1f))
        }

        Spacer(Modifier.height(18.dp))

        // Last 7 days chart
        TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Last 7 days", style = MaterialTheme.typography.titleMedium, color = colors.ink)
                Spacer(Modifier.height(14.dp))
                val maxVal = (summary.last7Days.maxOrNull() ?: 0).coerceAtLeast(1)
                Row(
                    Modifier.fillMaxWidth().height(84.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    summary.last7Days.forEach { value ->
                        val fraction = value.toFloat() / maxVal
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(fraction.coerceAtLeast(0.06f))
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(if (value > 0) colors.primary else colors.surfaceVariant),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel("Recent matches")
        Spacer(Modifier.height(10.dp))
        if (summary.recent.isEmpty()) {
            Text("No matches yet — play a round!", style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.recent.forEach { MatchRow(it) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MiniStat(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    TicCard(cornerRadius = 16, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TicTacTheme.colors.inkMuted)
        }
    }
}

@Composable
private fun MatchRow(match: MatchEntity) {
    val colors = TicTacTheme.colors
    val isAi = match.mode == GameMode.VS_AI.name
    val result = runCatching { MatchResult.valueOf(match.result) }.getOrDefault(MatchResult.DRAW)
    val (chipText, chipColor, chipBg) = when (result) {
        MatchResult.WIN -> Triple("WIN", colors.playerX, colors.playerXSoft)
        MatchResult.LOSS -> Triple("LOSS", colors.playerO, colors.playerOSoft)
        MatchResult.DRAW -> Triple("DRAW", colors.inkFaint, colors.surfaceVariant)
    }
    TicCard(cornerRadius = 14, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = if (isAi) Icons.Rounded.SmartToy else Icons.Rounded.Group,
                tint = if (isAi) colors.playerO else colors.playerX,
                background = if (isAi) colors.playerOSoft else colors.playerXSoft,
                size = 34, cornerRadius = 11,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                val title = if (isAi) "Vs AI · ${match.difficulty?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}" else "Pass & Play"
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.ink)
            }
            Text(
                chipText,
                style = MaterialTheme.typography.labelSmall,
                color = chipColor,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(chipBg).padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
    }
}
