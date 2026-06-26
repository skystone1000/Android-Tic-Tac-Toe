# TicTac Revamp — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the existing 2-player Tic Tac Toe app into a polished, modern product with onboarding, multiple game modes (pass-and-play, vs AI), persistent stats, settings, profile, and a reusable design system.

**Architecture:** Single-module Android app migrated from Java/XML Views to **Kotlin + Jetpack Compose + Material 3**, structured in clean layers — a pure-Kotlin domain layer (game engine + AI, fully unit-tested), a data layer (DataStore for settings, Room for match history), and a Compose UI layer driven by MVVM ViewModels exposing `StateFlow`. Navigation via Navigation-Compose.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation-Compose, AndroidX Lifecycle/ViewModel, Kotlin Coroutines + Flow, Room, DataStore (Preferences), JUnit4 + kotlin-test + Turbine + Compose UI test, Robolectric (optional).

---

## ⚠️ Key Decisions (read before executing)

These are baked into the plan. If any is wrong, stop and revise the plan before coding.

1. **Migrate Java/XML → Kotlin/Compose.** The current app is two Java `Activity` classes (`MainActivity`, `TwoPlayerActivity`) with XML layouts. A modern design system + reusable components is far cheaper to build and maintain in Compose. The old code is replaced, not extended. The package `com.example.tictactoe` and `compileSdk 35 / minSdk 21 / targetSdk 35` toolchain from the recent build fix are kept.
2. **Online multiplayer is Phase 7 (future), not MVP.** It requires a backend (Firebase Realtime DB / Firestore or a custom server) and auth. The MVP ships with Pass & Play + Vs AI. The Home screen shows the Online entry as "Coming soon" until Phase 7.
3. **Final visual design is pending.** The user will supply exact UI (from Stitch). Tasks that depend on pixel-level layout use a **placeholder design token set** (defined in Phase 1) and a structural Composable skeleton. When the real design lands, only the token values and fine layout change — the component APIs and screen structure stay. Tasks affected are marked **🎨 DESIGN-DEPENDENT**.
4. **DI:** No Hilt for MVP — manual construction / a tiny `AppContainer` keeps the dependency graph trivial and the plan shorter. Revisit if the graph grows.
5. **State model:** One `GameState` data class is the single source of truth for the board. UI is a pure function of state; all logic lives in the domain layer so it is testable without an emulator.

---

## File Structure

```
app/src/main/java/com/example/tictactoe/
  TicTacApp.kt                      # Application class, AppContainer wiring
  MainActivity.kt                   # Single Compose host activity
  di/
    AppContainer.kt                 # Manual DI: builds repos + use cases
  domain/
    model/
      Player.kt                     # enum X, O
      Cell.kt                       # sealed: Empty | Taken(Player)
      GameState.kt                  # board, currentPlayer, status, winningLine
      GameStatus.kt                 # InProgress | Won(Player, line) | Draw
      Difficulty.kt                 # EASY, MEDIUM, HARD
      GameMode.kt                   # PASS_AND_PLAY, VS_AI, ONLINE
    engine/
      GameEngine.kt                 # pure rules: move, winner detection, reset
    ai/
      AiOpponent.kt                 # interface
      MinimaxAi.kt                  # difficulty-aware minimax + randomness
  data/
    settings/
      SettingsRepository.kt         # DataStore-backed settings
      AppSettings.kt                # data class: sound, haptics, theme, defaults
    stats/
      MatchEntity.kt                # Room entity
      MatchDao.kt                   # Room DAO
      AppDatabase.kt                # Room database
      StatsRepository.kt            # aggregates wins/losses/draws/streak
  ui/
    theme/
      Color.kt                      # palette tokens (light + dark)
      Type.kt                       # typography scale
      Shape.kt                      # corner/spacing tokens
      Theme.kt                      # TicTacTheme { } MaterialTheme wrapper
    components/
      TicButton.kt                  # primary / secondary / icon variants
      GameTile.kt                   # X/O/empty/winning/disabled states
      GameBoard.kt                  # 3x3 grid of GameTile
      ScoreChip.kt
      TurnIndicator.kt
      StatCard.kt
      SettingRow.kt                 # toggle / slider / segmented rows
      ResultDialog.kt
      PageIndicator.kt
    navigation/
      Destinations.kt               # route constants + args
      TicTacNavHost.kt              # NavHost wiring all screens
    screens/
      splash/SplashScreen.kt
      onboarding/OnboardingScreen.kt + OnboardingViewModel.kt
      home/HomeScreen.kt + HomeViewModel.kt
      setup/MatchSetupScreen.kt + MatchSetupViewModel.kt
      game/GameScreen.kt + GameViewModel.kt
      stats/StatsScreen.kt + StatsViewModel.kt
      settings/SettingsScreen.kt + SettingsViewModel.kt
      profile/ProfileScreen.kt + ProfileViewModel.kt

app/src/test/java/com/example/tictactoe/         # JVM unit tests (domain, data)
app/src/androidTest/java/com/example/tictactoe/   # Compose UI tests
docs/tictactoe-revamp-plan.md                     # this file
```

