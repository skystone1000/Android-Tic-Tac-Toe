package com.skystone1000.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.skystone1000.ui.components.Avatar
import com.skystone1000.ui.components.AvatarTone
import com.skystone1000.ui.components.TicCard
import com.skystone1000.ui.theme.SpaceGrotesk
import com.skystone1000.ui.theme.TicTacTheme

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    contentPadding: PaddingValues,
    onSaveName: (String) -> Unit,
) {
    val colors = TicTacTheme.colors
    var editing by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        // Gradient header
        Box(
            Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF4F3FF0), Color(0xFF3A2FB0)))),
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(top = 24.dp, start = 22.dp, end = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(initial = state.name, tone = AvatarTone.Teal, size = 84, cornerRadius = 28)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(state.name, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                Spacer(Modifier.width(6.dp))
                Box(Modifier.clip(RoundedCornerShape(8.dp))) {
                    Icon(
                        Icons.Rounded.Edit, contentDescription = "Edit name", tint = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(6.dp)
                            .height(18.dp),
                    )
                }
            }
            // Make the whole name row tappable to edit
            Spacer(Modifier.height(0.dp))

            Spacer(Modifier.height(28.dp))

            // Summary cards (no levels / XP / achievements)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Games", state.summary.totalGames, colors.primary, Modifier.weight(1f))
                SummaryCard("Wins", state.summary.wins, colors.playerX, Modifier.weight(1f))
                SummaryCard("Win %", state.summary.winRatePercent, colors.playerO, Modifier.weight(1f), suffix = "%")
            }

            Spacer(Modifier.height(14.dp))
            TicCard(cornerRadius = 18, modifier = Modifier.fillMaxWidth(), onClick = { editing = true }) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(14.dp))
                    Text("Edit display name", style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                }
            }
        }
    }

    if (editing) {
        var draft by remember { mutableStateOf(state.name) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Display name") },
            text = {
                OutlinedTextField(value = draft, onValueChange = { draft = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = { onSaveName(draft); editing = false }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SummaryCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier, suffix: String = "") {
    TicCard(cornerRadius = 16, modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value$suffix", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TicTacTheme.colors.inkMuted)
        }
    }
}
