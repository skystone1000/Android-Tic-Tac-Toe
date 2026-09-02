package com.skystone1000.xoxo.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.ui.components.Avatar
import com.skystone1000.xoxo.ui.components.AvatarTone
import com.skystone1000.xoxo.ui.components.TicCard
import com.skystone1000.xoxo.ui.layout.MAX_CONTENT_WIDTH_DP
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicTacTheme

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    contentPadding: PaddingValues,
    onSaveName: (String) -> Unit,
) {
    val colors = TicTacTheme.colors
    // Saveable: rotating with the rename dialog open used to close it and discard the draft.
    var editing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable(state.name) { mutableStateOf(state.name) }

    // The gradient header is edge-to-edge: it bleeds up behind the status bar, so the window
    // insets are applied *inside* it rather than to the scroll column.
    val topInset = contentPadding.calculateTopPadding()
    val bottomInset = contentPadding.calculateBottomPadding()

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Gradient header sized by its own content, so it can never be outgrown at large font
        // scale (it used to be a fixed 230dp box behind an unanchored, overlapping column).
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF4F3FF0), Color(0xFF3A2FB0))))
                .padding(top = topInset + 32.dp, bottom = 40.dp, start = 22.dp, end = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(initial = state.name, tone = AvatarTone.Teal, size = 84, cornerRadius = 28)
            Spacer(Modifier.height(12.dp))
            // The whole name row is the edit affordance — the pencil used to look tappable and
            // do nothing at all.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { editing = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.name,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit name",
                    tint = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(6.dp)
                        .size(18.dp),
                )
            }
        }

        Column(
            Modifier
                .widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
                .fillMaxWidth()
                .padding(top = 24.dp, start = 22.dp, end = 22.dp, bottom = bottomInset + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
            Text(
                "$value$suffix", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
                fontSize = 22.sp, color = color, maxLines = 1,
            )
            Text(
                label, style = MaterialTheme.typography.bodyMedium,
                color = TicTacTheme.colors.inkMuted, maxLines = 1,
            )
        }
    }
}
