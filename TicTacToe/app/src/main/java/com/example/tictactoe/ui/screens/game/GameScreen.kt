package com.example.tictactoe.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.domain.model.Cell
import com.example.tictactoe.domain.model.GameStatus
import com.example.tictactoe.domain.model.Player
import com.example.tictactoe.ui.components.GameBoard
import com.example.tictactoe.ui.components.ResultDialog
import com.example.tictactoe.ui.components.TileMark
import com.example.tictactoe.ui.components.TurnIndicator
import com.example.tictactoe.ui.theme.SpaceGrotesk
import com.example.tictactoe.ui.theme.TicTacTheme
import kotlinx.coroutines.delay

private fun Cell.toMark(): TileMark = when (this) {
    Cell.Empty -> TileMark.EMPTY
    is Cell.Taken -> if (player == Player.X) TileMark.X else TileMark.O
}

@Composable
fun GameScreen(
    modeLabel: String,
    isVsAi: Boolean,
    humanSymbol: Player,
    state: GameUiState,
    onTileClick: (Int) -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
) {
    val colors = TicTacTheme.colors
    val haptics = LocalHapticFeedback.current
    val status = state.gameState.status

    // Round timer
    var seconds by remember { mutableIntStateOf(0) }
    val isEmptyBoard = state.gameState.board.all { it == Cell.Empty }
    val inProgress = status == GameStatus.InProgress
    LaunchedEffect(isEmptyBoard) { if (isEmptyBoard) seconds = 0 }
    LaunchedEffect(inProgress) {
        while (inProgress) {
            delay(1000)
            seconds++
        }
    }

    val marks = state.gameState.board.map { it.toMark() }
    val winningLine = (status as? GameStatus.Won)?.line?.toSet() ?: emptySet()

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 8.dp),
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onQuit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back",
                    tint = colors.ink, modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(modeLabel, style = MaterialTheme.typography.titleMedium, color = colors.inkMuted)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(28.dp))
        }

        // Score panel
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreCard(state.playerXName, state.scoreX, colors.playerX, colors.playerXSoft, Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VS", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = colors.inkFaint, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.clip(CircleShape).background(colors.ink).padding(horizontal = 11.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formatTime(seconds), fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
            ScoreCard(state.playerOName, state.scoreO, colors.playerO, colors.playerOSoft, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Turn indicator
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val (text, dot) = statusPill(state, isVsAi)
            TurnIndicator(text = text, dotColor = dot)
        }

        Spacer(Modifier.height(16.dp))

        // Board
        GameBoard(
            marks = marks,
            onCellClick = { index ->
                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onTileClick(index)
            },
            winningLine = winningLine,
            enabled = inProgress && !state.isAiThinking,
            modifier = Modifier.padding(horizontal = 26.dp),
        )

        Spacer(Modifier.weight(1f))

        // Controls
        Row(
            Modifier.fillMaxWidth().padding(22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ControlButton("Restart", Icons.Rounded.Replay, colors.onPrimaryContainer, colors.primaryContainer, Modifier.weight(1f), onRestart)
            ControlButton("Quit", Icons.Rounded.Close, colors.inkMuted, colors.card, Modifier.weight(1f), onQuit)
        }
    }

    // Result overlay
    if (status != GameStatus.InProgress) {
        val winner = (status as? GameStatus.Won)?.player
        val isWin = winner != null && (!isVsAi || winner == humanSymbol)
        val title = when {
            winner == null -> "It's a draw"
            isVsAi && winner == humanSymbol -> "You win!"
            isVsAi -> "You lost"
            else -> "${if (winner == Player.X) state.playerXName else state.playerOName} wins!"
        }
        val subtitle = when {
            winner == null -> "Nobody takes this round"
            else -> "Score · X ${state.scoreX} · O ${state.scoreO}"
        }
        ResultDialog(
            title = title,
            subtitle = subtitle,
            win = isWin,
            onPlayAgain = onRestart,
            onHome = onQuit,
        )
    }
}

@Composable
private fun ScoreCard(name: String, score: Int, color: Color, soft: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(soft)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(name, style = MaterialTheme.typography.labelMedium, color = color)
        Text("$score", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = color)
    }
}

@Composable
private fun ControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

private fun statusPill(state: GameUiState, isVsAi: Boolean): Pair<String, Color> {
    val gs = state.gameState
    return when (val s = gs.status) {
        GameStatus.InProgress -> {
            if (state.isAiThinking) "AI is thinking…" to Color(0xFFF97316)
            else {
                val name = if (gs.currentPlayer == Player.X) state.playerXName else state.playerOName
                val color = if (gs.currentPlayer == Player.X) Color(0xFF14B8A6) else Color(0xFFF97316)
                "$name — turn" to color
            }
        }
        is GameStatus.Won -> {
            val name = if (s.player == Player.X) state.playerXName else state.playerOName
            "$name completes a row!" to (if (s.player == Player.X) Color(0xFF14B8A6) else Color(0xFFF97316))
        }
        GameStatus.Draw -> "It's a draw" to Color(0xFF9A9AAE)
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
