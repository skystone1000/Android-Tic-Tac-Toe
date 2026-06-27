# Codebase Map

> **Read this before grepping the source.** It is the file-by-file index of the app so you can
> jump straight to the right file. Pair with [ARCHITECTURE.md](ARCHITECTURE.md) (how it fits
> together) and [FEATURES.md](FEATURES.md) (what it does). If you add, move, or repurpose a file,
> update this index in the same change.

- **Module dir:** `TicTacToe/` (keeps the legacy name; the app/package are XOXO)
- **App / package:** XOXO — `com.skystone1000.xoxo`
- **Source root:** `TicTacToe/app/src/main/java/com/skystone1000/xoxo/`
- **Tests:** `TicTacToe/app/src/test/java/com/skystone1000/xoxo/` (JVM unit tests)

---

## Top level

| File                  | Purpose                                                                     |
| --------------------- | --------------------------------------------------------------------------- |
| `TicTacApp.kt`        | `Application` subclass; constructs and holds the `AppContainer`.            |
| `MainActivity.kt`     | Single `ComponentActivity` host. Enables edge-to-edge, observes the theme setting, hosts `TicTacTheme { TicTacNavHost() }`. |

## `di/` — dependency injection (manual)

| File                  | Purpose                                                                     |
| --------------------- | --------------------------------------------------------------------------- |
| `AppContainer.kt`     | Builds the singletons: `gameEngine`, `aiFactory: (Difficulty) -> AiOpponent`, `AppDatabase`, `statsRepository`, `settingsRepository`. Owns the DataStore delegate. |
| `ContainerAccess.kt`  | `appContainer()` — composable accessor that reads the container off `TicTacApp` via `LocalContext`. |

## `domain/` — pure Kotlin, no Android, unit-tested

| File                  | Purpose                                                                     |
| --------------------- | --------------------------------------------------------------------------- |
| `model/Models.kt`     | `Player`, `Cell` (`Empty`/`Taken`), `GameStatus` (`InProgress`/`Won`/`Draw`), `GameState`, `Difficulty`, `GameMode` (only `PASS_AND_PLAY`, `VS_AI`). |
| `engine/GameEngine.kt`| Stateless rules: `move()`, `reset()`, win/draw evaluation over 8 lines.    |
| `ai/AiOpponent.kt`    | `interface AiOpponent { fun chooseMove(state): Int }`.                      |
| `ai/MinimaxAi.kt`     | Difficulty-aware minimax. EASY/MEDIUM/HARD = 0.8/0.3/0.0 random chance; injectable `Random`. |

## `data/` — persistence

| File                        | Purpose                                                                |
| --------------------------- | ---------------------------------------------------------------------- |
| `settings/AppSettings.kt`   | `AppSettings` data class + `ThemeMode`, `BoardTheme` enums + defaults. |
| `settings/SettingsRepository.kt` | DataStore-backed. `settings: Flow<AppSettings>` + suspend setters; reads tolerate bad enum values. |
| `stats/MatchEntity.kt`      | Room `@Entity` `matches` + `MatchResult` enum (WIN/LOSS/DRAW).         |
| `stats/MatchDao.kt`         | `insert`, `observeAll()` (newest-first `Flow`), `clear()`.            |
| `stats/AppDatabase.kt`      | Room `@Database` (v1, `tictac.db`); `build(context)` factory.         |
| `stats/StatsRepository.kt`  | `summary: Flow<StatsSummary>`, `record(...)`, `clear()`. Computes win-rate, current streak, last-7-days buckets. `now` clock is injectable. |

## `ui/theme/` — design system

| File         | Purpose                                                                              |
| ------------ | ------------------------------------------------------------------------------------ |
| `Color.kt`   | `TicColors` semantic palette + `LightTicColors`/`DarkTicColors` + `LocalTicColors`. |
| `Type.kt`    | Variable-font families (`SpaceGrotesk`, `HankenGrotesk`) + `TicTypography` scale.    |
| `Shape.kt`   | `TicShapes` corner radii + spacing tokens.                                           |
| `Theme.kt`   | `TicTacTheme(darkTheme) { }` wrapper; maps `TicColors` → M3 `ColorScheme`; sets status/nav bar colors; `TicTacTheme.colors` accessor. |

## `ui/components/` — reusable widgets