**Build order rationale:** foundation → design system → domain (pure, no Android) → data → navigation shell → screens (wired to real ViewModels) → polish. Each phase produces something runnable or testable on its own.

---

## Phase 0 — Project Foundation

Goal: Kotlin + Compose build green, old Java/XML removed, app launches to an empty themed screen.

### Task 0.1: Add Kotlin + Compose to the Gradle build

**Files:**
- Modify: `TicTacToe/build.gradle` (root) — add Kotlin + Compose-compiler plugin aliases
- Modify: `TicTacToe/app/build.gradle`
- Modify: `TicTacToe/gradle.properties`

- [ ] **Step 1: Update root `build.gradle` plugins block**

```groovy
// Top-level build file.
plugins {
    id 'com.android.application' version '8.6.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.24' apply false
}
```

- [ ] **Step 2: Rewrite `app/build.gradle` for Kotlin + Compose**

```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace 'com.example.tictactoe'
    compileSdk 35

    defaultConfig {
        applicationId "com.example.tictactoe"
        minSdk 21
        targetSdk 35
        versionCode 2
        versionName "2.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary true }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = '17' }
    buildFeatures { compose true }
    composeOptions { kotlinCompilerExtensionVersion '1.5.14' } // matches Kotlin 1.9.24
    packaging { resources { excludes += '/META-INF/{AL2.0,LGPL2.1}' } }
}

dependencies {
    def composeBom = platform('androidx.compose:compose-bom:2024.06.00')
    implementation composeBom
    androidTestImplementation composeBom

    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.activity:activity-compose:1.9.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.8.3'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3'

    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.material:material-icons-extended'
    implementation 'androidx.navigation:navigation-compose:2.7.7'

    implementation 'androidx.datastore:datastore-preferences:1.1.1'
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1' // replaced by KSP in Task 0.3 note

    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1'
    testImplementation 'app.cash.turbine:turbine:1.1.0'

    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}
```

> Note: Room with Kotlin should use KSP. Add `id 'com.google.devtools.ksp' version '1.9.24-1.0.20' apply false` to root and `id 'com.google.devtools.ksp'` to app, then `ksp 'androidx.room:room-compiler:2.6.1'`. Done in Phase 4 Task 4.1 to keep this task focused.

- [ ] **Step 3: Build to verify the toolchain resolves**

Run: `cd TicTacToe && JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:help`
Expected: `BUILD SUCCESSFUL` (dependency graph resolves; no code compiled yet).

- [ ] **Step 4: Commit**

```bash
git add TicTacToe/build.gradle TicTacToe/app/build.gradle
git commit -m "build: add Kotlin + Jetpack Compose toolchain"
```

### Task 0.2: Remove legacy Java/XML, add Compose host

**Files:**
- Delete: `app/src/main/java/com/example/tictactoe/MainActivity.java`, `TwoPlayerActivity.java`
- Delete: `app/src/main/res/layout/activity_main.xml`, `activity_two_player.xml`
- Create: `app/src/main/java/com/example/tictactoe/MainActivity.kt`
- Create: `app/src/main/java/com/example/tictactoe/TicTacApp.kt`
- Modify: `app/src/main/AndroidManifest.xml` (set `.TicTacApp`, single activity)

- [ ] **Step 1: Delete the Java activities and XML layouts**

```bash
git rm app/src/main/java/com/example/tictactoe/MainActivity.java \
       app/src/main/java/com/example/tictactoe/TwoPlayerActivity.java \
       app/src/main/res/layout/activity_main.xml \
       app/src/main/res/layout/activity_two_player.xml
```

- [ ] **Step 2: Create `TicTacApp.kt`**

```kotlin
package com.example.tictactoe

import android.app.Application

class TicTacApp : Application()
```

- [ ] **Step 3: Create `MainActivity.kt` with an empty themed surface**

