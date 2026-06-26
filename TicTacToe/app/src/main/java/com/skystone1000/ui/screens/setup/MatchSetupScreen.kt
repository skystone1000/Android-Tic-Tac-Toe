package com.skystone1000.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.domain.model.Difficulty
import com.skystone1000.domain.model.GameMode
import com.skystone1000.domain.model.Player
import com.skystone1000.ui.components.SectionLabel
import com.skystone1000.ui.components.SegmentedControl
import com.skystone1000.ui.components.TicButton
import com.skystone1000.ui.components.TicCard
import com.skystone1000.ui.components.TicTopBar
import com.skystone1000.ui.theme.SpaceGrotesk
import com.skystone1000.ui.theme.TicTacTheme

@Composable
fun MatchSetupScreen(
    mode: GameMode,
    initialDifficulty: Difficulty,
    onStart: (symbol: Player, difficulty: Difficulty) -> Unit,
    onBack: () -> Unit,
) {
    val colors = TicTacTheme.colors
    var symbol by remember { mutableStateOf(Player.X) }
    var difficulty by remember { mutableStateOf(initialDifficulty) }

    Column(Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        TicTopBar(title = "Match setup", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            SectionLabel("Your symbol")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SymbolChoice("X", colors.playerX, colors.playerXSoft, symbol == Player.X, Modifier.weight(1f)) { symbol = Player.X }
                SymbolChoice("O", colors.playerO, colors.playerOSoft, symbol == Player.O, Modifier.weight(1f)) { symbol = Player.O }
            }

            if (mode == GameMode.VS_AI) {
                Spacer(Modifier.height(20.dp))
                SectionLabel("Difficulty")
                Spacer(Modifier.height(10.dp))
                SegmentedControl(
                    options = listOf("Easy", "Medium", "Hard"),
                    selectedIndex = difficulty.ordinal,
                    onSelect = { difficulty = Difficulty.entries[it] },
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Players")
            Spacer(Modifier.height(10.dp))
            PlayerRow(
                label = "You",
                symbolText = symbol.name,
                symbolColor = if (symbol == Player.X) colors.playerX else colors.playerO,
            )
            Spacer(Modifier.height(10.dp))
            PlayerRow(
                label = if (mode == GameMode.VS_AI) "AI · ${difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}" else "Friend",
                symbolText = symbol.opponent().name,
                symbolColor = if (symbol.opponent() == Player.X) colors.playerX else colors.playerO,
                isBot = mode == GameMode.VS_AI,
            )
        }

        TicButton(
            text = "Start match",
            onClick = { onStart(symbol, difficulty) },
            leadingIcon = Icons.Rounded.PlayArrow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
        )
    }
}

@Composable
private fun SymbolChoice(
    text: String,
    color: Color,
    soft: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) soft else TicTacTheme.colors.card)
            .border(2.dp, if (selected) color else TicTacTheme.colors.outline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = color)
    }
}

@Composable
private fun PlayerRow(
    label: String,
    symbolText: String,
    symbolColor: Color,
    isBot: Boolean = false,
) {
    val colors = TicTacTheme.colors
    TicCard(cornerRadius = 16, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(symbolColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                if (isBot) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = symbolColor, modifier = Modifier.size(20.dp))
                } else {
                    Text(label.take(1).uppercase(), fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = symbolColor)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, color = colors.ink, modifier = Modifier.weight(1f))
            Text(symbolText, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = symbolColor)
        }
    }
}
