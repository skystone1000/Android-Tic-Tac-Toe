# XOXO — Android Tic-Tac-Toe

A polished, **offline** tic-tac-toe game for Android, built with **Kotlin + Jetpack Compose +
Material 3**. Play pass-and-play with a friend or against three levels of AI, with persistent stats,
settings, and a custom design system.

> The Gradle module lives in [`TicTacToe/`](TicTacToe/) (the directory keeps the old project name).
> The app and package are **XOXO** / `com.skystone1000.xoxo`.

<p align="center">
  <img src="Screenshots/v1.0.png" alt="XOXO screenshot" width="280">
</p>

---

## Features at a glance

- **Two modes:** Pass & Play (2 players, one device) and Vs AI (Easy / Medium / Hard).
- **Unbeatable AI** on Hard (minimax); beatable on Easy/Medium.
- **Persistent stats:** win rate, win-streak, W/L/D, last-7-days chart, recent matches.
- **Settings:** sound, haptics, dark mode, default difficulty, board theme — all persisted.
- **Onboarding, splash, profile**, light/dark theme, edge-to-edge, animations, confetti.
- **Completely offline.** No online play, no levels/XP/achievements (by design).

Full details: [`TicTacToe/docs/FEATURES.md`](TicTacToe/docs/FEATURES.md).

---

## Tech stack

Kotlin 1.9.24 · Jetpack Compose (BOM 2024.06.00) · Material 3 · Navigation-Compose · Coroutines /
Flow · MVVM (ViewModel + StateFlow) · Room · DataStore · manual DI. Build: Gradle 8.7 · AGP 8.6.0 ·
KSP. `minSdk 21`, `targetSdk/compileSdk 35`, Java 17.

---

## Getting started

### Prerequisites
- **Android Studio** (latest stable) with the **Android SDK for API 35**.
- **JDK 17.** Android Studio ships one (its bundled JBR) — you don't need a separate install.

### Open & run
1. Clone the repo and open the **`TicTacToe/`** directory in Android Studio (it is the Gradle root,
   not the repo root).
2. Let Gradle sync. Android Studio creates `TicTacToe/local.properties` with your SDK path
   automatically (it's git-ignored).
3. Run the `app` configuration on an emulator or device (API 21+).

### Command line
From the `TicTacToe/` directory:

```bash
# Build a debug APK
./gradlew :app:assembleDebug

# Run the JVM unit tests (domain engine, AI, stats)
./gradlew :app:testDebugUnitTest
```

> **macOS note:** if no JDK 17 is on your `PATH`, point Gradle at Android Studio's bundled JBR:
> ```bash
> JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug
> ```

---

## How to deep-dive the code

**Start with the docs, not the source** — they are written to give you the whole picture in a few
minutes and to keep you from re-reading the same files:

1. [`TicTacToe/docs/ARCHITECTURE.md`](TicTacToe/docs/ARCHITECTURE.md) — layers, data flow,
   navigation, and key design decisions. *Read this first.*
2. [`TicTacToe/docs/CODEBASE.md`](TicTacToe/docs/CODEBASE.md) — file-by-file index; where
   everything lives.
3. [`TicTacToe/docs/FEATURES.md`](TicTacToe/docs/FEATURES.md) — what the app does, screen by screen.

The original implementation plan is preserved at
[`TicTacToe/docs/tictactoe-revamp-plan.md`](TicTacToe/docs/tictactoe-revamp-plan.md).

**Reading order in code** once you have the map: `domain/` (pure rules + AI) → `data/`
(repositories) → `ui/screens/game/` (the centerpiece) → the rest of `ui/`.

---

## Project layout

```
Android-Tic-Tac-Toe/
├── README.md                  ← you are here
├── Screenshots/ , Assets/     ← marketing/art assets
├── figma/                     ← reference design
└── TicTacToe/                 ← the Gradle project (open THIS in Android Studio)
    ├── app/src/main/java/com/skystone1000/xoxo/
    │   ├── domain/            ← pure game engine + AI + models (unit-tested)
    │   ├── data/              ← settings (DataStore) + stats (Room)
    │   ├── di/                ← manual AppContainer
    │   └── ui/                ← theme, components, navigation, screens (MVVM)
    ├── app/src/test/          ← JVM unit tests
    └── docs/                  ← ARCHITECTURE / CODEBASE / FEATURES + build plan
```

---

## Contributing

1. **Read the three docs above** before changing code so you land in the right layer.
2. Keep the **domain layer pure** (no Android imports) and covered by unit tests
   (`./gradlew :app:testDebugUnitTest`).
3. Follow the existing patterns: ViewModels expose `StateFlow<UiState>`; Composables stay
   stateless and render state; new dependencies are wired in `di/AppContainer.kt`.
4. Respect the product constraints: **offline-only**, and **no levels/XP/achievements** — keep only
   the win-streak stat.
5. **Update the docs in the same change.** If you alter architecture, file layout, or behaviour,
   edit `ARCHITECTURE.md` / `CODEBASE.md` / `FEATURES.md` accordingly. See
   [`CLAUDE.md`](CLAUDE.md) for the documentation-maintenance rules (used by AI assistants and a
   useful checklist for humans too).