```kotlin
package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text("TicTac")
                }
            }
        }
    }
}
```

- [ ] **Step 4: Update `AndroidManifest.xml`**

```xml
<application
    android:name=".TicTacApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.TicTacToe">
    <activity android:name=".MainActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

- [ ] **Step 5: Build + install to confirm it launches**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`; app shows "TicTac".

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor: replace Java/XML activities with Compose host"
```

---

## Phase 1 — Design System & Component Library 🎨 DESIGN-DEPENDENT

Goal: A `TicTacTheme` and a catalog of reusable, previewable components. Values are **placeholders** until final design arrives; APIs are stable.

### Task 1.1: Define theme tokens (color, type, shape)

**Files:** Create `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`

- [ ] **Step 1: `Color.kt` — placeholder palette (light + dark)**

```kotlin
package com.example.tictactoe.ui.theme

import androidx.compose.ui.graphics.Color

// Brand
val Indigo = Color(0xFF5B5BD6)
val IndigoDark = Color(0xFF3A3AAE)
// Player accents
val PlayerX = Color(0xFF2BB3C0)   // teal
val PlayerO = Color(0xFFFF7A59)   // coral
// Neutrals
val Surface = Color(0xFFF7F7FB)
val SurfaceDark = Color(0xFF14141B)
val OnSurface = Color(0xFF1A1A22)
val OnSurfaceDark = Color(0xFFECECF2)
```

- [ ] **Step 2: `Shape.kt` — radii + spacing scale**

```kotlin
package com.example.tictactoe.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TicShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

object Spacing {
    val xs = 4.dp; val sm = 8.dp; val md = 16.dp; val lg = 24.dp; val xl = 32.dp
}
```

- [ ] **Step 3: `Type.kt` — type scale** (placeholder fonts = system; swap to brand fonts when design lands)

```kotlin
package com.example.tictactoe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TicTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)
```

- [ ] **Step 4: `Theme.kt` — wrap MaterialTheme with light/dark color schemes**

```kotlin
package com.example.tictactoe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Indigo, surface = Surface, onSurface = OnSurface,
)
private val DarkColors = darkColorScheme(
    primary = IndigoDark, surface = SurfaceDark, onSurface = OnSurfaceDark,
)

@Composable
fun TicTacTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TicTypography,
        shapes = TicShapes,
        content = content,
    )
}
```

- [ ] **Step 5: Wire `TicTacTheme` into `MainActivity`, build, commit**

Replace `MaterialTheme { ... }` in `MainActivity.kt` with `TicTacTheme { ... }`.
Run: `./gradlew :app:assembleDebug` → Expected: `BUILD SUCCESSFUL`.
```bash
git add -A && git commit -m "feat: add TicTac design system theme tokens"
```

### Task 1.2: Core components with @Preview

**Files:** Create `ui/components/TicButton.kt`, `GameTile.kt`, `GameBoard.kt`, `TurnIndicator.kt`, `ScoreChip.kt`, `StatCard.kt`, `SettingRow.kt`, `ResultDialog.kt`, `PageIndicator.kt`

For each component: implement the Composable + a `@Preview`. Example for `GameTile` (the most stateful):

- [ ] **Step 1: `GameTile.kt`**

```kotlin
package com.example.tictactoe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tictactoe.domain.model.Player

enum class TileVisual { EMPTY, X, O, X_WIN, O_WIN }

@Composable
fun GameTile(
    visual: TileVisual,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().aspectRatio(1f)
            .clickable(enabled = enabled && visual == TileVisual.EMPTY) { onClick() },
        // colors/shape come from theme; refine when design lands
    ) {
        Text(
            text = when (visual) {
                TileVisual.X, TileVisual.X_WIN -> "X"
                TileVisual.O, TileVisual.O_WIN -> "O"
                TileVisual.EMPTY -> ""
            }
        )
    }
}

