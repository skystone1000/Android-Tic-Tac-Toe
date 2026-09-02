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
| `BoardPalette.kt` | `BoardPalette` + `boardPaletteFor(BoardTheme)` — resolves the user's board-theme choice into the six colors the game screen draws with (board, score cards, turn pill). CLASSIC follows `TicColors`; MIDNIGHT and AURORA are fixed looks. |
| `Theme.kt`   | `TicTacTheme(darkTheme) { }` wrapper; maps `TicColors` → M3 `ColorScheme`; sets status/nav bar colors; `TicTacTheme.colors` accessor. |

## `ui/layout/` — adaptive layer

| File            | Purpose                                                                       |
| --------------- | ----------------------------------------------------------------------------- |
| `WindowSize.kt` | `WidthClass`/`HeightClass`/`WindowSize` + the pure `windowSizeOf()` classifier and the `rememberWindowSize()` composable (reads `LocalWindowInfo`, so it tracks split-screen and foldables). |
| `Sizing.kt`     | Pure layout maths in bare `Float` dp/sp so it unit-tests on the JVM: `boardSideDp()`, `boardGapDp()`, `tileMarkSp()`, `tileCornerDp()`, and the `MAX_BOARD_SIDE_DP` / `MAX_BOARD_SIDE_LARGE_DP` / `MAX_CONTENT_WIDTH_DP` caps. |

## `ui/components/` — reusable widgets

| File                  | Public composables                                                          |
| --------------------- | --------------------------------------------------------------------------- |
| `GameBoard.kt`        | `GameBoard` — 3×3 grid from a size-9 `List<TileMark>`; highlights winning line. Sizes itself to the largest square that **fits the box it is given** (capped), so it can never overflow its parent. Takes a `BoardPalette`. |
| `GameTile.kt`         | `GameTile` — single cell; `TileMark` enum; X/O/empty/highlighted/enabled states. Derives its corner radius and glyph size from its own measured size; takes a `positionLabel` for TalkBack and a `BoardPalette`. |
| `TicButton.kt`        | `TicButton` (primary, optional leading icon), `TicIconButton`.             |
| `Common.kt`           | `SectionLabel`, `TicCard`, `TicTopBar`, `IconBadge`.                        |
| `SegmentedControl.kt` | `SegmentedControl` — used for difficulty pickers.                          |
| `SettingRow.kt`       | `ToggleRow` (switch row), `NavRow` (tappable row with trailing text).      |
| `ResultDialog.kt`     | `ResultDialog` — win/lose/draw overlay; confetti on win.                   |
| `Confetti.kt`         | `Confetti` — lightweight Canvas falling-particle overlay.                  |
| `Avatar.kt`           | `Avatar` + `AvatarTone` — initial-based rounded avatar.                    |
| `TurnIndicator.kt`    | `TurnIndicator` — pill showing whose turn / status, with a colored dot.    |
| `PageIndicator.kt`    | `PageIndicator` — onboarding pager dots.                                   |
| `ScreenContainer.kt`  | `ScreenContainer` — the shared single-column screen body: paints the background, applies the scaffold padding, scrolls, and centres content capped at `MAX_CONTENT_WIDTH_DP`. |

## `ui/navigation/`

| File                  | Purpose                                                                     |
| --------------------- | --------------------------------------------------------------------------- |
| `Destinations.kt`     | `Routes` table + typed arg builders; `HomeTab` enum (bottom-nav tabs).     |
| `TicTacNavHost.kt`    | The `NavHost` wiring every route to its screen + ViewModel; tab helper and `navigateTab`. |
| `MainScaffold.kt`     | `Scaffold` shared by the four tab screens: a custom bottom bar normally, and a left **navigation rail** on expanded-width windows. |

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
| `ui/layout/WindowSizeTest.kt`           | Breakpoint classification for phone/tablet, portrait/landscape, and the two-pane decision. |
| `ui/layout/SizingTest.kt`               | Board square fitting, the size caps, unbounded-height fallback, gap/mark/corner scaling. |

### Instrumented tests (`app/src/androidTest/`)

| File                                    | Covers                                                     |
| --------------------------------------- | ---------------------------------------------------------- |
| `ui/screens/game/GameScreenSizeTest.kt` | Renders `GameScreen` at five window sizes (phone portrait/landscape, tablet portrait/landscape, narrow split-screen) and asserts every cell and both controls lie **inside** the window. It checks geometric bounds rather than `assertIsDisplayed`, because a `Column` does not clip — an overflowing board still reports its tiles as "displayed". Requires a connected device or emulator. |

