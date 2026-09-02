package com.skystone1000.xoxo.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/** Material width breakpoints: compact < 600dp, medium 600..839dp, expanded >= 840dp. */
enum class WidthClass { COMPACT, MEDIUM, EXPANDED }

/** Material height breakpoints: compact < 480dp, medium 480..899dp, expanded >= 900dp. */
enum class HeightClass { COMPACT, MEDIUM, EXPANDED }

/**
 * The size bucket of the window the app is currently drawn into. This is the *window*, not the
 * device: in split-screen a tablet reports a compact width, which is exactly what we want.
 */
data class WindowSize(val width: WidthClass, val height: HeightClass) {

    val isExpandedWidth: Boolean get() = width == WidthClass.EXPANDED

    val isCompactHeight: Boolean get() = height == HeightClass.COMPACT

    /**
     * True when the game screen should put the board beside its chrome instead of under it —
     * i.e. whenever height is scarce (landscape) or there is width to spare (large tablet).
     */
    val useTwoPaneGame: Boolean get() = isCompactHeight || isExpandedWidth
}

/** Pure classifier. Kept free of Compose so it unit-tests on the JVM. */
fun windowSizeOf(widthDp: Float, heightDp: Float): WindowSize = WindowSize(
    width = when {
        widthDp < 600f -> WidthClass.COMPACT
        widthDp < 840f -> WidthClass.MEDIUM
        else -> WidthClass.EXPANDED
    },
    height = when {
        heightDp < 480f -> HeightClass.COMPACT
        heightDp < 900f -> HeightClass.MEDIUM
        else -> HeightClass.EXPANDED
    },
)

/**
 * The current window's size class. Reads [LocalWindowInfo] rather than `LocalConfiguration` so it
 * tracks live window resizes (split-screen drag, foldable unfold, desktop windowing).
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return remember(containerSize, density) {
        with(density) {
            windowSizeOf(containerSize.width.toDp().value, containerSize.height.toDp().value)
        }
    }
}