@Preview
@Composable
private fun GameTilePreview() {
    GameTile(visual = TileVisual.X, enabled = true, onClick = {})
}
```

- [ ] **Step 2:** Implement `TicButton` (variants: Primary, Secondary, Icon via an `enum class TicButtonStyle`), `GameBoard` (renders a `List<TileVisual>` of size 9 in a 3×3 grid, exposes `onTileClick: (Int) -> Unit`), `TurnIndicator`, `ScoreChip`, `StatCard`, `SettingRow` (toggle/slider/segmented overloads), `ResultDialog`, `PageIndicator` — each with a `@Preview`.

- [ ] **Step 3:** Build, eyeball previews in Android Studio, commit.

Run: `./gradlew :app:assembleDebug` → Expected: `BUILD SUCCESSFUL`.
```bash
git add -A && git commit -m "feat: add reusable component library with previews"
```

> When final UI design arrives: revisit Phase 1 token values + component styling only. Screen structure (Phase 6) and logic (Phases 2–4) are unaffected.

---

## Phase 2 — Core Game Engine (pure Kotlin, TDD)

Goal: All game rules in `domain/`, 100% unit-tested, no Android imports.

### Task 2.1: Domain models

**Files:** Create `domain/model/Player.kt`, `Cell.kt`, `GameStatus.kt`, `GameState.kt`, `Difficulty.kt`, `GameMode.kt`

- [ ] **Step 1: Write the models**

```kotlin
// Player.kt
package com.example.tictactoe.domain.model
enum class Player { X, O { }; fun opponent() = if (this == X) O else X }
```
```kotlin
// Cell.kt
package com.example.tictactoe.domain.model
sealed interface Cell {
    data object Empty : Cell
    data class Taken(val player: Player) : Cell
}
```
```kotlin
// GameStatus.kt
package com.example.tictactoe.domain.model
sealed interface GameStatus {
    data object InProgress : GameStatus
    data class Won(val player: Player, val line: List<Int>) : GameStatus
    data object Draw : GameStatus
}
```
```kotlin
// GameState.kt
package com.example.tictactoe.domain.model
data class GameState(
    val board: List<Cell> = List(9) { Cell.Empty },
    val currentPlayer: Player = Player.X,
    val status: GameStatus = GameStatus.InProgress,
)
```
```kotlin
// Difficulty.kt / GameMode.kt
package com.example.tictactoe.domain.model
enum class Difficulty { EASY, MEDIUM, HARD }
enum class GameMode { PASS_AND_PLAY, VS_AI, ONLINE }
```

- [ ] **Step 2:** Build (`./gradlew :app:compileDebugKotlin`) → Expected: `BUILD SUCCESSFUL`. Commit: `feat: add domain models`.

### Task 2.2: GameEngine — move + win detection (TDD)

**Files:** Create `domain/engine/GameEngine.kt`; Test `app/src/test/java/com/example/tictactoe/domain/engine/GameEngineTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
package com.example.tictactoe.domain.engine

import com.example.tictactoe.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private val engine = GameEngine()

    @Test fun `X plays first cell, turn passes to O`() {
        val s = engine.move(GameState(), 0)
        assertEquals(Cell.Taken(Player.X), s.board[0])
        assertEquals(Player.O, s.currentPlayer)
    }

    @Test fun `move on taken cell is rejected (state unchanged)`() {
        val s1 = engine.move(GameState(), 0)
        val s2 = engine.move(s1, 0)
        assertEquals(s1, s2)
    }

    @Test fun `top row of X is a win`() {
        var s = GameState()
        s = engine.move(s, 0); s = engine.move(s, 3) // X0, O3
        s = engine.move(s, 1); s = engine.move(s, 4) // X1, O4
        s = engine.move(s, 2)                        // X2 -> win
        assertEquals(GameStatus.Won(Player.X, listOf(0,1,2)), s.status)
    }

    @Test fun `full board with no line is a draw`() {
        // X O X / X O O / O X X  -> arranged for no 3-in-a-row
        val order = listOf(0,1,2,4,3,5,7,6,8)
        var s = GameState()
        order.forEach { s = engine.move(s, it) }
        assertEquals(GameStatus.Draw, s.status)
    }

    @Test fun `no moves accepted after game is won`() {
        var s = GameState()
        listOf(0,3,1,4,2).forEach { s = engine.move(s, it) } // X wins
        val after = engine.move(s, 5)
        assertEquals(s, after)
    }

    @Test fun `reset returns a fresh in-progress state`() {
        var s = engine.move(GameState(), 0)
        assertEquals(GameState(), engine.reset())
    }
}
```

- [ ] **Step 2: Run tests, verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "*GameEngineTest"`
Expected: FAIL — `GameEngine` unresolved.

- [ ] **Step 3: Implement `GameEngine`**

