package com.skystone1000.xoxo.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.domain.model.Cell
import com.skystone1000.xoxo.domain.model.GameStatus
import com.skystone1000.xoxo.domain.model.Player
import com.skystone1000.xoxo.ui.components.GameBoard
import com.skystone1000.xoxo.ui.components.ResultDialog
import com.skystone1000.xoxo.ui.components.TileMark
import com.skystone1000.xoxo.ui.components.TurnIndicator
import com.skystone1000.xoxo.ui.layout.MAX_BOARD_SIDE_DP
import com.skystone1000.xoxo.ui.layout.MAX_BOARD_SIDE_LARGE_DP
import com.skystone1000.xoxo.ui.layout.MAX_CONTENT_WIDTH_DP
import com.skystone1000.xoxo.ui.layout.WidthClass
import com.skystone1000.xoxo.ui.layout.rememberWindowSize
import com.skystone1000.xoxo.ui.theme.BoardPalette
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicColors
import com.skystone1000.xoxo.ui.theme.TicTacTheme
import com.skystone1000.xoxo.ui.theme.boardPaletteFor
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
    val windowSize = rememberWindowSize()
    val boardPalette = boardPaletteFor(state.boardTheme)
    val boardMaxSide = if (windowSize.width == WidthClass.COMPACT) {
        MAX_BOARD_SIDE_DP.dp
    } else {
        MAX_BOARD_SIDE_LARGE_DP.dp
    }

    // Round timer. rememberSaveable so a rotation does not reset the clock mid-round.
    var seconds by rememberSaveable { mutableIntStateOf(0) }
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

    val board: @Composable (Modifier) -> Unit = { boardModifier ->
        GameBoard(
            marks = marks,
            onCellClick = { index ->
                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onTileClick(index)
            },
            palette = boardPalette,
            winningLine = winningLine,
            enabled = inProgress && !state.isAiThinking,
            maxSide = boardMaxSide,
            modifier = boardModifier,
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            // safeDrawing, not systemBars: in landscape the display cutout is on a side edge and
            // would otherwise sit on top of the back arrow and the board.
            .safeDrawingPadding(),
    ) {
        if (windowSize.useTwoPaneGame) {
            TwoPaneGame(modeLabel, state, isVsAi, seconds, boardPalette, onRestart, onQuit, board)
        } else {
            SinglePaneGame(modeLabel, state, isVsAi, seconds, boardPalette, onRestart, onQuit, board)
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

/** Portrait phone and portrait tablet: chrome above, board below, controls pinned to the bottom. */
@Composable
private fun SinglePaneGame(
    modeLabel: String,
    state: GameUiState,
    isVsAi: Boolean,
    seconds: Int,
    palette: BoardPalette,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    board: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val capped = Modifier.widthIn(max = MAX_CONTENT_WIDTH_DP.dp).fillMaxWidth()

        TopBar(modeLabel, onQuit, capped)
        ScorePanel(state, seconds, palette, capped.padding(horizontal = 22.dp))
        Spacer(Modifier.height(16.dp))
        TurnPill(state, isVsAi, palette)
        Spacer(Modifier.height(16.dp))
        // weight(1f) gives the board a bounded height, so it can never overflow the column.
        board(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 26.dp, vertical = 4.dp))
        Controls(onRestart, onQuit, capped.padding(22.dp))
    }
}

/** Landscape and expanded width: chrome in a left column, board filling the right pane. */
@Composable
private fun TwoPaneGame(
    modeLabel: String,
    state: GameUiState,
    isVsAi: Boolean,
    seconds: Int,
    palette: BoardPalette,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    board: @Composable (Modifier) -> Unit,
) {
    Row(Modifier.fillMaxSize().padding(top = 8.dp)) {
        // Box + Center so the capped column sits in the middle of its half rather than hugging
        // the screen edge, which left a wide dead gutter between the chrome and the board.
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(modeLabel, onQuit, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            ScorePanel(state, seconds, palette, Modifier.fillMaxWidth().padding(horizontal = 22.dp))
            Spacer(Modifier.height(16.dp))
            TurnPill(state, isVsAi, palette)
            Spacer(Modifier.height(20.dp))
            Controls(onRestart, onQuit, Modifier.fillMaxWidth().padding(horizontal = 22.dp))
        }
        }
        board(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun TopBar(modeLabel: String, onQuit: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TicTacTheme.colors
    Row(
        modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            // 48dp minimum touch target; the glyph inside stays 24dp.
            Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onQuit),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back",
                tint = colors.ink, modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            modeLabel,
            style = MaterialTheme.typography.titleMedium,
            color = colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun ScorePanel(state: GameUiState, seconds: Int, palette: BoardPalette, modifier: Modifier = Modifier) {
    val colors = TicTacTheme.colors
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScoreCard(state.playerXName, state.scoreX, palette.markX, palette.softX, Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VS", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = colors.inkFaint, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.clip(CircleShape).background(colors.ink).padding(horizontal = 11.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    formatTime(seconds), fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
                    color = Color.White, fontSize = 13.sp, maxLines = 1,
                )
            }
        }
        ScoreCard(state.playerOName, state.scoreO, palette.markO, palette.softO, Modifier.weight(1f))
    }
}

@Composable
private fun TurnPill(state: GameUiState, isVsAi: Boolean, palette: BoardPalette) {
    val colors = TicTacTheme.colors
    Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
        val (text, dot) = statusPill(state, isVsAi, palette, colors)
        TurnIndicator(text = text, dotColor = dot)
    }
}

@Composable
private fun Controls(onRestart: () -> Unit, onQuit: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TicTacTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ControlButton("Restart", Icons.Rounded.Replay, colors.onPrimaryContainer, colors.primaryContainer, Modifier.weight(1f), onRestart)
        ControlButton("Quit", Icons.Rounded.Close, colors.inkMuted, colors.card, Modifier.weight(1f), onQuit)
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
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            "$score", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
            fontSize = 30.sp, color = color, maxLines = 1,
        )
    }
}

@Composable
private fun ControlButton(
    label: String,
    icon: ImageVector,
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
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
    }
}

/**
 * Status pill text + dot color. Reads the board palette so the whole screen retint together with
 * the chosen board theme (the colors used to be hardcoded hex).
 */
private fun statusPill(
    state: GameUiState,
    isVsAi: Boolean,
    palette: BoardPalette,
    colors: TicColors,
): Pair<String, Color> {
    val gs = state.gameState
    return when (val s = gs.status) {
        GameStatus.InProgress -> {
            if (state.isAiThinking) {
                "AI is thinking…" to palette.markO
            } else {
                val name = if (gs.currentPlayer == Player.X) state.playerXName else state.playerOName
                val color = if (gs.currentPlayer == Player.X) palette.markX else palette.markO
                "$name — turn" to color
            }
        }
        is GameStatus.Won -> {
            val name = if (s.player == Player.X) state.playerXName else state.playerOName
            "$name completes a row!" to (if (s.player == Player.X) palette.markX else palette.markO)
        }
        GameStatus.Draw -> "It's a draw" to colors.inkFaint
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