| File                  | Public composables                                                          |
| --------------------- | --------------------------------------------------------------------------- |
| `GameBoard.kt`        | `GameBoard` — 3×3 grid from a size-9 `List<TileMark>`; highlights winning line. |
| `GameTile.kt`         | `GameTile` — single cell; `TileMark` enum; X/O/empty/highlighted/enabled states. |
| `TicButton.kt`        | `TicButton` (primary, optional leading icon), `TicIconButton`.             |
| `Common.kt`           | `SectionLabel`, `TicCard`, `TicTopBar`, `IconBadge`.                        |
| `SegmentedControl.kt` | `SegmentedControl` — used for difficulty pickers.                          |
| `SettingRow.kt`       | `ToggleRow` (switch row), `NavRow` (tappable row with trailing text).      |
| `ResultDialog.kt`     | `ResultDialog` — win/lose/draw overlay; confetti on win.                   |
| `Confetti.kt`         | `Confetti` — lightweight Canvas falling-particle overlay.                  |
| `Avatar.kt`           | `Avatar` + `AvatarTone` — initial-based rounded avatar.                    |
| `TurnIndicator.kt`    | `TurnIndicator` — pill showing whose turn / status, with a colored dot.    |
| `PageIndicator.kt`    | `PageIndicator` — onboarding pager dots.                                   |

## `ui/navigation/`

| File                  | Purpose                                                                     |
| --------------------- | --------------------------------------------------------------------------- |
| `Destinations.kt`     | `Routes` table + typed arg builders; `HomeTab` enum (bottom-nav tabs).     |
| `TicTacNavHost.kt`    | The `NavHost` wiring every route to its screen + ViewModel; tab helper and `navigateTab`. |
| `MainScaffold.kt`     | `Scaffold` + custom bottom bar shared by the four tab screens.             |

## `ui/screens/` — one package per screen

| Package        | Files                                  | Notes                                              |
| -------------- | -------------------------------------- | -------------------------------------------------- |
| `splash/`      | `SplashScreen.kt`                       | Animated logo; waits for settings, then continues. No VM. |
| `onboarding/`  | `OnboardingScreen.kt`                   | 3-page `HorizontalPager`; Skip/Next/Get-started. No VM. |
| `home/`        | `HomeScreen.kt`, `HomeViewModel.kt`     | Greeting, win-streak banner, Pass&Play + Vs-AI cards. |
| `setup/`       | `MatchSetupScreen.kt`                   | Symbol + difficulty + players; "Start match". No VM (local state). |
| `game/`        | `GameScreen.kt`, `GameViewModel.kt`     | The board, scores, timer, controls, result overlay. **Core logic.** |
| `stats/`       | `StatsScreen.kt`, `StatsViewModel.kt`   | Win-rate, streak, W/L/D, last-7-days bars, recent matches. |
| `settings/`    | `SettingsScreen.kt`, `SettingsViewModel.kt` | Sound/haptics/dark-mode toggles, default difficulty, board theme, about. |
| `profile/`     | `ProfileScreen.kt`, `ProfileViewModel.kt`   | Avatar, editable display name, summary cards. |

---

## Tests (`app/src/test/`)

| File                                    | Covers                                                             |
| --------------------------------------- | ----------------------------------------------------------------- |
| `domain/engine/GameEngineTest.kt`       | Moves, turn-passing, row/diagonal wins, draw, post-game lockout, reset. |
| `domain/ai/MinimaxAiTest.kt`            | HARD takes wins / blocks threats / never loses; legal-move guarantee. Uses seeded `Random`. |
| `data/stats/StatsRepositoryTest.kt`     | Win/loss/draw counts, win-rate, streak logic, last-7-days bucketing. Uses a `FakeMatchDao` + injected clock. |

There is no `androidTest/` source set checked in; the Compose-UI-test dependencies are present for
when instrumented tests are added.

---

## Build & resource files

| Path                                   | Purpose                                                        |
| -------------------------------------- | ------------------------------------------------------------- |
| `TicTacToe/build.gradle`               | Root: plugin versions (AGP 8.6.0, Kotlin 1.9.24, KSP).        |
| `TicTacToe/app/build.gradle`           | App module: SDKs, Compose, dependencies.                      |
| `TicTacToe/settings.gradle`            | Includes `:app`; repositories.                                |
| `TicTacToe/gradle.properties`          | AndroidX on, JVM args.                                        |
| `TicTacToe/local.properties`           | Local SDK path (git-ignored, machine-specific).               |
| `app/src/main/AndroidManifest.xml`     | `.TicTacApp` + single `.MainActivity` launcher.               |
| `app/src/main/res/values/strings.xml`  | `app_name` = "XOXO".                                          |
| `app/src/main/res/font/`               | Space Grotesk + Hanken Grotesk variable fonts.                |
| `app/src/main/res/mipmap-*/`, `drawable*/` | Launcher icons and game art.                              |
| `TicTacToe/docs/`                      | This documentation + `tictactoe-revamp-plan.md` (original build plan). |
