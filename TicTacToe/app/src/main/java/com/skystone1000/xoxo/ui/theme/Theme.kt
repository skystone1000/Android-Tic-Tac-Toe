package com.skystone1000.xoxo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun materialSchemeFrom(c: TicColors) = if (c.isDark) {
    darkColorScheme(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.onPrimaryContainer,
        background = c.background,
        onBackground = c.ink,
        surface = c.card,
        onSurface = c.ink,
        surfaceVariant = c.surfaceVariant,
        onSurfaceVariant = c.inkMuted,
        outline = c.outline,
    )
} else {
    lightColorScheme(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.onPrimaryContainer,
        background = c.background,
        onBackground = c.ink,
        surface = c.card,
        onSurface = c.ink,
        surfaceVariant = c.surfaceVariant,
        onSurfaceVariant = c.inkMuted,
        outline = c.outline,
    )
}

@Composable
fun TicTacTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val ticColors = if (darkTheme) DarkTicColors else LightTicColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ticColors.background.toArgb()
            window.navigationBarColor = ticColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalTicColors provides ticColors) {
        MaterialTheme(
            colorScheme = materialSchemeFrom(ticColors),
            typography = TicTypography,
            shapes = TicShapes,
            content = content,
        )
    }
}

/** Access point for the semantic TicTac palette: `TicTacTheme.colors.playerX`. */
object TicTacTheme {
    val colors: TicColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTicColors.current
}
