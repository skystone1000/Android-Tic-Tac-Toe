# Adaptive UI (Large Screen, Landscape & Configuration) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development
> (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every XOXO screen render correctly and look intentional on tablets, foldables,
landscape phones and multi-window, and fix the configuration-change, dead-setting, font-scale and
accessibility defects found alongside the reported game-screen bug.

**Architecture:** Introduce one small, unit-testable adaptive layer (`ui/layout/`) that turns the
window's size into a `WindowSize` value plus pure sizing helpers. Every screen reads that value
instead of assuming "one phone-width column". The game board becomes a self-sizing square that
fits the space it is given rather than a `fillMaxWidth()` block that overflows. Tab screens share
a `ScreenContainer` that caps content width and centres it. No architectural layer is added or
moved — this stays MVVM with a pure domain layer.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose (BOM 2026.08.00), Material 3, Navigation-Compose
2.9.8, JUnit4 + Compose UI test. No new Gradle dependencies are required.

**Spec:** This document. Part 1 below is the analysis/spec; Part 2 is the plan that implements it.

## Global Constraints

- **Offline-only.** No online/multiplayer. `GameMode` stays `PASS_AND_PLAY` and `VS_AI` only.
- **No progression system.** No levels, XP or achievements — the win-streak stat is the only one.
- **Domain layer stays pure.** Nothing in `domain/` may gain an Android import. All new adaptive
  code lives in `ui/`.
- **Gradle root is `TicTacToe/`.** Run `./gradlew` from there, never the repo root.
- **No new dependencies.** Everything used here ships in the Compose BOM already declared in
  `TicTacToe/app/build.gradle`.
- **Docs are part of "done."** `ARCHITECTURE.md`, `CODEBASE.md` and `FEATURES.md` must be updated
  in the same change that alters structure or behaviour (see `CLAUDE.md` §2).
- **minSdk 24 / targetSdk 37.** Because `targetSdk >= 36`, Android ignores orientation and
  resizability restrictions on large screens — the app **cannot** opt out of landscape or
  multi-window on a tablet. Landscape support is mandatory, not optional.
- **Material breakpoints:** width compact `< 600dp`, medium `600–839dp`, expanded `>= 840dp`;
  height compact `< 480dp`, medium `480–899dp`, expanded `>= 900dp`.

---

# Part 1 — Analysis

## 1.1 The reported bug: the game screen collapses on large screens

`ui/components/GameBoard.kt:28` sizes the board with `fillMaxWidth()`, and each
`GameTile` inside it is `Modifier.weight(1f)` + `aspectRatio(1f)`
(`ui/components/GameTile.kt:75`). The board therefore has **no height of its own** — its height is
always *exactly the window width minus horizontal padding*. Meanwhile `GameScreen` stacks
everything in a plain, non-scrollable `Column` (`ui/screens/game/GameScreen.kt:88`).

Worked example on a 10" tablet:

| Window                      | Board height demanded | Other chrome | Total   | Fits? |
| --------------------------- | --------------------- | ------------ | ------- | ----- |
| Phone portrait 411×891 dp   | 411 − 52 = **359 dp** | ~300 dp      | 659 dp  | yes   |
| Tablet portrait 800×1280 dp | 800 − 52 = **748 dp** | ~300 dp      | 1048 dp | tight |
| Tablet landscape 1280×800dp | 1280 − 52 = **1228dp**| ~300 dp      | 1528 dp | **no** |
| Phone landscape 891×411 dp  | 891 − 52 = **839 dp** | ~300 dp      | 1139 dp | **no** |

When the demand exceeds the window, Compose's `Column` does not shrink the board — it lays the
children out past the bottom edge and clips. Concretely, on any landscape or large landscape
window the user sees:

- **BUG-01 (critical, functional):** the bottom rows of the board are cut off, so cells 6/7/8 are
  **unreachable** — the game becomes unwinnable in landscape.
- **BUG-02 (critical, UI):** the *Restart* and *Quit* controls (`GameScreen.kt:160-166`) are pushed
  off screen entirely. The only way out is the system back gesture.
- **BUG-03 (high, UI):** `Spacer(Modifier.weight(1f))` at `GameScreen.kt:158` gets zero space, so
  the intended breathing room between board and controls silently disappears even where it fits.
- **BUG-04 (high, UI):** on tablet portrait the board balloons to ~748 dp square — a cartoonishly
  large board with tiny marks (see BUG-05) that dwarfs the rest of the screen.

## 1.2 Fixed type and radii inside a stretchy board

- **BUG-05 (high, UI):** `GameTile` hardcodes `markFontSize: Int = 46` (`GameTile.kt:42`) and
  `cornerRadius: Int = 20` (`GameTile.kt:41`). On a 748 dp board each tile is ~240 dp, so a 46 sp
  "X" occupies under a fifth of its cell and the 20 dp corners read as almost square. The board
  looks broken even where it fits.
- **BUG-06 (medium, UI):** `GameBoard` hardcodes a 10 dp gap between tiles regardless of board
  size, so the grid rhythm is wrong at both extremes.

## 1.3 Every other screen assumes one phone-width column

None of the screens read the window size. On a 1280 dp-wide tablet:

- **BUG-07 (high, UI):** `HomeScreen` (`HomeScreen.kt:119`, `:134`), `SettingsScreen`
  (`:75`, `:91`, `:108`, `:127`), `StatsScreen` and `ProfileScreen` (`:91`, `:98`) stretch every
  card to `fillMaxWidth()`. Rows become ~1240 dp wide with a 48 dp icon at one end and a chevron at
  the other — a lake of empty space. Text line length blows past the readable ~75-character limit.
- **BUG-08 (high, UI):** `MainScaffold`'s bottom bar (`MainScaffold.kt:52-60`) spreads four tabs
  `SpaceEvenly` across the full width, leaving ~300 dp between neighbouring tabs on a tablet.
  Material guidance is a navigation rail at expanded width; the bottom bar is also a long reach on
  a large device held in two hands.
- **BUG-09 (medium, UI):** `MatchSetupScreen`'s symbol tiles (`MatchSetupScreen.kt:120`) are fixed
  at `height(88.dp)` but `weight(1f)` wide, so on a tablet they become 600×88 dp letterboxes with a
  44 sp glyph floating in the middle.
- **BUG-10 (medium, UI):** `OnboardingScreen`'s illustration is a fixed `size(200.dp)` box
  (`OnboardingScreen.kt:81`) inside a `verticalArrangement = Center` column with no scroll. On a
  landscape phone (411 dp tall) the 200 dp art + 36 dp spacer + title + body overflow and clip.

## 1.4 Missing scroll containers

- **BUG-11 (high, UI):** `ProfileScreen` has **no** `verticalScroll` (`ProfileScreen.kt:64-70`).
  In landscape, in split-screen, or at 200% font scale its content is clipped with no way to reach
  the "Edit display name" card. `HomeScreen`, `StatsScreen` and `SettingsScreen` do scroll;
  `MatchSetupScreen` (`MatchSetupScreen.kt:63`) and `OnboardingScreen` do not.
- **BUG-12 (medium, UI):** `ProfileScreen`'s gradient header is a fixed `height(230.dp)`
  (`ProfileScreen.kt:57`) that the content column is *not* anchored to. At large font scale the
  avatar + name row grows past 230 dp and white-on-gradient text lands on the page background,
  becoming unreadable.

## 1.5 Insets are handled with the wrong API

- **BUG-13 (medium, UI):** `GameScreen.kt:91`, `MatchSetupScreen.kt:60` and
  `OnboardingScreen.kt:62` use `systemBarsPadding()`. That excludes the **display cutout**, which
  in landscape sits on the *left or right* edge. On a notched phone or tablet in landscape the back
  arrow and the board's edge tiles slide under the camera housing. `safeDrawingPadding()` is the
  correct modifier.

## 1.6 State is lost on every configuration change

The activity is recreated on rotation, fold/unfold, and window resize (there is no
`android:configChanges` in `AndroidManifest.xml`, which is correct for Compose). ViewModels
survive; `remember` does not. These use `remember` where `rememberSaveable` is required:

- **BUG-14 (high, functional):** `MatchSetupScreen.kt:57-58` — the chosen symbol and difficulty
  reset to X/initial on rotation, silently changing what match the user is about to start.
- **BUG-15 (medium, functional):** `HomeScreen.kt:62` — the difficulty picked in the Vs-AI card
  resets on rotation.
- **BUG-16 (medium, functional):** `ProfileScreen.kt:50` and `:109` — rotating with the rename
  dialog open closes it and discards the typed draft.
- **BUG-17 (medium, functional):** `GameScreen.kt:73` — the round timer resets to 0:00 on every
  rotation, so the displayed time is wrong for the rest of the round.

`GameViewModel` state itself is safe: `viewModel()` is scoped to the `NavBackStackEntry`, whose
`ViewModelStore` is retained across configuration changes.

## 1.7 Settings that do nothing

- **BUG-18 (high, functional):** **Board theme is dead code.** `BoardTheme` is persisted
  (`AppSettings.kt`), surfaced as three swatches (`SettingsScreen.kt:145-160`) and written by
  `SettingsViewModel.setBoardTheme`, but a repo-wide grep finds **no reader** anywhere outside the
  settings screen. Tapping a swatch changes the selection ring and nothing else. The board always
  renders CLASSIC.
- **BUG-19 (high, functional):** **Sound effects are dead code.** `soundEnabled` is persisted and
  toggled, but nothing reads it, there is no audio playback code anywhere, and `res/raw/` is
  **empty**. The toggle is inert.
- **BUG-20 (medium, functional):** **Dark mode is a one-way door.** `SettingsViewModel.setDarkMode`
  maps the switch to `ThemeMode.DARK` or `ThemeMode.LIGHT`. `ThemeMode.SYSTEM` is the default but
  becomes unreachable the moment the user touches the switch — there is no way back to "follow
  system".
- **BUG-21 (low, UI):** `res/values-night/themes.xml` swaps `windowBackground` on the **system**
  dark setting, but the app's theme follows `AppSettings.themeMode`. With the app forced to DARK on
  a light system, every activity start and configuration change flashes a light `#F4F4F8` window
  before Compose paints. Same in reverse.

## 1.8 Font scale and text overflow

Large-screen devices ship with larger display/font scale far more often than phones.

- **BUG-22 (medium, UI):** No `maxLines`/`overflow` anywhere. `ScoreCard`'s name
  (`GameScreen.kt:190`) wraps or clips with "Player X" at 200% scale; `TurnIndicator`'s pill text,
  `MatchRow`'s title (`StatsScreen.kt:163`) and `PlayerRow`'s label
  (`MatchSetupScreen.kt:156`) have the same exposure.
- **BUG-23 (medium, UI):** Score digits at a fixed `fontSize = 30.sp` (`GameScreen.kt:191`) and
  symbol glyphs at `44.sp` (`MatchSetupScreen.kt:129`) sit inside fixed-height containers; at high
  font scale they clip vertically.

## 1.9 Accessibility

- **BUG-24 (medium, a11y):** Touch targets below the 48 dp minimum: the game back button
  (`GameScreen.kt:100`, 28 dp), `TicTopBar`'s back button (`Common.kt:74`, 28 dp) and the board
  theme swatches (`SettingsScreen.kt:154`, 42 dp).
- **BUG-25 (medium, a11y):** Every empty cell announces the identical `"empty cell"`
  (`GameTile.kt:94-98`) with no position, so a TalkBack user cannot tell the nine cells apart.
- **BUG-26 (low, a11y/functional):** In `ProfileScreen.kt:78-88` the pencil `Icon` carries
  `contentDescription = "Edit name"` but sits in a **non-clickable** `Box`. It reads as an action
  and does nothing. Directly below it, `Spacer(Modifier.height(0.dp))` and the comment
  "Make the whole name row tappable to edit" (`ProfileScreen.kt:85-86`) are abandoned scaffolding.

## 1.10 Result overlay sizing

- **BUG-27 (high, UI):** `ResultDialog` uses `usePlatformDefaultWidth = false` and centres a
  wrap-content `Column` (`ResultDialog.kt:88-96`). The button `Row` inside it uses
  `weight(1f)`/`weight(1.3f)`, and weighted children expand to the *incoming max constraint* — the
  full dialog width. So the "wrap-content" card is actually **full window width minus 56 dp**. On a
  1280 dp tablet the end-of-round card spans the whole screen with two enormous buttons.
- **BUG-28 (low, functional):** `onDismissRequest = {}` (`ResultDialog.kt:59`) swallows the system
  back gesture. Combined with BUG-02 (controls off screen in landscape), a user who finishes a
  round in landscape has no on-screen and no gesture route out.

## 1.11 Theme drift

- **BUG-29 (low, UI):** `GameScreen.statusPill` hardcodes `Color(0xFF14B8A6)`,
  `Color(0xFFF97316)` and `Color(0xFF9A9AAE)` (`GameScreen.kt:222-238`) instead of reading
  `TicTacTheme.colors.playerX` / `.playerO` / `.inkFaint`. These will silently diverge from the
  palette and will not respond to the board theme fixed in BUG-18.

## 1.12 Nothing catches any of this

- **BUG-30 (high, process):** There is no `androidTest/` source set and no UI test of any kind. The
  Compose UI test dependencies are declared but unused. Nothing in CI would have caught a board
  that overflows its window.

## 1.13 Severity roll-up

| Severity | IDs |
| -------- | --- |
| Critical | BUG-01, BUG-02 |
| High     | BUG-03, BUG-04, BUG-05, BUG-07, BUG-08, BUG-11, BUG-14, BUG-18, BUG-19, BUG-27, BUG-30 |
| Medium   | BUG-06, BUG-09, BUG-10, BUG-12, BUG-13, BUG-15, BUG-16, BUG-17, BUG-20, BUG-22, BUG-23, BUG-24, BUG-25 |
| Low      | BUG-21, BUG-26, BUG-28, BUG-29 |

## 1.14 Two decisions this plan makes on your behalf

Both are called out here so they are easy to reverse:

1. **BUG-19 (sound)** is fixed by **removing the inert toggle from the Settings UI** while keeping
   the `soundEnabled` field in `AppSettings` and its repository setter. Shipping a real sound
   engine needs audio assets that do not exist in the repo (`res/raw/` is empty), and sourcing
   licensed audio is outside a bug-fix change. A shipped switch that does nothing is worse than no
   switch. Task 11 re-adds it in one commit once assets land.
2. **BUG-21 (launch flash)** is accepted, not fixed. Reading DataStore before the first frame would
   mean a blocking disk read on the main thread at startup. The three-way theme control from
   Task 12 makes the mismatch rarer, and the flash is a single frame.

---

# Part 2 — Implementation Plan

## File structure

**New files**

| Path | Responsibility |
| ---- | -------------- |
| `ui/layout/WindowSize.kt` | `WidthClass`/`HeightClass`/`WindowSize` types, the pure `windowSizeOf()` classifier, and the `rememberWindowSize()` composable that reads the real window. |
| `ui/layout/Sizing.kt` | Pure sizing maths: `boardSideDp()`, `boardGapFraction`, `tileMarkFraction`. No Compose imports beyond none — plain Kotlin so it unit-tests on the JVM. |
| `ui/components/ScreenContainer.kt` | The shared scrolling, width-capped, centred content column used by every tab and detail screen. |
| `ui/theme/BoardPalette.kt` | Maps `BoardTheme` → the four board colours, replacing direct `TicTacTheme.colors.playerX` reads inside the board. |
| `app/src/test/java/com/skystone1000/xoxo/ui/layout/WindowSizeTest.kt` | JVM tests for `windowSizeOf()`. |
| `app/src/test/java/com/skystone1000/xoxo/ui/layout/SizingTest.kt` | JVM tests for `boardSideDp()`. |
| `app/src/androidTest/java/com/skystone1000/xoxo/ui/screens/game/GameScreenSizeTest.kt` | Instrumented Compose tests asserting the board and controls are reachable at four window sizes. |

**Modified files**

| Path | Change |
| ---- | ------ |
| `ui/components/GameBoard.kt` | Self-sizing square board; adaptive gap. |
| `ui/components/GameTile.kt` | Derives corner radius and mark size from its own measured size; positional content description; palette-driven colours. |
| `ui/components/ResultDialog.kt` | Width-capped card; back gesture goes home. |
| `ui/components/Common.kt` | 48 dp back-button target in `TicTopBar`. |
| `ui/navigation/MainScaffold.kt` | Navigation rail at expanded width, bottom bar otherwise. |
| `ui/screens/game/GameScreen.kt` | Single-pane and two-pane layouts; `safeDrawingPadding`; saveable timer; theme colours in `statusPill`; text overflow guards. |
| `ui/screens/home/HomeScreen.kt` | `ScreenContainer`; `rememberSaveable` difficulty. |
| `ui/screens/stats/StatsScreen.kt` | `ScreenContainer`; text overflow guards. |
| `ui/screens/settings/SettingsScreen.kt` | `ScreenContainer`; three-way theme control; sound row removed; 48 dp swatch targets. |
| `ui/screens/profile/ProfileScreen.kt` | Scroll; adaptive header; tappable name row; saveable dialog state. |
| `ui/screens/setup/MatchSetupScreen.kt` | `ScreenContainer`; adaptive symbol tiles; `rememberSaveable`; `safeDrawingPadding`. |
| `ui/screens/onboarding/OnboardingScreen.kt` | Adaptive illustration; scrollable page; `safeDrawingPadding`. |
| `ui/screens/settings/SettingsViewModel.kt` | `setThemeMode(ThemeMode)` replaces `setDarkMode(Boolean)`. |
| `ui/screens/game/GameViewModel.kt` | Expose `boardTheme` in `GameUiState`. |
| `ui/navigation/TicTacNavHost.kt` | Wire the renamed settings intent. |
| `docs/ARCHITECTURE.md`, `docs/CODEBASE.md`, `docs/FEATURES.md` | Sync (Task 15). |

## Task ordering

Tasks 1–2 are the foundation and must land first. Tasks 3–5 fix the reported bug. Tasks 6–14 are
independent of each other and can be parallelised once Task 5 is merged.

---

### Task 1: Window-size classification

**Files:**
- Create: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/layout/WindowSize.kt`
- Test: `TicTacToe/app/src/test/java/com/skystone1000/xoxo/ui/layout/WindowSizeTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `enum class WidthClass { COMPACT, MEDIUM, EXPANDED }`
  - `enum class HeightClass { COMPACT, MEDIUM, EXPANDED }`
  - `data class WindowSize(val width: WidthClass, val height: HeightClass)` with
    `val isExpandedWidth: Boolean`, `val isCompactHeight: Boolean`, `val useTwoPaneGame: Boolean`
  - `fun windowSizeOf(widthDp: Float, heightDp: Float): WindowSize` — pure
  - `@Composable fun rememberWindowSize(): WindowSize`

- [ ] **Step 1: Write the failing test**

Create `TicTacToe/app/src/test/java/com/skystone1000/xoxo/ui/layout/WindowSizeTest.kt`:

```kotlin
package com.skystone1000.xoxo.ui.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowSizeTest {

    @Test
    fun `phone portrait is compact width and medium height`() {
        val size = windowSizeOf(411f, 891f)
        assertEquals(WidthClass.COMPACT, size.width)
        assertEquals(HeightClass.MEDIUM, size.height)
    }

    @Test
    fun `phone landscape is medium width and compact height`() {
        val size = windowSizeOf(891f, 411f)
        assertEquals(WidthClass.MEDIUM, size.width)
        assertEquals(HeightClass.COMPACT, size.height)
    }

    @Test
    fun `tablet portrait is medium width and expanded height`() {
        val size = windowSizeOf(800f, 1280f)
        assertEquals(WidthClass.MEDIUM, size.width)
        assertEquals(HeightClass.EXPANDED, size.height)
    }

    @Test
    fun `tablet landscape is expanded width`() {
        val size = windowSizeOf(1280f, 800f)
        assertEquals(WidthClass.EXPANDED, size.width)
        assertEquals(HeightClass.MEDIUM, size.height)
    }

    @Test
    fun `breakpoints are inclusive at the lower bound`() {
        assertEquals(WidthClass.MEDIUM, windowSizeOf(600f, 800f).width)
        assertEquals(WidthClass.EXPANDED, windowSizeOf(840f, 800f).width)
        assertEquals(HeightClass.MEDIUM, windowSizeOf(600f, 480f).height)
        assertEquals(HeightClass.EXPANDED, windowSizeOf(600f, 900f).height)
    }

    @Test
    fun `two pane game is used in landscape and on expanded width`() {
        assertTrue(windowSizeOf(891f, 411f).useTwoPaneGame)
        assertTrue(windowSizeOf(1280f, 800f).useTwoPaneGame)
        assertFalse(windowSizeOf(411f, 891f).useTwoPaneGame)
        assertFalse(windowSizeOf(800f, 1280f).useTwoPaneGame)
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

```bash
cd TicTacToe && ./gradlew :app:testDebugUnitTest --tests '*WindowSizeTest*'
```

Expected: compilation failure — `Unresolved reference: windowSizeOf`.

- [ ] **Step 3: Write the implementation**

Create `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/layout/WindowSize.kt`:

```kotlin
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
```

- [ ] **Step 4: Run the test and confirm it passes**

```bash
cd TicTacToe && ./gradlew :app:testDebugUnitTest --tests '*WindowSizeTest*'
```

Expected: `BUILD SUCCESSFUL`, 6 tests passing.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/layout/WindowSize.kt TicTacToe/app/src/test/java/com/skystone1000/xoxo/ui/layout/WindowSizeTest.kt && git commit -m "feat(ui): add window size classification for adaptive layouts"
```

---

### Task 2: Pure board-sizing maths

**Files:**
- Create: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/layout/Sizing.kt`
- Test: `TicTacToe/app/src/test/java/com/skystone1000/xoxo/ui/layout/SizingTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `const val MAX_BOARD_SIDE_DP: Float = 420f`
  - `const val MAX_CONTENT_WIDTH_DP: Float = 560f`
  - `fun boardSideDp(availableWidthDp: Float, availableHeightDp: Float, maxSideDp: Float = MAX_BOARD_SIDE_DP): Float`
  - `fun boardGapDp(sideDp: Float): Float`
  - `fun tileMarkSp(tileSideDp: Float): Float`
  - `fun tileCornerDp(tileSideDp: Float): Float`

- [ ] **Step 1: Write the failing test**

Create `TicTacToe/app/src/test/java/com/skystone1000/xoxo/ui/layout/SizingTest.kt`:

```kotlin
package com.skystone1000.xoxo.ui.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class SizingTest {

    @Test
    fun `board fits the narrower dimension on a phone`() {
        assertEquals(359f, boardSideDp(359f, 620f), 0.01f)
    }

    @Test
    fun `board is capped so it never balloons on a tablet`() {
        assertEquals(MAX_BOARD_SIDE_DP, boardSideDp(748f, 900f), 0.01f)
    }

    @Test
    fun `board is limited by height in landscape`() {
        assertEquals(300f, boardSideDp(1200f, 300f), 0.01f)
    }

    @Test
    fun `board never goes negative when there is no room`() {
        assertEquals(0f, boardSideDp(-40f, 200f), 0.01f)
    }

    @Test
    fun `unbounded height falls back to width`() {
        assertEquals(MAX_BOARD_SIDE_DP, boardSideDp(500f, Float.POSITIVE_INFINITY), 0.01f)
    }

    @Test
    fun `gap scales with the board but stays within bounds`() {
        assertEquals(6f, boardGapDp(100f), 0.01f)   // clamped low
        assertEquals(10.5f, boardGapDp(350f), 0.01f) // 3% of 350
        assertEquals(14f, boardGapDp(900f), 0.01f)  // clamped high
    }

    @Test
    fun `mark size is a fixed fraction of the tile`() {
        assertEquals(46f, tileMarkSp(100f), 0.01f)
        assertEquals(23f, tileMarkSp(50f), 0.01f)
    }

    @Test
    fun `tile corner scales and clamps`() {
        assertEquals(12f, tileCornerDp(40f), 0.01f)  // clamped low
        assertEquals(22f, tileCornerDp(100f), 0.01f) // 22% of 100
        assertEquals(28f, tileCornerDp(300f), 0.01f) // clamped high
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

```bash
cd TicTacToe && ./gradlew :app:testDebugUnitTest --tests '*SizingTest*'
```

Expected: compilation failure — `Unresolved reference: boardSideDp`.

- [ ] **Step 3: Write the implementation**

Create `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/layout/Sizing.kt`:

```kotlin
package com.skystone1000.xoxo.ui.layout

/**
 * Pure layout maths shared by the board and the screen containers. Deliberately expressed in bare
 * `Float` dp/sp values rather than `Dp`/`TextUnit` so it unit-tests on the JVM without Robolectric.
 */

/** The board never grows past this, however much room there is. Keeps tablets from looking silly. */
const val MAX_BOARD_SIDE_DP: Float = 420f

/** Reading-comfort cap for single-column screen content on wide windows. */
const val MAX_CONTENT_WIDTH_DP: Float = 560f

/**
 * The largest square that fits in the given space, capped at [maxSideDp].
 *
 * An infinite [availableHeightDp] (an unbounded parent, e.g. inside a scroll container) falls back
 * to the width so the board still gets a finite size instead of crashing the measure pass.
 */
fun boardSideDp(
    availableWidthDp: Float,
    availableHeightDp: Float,
    maxSideDp: Float = MAX_BOARD_SIDE_DP,
): Float {
    val height = if (availableHeightDp.isFinite()) availableHeightDp else availableWidthDp
    return minOf(availableWidthDp, height, maxSideDp).coerceAtLeast(0f)
}

/** Gap between tiles: 3% of the board side, clamped to a sane 6..14 dp. */
fun boardGapDp(sideDp: Float): Float = (sideDp * 0.03f).coerceIn(6f, 14f)

/** Mark glyph size: 46% of the tile side, so an "X" always fills its cell the same way. */
fun tileMarkSp(tileSideDp: Float): Float = tileSideDp * 0.46f

/** Tile corner radius: 22% of the tile side, clamped to 12..28 dp. */
fun tileCornerDp(tileSideDp: Float): Float = (tileSideDp * 0.22f).coerceIn(12f, 28f)
```

- [ ] **Step 4: Run the test and confirm it passes**

```bash
cd TicTacToe && ./gradlew :app:testDebugUnitTest --tests '*SizingTest*'
```

Expected: `BUILD SUCCESSFUL`, 8 tests passing.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/layout/Sizing.kt TicTacToe/app/src/test/java/com/skystone1000/xoxo/ui/layout/SizingTest.kt && git commit -m "feat(ui): add pure board sizing maths with unit tests"
```

---

### Task 3: Self-sizing board and tiles (BUG-01, BUG-04, BUG-05, BUG-06, BUG-25)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameTile.kt` (whole file)
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameBoard.kt` (whole file)

**Interfaces:**
- Consumes: `boardSideDp`, `boardGapDp`, `tileMarkSp`, `tileCornerDp` from Task 2.
- Produces:
  - `GameTile(mark, onClick, modifier, positionLabel: String, highlighted, enabled)` — the
    `cornerRadius: Int` and `markFontSize: Int` parameters are **removed**; the tile measures
    itself. `positionLabel` is a new **required** parameter placed after `modifier`.
  - `GameBoard(marks, onCellClick, modifier, winningLine, enabled, maxSide: Dp = MAX_BOARD_SIDE_DP.dp)`
    — the board now centres a square inside whatever box it is given.

- [ ] **Step 1: Replace `GameTile.kt` with the self-sizing version**

Overwrite `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameTile.kt`:

```kotlin
package com.skystone1000.xoxo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.ui.layout.tileCornerDp
import com.skystone1000.xoxo.ui.layout.tileMarkSp
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicTacTheme

enum class TileMark { EMPTY, X, O }

/**
 * One board cell. It fills whatever box the caller gives it and derives its corner radius and
 * glyph size from that measured size, so it looks right at every board scale.
 *
 * @param positionLabel human-readable position, e.g. "row 1, column 2", used for TalkBack.
 */
@Composable
fun GameTile(
    mark: TileMark,
    onClick: () -> Unit,
    positionLabel: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = TicTacTheme.colors

    val markColor: Color = when (mark) {
        TileMark.X -> colors.playerX
        TileMark.O -> colors.playerO
        TileMark.EMPTY -> Color.Transparent
    }
    val background: Color = when (mark) {
        TileMark.X -> colors.playerXSoft
        TileMark.O -> colors.playerOSoft
        TileMark.EMPTY -> colors.card
    }
    val borderColor = when {
        highlighted && mark == TileMark.X -> colors.playerX
        highlighted && mark == TileMark.O -> colors.playerO
        else -> colors.outline
    }
    val borderWidth = if (highlighted) 2.dp else 1.5.dp

    // Pop-in scale whenever the tile becomes filled.
    val markScale by animateFloatAsState(
        targetValue = if (mark == TileMark.EMPTY) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "markScale",
    )

    val description = when (mark) {
        TileMark.X -> "$positionLabel, X"
        TileMark.O -> "$positionLabel, O"
        TileMark.EMPTY -> "$positionLabel, empty"
    }

    BoxWithConstraints(
        modifier = modifier
            .clickable(
                enabled = enabled && mark == TileMark.EMPTY,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        val tileSide = minOf(maxWidth, maxHeight)
        val shape = RoundedCornerShape(tileCornerDp(tileSide.value).dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (highlighted) Modifier.shadow(10.dp, shape, ambientColor = markColor, spotColor = markColor)
                    else Modifier
                )
                .clip(shape)
                .background(background, shape)
                .border(borderWidth, borderColor, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (mark != TileMark.EMPTY) {
                Text(
                    text = if (mark == TileMark.X) "X" else "O",
                    color = markColor,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = tileMarkSp(tileSide.value).sp,
                    modifier = Modifier.scale(markScale),
                )
            }
        }
    }
}
```

- [ ] **Step 2: Replace `GameBoard.kt` with the square-fitting version**

Overwrite `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameBoard.kt`:

```kotlin
package com.skystone1000.xoxo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skystone1000.xoxo.ui.layout.MAX_BOARD_SIDE_DP
import com.skystone1000.xoxo.ui.layout.boardGapDp
import com.skystone1000.xoxo.ui.layout.boardSideDp

/**
 * Renders a 3x3 board from a flat list of 9 [TileMark]s.
 *
 * The board is always a **square that fits the box it is given** — never taller than the space
 * available — and is capped at [maxSide] so it does not balloon on tablets. Give it a bounded box
 * (e.g. `Modifier.weight(1f).fillMaxWidth()`); an unbounded height falls back to the width.
 *
 * @param marks size-9 list, index 0..8 row-major.
 * @param winningLine indices that form the winning line (highlighted), or empty.
 */
@Composable
fun GameBoard(
    marks: List<TileMark>,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    winningLine: Set<Int> = emptySet(),
    enabled: Boolean = true,
    maxSide: Dp = MAX_BOARD_SIDE_DP.dp,
) {
    require(marks.size == 9) { "GameBoard expects 9 marks, got ${marks.size}" }
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val heightDp = if (constraints.hasBoundedHeight) maxHeight.value else Float.POSITIVE_INFINITY
        val side = boardSideDp(maxWidth.value, heightDp, maxSide.value).dp
        val gap = boardGapDp(side.value).dp

        Column(
            modifier = Modifier.size(side),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        GameTile(
                            mark = marks[index],
                            onClick = { onCellClick(index) },
                            positionLabel = "row ${row + 1}, column ${col + 1}",
                            highlighted = index in winningLine,
                            enabled = enabled,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: Fix the one call site and compile**

`GameScreen.kt` currently passes `modifier = Modifier.padding(horizontal = 26.dp)` to `GameBoard`.
Leave it for now — Task 4 rewrites that screen. Just confirm the module still compiles:

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`. If it fails on an unresolved `cornerRadius`/`markFontSize` argument
anywhere, that call site was missed — remove the argument, it is now derived.

- [ ] **Step 4: Run the full unit suite**

```bash
cd TicTacToe && ./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`, all existing domain/data tests still passing.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameBoard.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameTile.kt && git commit -m "fix(board): size the board to fit its box instead of the full window width"
```

---

### Task 4: Adaptive game screen (BUG-01, BUG-02, BUG-03, BUG-13, BUG-17, BUG-22, BUG-24, BUG-29)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameScreen.kt` (whole file)

**Interfaces:**
- Consumes: `rememberWindowSize()`, `WindowSize.useTwoPaneGame` (Task 1);
  `MAX_CONTENT_WIDTH_DP` (Task 2); the new `GameBoard` (Task 3).
- Produces: `GameScreen(modeLabel, isVsAi, humanSymbol, state, onTileClick, onRestart, onQuit)` —
  signature unchanged, so `TicTacNavHost.kt` needs no edit.

- [ ] **Step 1: Rewrite `GameScreen.kt`**

Overwrite `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameScreen.kt`:

```kotlin
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
import com.skystone1000.xoxo.ui.layout.MAX_CONTENT_WIDTH_DP
import com.skystone1000.xoxo.ui.layout.rememberWindowSize
import com.skystone1000.xoxo.ui.theme.SpaceGrotesk
import com.skystone1000.xoxo.ui.theme.TicColors
import com.skystone1000.xoxo.ui.theme.TicTacTheme
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
            winningLine = winningLine,
            enabled = inProgress && !state.isAiThinking,
            modifier = boardModifier,
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding(),
    ) {
        if (windowSize.useTwoPaneGame) {
            TwoPaneGame(modeLabel, state, isVsAi, seconds, onRestart, onQuit, board)
        } else {
            SinglePaneGame(modeLabel, state, isVsAi, seconds, onRestart, onQuit, board)
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
        ScorePanel(state, seconds, capped.padding(horizontal = 22.dp))
        Spacer(Modifier.height(16.dp))
        TurnPill(state, isVsAi)
        Spacer(Modifier.height(16.dp))
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
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    board: @Composable (Modifier) -> Unit,
) {
    Row(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .widthIn(max = MAX_CONTENT_WIDTH_DP.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(modeLabel, onQuit, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            ScorePanel(state, seconds, Modifier.fillMaxWidth().padding(horizontal = 22.dp))
            Spacer(Modifier.height(16.dp))
            TurnPill(state, isVsAi)
            Spacer(Modifier.height(20.dp))
            Controls(onRestart, onQuit, Modifier.fillMaxWidth().padding(horizontal = 22.dp))
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
private fun ScorePanel(state: GameUiState, seconds: Int, modifier: Modifier = Modifier) {
    val colors = TicTacTheme.colors
    Row(
        modifier,
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
                Text(formatTime(seconds), fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
            }
        }
        ScoreCard(state.playerOName, state.scoreO, colors.playerO, colors.playerOSoft, Modifier.weight(1f))
    }
}

@Composable
private fun TurnPill(state: GameUiState, isVsAi: Boolean) {
    val colors = TicTacTheme.colors
    Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
        val (text, dot) = statusPill(state, isVsAi, colors)
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
        Text("$score", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = color, maxLines = 1)
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
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
    }
}

/** Status pill text + dot colour. Reads the palette so it tracks the theme (was hardcoded hex). */
private fun statusPill(state: GameUiState, isVsAi: Boolean, colors: TicColors): Pair<String, Color> {
    val gs = state.gameState
    return when (val s = gs.status) {
        GameStatus.InProgress -> {
            if (state.isAiThinking) "AI is thinking…" to colors.playerO
            else {
                val name = if (gs.currentPlayer == Player.X) state.playerXName else state.playerOName
                val color = if (gs.currentPlayer == Player.X) colors.playerX else colors.playerO
                "$name — turn" to color
            }
        }
        is GameStatus.Won -> {
            val name = if (s.player == Player.X) state.playerXName else state.playerOName
            "$name completes a row!" to (if (s.player == Player.X) colors.playerX else colors.playerO)
        }
        GameStatus.Draw -> "It's a draw" to colors.inkFaint
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
```

Note: the unused `isVsAi` parameter warning on `SinglePaneGame`/`TwoPaneGame` is intentional — both
pass it through to `TurnPill`.

- [ ] **Step 2: Compile and run the unit suite**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`. Kotlin will warn that `isVsAi` is unused in `SinglePaneGame` if you
forget to forward it to `TurnPill` — that warning is a real bug, not noise.

- [ ] **Step 3: Verify by hand on a tablet emulator**

Launch a `Pixel Tablet` AVD (or `Resizable (Experimental)` set to Tablet), install, start a Vs-AI
match, and check in **both** orientations: all nine cells tappable; Restart and Quit visible; board
centred and no wider than ~420 dp; marks fill their cells.

```bash
cd TicTacToe && ./gradlew :app:installDebug
```

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameScreen.kt && git commit -m "fix(game): adaptive single/two-pane game screen for tablets and landscape"
```

---

### Task 5: Width-capped result overlay (BUG-27, BUG-28)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/ResultDialog.kt:55-96`

**Interfaces:**
- Consumes: nothing new.
- Produces: `ResultDialog(title, subtitle, win, onPlayAgain, onHome)` — signature unchanged.

- [ ] **Step 1: Cap the card width and let back go home**

In `ResultDialog.kt`, add these imports:

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
```

Replace the `Dialog(...)` block (currently `ResultDialog.kt:57-67`) with:

```kotlin
    Dialog(
        // Back gesture leaves the round rather than being swallowed.
        onDismissRequest = onHome,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (win) {
                Confetti(modifier = Modifier.fillMaxSize())
            }
            ResultCard(title, subtitle, win, onPlayAgain, onHome)
        }
    }
```

Replace the `Column` modifier chain inside `ResultCard` (currently `ResultDialog.kt:88-96`) with:

```kotlin
    Column(
        modifier = Modifier
            .scale(scale)
            .padding(28.dp)
            // Weighted buttons below expand to the incoming max width, so without this cap the
            // "wrap content" card spans the whole window on a tablet.
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(colors.card)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
```

- [ ] **Step 2: Compile**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Verify by hand**

On the tablet emulator, finish a round. The card should be a centred ~400 dp panel, not a
full-width banner. Press back — it should return home instead of doing nothing.

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/ResultDialog.kt && git commit -m "fix(result): cap the end-of-round card width and honour the back gesture"
```

---

### Task 6: Shared screen container (BUG-07, BUG-11)

**Files:**
- Create: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/ScreenContainer.kt`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/home/HomeScreen.kt:59-66`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/stats/StatsScreen.kt:47-56`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsScreen.kt:60-69`

**Interfaces:**
- Consumes: `MAX_CONTENT_WIDTH_DP` (Task 2).
- Produces:
  `@Composable fun ScreenContainer(contentPadding: PaddingValues, modifier: Modifier = Modifier, maxContentWidth: Dp = MAX_CONTENT_WIDTH_DP.dp, horizontalPadding: Dp = 22.dp, verticalPadding: Dp = 8.dp, scrollable: Boolean = true, content: @Composable ColumnScope.() -> Unit)`

- [ ] **Step 1: Create the container**

Create `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/ScreenContainer.kt`:

```kotlin
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
 * column that is capped at [maxContentWidth]. Without the cap, cards stretch to 1200dp+ on a
 * tablet and read as an empty page with icons stranded at the edges.
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
```

- [ ] **Step 2: Adopt it in `HomeScreen.kt`**

Replace the outer `Column(...)` opening (`HomeScreen.kt:59-66`):

```kotlin
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp),
    ) {
```

with:

```kotlin
    ScreenContainer(contentPadding = contentPadding) {
```

Add `import com.skystone1000.xoxo.ui.components.ScreenContainer` and remove the now-unused
`fillMaxSize`, `rememberScrollState` and `verticalScroll` imports.

- [ ] **Step 3: Adopt it in `StatsScreen.kt` and `SettingsScreen.kt`**

Apply the identical replacement to `StatsScreen.kt:47-56` and `SettingsScreen.kt:60-69` — both
have the same `fillMaxSize().background(colors.background).padding(contentPadding).verticalScroll(...).padding(horizontal = 22.dp, vertical = 8.dp)`
chain. Replace each with `ScreenContainer(contentPadding = contentPadding) {` and prune the imports
the compiler flags.

- [ ] **Step 4: Compile**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL` with no unused-import warnings from the three edited screens.

- [ ] **Step 5: Verify by hand**

On the tablet emulator in landscape, visit Home, Stats and Settings. Cards should be a centred
560 dp column, not full-bleed.

- [ ] **Step 6: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/ScreenContainer.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/home/HomeScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/stats/StatsScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsScreen.kt && git commit -m "fix(ui): cap and centre tab-screen content width on large windows"
```

---

### Task 7: Navigation rail on expanded windows (BUG-08)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/navigation/MainScaffold.kt` (whole file)

**Interfaces:**
- Consumes: `rememberWindowSize()`, `WindowSize.isExpandedWidth` (Task 1).
- Produces: `MainScaffold(current, onSelect, content)` — signature unchanged, so
  `TicTacNavHost.kt` needs no edit.

- [ ] **Step 1: Add the rail branch**

In `MainScaffold.kt`, add imports:

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import com.skystone1000.xoxo.ui.layout.rememberWindowSize
```

Replace the body of `MainScaffold` with:

```kotlin
@Composable
fun MainScaffold(
    current: HomeTab,
    onSelect: (HomeTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSize = rememberWindowSize()
    if (windowSize.isExpandedWidth) {
        // Expanded width: a rail is a shorter reach than a bar spread across 1200dp.
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
```

Add the rail below `BottomBar`:

```kotlin
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
                    // 48dp minimum interactive size.
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
                )
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Verify by hand**

On the tablet emulator in landscape (>= 840 dp wide), the four tabs sit in a left rail. Rotate to
portrait (800 dp) — the bottom bar returns. Tab selection state survives the switch.

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/navigation/MainScaffold.kt && git commit -m "feat(nav): use a navigation rail on expanded-width windows"
```

---

### Task 8: Profile screen — scroll, header, tappable name (BUG-11, BUG-12, BUG-16, BUG-26)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/profile/ProfileScreen.kt:48-115`

**Interfaces:**
- Consumes: `ScreenContainer` (Task 6).
- Produces: `ProfileScreen(state, contentPadding, onSaveName)` — signature unchanged.

- [ ] **Step 1: Make the state saveable and the header adaptive**

In `ProfileScreen.kt`, swap `remember` for `rememberSaveable`:

```kotlin
import androidx.compose.runtime.saveable.rememberSaveable
```

`ProfileScreen.kt:50`:

```kotlin
    var editing by rememberSaveable { mutableStateOf(false) }
```

`ProfileScreen.kt:109` — hoist the draft out of the `if (editing)` block so it survives rotation.
Move it to sit next to `editing`, keyed on the incoming name:

```kotlin
    var draft by rememberSaveable(state.name) { mutableStateOf(state.name) }
```

and delete the `var draft by remember { mutableStateOf(state.name) }` line inside `if (editing)`.

- [ ] **Step 2: Replace the fixed header with an intrinsic one**

Replace the whole `Box(Modifier.fillMaxSize()...)` body (`ProfileScreen.kt:53-105`) with:

```kotlin
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Gradient header sized by its own content, so it can never be outgrown at large font
        // scale (it used to be a fixed 230dp box behind an unanchored column).
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF4F3FF0), Color(0xFF3A2FB0))))
                .padding(top = 32.dp, bottom = 40.dp, start = 22.dp, end = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(initial = state.name, tone = AvatarTone.Teal, size = 84, cornerRadius = 28)
            Spacer(Modifier.height(12.dp))
            // The whole name row is the edit affordance — the pencil used to look tappable and
            // do nothing.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { editing = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.name,
                    fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp,
                    color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Rounded.Edit, contentDescription = "Edit name", tint = Color.White,
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
                .padding(top = 24.dp, start = 22.dp, end = 22.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
```

Add the imports the compiler asks for:
`androidx.compose.foundation.clickable`, `androidx.compose.foundation.rememberScrollState`,
`androidx.compose.foundation.verticalScroll`, `androidx.compose.foundation.layout.widthIn`,
`androidx.compose.foundation.layout.size`, `androidx.compose.ui.text.style.TextOverflow`,
`com.skystone1000.xoxo.ui.layout.MAX_CONTENT_WIDTH_DP`. Remove the now-unused `Box` and `height`
imports.

- [ ] **Step 3: Compile**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Verify by hand**

Phone in landscape: the Profile tab scrolls and the "Edit display name" card is reachable. Tap the
name in the header — the rename dialog opens. Rotate with it open — it stays open with the draft
intact. Set system font size to the largest step — nothing clips.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/profile/ProfileScreen.kt && git commit -m "fix(profile): scrollable body, intrinsic header, tappable name row"
```

---

### Task 9: Match setup — adaptive, scrollable, saveable (BUG-09, BUG-13, BUG-14)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/setup/MatchSetupScreen.kt:56-135`

**Interfaces:**
- Consumes: `MAX_CONTENT_WIDTH_DP` (Task 2).
- Produces: `MatchSetupScreen(mode, initialDifficulty, onStart, onBack)` — signature unchanged.

- [ ] **Step 1: Make the choices survive rotation**

Add `import androidx.compose.runtime.saveable.rememberSaveable` and change
`MatchSetupScreen.kt:57-58` to:

```kotlin
    var symbol by rememberSaveable { mutableStateOf(Player.X) }
    var difficulty by rememberSaveable { mutableStateOf(initialDifficulty) }
```

`Player` and `Difficulty` are enums, which `rememberSaveable` handles via the default
`Saver` for `Serializable`/`Parcelable`-compatible types. If the compiler or runtime rejects them,
store the ordinal instead:

```kotlin
    var symbolOrdinal by rememberSaveable { mutableIntStateOf(Player.X.ordinal) }
    val symbol = Player.entries[symbolOrdinal]
```

- [ ] **Step 2: Fix insets, scroll and content width**

Replace `MatchSetupScreen.kt:60-68`:

```kotlin
    Column(Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        TicTopBar(title = "Match setup", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 22.dp),
        ) {
```

with:

```kotlin
    Column(
        Modifier.fillMaxSize().background(colors.background).safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TicTopBar(
            title = "Match setup",
            onBack = onBack,
            modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH_DP.dp).fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
```

and give the "Start match" button the same cap — change its modifier
(`MatchSetupScreen.kt:107-111`) to:

```kotlin
        modifier = Modifier
            .widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 24.dp),
```

Swap the `systemBarsPadding` import for `safeDrawingPadding`, and add `verticalScroll`,
`rememberScrollState`, `widthIn`, and `MAX_CONTENT_WIDTH_DP`.

- [ ] **Step 3: Make the symbol tiles square instead of letterboxed**

Replace the `SymbolChoice` `Box` modifier (`MatchSetupScreen.kt:118-125`):

```kotlin
    Box(
        modifier = modifier
            .height(88.dp)
```

with:

```kotlin
    Box(
        modifier = modifier
            // Square, so it stays a "tile" at every width instead of a 600x88 letterbox.
            .aspectRatio(1f)
            .heightIn(max = 140.dp)
```

Add `import androidx.compose.foundation.layout.heightIn`. `aspectRatio` is already imported at
`MatchSetupScreen.kt:11`. Remove the now-unused `height` import if the compiler flags it.

- [ ] **Step 4: Compile and verify**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin :app:installDebug
```

Open Match setup, pick O and Hard, rotate — both selections persist. On a tablet the sheet is a
centred 560 dp column with square symbol tiles.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/setup/MatchSetupScreen.kt && git commit -m "fix(setup): saveable choices, safe insets, square symbol tiles"
```

---

### Task 10: Onboarding in short windows (BUG-10, BUG-13)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/onboarding/OnboardingScreen.kt:62-99`

**Interfaces:**
- Consumes: `rememberWindowSize()` (Task 1).
- Produces: `OnboardingScreen(onFinish)` — signature unchanged.

- [ ] **Step 1: Scale the illustration and let the page scroll**

Add imports:

```kotlin
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.skystone1000.xoxo.ui.layout.MAX_CONTENT_WIDTH_DP
import com.skystone1000.xoxo.ui.layout.rememberWindowSize
```

Change `OnboardingScreen.kt:62` from `.systemBarsPadding()` to `.safeDrawingPadding()`.

Inside the composable body, above `Box(...)`, add:

```kotlin
    val windowSize = rememberWindowSize()
    // The 200dp illustration does not fit a 411dp-tall landscape phone alongside the copy.
    val artSize = if (windowSize.isCompactHeight) 120.dp else 200.dp
```

Replace the pager page `Column` (`OnboardingScreen.kt:75-99`) opening with:

```kotlin
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(artSize)
                            .clip(RoundedCornerShape(artSize * 0.24f))
                            .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.playerXSoft))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(page.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(artSize * 0.44f))
                    }
                    Spacer(Modifier.height(if (windowSize.isCompactHeight) 20.dp else 36.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.inkMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH_DP.dp),
                    )
                }
```

Note `verticalArrangement = Center` and `verticalScroll` compose correctly: when content is shorter
than the viewport it stays centred; when it is taller it scrolls.

- [ ] **Step 2: Cap the footer width**

Change the footer `Row` modifier (`OnboardingScreen.kt:100-104`) to:

```kotlin
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 36.dp),
```

- [ ] **Step 3: Compile and verify**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin :app:installDebug
```

Clear app data (`adb shell pm clear com.skystone1000.xoxo`) to see onboarding again. Rotate a phone
to landscape on each of the three pages — nothing clips, Next/Get-started stays reachable.

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/onboarding/OnboardingScreen.kt && git commit -m "fix(onboarding): scale the illustration and scroll in short windows"
```

---

### Task 11: Remove the inert sound toggle (BUG-19)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsScreen.kt:72-88`

**Interfaces:**
- Consumes: nothing new.
- Produces: `SettingsScreen(settings, contentPadding, onHaptics, onThemeMode, onDifficulty, onBoardTheme)`
  — the `onSound: (Boolean) -> Unit` parameter is **removed**. `onThemeMode` arrives in Task 12;
  do this task first and keep `onDarkMode` untouched here.

See §1.14 for why this is a removal rather than an implementation. `AppSettings.soundEnabled`,
`SettingsRepository.setSound` and `SettingsViewModel.setSound` all stay — only the UI row goes,
so restoring it later is a one-line change.

- [ ] **Step 1: Delete the row and the parameter**

In `SettingsScreen.kt`, remove `onSound: (Boolean) -> Unit,` from the parameter list, and remove
these two lines from the General card:

```kotlin
                ToggleRow(Icons.AutoMirrored.Rounded.VolumeUp, "Sound effects", settings.soundEnabled, onSound)
                Divider()
```

Remove the now-unused `import androidx.compose.material.icons.automirrored.rounded.VolumeUp`.

- [ ] **Step 2: Update the call site**

In `TicTacNavHost.kt`, delete the `onSound = vm::setSound,` line from the `SettingsScreen(...)` call.

- [ ] **Step 3: Add a note so the intent is not lost**

Above the General `TicCard` in `SettingsScreen.kt`, add:

```kotlin
        // Sound effects: the setting is still persisted (AppSettings.soundEnabled) but the row is
        // hidden until real audio assets land in res/raw. A switch that does nothing is worse
        // than no switch. See docs/ADAPTIVE-UI-PLAN.md §1.14.
```

- [ ] **Step 4: Compile**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/navigation/TicTacNavHost.kt && git commit -m "fix(settings): hide the inert sound toggle until audio assets exist"
```

---

### Task 12: Three-way theme control (BUG-20) and 48dp swatches (BUG-24)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsViewModel.kt:26-28`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsScreen.kt`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/navigation/TicTacNavHost.kt:90-100`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/Common.kt:70-78`

**Interfaces:**
- Consumes: `ThemeMode` (existing).
- Produces:
  - `SettingsViewModel.setThemeMode(mode: ThemeMode)` replaces `setDarkMode(dark: Boolean)`.
  - `SettingsScreen(..., onThemeMode: (ThemeMode) -> Unit, ...)` replaces `onDarkMode`.

- [ ] **Step 1: Change the ViewModel intent**

In `SettingsViewModel.kt`, replace:

```kotlin
    fun setDarkMode(dark: Boolean) = viewModelScope.launch {
        repository.setThemeMode(if (dark) ThemeMode.DARK else ThemeMode.LIGHT)
    }
```

with:

```kotlin
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
```

- [ ] **Step 2: Replace the switch with a segmented control**

In `SettingsScreen.kt`, change the parameter `onDarkMode: (Boolean) -> Unit,` to
`onThemeMode: (ThemeMode) -> Unit,`, and replace the dark-mode `ToggleRow` block:

```kotlin
                Divider()
                ToggleRow(
                    Icons.Rounded.DarkMode, "Dark mode",
                    settings.themeMode == ThemeMode.DARK, onDarkMode,
                )
```

with a row that offers all three modes, so "follow system" stays reachable:

```kotlin
                Divider()
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
```

`ThemeMode` is declared `SYSTEM, LIGHT, DARK`, so `ordinal` and `entries[index]` line up with the
option list exactly. If you reorder the enum, reorder the list.

- [ ] **Step 3: Give the board-theme swatches a 48dp target**

Replace the `BoardThemeSwatch` body (`SettingsScreen.kt:148-160`) with:

```kotlin
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
```

- [ ] **Step 4: Give `TicTopBar`'s back button a 48dp target**

In `Common.kt`, replace the back-button `Box` modifier (`Common.kt:70-76`):

```kotlin
            Box(
                Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .size(28.dp),
```

with:

```kotlin
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
```

and give the icon inside it an explicit size so the glyph does not grow:

```kotlin
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back",
                    tint = colors.ink, modifier = Modifier.size(24.dp),
                )
```

- [ ] **Step 5: Update the call site**

In `TicTacNavHost.kt`, change `onDarkMode = vm::setDarkMode,` to `onThemeMode = vm::setThemeMode,`.

- [ ] **Step 6: Compile and verify**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin :app:installDebug
```

In Settings, pick Dark, then System — the app follows the OS again. Swatches and back arrows have
comfortable targets.

- [ ] **Step 7: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/settings/SettingsViewModel.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/navigation/TicTacNavHost.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/Common.kt && git commit -m "fix(settings): three-way appearance control and 48dp touch targets"
```

---

### Task 13: Wire up the board theme (BUG-18)

**Files:**
- Create: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/theme/BoardPalette.kt`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameViewModel.kt:28-36,55-60`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameTile.kt`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameBoard.kt`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameScreen.kt`

**Interfaces:**
- Consumes: `BoardTheme` (existing), `TicColors` (existing).
- Produces:
  - `data class BoardPalette(val markX: Color, val markO: Color, val softX: Color, val softO: Color, val tile: Color, val outline: Color)`
  - `@Composable fun boardPaletteFor(theme: BoardTheme): BoardPalette`
  - `GameUiState` gains `val boardTheme: BoardTheme = BoardTheme.CLASSIC`
  - `GameBoard(..., palette: BoardPalette)` and `GameTile(..., palette: BoardPalette)` — a new
    **required** parameter on both.

- [ ] **Step 1: Create the palette**

Create `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/theme/BoardPalette.kt`:

```kotlin
package com.skystone1000.xoxo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.skystone1000.xoxo.data.settings.BoardTheme

/** The six colours the board needs. Kept separate from [TicColors] so board themes can vary. */
data class BoardPalette(
    val markX: Color,
    val markO: Color,
    val softX: Color,
    val softO: Color,
    val tile: Color,
    val outline: Color,
)

/**
 * Resolves the user's [BoardTheme] choice into concrete colours. CLASSIC follows the app palette
 * (and therefore light/dark); MIDNIGHT and AURORA are fixed looks.
 */
@Composable
@ReadOnlyComposable
fun boardPaletteFor(theme: BoardTheme): BoardPalette {
    val c = LocalTicColors.current
    return when (theme) {
        BoardTheme.CLASSIC -> BoardPalette(
            markX = c.playerX, markO = c.playerO,
            softX = c.playerXSoft, softO = c.playerOSoft,
            tile = c.card, outline = c.outline,
        )
        BoardTheme.MIDNIGHT -> BoardPalette(
            markX = Color(0xFF5EEAD4), markO = Color(0xFFFDBA74),
            softX = Color(0xFF14313A), softO = Color(0xFF3A2A18),
            tile = Color(0xFF16162A), outline = Color(0xFF2B2B42),
        )
        BoardTheme.AURORA -> BoardPalette(
            markX = Color(0xFF7C6CF5), markO = Color(0xFFE879A6),
            softX = Color(0xFFE9E6FF), softO = Color(0xFFFDE7F0),
            tile = Color(0xFFF7F5FF), outline = Color(0xFFDCD6F7),
        )
    }
}
```

- [ ] **Step 2: Carry the setting into the game state**

In `GameViewModel.kt`, add `import com.skystone1000.xoxo.data.settings.BoardTheme`, add the field to
`GameUiState`:

```kotlin
    val hapticsEnabled: Boolean = true,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
```

and widen the settings collector in `init`:

```kotlin
        settingsRepository.settings
            .onEach { s ->
                _ui.value = _ui.value.copy(
                    hapticsEnabled = s.hapticsEnabled,
                    boardTheme = s.boardTheme,
                )
            }
            .launchIn(viewModelScope)
```

- [ ] **Step 3: Thread the palette through board and tile**

In `GameTile.kt`, add `palette: BoardPalette` as a required parameter after `positionLabel`, add
`import com.skystone1000.xoxo.ui.theme.BoardPalette`, delete
`val colors = TicTacTheme.colors` and the `TicTacTheme` import, and swap the colour lookups:

```kotlin
    val markColor: Color = when (mark) {
        TileMark.X -> palette.markX
        TileMark.O -> palette.markO
        TileMark.EMPTY -> Color.Transparent
    }
    val background: Color = when (mark) {
        TileMark.X -> palette.softX
        TileMark.O -> palette.softO
        TileMark.EMPTY -> palette.tile
    }
    val borderColor = when {
        highlighted && mark == TileMark.X -> palette.markX
        highlighted && mark == TileMark.O -> palette.markO
        else -> palette.outline
    }
```

In `GameBoard.kt`, add `palette: BoardPalette` as a required parameter after `onCellClick`, and
pass `palette = palette` down to each `GameTile`.

- [ ] **Step 4: Resolve the palette in `GameScreen`**

In `GameScreen.kt`, inside the `board` lambda definition, add above it:

```kotlin
    val boardPalette = boardPaletteFor(state.boardTheme)
```

and pass `palette = boardPalette` in the `GameBoard(...)` call. Add
`import com.skystone1000.xoxo.ui.theme.boardPaletteFor`.

- [ ] **Step 5: Compile and verify**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:installDebug
```

Settings → Board theme → Midnight, then start a match: the board renders in the Midnight colours.
Switch to Aurora mid-match — the board recolours live (the setting flows through DataStore).

- [ ] **Step 6: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/theme/BoardPalette.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameBoard.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/GameTile.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/game/GameViewModel.kt && git commit -m "feat(board): make the board theme setting actually change the board"
```

---

### Task 14: Home difficulty saveable + text overflow sweep (BUG-15, BUG-22, BUG-23)

**Files:**
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/home/HomeScreen.kt:62`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/stats/StatsScreen.kt:161-166`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/setup/MatchSetupScreen.kt:153-158`
- Modify: `TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/TurnIndicator.kt:36-42`

**Interfaces:**
- Consumes: nothing new.
- Produces: no signature changes.

- [ ] **Step 1: Make the Home difficulty survive rotation**

`HomeScreen.kt:62` — replace:

```kotlin
    var difficulty by remember(state.defaultDifficulty) { mutableStateOf(state.defaultDifficulty) }
```

with:

```kotlin
    var difficultyOrdinal by rememberSaveable(state.defaultDifficulty) {
        mutableIntStateOf(state.defaultDifficulty.ordinal)
    }
    val difficulty = Difficulty.entries[difficultyOrdinal]
```

and change the `SegmentedControl`'s `onSelect` to `{ difficultyOrdinal = it }`. Add imports
`androidx.compose.runtime.mutableIntStateOf` and `androidx.compose.runtime.saveable.rememberSaveable`;
remove `remember`/`mutableStateOf`/`setValue` imports if the compiler flags them as unused.

- [ ] **Step 2: Guard the four texts that can overflow**

Add `import androidx.compose.ui.text.style.TextOverflow` to each file and set
`maxLines = 1, overflow = TextOverflow.Ellipsis` on:

- `TurnIndicator.kt:41` — the pill's `Text(text, ...)`
- `StatsScreen.kt:165` — `Text(title, ...)` in `MatchRow`
- `MatchSetupScreen.kt:156` — `Text(label, ..., modifier = Modifier.weight(1f))` in `PlayerRow`

For example, `TurnIndicator.kt`:

```kotlin
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
```

- [ ] **Step 3: Compile and verify at 200% font scale**

```bash
cd TicTacToe && ./gradlew :app:compileDebugKotlin :app:installDebug
adb shell settings put system font_scale 2.0
```

Walk Home → Setup → Game → Stats. Nothing clips or wraps into a broken layout. Reset afterwards:

```bash
adb shell settings put system font_scale 1.0
```

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/home/HomeScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/stats/StatsScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/screens/setup/MatchSetupScreen.kt TicTacToe/app/src/main/java/com/skystone1000/xoxo/ui/components/TurnIndicator.kt && git commit -m "fix(ui): saveable home difficulty and text-overflow guards at large font scale"
```

---

### Task 15: Instrumented size regression tests (BUG-30)

**Files:**
- Create: `TicTacToe/app/src/androidTest/java/com/skystone1000/xoxo/ui/screens/game/GameScreenSizeTest.kt`

**Interfaces:**
- Consumes: `GameScreen`, `GameUiState` (existing); `boardPaletteFor` (Task 13).
- Produces: nothing consumed by other tasks.

This is the guard that would have caught BUG-01 and BUG-02. `DeviceConfigurationOverride.ForcedSize`
forces the composition into an arbitrary window size without needing four emulators. It requires a
connected device or a running emulator.

- [ ] **Step 1: Write the failing test**

Create
`TicTacToe/app/src/androidTest/java/com/skystone1000/xoxo/ui/screens/game/GameScreenSizeTest.kt`:

```kotlin
package com.skystone1000.xoxo.ui.screens.game

import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skystone1000.xoxo.domain.model.Player
import com.skystone1000.xoxo.ui.theme.TicTacTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenSizeTest {

    @get:Rule
    val rule = createComposeRule()

    private fun renderAt(size: DpSize) {
        rule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size)) {
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

    private fun assertBoardAndControlsUsable() {
        // All nine cells must be laid out and on screen — BUG-01 clipped rows 2 and 3.
        for (row in 1..3) {
            for (col in 1..3) {
                rule.onNodeWithContentDescription("row $row, column $col, empty")
                    .assertIsDisplayed()
            }
        }
        // BUG-02 pushed both controls off the bottom edge.
        rule.onNodeWithText("Restart").assertIsDisplayed()
        rule.onNodeWithText("Quit").assertIsDisplayed()
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
}
```

If Task 13 landed first, `GameScreen` resolves the palette internally from
`GameUiState.boardTheme`, so this test needs no change. If `GameScreen` gained a required palette
parameter instead, pass `boardPaletteFor(BoardTheme.CLASSIC)`.

- [ ] **Step 2: Run it against the pre-fix code to prove it catches the bug**

If you are running this task in isolation, stash the Task 3/4 changes first
(`git stash`), run, and confirm the landscape and tablet cases **fail**. Then `git stash pop`.

```bash
cd TicTacToe && ./gradlew :app:connectedDebugAndroidTest --tests '*GameScreenSizeTest*'
```

Expected before the fixes: `phoneLandscape` and `tabletLandscape` fail on
`assertIsDisplayed` for "row 3, column 1, empty".

- [ ] **Step 3: Run it against the fixed code**

```bash
cd TicTacToe && ./gradlew :app:connectedDebugAndroidTest --tests '*GameScreenSizeTest*'
```

Expected: `BUILD SUCCESSFUL`, 4 tests passing. Requires a running emulator/device — if none is
attached the task fails with "No connected devices!", which is a setup problem, not a test failure.

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/app/src/androidTest && git commit -m "test(game): assert the board and controls fit at four window sizes"
```

---

### Task 16: Documentation sync

**Files:**
- Modify: `TicTacToe/docs/ARCHITECTURE.md`
- Modify: `TicTacToe/docs/CODEBASE.md`
- Modify: `TicTacToe/docs/FEATURES.md`

Required by `CLAUDE.md` §2 — a change that touches structure or behaviour without a matching docs
edit is incomplete.

- [ ] **Step 1: `ARCHITECTURE.md`**

- Add a **`ui/layout/`** bullet to the "UI layer" section: "the adaptive layer — window size
  classification (`WindowSize.kt`) and pure layout maths (`Sizing.kt`). Screens branch on
  `rememberWindowSize()` rather than assuming a phone-width column."
- Add a design decision #9: **Adaptive by window, not by device.** Layout branches on the current
  *window* size class read from `LocalWindowInfo`, so split-screen and foldables behave correctly.
  Content is capped at `MAX_CONTENT_WIDTH_DP` and the board at `MAX_BOARD_SIDE_DP`; the game screen
  switches to a two-pane layout when height is compact or width is expanded; the bottom bar becomes
  a navigation rail at expanded width.
- Add a design decision #10: **`targetSdk 37` means orientation cannot be locked on large screens**,
  so landscape is a supported first-class layout.
- In "Where to make common changes", add rows: "Breakpoints / adaptive rules → `ui/layout/*`" and
  "Board colours per board-theme → `ui/theme/BoardPalette.kt`".

- [ ] **Step 2: `CODEBASE.md`**

- Add a `ui/layout/` table with `WindowSize.kt` and `Sizing.kt`.
- Add `ScreenContainer.kt` to the `ui/components/` table.
- Add `BoardPalette.kt` to the `ui/theme/` table.
- Update the `GameBoard.kt` / `GameTile.kt` rows to say they self-size and take a `BoardPalette`.
- Update the Tests section: add `ui/layout/WindowSizeTest.kt` and `ui/layout/SizingTest.kt` to the
  JVM table, and replace "There is no `androidTest/` source set checked in" with a row for
  `androidTest/.../GameScreenSizeTest.kt`.
- Add this plan (`docs/ADAPTIVE-UI-PLAN.md`) to the `TicTacToe/docs/` row.

- [ ] **Step 3: `FEATURES.md`**

- Settings screen: the "Sound effects" toggle is removed; "Dark mode" becomes a three-way
  **Appearance** control (System / Light / Dark); **Board theme now takes effect** on the game
  board.
- Game screen: describe the two layouts (stacked in portrait, side-by-side in landscape and on
  large screens) and the capped board size.
- Profile screen: the name row in the header is tappable to rename; the screen scrolls.
- Navigation: a left navigation rail replaces the bottom bar on expanded-width windows.

- [ ] **Step 4: Verify the docs match reality**

Re-read each edited section against the code as it now stands. Every file named must exist; every
behaviour described must be the shipped one.

- [ ] **Step 5: Commit**

```bash
git add TicTacToe/docs && git commit -m "docs: sync architecture, codebase and features with the adaptive UI work"
```

---

## Final verification

Run before declaring the work complete:

```bash
cd TicTacToe && ./gradlew :app:testDebugUnitTest :app:assembleDebug
```

```bash
cd TicTacToe && ./gradlew :app:connectedDebugAndroidTest
```

Then walk the app by hand on **three** configurations and confirm every screen (Splash, Onboarding,
Home, Stats, Profile, Settings, Match setup, Game, Result) renders without clipping and with every
control reachable:

1. Phone portrait and landscape (e.g. Pixel 8).
2. Tablet portrait and landscape (e.g. Pixel Tablet).
3. Tablet in 50/50 split-screen — the app should behave as a compact-width phone.

## Coverage check

| Bug | Fixed in |
| --- | -------- |
| BUG-01, BUG-04 | Tasks 3, 4 |
| BUG-02, BUG-03 | Task 4 |
| BUG-05, BUG-06 | Task 3 |
| BUG-07 | Tasks 6, 8, 9, 10 |
| BUG-08 | Task 7 |
| BUG-09 | Task 9 |
| BUG-10 | Task 10 |
| BUG-11 | Tasks 6, 8, 9, 10 |
| BUG-12 | Task 8 |
| BUG-13 | Tasks 4, 9, 10 |
| BUG-14 | Task 9 |
| BUG-15 | Task 14 |
| BUG-16 | Task 8 |
| BUG-17 | Task 4 |
| BUG-18 | Task 13 |
| BUG-19 | Task 11 (removal — see §1.14) |
| BUG-20 | Task 12 |
| BUG-21 | **Accepted, not fixed** — see §1.14 |
| BUG-22, BUG-23 | Tasks 4, 14 |
| BUG-24 | Tasks 4, 12 |
| BUG-25 | Task 3 |
| BUG-26 | Task 8 |
| BUG-27, BUG-28 | Task 5 |
| BUG-29 | Task 4 |
| BUG-30 | Task 15 |