```kotlin
package com.example.tictactoe.domain.engine

import com.example.tictactoe.domain.model.*

class GameEngine {
    private val lines = listOf(
        listOf(0,1,2), listOf(3,4,5), listOf(6,7,8),
        listOf(0,3,6), listOf(1,4,7), listOf(2,5,8),
        listOf(0,4,8), listOf(2,4,6),
    )

    fun reset() = GameState()

    fun move(state: GameState, index: Int): GameState {
        if (state.status != GameStatus.InProgress) return state
        if (state.board[index] != Cell.Empty) return state
        val board = state.board.toMutableList()
        board[index] = Cell.Taken(state.currentPlayer)
        val status = evaluate(board)
        return state.copy(
            board = board,
            currentPlayer = if (status == GameStatus.InProgress)
                state.currentPlayer.opponent() else state.currentPlayer,
            status = status,
        )
    }

    private fun evaluate(board: List<Cell>): GameStatus {
        for (line in lines) {
            val (a, b, c) = line
            val ca = board[a]
            if (ca is Cell.Taken && ca == board[b] && ca == board[c]) {
                return GameStatus.Won(ca.player, line)
            }
        }
        return if (board.none { it == Cell.Empty }) GameStatus.Draw
        else GameStatus.InProgress
    }
}
```

- [ ] **Step 4: Run tests, verify pass**

Run: `./gradlew :app:testDebugUnitTest --tests "*GameEngineTest"` → Expected: PASS (6 tests).

- [ ] **Step 5: Commit** — `feat: add tested game engine`.

---

## Phase 3 — AI Opponent (TDD)

Goal: A difficulty-aware AI. HARD = unbeatable (minimax), MEDIUM = minimax with occasional random move, EASY = mostly random.

### Task 3.1: AI interface + Minimax (TDD)

**Files:** Create `domain/ai/AiOpponent.kt`, `domain/ai/MinimaxAi.kt`; Test `.../domain/ai/MinimaxAiTest.kt`

- [ ] **Step 1: Write failing tests** (use a fixed seed so EASY/MEDIUM are deterministic)

```kotlin
package com.example.tictactoe.domain.ai

import com.example.tictactoe.domain.engine.GameEngine
import com.example.tictactoe.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MinimaxAiTest {
    private val engine = GameEngine()

    @Test fun `HARD takes the immediate winning move`() {
        // O to move; O at 0,1 -> must play 2 to win
        var s = GameState()
        listOf(3,0, 4,1).forEach { s = engine.move(s, it) } // X3 O0 X4 O1 ; X to move? track carefully
        // Arrange so it's O's turn with a winning move at 2; see helper below.
        val ai = MinimaxAi(engine, Difficulty.HARD, Random(0))
        val move = ai.chooseMove(stateWhereOWinsAt2())
        assertEquals(2, move)
    }

    @Test fun `HARD blocks the opponent's winning move`() {
        val ai = MinimaxAi(engine, Difficulty.HARD, Random(0))
        val move = ai.chooseMove(stateWhereXThreatensAt2())
        assertEquals(2, move)
    }

    @Test fun `HARD never loses across many self-play games`() {
        repeat(20) { seed ->
            val ai = MinimaxAi(engine, Difficulty.HARD, Random(seed.toLong()))
            // Opponent plays random; assert AI result is Won or Draw, never Lost.
            assertNotEquals(Player.X, loserOfSelfPlay(ai, Random(seed.toLong())))
        }
    }

    @Test fun `chooseMove returns a legal empty cell`() {
        val ai = MinimaxAi(engine, Difficulty.EASY, Random(0))
        val move = ai.chooseMove(GameState())
        assertTrue(move in 0..8)
    }
}
// Helper builders stateWhereOWinsAt2(), stateWhereXThreatensAt2(),
// loserOfSelfPlay(...) to be written in the test file.
```

- [ ] **Step 2: Run, verify fail.** Run: `./gradlew :app:testDebugUnitTest --tests "*MinimaxAiTest"` → Expected: FAIL.

- [ ] **Step 3: Implement `AiOpponent` + `MinimaxAi`**

```kotlin
// AiOpponent.kt
package com.example.tictactoe.domain.ai
import com.example.tictactoe.domain.model.GameState
interface AiOpponent { fun chooseMove(state: GameState): Int }
```
```kotlin
// MinimaxAi.kt
package com.example.tictactoe.domain.ai

import com.example.tictactoe.domain.engine.GameEngine
import com.example.tictactoe.domain.model.*
import kotlin.random.Random

