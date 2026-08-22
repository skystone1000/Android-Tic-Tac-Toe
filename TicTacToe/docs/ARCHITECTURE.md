# Architecture

> **Read this before reading source.** This document is the high-level map of how XOXO is
> put together. Pair it with [CODEBASE.md](CODEBASE.md) (file-by-file index) and
> [FEATURES.md](FEATURES.md) (what the app does). If you change the structure described here,
> update this file in the same change.

XOXO is a single-module Android app written in **Kotlin + Jetpack Compose + Material 3**. It is
a clean rewrite of an older Java/XML two-player game. The codebase is deliberately layered so the
game rules and AI are pure Kotlin (unit-testable without an emulator) and the UI is a pure
function of state.

---

## Tech stack

| Concern              | Choice                                                            |
| -------------------- | ---------------------------------------------------------------- |
| Language             | Kotlin 2.2.10 (provided by AGP's built-in Kotlin — not declared)   |
| UI toolkit           | Jetpack Compose (BOM 2026.08.00), Material 3                      |
| Navigation           | Navigation-Compose 2.9.8                                          |
| Async / reactivity   | Kotlin Coroutines + Flow / StateFlow                             |
| Architecture pattern | MVVM — ViewModels expose `StateFlow`, Composables render it      |
| Settings persistence | DataStore (Preferences) 1.2.1                                     |
| Stats persistence    | Room 2.8.4 (with KSP)                                             |
| Dependency injection | Manual — a hand-written `AppContainer` (no Hilt/Dagger)          |
| Build                | Gradle 9.7.1, Android Gradle Plugin 9.3.1, KSP 2.2.10-2.0.2       |
| SDK                  | `minSdk 24`, `compileSdk`/`targetSdk 37`, Java 17 bytecode        |
| Testing              | JUnit4, kotlinx-coroutines-test, Turbine, Compose UI test        |

The app is **completely offline** — there is no networking dependency and no online play.

---

## Layered design

```
┌─────────────────────────────────────────────────────────────┐
│                          UI LAYER                            │
│  Compose screens + reusable components + theme               │
│  ViewModels expose StateFlow<UiState>; UI is stateless       │
│  ui/screens/*  ui/components/*  ui/navigation/*  ui/theme/*  │
└───────────────▲──────────────────────────────┬──────────────┘
                │ observes StateFlow            │ calls intents
                │                               ▼
┌───────────────┴──────────────────────────────────────────────┐
│                        DATA LAYER                             │
│  SettingsRepository (DataStore)  StatsRepository (Room)      │
│  data/settings/*   data/stats/*                             │
└───────────────▲───────────────────────────────────────────────┘
                │ depends on
┌───────────────┴───────────────────────────────────────────────┐
│                  DOMAIN LAYER (pure Kotlin)                    │
│  GameEngine (rules)  MinimaxAi (opponent)  model/* (types)   │
│  No Android imports. Fully unit-tested.                       │
└────────────────────────────────────────────────────────────────┘

         di/AppContainer wires all three layers together,
         held by the TicTacApp Application instance.
```

### Domain layer — `domain/`
Pure Kotlin, **no Android imports**, fully unit-tested.

- **`model/Models.kt`** — the data vocabulary: `Player` (X/O), `Cell` (`Empty`/`Taken`), `GameStatus`
  (`InProgress`/`Won`/`Draw`), `GameState` (immutable board snapshot), `Difficulty`, `GameMode`.
- **`engine/GameEngine.kt`** — the rules. Stateless: `move(state, index)` returns a **new**
  `GameState`; illegal moves return the input unchanged. It detects wins (8 lines) and draws.
  `GameState` is the single source of truth for a round.
- **`ai/MinimaxAi.kt`** — a difficulty-aware minimax opponent implementing the `AiOpponent`
  interface. HARD is optimal/unbeatable; MEDIUM and EASY mix in random moves (30% / 80%) so they
  are beatable. `Random` is injectable for deterministic tests.

### Data layer — `data/`
Persists across app launches. Both repositories expose `Flow` so the UI reacts to changes.

- **`settings/`** — `SettingsRepository` wraps a `DataStore<Preferences>` and maps it to an
  `AppSettings` data class (sound, haptics, theme mode, default difficulty, board theme, player
  name, onboarding-seen flag). Each setting has a suspend setter.
- **`stats/`** — Room database (`AppDatabase`) with one table (`matches` / `MatchEntity`), accessed
  through `MatchDao`. `StatsRepository` records finished rounds and aggregates the rows into a
  `StatsSummary` (wins/losses/draws, win-rate %, current win-streak, last-7-days buckets, recent
  matches).

### UI layer — `ui/`
MVVM with Compose.

- **`theme/`** — the design system: a custom semantic palette (`TicColors` via `LocalTicColors`,
  exposed as `TicTacTheme.colors`), Material 3 color schemes derived from it, variable-font
  typography (Space Grotesk + Hanken Grotesk), and shapes.
- **`components/`** — the reusable, screen-agnostic component library (buttons, board, tiles,
  cards, segmented control, dialogs, confetti, avatars, etc.).
- **`navigation/`** — the route table (`Routes`), the `NavHost` (`TicTacNavHost`), and the
  bottom-bar scaffold (`MainScaffold`) used by the four tab screens.
- **`screens/`** — one package per screen. Each has a `*Screen.kt` Composable and, where it needs
  state, a `*ViewModel.kt` that exposes a `StateFlow<UiState>` and intent functions.

### Dependency injection — `di/`
No DI framework. `AppContainer` constructs every singleton (`GameEngine`, the `aiFactory`,
`AppDatabase`, `StatsRepository`, `SettingsRepository`) once. `TicTacApp` (the `Application`) holds
the instance; `appContainer()` is a composable accessor that reaches it through `LocalContext`.
ViewModels are built by `*ViewModel.factory(container)` helpers.

---

## State & data flow

The app follows **unidirectional data flow**:

1. A repository exposes a `Flow` (DataStore stream / Room query).
2. A ViewModel transforms it into a `StateFlow<UiState>` (often via `combine` + `stateIn`).
3. The screen collects it with `collectAsStateWithLifecycle()` and renders — the UI holds no
   business state of its own.
4. User actions call ViewModel intent functions (e.g. `onTileClick`, `setName`, `setSound`), which
   update state or write to a repository; the change flows back down through the same `Flow`.

**Game-round example** (`GameViewModel`):
`onTileClick(i)` → `GameEngine.move()` produces a new `GameState` → if VS-AI and it's the AI's
turn, a coroutine delays ~450 ms then plays `MinimaxAi.chooseMove()` → on a terminal status the
session score updates and `StatsRepository.record()` writes a `MatchEntity` (once per round).

---

## Navigation model

A single `NavHost` (`TicTacNavHost`) hosts every destination. Routes live in `Routes`:

- **Linear flow:** `splash` → `onboarding` (first launch only) → `home`.
- **Four bottom-nav tabs** wrapped in `MainScaffold`: `home`, `stats`, `profile`, `settings`.
- **Argument routes:** `setup/{mode}/{difficulty}` and `game/{mode}/{difficulty}/{symbol}` — args
  are enum/`Player` names parsed back with `valueOf`. Typed builders (`Routes.setup(...)`,
  `Routes.game(...)`) construct them.

The start destination is always `splash`; the splash decides whether to continue to `onboarding`
or `home` based on the persisted `hasSeenOnboarding` flag.

---

## Key design decisions

1. **Kotlin/Compose rewrite** of the original Java/XML app — the old activities and layouts were
   replaced, not extended.
2. **Pure domain layer.** All game logic lives in `domain/` with zero Android dependencies so it
   runs as fast JVM unit tests.
3. **Single source of truth.** One immutable `GameState` describes a round; the UI is a pure
   function of it.
4. **Manual DI.** The dependency graph is small, so a hand-written `AppContainer` is simpler than
   Hilt. Revisit if the graph grows.
5. **Offline-only, no progression.** No online/multiplayer mode and no levels/XP/achievements —
   only a fun **win-streak** stat. These are firm product constraints (the Figma design shows
   online "Quick Match" and an XP ladder; both were intentionally omitted).
6. **Custom semantic theme.** Material 3's `ColorScheme` covers brand/surface basics; game-specific
   colors (player marks, soft tile backgrounds) live in `TicColors` and are read via
   `TicTacTheme.colors`.
7. **AGP built-in Kotlin.** From AGP 9 the Kotlin Gradle Plugin is no longer applied by the project
   — AGP supplies Kotlin itself (2.2.10). There is therefore **no Kotlin version in any build
   file**; it moves when AGP moves. Compiler settings live in a top-level `kotlin { compilerOptions
   { … } }` block, not the old `android.kotlinOptions`. The Compose compiler is still applied
   explicitly (`org.jetbrains.kotlin.plugin.compose`), and its version **must match AGP's Kotlin
   version** — bump the two together.
8. **Edge-to-edge system bars.** `MainActivity` calls `enableEdgeToEdge()` and screens apply insets.
   `window.statusBarColor` / `navigationBarColor` are deliberately *not* set — they are no-ops from
   API 35 onwards; only bar icon appearance is controlled, in `TicTacTheme`.

---

## Where to make common changes

| You want to change…                | Go to…                                                          |
| ---------------------------------- | -------------------------------------------------------------- |
| Win detection / move rules         | `domain/engine/GameEngine.kt`                                  |
| AI strength / behaviour            | `domain/ai/MinimaxAi.kt`                                       |
| A new persisted setting            | `data/settings/AppSettings.kt` + `SettingsRepository.kt`      |
| What a "match" stores / stat math  | `data/stats/MatchEntity.kt` + `StatsRepository.kt`            |
| Colors, fonts, shapes              | `ui/theme/*`                                                   |
| A reusable widget                  | `ui/components/*`                                              |
| A screen's layout                  | `ui/screens/<screen>/<Screen>.kt`                             |
| A screen's state/logic             | `ui/screens/<screen>/<Screen>ViewModel.kt`                    |
| Routes / navigation wiring         | `ui/navigation/Routes` + `TicTacNavHost.kt`                   |
| App-wide wiring / new singleton    | `di/AppContainer.kt`                                          |

See [CODEBASE.md](CODEBASE.md) for the full file index and [FEATURES.md](FEATURES.md) for the
user-facing behaviour each piece implements.
