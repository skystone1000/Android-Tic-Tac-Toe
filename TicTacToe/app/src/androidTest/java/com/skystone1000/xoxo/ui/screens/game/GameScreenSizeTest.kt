package com.skystone1000.xoxo.ui.screens.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skystone1000.xoxo.domain.model.Player
import com.skystone1000.xoxo.ui.theme.TicTacTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for the large-screen bug: the board used to be sized `fillMaxWidth()` with
 * square tiles, so its height was always the window *width*. In landscape and on tablets that
 * overflowed the column — the bottom rows were clipped (and unclickable) and the Restart/Quit
 * controls were pushed off screen entirely.
 */
@RunWith(AndroidJUnit4::class)
class GameScreenSizeTest {

    @get:Rule
    val rule = createComposeRule()

    /** The window size the current test is rendering at; everything must fit inside it. */
    private var windowSize: DpSize = DpSize.Zero

    /**
     * Forces both the layout size and the reported window size, so the adaptive branch under test
     * is the one the given window would really take.
     */
    @OptIn(ExperimentalComposeUiApi::class)
    private fun renderAt(size: DpSize) {
        windowSize = size
        rule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size)) {
                WithWindowSize(size) {
                    TicTacTheme(darkTheme = false) {
                        GameScreen(
                            modeLabel = "Vs AI · Hard",
                            isVsAi = true,
                            humanSymbol = Player.X,
                            state = GameUiState(),
                            onTileClick = {},
                            onRestart = {},
                            onQuit = {},
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WithWindowSize(size: DpSize, content: @Composable () -> Unit) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val px = with(density) { IntSize(size.width.roundToPx(), size.height.roundToPx()) }
        CompositionLocalProvider(LocalWindowInfo provides FakeWindowInfo(px), content = content)
    }

    private class FakeWindowInfo(override val containerSize: IntSize) : WindowInfo {
        override val isWindowFocused: Boolean = true

        @OptIn(ExperimentalComposeUiApi::class)
        override val keyboardModifiers: PointerKeyboardModifiers = PointerKeyboardModifiers(0)
    }

    /**
     * Asserts the node is fully inside the window.
     *
     * [assertIsDisplayed] is not enough on its own: a Column does not clip its children, so a
     * board that overflows the screen still reports its tiles as "displayed". The original bug
     * was precisely an overflow, so the assertion has to be about geometry.
     */
    private fun SemanticsNodeInteraction.assertFitsInWindow(name: String) {
        assertIsDisplayed()
        val b = getUnclippedBoundsInRoot()
        val slack = 1.dp // rounding
        if (b.left < -slack || b.top < -slack ||
            b.right > windowSize.width + slack || b.bottom > windowSize.height + slack
        ) {
            throw AssertionError(
                "$name is outside the ${windowSize.width.value.toInt()}x" +
                    "${windowSize.height.value.toInt()}dp window: " +
                    "left=${b.left} top=${b.top} right=${b.right} bottom=${b.bottom}"
            )
        }
    }

    private fun assertBoardAndControlsUsable() {
        // All nine cells must be laid out inside the window — the bottom rows used to be pushed
        // past the bottom edge, which made them unreachable.
        for (row in 1..3) {
            for (col in 1..3) {
                rule.onNodeWithContentDescription("row $row, column $col, empty")
                    .assertFitsInWindow("cell row $row col $col")
            }
        }
        // Both controls must be reachable; they used to be shoved off the bottom entirely.
        rule.onNodeWithText("Restart").assertFitsInWindow("Restart")
        rule.onNodeWithText("Quit").assertFitsInWindow("Quit")
    }

    @Test
    fun phonePortrait_boardAndControlsAreUsable() {
        renderAt(DpSize(411.dp, 891.dp))
        assertBoardAndControlsUsable()
    }

    @Test
    fun phoneLandscape_boardAndControlsAreUsable() {
        renderAt(DpSize(891.dp, 411.dp))
        assertBoardAndControlsUsable()
    }

    @Test
    fun tabletPortrait_boardAndControlsAreUsable() {
        renderAt(DpSize(800.dp, 1280.dp))
        assertBoardAndControlsUsable()
    }

    @Test
    fun tabletLandscape_boardAndControlsAreUsable() {
        renderAt(DpSize(1280.dp, 800.dp))
        assertBoardAndControlsUsable()
    }

    @Test
    fun splitScreenNarrow_boardAndControlsAreUsable() {
        renderAt(DpSize(360.dp, 640.dp))
        assertBoardAndControlsUsable()
    }
}