class MinimaxAi(
    private val engine: GameEngine,
    private val difficulty: Difficulty,
    private val random: Random = Random.Default,
) : AiOpponent {

    private val randomChance = when (difficulty) {
        Difficulty.EASY -> 0.8
        Difficulty.MEDIUM -> 0.3
        Difficulty.HARD -> 0.0
    }

    override fun chooseMove(state: GameState): Int {
        val empties = state.board.indices.filter { state.board[it] == Cell.Empty }
        if (empties.isEmpty()) error("No moves available")
        if (random.nextDouble() < randomChance) return empties.random(random)
        return bestMove(state, empties)
    }

    private fun bestMove(state: GameState, empties: List<Int>): Int {
        val me = state.currentPlayer
        var bestScore = Int.MIN_VALUE
        var best = empties.first()
        for (i in empties) {
            val score = minimax(engine.move(state, i), me, depth = 1)
            if (score > bestScore) { bestScore = score; best = i }
        }
        return best
    }

    // Returns score from `me`'s perspective; prefers faster wins / slower losses via depth.
    private fun minimax(state: GameState, me: Player, depth: Int): Int {
        when (val st = state.status) {
            is GameStatus.Won -> return if (st.player == me) 10 - depth else depth - 10
            GameStatus.Draw -> return 0
            GameStatus.InProgress -> {}
        }
        val empties = state.board.indices.filter { state.board[it] == Cell.Empty }
        val maximizing = state.currentPlayer == me
        var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
        for (i in empties) {
            val score = minimax(engine.move(state, i), me, depth + 1)
            best = if (maximizing) maxOf(best, score) else minOf(best, score)
        }
        return best
    }
}
```

- [ ] **Step 4: Run, verify pass.** Run: `./gradlew :app:testDebugUnitTest --tests "*MinimaxAiTest"` → Expected: PASS.

- [ ] **Step 5: Commit** — `feat: add difficulty-aware minimax AI`.

---

## Phase 4 — Persistence

Goal: Settings via DataStore, match history + aggregate stats via Room.

### Task 4.1: Enable KSP + Room database

**Files:** Modify root + app `build.gradle`; Create `data/stats/MatchEntity.kt`, `MatchDao.kt`, `AppDatabase.kt`

- [ ] **Step 1:** Add KSP plugin (root: `id 'com.google.devtools.ksp' version '1.9.24-1.0.20' apply false`; app: `id 'com.google.devtools.ksp'`), replace `annotationProcessor` with `ksp 'androidx.room:room-compiler:2.6.1'`.

- [ ] **Step 2:** `MatchEntity.kt`

```kotlin
package com.example.tictactoe.data.stats
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,          // GameMode name
    val result: String,        // "X", "O", or "DRAW"
    val difficulty: String?,   // null for pass-and-play
    val playedAt: Long,        // epoch millis
)
```

- [ ] **Step 3:** `MatchDao.kt` (insert + aggregate queries returning `Flow`), `AppDatabase.kt` (`@Database(entities=[MatchEntity::class], version=1)`).

- [ ] **Step 4:** Build (`./gradlew :app:assembleDebug`) → Expected: `BUILD SUCCESSFUL`. Commit `feat: add Room match history database`.

### Task 4.2: StatsRepository (TDD with in-memory Room)

**Files:** Create `data/stats/StatsRepository.kt`; Test `androidTest/.../StatsRepositoryTest.kt` (instrumented, in-memory DB)

- [ ] **Step 1:** Write instrumented test: insert matches → assert wins/losses/draws/win-rate/current-streak. Use `Room.inMemoryDatabaseBuilder`.
- [ ] **Step 2:** Run → fail. `./gradlew :app:connectedDebugAndroidTest --tests "*StatsRepositoryTest"` (needs emulator/device).
- [ ] **Step 3:** Implement `StatsRepository` (exposes `Flow<StatsSummary>` with totals + streak computed from ordered matches).
- [ ] **Step 4:** Run → pass. **Step 5:** Commit `feat: add stats repository`.

### Task 4.3: SettingsRepository (DataStore, TDD)

**Files:** Create `data/settings/AppSettings.kt`, `SettingsRepository.kt`; Test `test/.../SettingsRepositoryTest.kt` (DataStore test with temp file + `runTest`)

- [ ] **Step 1:** `AppSettings` data class: `soundEnabled`, `hapticsEnabled`, `themeMode` (SYSTEM/LIGHT/DARK enum), `defaultDifficulty`, `boardTheme`.
- [ ] **Step 2:** Write failing test: write a setting → read back via `Flow.first()`.
- [ ] **Step 3:** Implement `SettingsRepository` over `DataStore<Preferences>`, exposing `val settings: Flow<AppSettings>` + suspend setters.
- [ ] **Step 4:** Run → pass. **Step 5:** Commit `feat: add settings repository`.

### Task 4.4: AppContainer wiring

**Files:** Create `di/AppContainer.kt`; Modify `TicTacApp.kt`

- [ ] **Step 1:** `AppContainer` constructs `GameEngine`, `AppDatabase`, `StatsRepository`, `SettingsRepository`, and an `aiFactory: (Difficulty) -> AiOpponent`.
- [ ] **Step 2:** Hold an `AppContainer` instance on `TicTacApp`. Build, commit `feat: wire AppContainer dependency graph`.

---

## Phase 5 — Navigation Shell

Goal: A `NavHost` with every route, navigable with placeholder screen bodies.

### Task 5.1: Destinations + NavHost

**Files:** Create `ui/navigation/Destinations.kt`, `TicTacNavHost.kt`; Modify `MainActivity.kt`

- [ ] **Step 1:** `Destinations.kt` — route constants: `splash`, `onboarding`, `home`, `setup/{mode}`, `game/{mode}/{difficulty}`, `stats`, `settings`, `profile`. Define typed helper builders for arg routes.
- [ ] **Step 2:** `TicTacNavHost.kt` — `NavHost` mapping each route to a temporary `Text(route)` placeholder; start destination decided by a `hasSeenOnboarding` flag (from SettingsRepository) → `splash`.
- [ ] **Step 3:** `MainActivity` hosts `TicTacTheme { TicTacNavHost(...) }`.
- [ ] **Step 4:** Build + run; tap through with temporary buttons. Commit `feat: add navigation graph with placeholder screens`.

---

## Phase 6 — Screens 🎨 DESIGN-DEPENDENT

Each screen = ViewModel (state + intents, JVM-unit-tested) + Composable (consumes state, structure stable, visuals refined when design lands). Pattern per screen below; repeat for all.

### Task 6.1: Splash

**Files:** Create `ui/screens/splash/SplashScreen.kt`
- [ ] Implement timed/animated logo; after delay, navigate to `onboarding` (if not seen) or `home`. Commit.

### Task 6.2: Onboarding

**Files:** `OnboardingScreen.kt`, `OnboardingViewModel.kt`
- [ ] **Step 1:** ViewModel test: 3 pages, `next()` advances, `skip()`/finish sets `hasSeenOnboarding=true` via SettingsRepository.
- [ ] **Step 2:** Run fail → implement ViewModel → run pass.
- [ ] **Step 3:** `OnboardingScreen` — `HorizontalPager` (3 slides) + `PageIndicator` + Skip/Next/Get-Started using `TicButton`. On finish, navigate to `home`. Commit.

### Task 6.3: Home

**Files:** `HomeScreen.kt`, `HomeViewModel.kt`
- [ ] **Step 1:** ViewModel exposes `HomeUiState` (streak, level from StatsRepository; Online = disabled "coming soon").
- [ ] **Step 2:** Test the state mapping. Implement. 
- [ ] **Step 3:** Screen: mode cards (Pass & Play → `setup/PASS_AND_PLAY`; Vs AI → `setup/VS_AI`; Online disabled), header with streak, nav icons to Settings/Stats/Profile. Commit.

### Task 6.4: Match Setup

**Files:** `MatchSetupScreen.kt`, `MatchSetupViewModel.kt`
- [ ] Choose symbol (X/O), difficulty (segmented, only for VS_AI), names/avatars, board theme. "Start" navigates to `game/{mode}/{difficulty}`. ViewModel-tested for valid-config gating. Commit.

### Task 6.5: Game (the centerpiece)

**Files:** `GameScreen.kt`, `GameViewModel.kt`; Test `test/.../GameViewModelTest.kt`
- [ ] **Step 1:** Write failing `GameViewModelTest` using `kotlinx-coroutines-test` + Turbine:
  - tapping an empty cell updates board + turn
  - in VS_AI, after the human move the AI responds automatically (inject a fake `AiOpponent` returning a fixed index)
  - a winning line sets `status = Won` and increments score + records a `MatchEntity`
  - `restart()` clears the board, keeps the session scoreboard
- [ ] **Step 2:** Run → fail.
- [ ] **Step 3:** Implement `GameViewModel`: holds `GameState` + session scores in a `StateFlow<GameUiState>`; `onTileClick(i)` calls `GameEngine.move`, and in VS_AI dispatches the AI move on a coroutine; on terminal status writes to `StatsRepository`. Honors haptics/sound settings.
- [ ] **Step 4:** Run → pass.
- [ ] **Step 5:** `GameScreen` — `TurnIndicator`, two `ScoreChip`s, `GameBoard` mapping `GameState.board` → `List<TileVisual>` (winning line → `*_WIN`), restart/undo/quit controls, tile drop animation. On terminal status show `ResultDialog`. Commit.

### Task 6.6: Result dialog wiring
- [ ] `ResultDialog` shows Win/Lose/Draw, winning line, updated session score, Play-Again (restart) / Home. Confetti on win (simple particle or Lottie — defer asset to design). Commit.

### Task 6.7: Stats

**Files:** `StatsScreen.kt`, `StatsViewModel.kt`
- [ ] ViewModel exposes `StatsSummary` Flow from `StatsRepository`. Screen: `StatCard`s (wins/losses/draws/win-rate/streak), a simple bar/donut of outcomes, recent-matches list. Commit.

### Task 6.8: Settings

**Files:** `SettingsScreen.kt`, `SettingsViewModel.kt`
- [ ] ViewModel binds `SettingsRepository.settings`; toggles for sound/haptics/theme, default difficulty, board-theme picker, About section. Each change persists immediately. Commit.

### Task 6.9: Profile

**Files:** `ProfileScreen.kt`, `ProfileViewModel.kt`
- [ ] Avatar, username (editable, persisted via DataStore), level/XP from match count, achievements grid (badges derived from stats thresholds — pure function, unit-tested). Commit.

### Task 6.10: Replace placeholder routes
- [ ] Swap every placeholder in `TicTacNavHost` for the real screen + ViewModel (obtained from `AppContainer`). Build, run end-to-end, commit `feat: wire all screens into navigation`.

---

## Phase 7 — Online Multiplayer (FUTURE — needs backend, separate plan)

Out of MVP scope; requires a backend decision. When prioritized, write a dedicated plan covering:
- Backend choice (Firebase Realtime DB/Firestore + Anonymous Auth recommended for speed) and `GameMode.ONLINE` enablement on Home.
- Matchmaking (quick match / room code), real-time board sync, turn authority + cheat-resistant move validation server-side, presence/disconnect handling, rematch.
- New layer: `data/online/` (remote data source, mappers to `GameState`), `OnlineGameViewModel`.
- Tests with a fake remote source.

---

## Phase 8 — Polish

- [ ] Tile placement + winning-line animations; result confetti.
- [ ] Sound effects + haptics (gated by settings); respect system reduce-motion.
- [ ] App icon / splash branding from final design assets.
- [ ] Accessibility: content descriptions on tiles ("row 1 column 2, empty"), 48dp touch targets, TalkBack pass, dynamic-type check.
- [ ] Edge-to-edge + predictive back; light/dark verified on device.
- [ ] `./gradlew :app:testDebugUnitTest` + `connectedCheck` green; manual smoke of every screen. Commit.

---

## Self-Review (coverage vs. the brief)

| Requested feature | Covered by |
| --- | --- |
| Onboarding screen | Task 6.2 |
| Start/Home screen | Task 6.3 |
| Game screen(s) | Tasks 6.5–6.6 (+ engine 2.2, AI 3.1) |
| Settings | Task 6.8 (+ repo 4.3) |
| Design system + components | Phase 1 (1.1, 1.2) |
| "Clean and modern" | Kotlin/Compose/M3 stack + Phase 8 polish |
| Stats / Profile (implied by "etc") | Tasks 6.7, 6.9 (+ Room 4.1–4.2) |
| Vs AI difficulties | Phase 3 |
| Online | Phase 7 (future, flagged) |

**Open inputs needed from you:**
1. Confirm the **Kotlin + Compose migration** (Key Decision #1) — or say to keep Java/XML.
2. The **final UI design** (Stitch output) to replace placeholder tokens/layouts in Phases 1 & 6.
3. Whether **Online** is wanted soon (triggers the Phase 7 sub-plan) or deferred.

---

## Execution Handoff

Two options once you're ready (and after the UI design lands for the design-dependent phases):

1. **Subagent-Driven (recommended)** — a fresh subagent per task, review between tasks.
2. **Inline Execution** — tasks run in-session with checkpoints.

Phases 2–4 (engine, AI, persistence) are **not** design-dependent and can start immediately; Phases 1 & 6 should wait for the final UI.