---

## Build & resource files

| Path                                   | Purpose                                                        |
| -------------------------------------- | ------------------------------------------------------------- |
| `TicTacToe/build.gradle`               | Root: plugin versions (AGP 9.3.1, Compose compiler, KSP).     |
| `TicTacToe/app/build.gradle`           | App module: SDKs, Compose, dependencies. The `release` build type has R8 on (`minifyEnabled` + `shrinkResources`). No `signingConfigs` yet — release builds are unsigned. Espresso and `test:runner` are pinned explicitly (3.7.0 / 1.7.0): `compose-ui-test-junit4` still resolves espresso 3.5.0, which reflects into a removed `InputManager.getInstance()` and crashes every instrumented test on API 37. |
| `TicTacToe/app/proguard-rules.pro`     | R8 keep rules: crash attributes (`SourceFile,LineNumberTable`), the whole `data.stats.**` package (Room resolves columns by name), and enum `values()`/`valueOf()`. See `docs/RELEASE-CHECKLIST.md` §1. |
| `TicTacToe/settings.gradle`            | Includes `:app`; repositories.                                |
| `TicTacToe/gradle.properties`          | AndroidX on, JVM args, KSP source-set opt-out (see below).    |
| `TicTacToe/local.properties`           | Local SDK path (git-ignored, machine-specific).               |
| `app/src/main/AndroidManifest.xml`     | `.TicTacApp` + single `.MainActivity` launcher.               |
| `app/src/main/res/values/strings.xml`  | `app_name` = "XOXO".                                          |
| `app/src/main/res/font/`               | Space Grotesk + Hanken Grotesk variable fonts.                |
| `app/src/main/res/drawable/`           | The three adaptive-icon vector layers: `ic_launcher_foreground` (teal X + orange O), `ic_launcher_background` (flat indigo), `ic_launcher_monochrome` (Android 13+ themed icons). The board, marks and all game art are drawn in Compose — there are no bitmap game assets. |
| `app/src/main/res/mipmap-anydpi-v26/`  | `ic_launcher.xml` / `ic_launcher_round.xml` — adaptive-icon descriptors; both declare background + foreground + monochrome. |
| `app/src/main/res/mipmap-*dpi/`        | Legacy launcher PNGs, 48→192 px, square + round.               |
| `app/src/main/res/drawable-*dpi/`      | `ic_stat_xoxo.png` — notification icon, 24 dp, mdpi→xxxhdpi. **Currently unreferenced**: the app sends no notifications; shipped ahead of that feature. Because `shrinkResources` is on it is **stripped from release builds** (`mapping/release/resources.txt` reports it unreachable); it returns automatically once something references `R.drawable.ic_stat_xoxo`. |
| `Assets/`                              | Every non-code asset, grouped here rather than at the repo root: `play-store/`, `Screenshots/`, `Claude Design/`. Nothing under it is compiled into the app. |
| `Assets/Screenshots/`                  | App screenshots embedded in the root `README.md`, plus dated capture sets (`2026-08-27/`) and `Old/` for pre-rewrite shots. |
| `Assets/Claude Design/`                | `TicTac.dc.html` + `support.js` — the reference design file the UI was built from. |
| `Assets/play-store/`                   | Play Console launch kit. Folders `01-`…`05-` are numbered in Console upload order (icon → feature graphic → screenshots → video → listing text); `_reference/` holds the brand kit and source captures and is never uploaded. All Console text fields are in `05-listing-text/PLAY-CONSOLE-TEXT.md`. Nothing here is compiled into the app. |
| `TicTacToe/docs/`                      | This documentation + `tictactoe-revamp-plan.md` (original build plan) + `PLAY-STORE-ASSET-PROMPT.md` (ready-to-run prompt for generating the Play Store asset kit; holds the canonical brand/data-safety brief) + `RELEASE-CHECKLIST.md` + `ADAPTIVE-UI-PLAN.md` (large-screen / landscape / configuration bug analysis and the task-by-task fix plan; **implemented**) + `BACKLOG.md` (deferred work: sound effects, launch-flash). |
| `TicTacToe/docs/RELEASE-CHECKLIST.md`  | The playbook for shipping an AAB to Play: build hardening (R8 + keep rules), signing setup, per-release pre-flight, build commands, Console upload and policy tasks. Read it before any release. §1 (R8 hardening) is now configured and verified; **§2 signing is still outstanding** — there is no `signingConfigs` block, so release builds are unsigned. |
