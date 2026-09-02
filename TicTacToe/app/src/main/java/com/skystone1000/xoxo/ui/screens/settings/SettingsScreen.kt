package com.skystone1000.xoxo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Vibration
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
import com.skystone1000.xoxo.data.settings.AppSettings
import com.skystone1000.xoxo.data.settings.BoardTheme
import com.skystone1000.xoxo.data.settings.ThemeMode
import com.skystone1000.xoxo.domain.model.Difficulty
import com.skystone1000.xoxo.ui.components.NavRow
import com.skystone1000.xoxo.ui.components.ScreenContainer
import com.skystone1000.xoxo.ui.components.SectionLabel
import com.skystone1000.xoxo.ui.components.SegmentedControl
import com.skystone1000.xoxo.ui.components.TicCard
import com.skystone1000.xoxo.ui.components.ToggleRow
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicTacTheme
import com.skystone1000.xoxo.ui.theme.boardPaletteFor

@Composable
fun SettingsScreen(
    settings: AppSettings,
    contentPadding: PaddingValues,
    onHaptics: (Boolean) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onBoardTheme: (BoardTheme) -> Unit,
) {
    val colors = TicTacTheme.colors
    ScreenContainer(contentPadding = contentPadding) {
        Text("Settings", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.ink)
        Spacer(Modifier.height(14.dp))

        SectionLabel("General")
        Spacer(Modifier.height(6.dp))
        // Sound effects: the setting is still persisted (AppSettings.soundEnabled) but the row is
        // hidden until real audio assets land in res/raw. A switch that does nothing is worse
        // than no switch. See docs/ADAPTIVE-UI-PLAN.md 1.14 and docs/BACKLOG.md.
        TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth()) {
            Column {
                ToggleRow(Icons.Rounded.Vibration, "Haptics", settings.hapticsEnabled, onHaptics)
                Divider()
                // Three-way, so ThemeMode.SYSTEM stays reachable. The old boolean switch mapped
                // only to LIGHT/DARK, making "follow system" a one-way door.
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.size(14.dp))
                        Text("Appearance", style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                    }
                    Spacer(Modifier.height(12.dp))
                    SegmentedControl(
                        options = listOf("System", "Light", "Dark"),
                        selectedIndex = settings.themeMode.ordinal,
                        onSelect = { onThemeMode(ThemeMode.entries[it]) },
                    )
                }
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
                    // Previews are built from the real palettes, so a swatch can never misrepresent
                    // the board it selects.
                    BoardTheme.entries.forEach { theme ->
                        val palette = boardPaletteFor(theme)
                        BoardThemeSwatch(
                            theme = theme,
                            selected = settings.boardTheme,
                            // markX/tile/markO: the X colour, the tile surface and the O colour,
                            // so each swatch reads as the board it actually selects.
                            brush = Brush.linearGradient(
                                listOf(palette.markX, palette.tile, palette.markO),
                            ),
                            onClick = { onBoardTheme(it) },
                        )
                    }
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
        // 48dp interactive box around a 42dp swatch — the swatch alone was under the minimum.
        Modifier
            .size(48.dp)
            .clickable { onClick(theme) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
                .border(2.dp, if (isSelected) TicTacTheme.colors.primary else Color.Transparent, RoundedCornerShape(12.dp)),
        )
    }
}
