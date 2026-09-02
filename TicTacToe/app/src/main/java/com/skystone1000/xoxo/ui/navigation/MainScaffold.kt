package com.skystone1000.xoxo.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skystone1000.xoxo.ui.layout.rememberWindowSize
import com.skystone1000.xoxo.ui.theme.TicTacTheme

private fun iconFor(tab: HomeTab): ImageVector = when (tab) {
    HomeTab.HOME -> Icons.Rounded.Home
    HomeTab.STATS -> Icons.Rounded.BarChart
    HomeTab.PROFILE -> Icons.Rounded.Person
    HomeTab.SETTINGS -> Icons.Rounded.Settings
}

@Composable
fun MainScaffold(
    current: HomeTab,
    onSelect: (HomeTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSize = rememberWindowSize()
    if (windowSize.isExpandedWidth) {
        // Expanded width: a rail is a far shorter reach than a bar spread across 1200dp, and in
        // landscape it stops the bar from eating scarce vertical space.
        Row(Modifier.fillMaxSize().background(TicTacTheme.colors.background)) {
            SideRail(current, onSelect)
            Scaffold(
                containerColor = TicTacTheme.colors.background,
                content = content,
            )
        }
    } else {
        Scaffold(
            containerColor = TicTacTheme.colors.background,
            bottomBar = { BottomBar(current, onSelect) },
            content = content,
        )
    }
}

@Composable
private fun SideRail(current: HomeTab, onSelect: (HomeTab) -> Unit) {
    val colors = TicTacTheme.colors
    Column(
        modifier = Modifier
            .width(88.dp)
            .fillMaxHeight()
            .background(colors.card)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HomeTab.entries.forEach { tab ->
            val selected = tab == current
            Column(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(tab) }
                    // Comfortably past the 48dp minimum interactive size.
                    .size(width = 72.dp, height = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    iconFor(tab),
                    contentDescription = tab.label,
                    tint = if (selected) colors.primary else colors.inkFaint,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) colors.primary else colors.inkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BottomBar(current: HomeTab, onSelect: (HomeTab) -> Unit) {
    val colors = TicTacTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp)
            .height(68.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        HomeTab.entries.forEach { tab ->
            val selected = tab == current
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(tab) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    iconFor(tab),
                    contentDescription = tab.label,
                    tint = if (selected) colors.primary else colors.inkFaint,
                    modifier = Modifier.height(24.dp),
                )
                Text(
                    tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) colors.primary else colors.inkFaint,
                    maxLines = 1,
                )
            }
        }
    }
}
