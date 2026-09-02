package com.skystone1000.xoxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skystone1000.xoxo.ui.layout.MAX_CONTENT_WIDTH_DP
import com.skystone1000.xoxo.ui.theme.TicTacTheme

/**
 * The standard body of a single-column screen.
 *
 * Paints the app background, applies the scaffold's [contentPadding], scrolls, and centres a
 * column capped at [maxContentWidth]. Without the cap, cards stretch to 1200dp+ on a tablet and
 * the screen reads as an empty page with icons stranded at either edge.
 */
@Composable
fun ScreenContainer(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = MAX_CONTENT_WIDTH_DP.dp,
    horizontalPadding: Dp = 22.dp,
    verticalPadding: Dp = 8.dp,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            // The background is painted here, not by each screen, so every tab matches.
            .background(TicTacTheme.colors.background)
            .padding(contentPadding)
            .then(if (scrollable) Modifier.verticalScroll(scroll) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            content = content,
        )
    }
}
