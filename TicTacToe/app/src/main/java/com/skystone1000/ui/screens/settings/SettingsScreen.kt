package com.skystone1000.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.data.settings.AppSettings
import com.skystone1000.data.settings.BoardTheme
import com.skystone1000.data.settings.ThemeMode
import com.skystone1000.domain.model.Difficulty
import com.skystone1000.ui.components.NavRow
import com.skystone1000.ui.components.SectionLabel
import com.skystone1000.ui.components.SegmentedControl
import com.skystone1000.ui.components.TicCard
import com.skystone1000.ui.components.ToggleRow
import com.skystone1000.ui.theme.SpaceGrotesk
import com.skystone1000.ui.theme.TicTacTheme

@Composable
fun SettingsScreen(
    settings: AppSettings,
    contentPadding: PaddingValues,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onDarkMode: (Boolean) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onBoardTheme: (BoardTheme) -> Unit,
) {
    val colors = TicTacTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp),
    ) {
        Text("Settings", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.ink)
        Spacer(Modifier.height(14.dp))

        SectionLabel("General")
        Spacer(Modifier.height(6.dp))
        TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth()) {
            Column {
                ToggleRow(Icons.Rounded.VolumeUp, "Sound effects", settings.soundEnabled, onSound)
                Divider()
                ToggleRow(Icons.Rounded.Vibration, "Haptics", settings.hapticsEnabled, onHaptics)
                Divider()
                ToggleRow(
                    Icons.Rounded.DarkMode, "Dark mode",
                    settings.themeMode == ThemeMode.DARK, onDarkMode,
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("Gameplay")
        Spacer(Modifier.height(6.dp))
        TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.size(14.dp))
                    Text("Default AI difficulty", style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                }
                Spacer(Modifier.height(12.dp))
                SegmentedControl(
                    options = listOf("Easy", "Medium", "Hard"),
                    selectedIndex = settings.defaultDifficulty.ordinal,
                    onSelect = { onDifficulty(Difficulty.entries[it]) },
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.size(14.dp))
                    Text("Board theme", style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BoardThemeSwatch(BoardTheme.CLASSIC, settings.boardTheme, Brush.linearGradient(listOf(colors.playerXSoft, colors.playerOSoft))) { onBoardTheme(it) }
                    BoardThemeSwatch(BoardTheme.MIDNIGHT, settings.boardTheme, Brush.linearGradient(listOf(Color(0xFF16162A), Color(0xFF23232E)))) { onBoardTheme(it) }
                    BoardThemeSwatch(BoardTheme.AURORA, settings.boardTheme, Brush.linearGradient(listOf(colors.primaryContainer, Color(0xFFFFFFFF)))) { onBoardTheme(it) }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("About")
        Spacer(Modifier.height(6.dp))
        TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth()) {
            NavRow(Icons.Rounded.Info, "About XOXO", trailing = "v2.0")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TicTacTheme.colors.background),
    )
}

@Composable
private fun BoardThemeSwatch(
    theme: BoardTheme,
    selected: BoardTheme,
    brush: Brush,
    onClick: (BoardTheme) -> Unit,
) {
    val isSelected = theme == selected
    Box(
        Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
            .border(2.dp, if (isSelected) TicTacTheme.colors.primary else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick(theme) },
    )
}
