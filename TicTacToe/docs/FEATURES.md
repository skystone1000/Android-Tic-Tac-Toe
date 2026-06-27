# Features

> **Read this before reasoning about behaviour.** It describes what XOXO does from the user's
> point of view and which files implement each feature. Pair with
> [ARCHITECTURE.md](ARCHITECTURE.md) and [CODEBASE.md](CODEBASE.md). If you change behaviour,
> update this file in the same change.

XOXO is a polished, **offline** tic-tac-toe game. Two play modes, three AI difficulties, persistent
stats and settings, onboarding, and a custom design system with animations. There is **no online
play and no levels/XP/achievements** — by design (see [ARCHITECTURE.md](ARCHITECTURE.md) →
*Key design decisions*).

---

## Game modes

### Pass & Play
Two humans share one device, alternating taps. X always moves first. Scores for the current
session are tracked per symbol. Implemented by `GameViewModel` with `mode = PASS_AND_PLAY`;
recorded matches track the **X** side as the WIN/LOSS subject.

### Vs AI (Easy / Medium / Hard)
The human picks a symbol; the AI plays the other. The AI move is dispatched on a coroutine with a
~450 ms "thinking" delay (`isAiThinking` drives an "AI is thinking…" pill). If the human chooses
O, the AI opens automatically.

- **Hard** — optimal minimax, **unbeatable** (you can only draw or lose).
- **Medium** — minimax but plays a random move 30% of the time.
- **Easy** — random move 80% of the time.

Difficulty is chosen on Home (segmented control) or Match Setup. Logic: `domain/ai/MinimaxAi.kt`;
wiring: `GameViewModel`.

---

## Screen-by-screen

### Splash — `ui/screens/splash/`
Gradient screen with an animated (spring/scale) XOXO logo and a loading spinner. Waits until
settings have loaded, then routes to Onboarding (first launch) or Home after ~1.1 s.

### Onboarding — `ui/screens/onboarding/`
A 3-page `HorizontalPager` ("Play anywhere", "Friend or AI", "Track your wins") with page-indicator
dots, a Skip link, a Next arrow, and a "Get started" button on the last page. Finishing sets the
persisted `hasSeenOnboarding` flag so it never shows again.

### Home — `ui/screens/home/`
- Greeting with the player's name and a settings icon + profile avatar (tap to navigate).
- **Win-streak banner** — the count of consecutive wins (the only "progression" stat).
- **Pass & Play** card → Match Setup.
- **Vs AI** card with an inline difficulty segmented control → Match Setup / game.

State comes from `HomeViewModel`, which `combine`s settings (name, default difficulty) and stats
(streak).

### Match Setup — `ui/screens/setup/`
Choose your symbol (X/O), choose difficulty (segmented — **only shown for Vs AI**), and preview the
two players (you vs Friend/AI). "Start match" navigates into the game with the chosen mode,
difficulty, and symbol as route args. Selection is local screen state (no ViewModel).

### Game — `ui/screens/game/` (the centerpiece)
- **3×3 board** (`GameBoard` / `GameTile`) rendered purely from `GameState.board`.
- **Score panel** — per-symbol session scores plus a live **round timer** (mm:ss) that resets each
  new round and ticks while a round is in progress.
- **Turn indicator** pill — whose turn it is (colored per symbol), "AI is thinking…", win, or draw.
- **Winning line** is highlighted when a player completes three in a row.
- **Haptics** — tapping a tile triggers haptic feedback when haptics are enabled in settings.
- **Controls** — Restart (new round, keeps session scores) and Quit (back to Home).
- **Result overlay** (`ResultDialog`) — appears on win/lose/draw with a contextual title, the
  session score, Play-Again and Home buttons, and **confetti on a win**.
- Each finished round is **recorded once** to the stats database.

Rules live in `domain/engine/GameEngine.kt`; orchestration (AI turns, scoring, recording, haptics
flag) in `GameViewModel`.

### Stats — `ui/screens/stats/`
Driven by `StatsRepository.summary`:
- **Win rate %** and **current streak** cards.
- **Wins / Losses / Draws** counters.
- **Last 7 days** mini bar chart (one bar per day, today on the right).
- **Recent matches** list (up to 10), each showing mode (Vs AI + difficulty, or Pass & Play) and a
  WIN/LOSS/DRAW chip. Empty state prompts you to play a round.

### Settings — `ui/screens/settings/`
Each change persists immediately via `SettingsViewModel` → `SettingsRepository` (DataStore):
- **Sound effects** toggle, **Haptics** toggle, **Dark mode** toggle.
- **Default AI difficulty** (segmented) — pre-selects difficulty on Home/Setup.
- **Board theme** swatches (Classic / Midnight / Aurora).
- **About** row showing the app version (v2.0).

### Profile — `ui/screens/profile/`
Gradient header with an initial-based avatar, the editable **display name** (dialog → persisted via
DataStore), and summary cards (Games, Wins, Win %). No levels/XP/achievements by design.

---

## Cross-cutting features

| Feature                  | Where                                                                   |
| ------------------------ | ----------------------------------------------------------------------- |
| **Light / Dark theme**   | `ThemeMode` setting → `MainActivity` picks scheme → `TicTacTheme`. Defaults to follow system. |
| **Edge-to-edge UI**      | `MainActivity.enableEdgeToEdge()`; status/nav bar colors set in `Theme.kt`; screens apply system-bar insets. |
| **Persistent settings**  | DataStore (`SettingsRepository`) — survives restarts.                   |
| **Persistent stats**     | Room (`StatsRepository` / `MatchEntity`) — survives restarts.           |
| **Custom typography**    | Variable fonts Space Grotesk (display/numerals) + Hanken Grotesk (UI).  |
| **Bottom navigation**    | `MainScaffold` — Home / Stats / Profile / Settings tabs with state save/restore. |
| **Animations**           | Splash logo spring, onboarding pager, tile/result transitions, confetti, AI-thinking delay. |

---

## Explicit non-features (by product decision)

- **No online / multiplayer / Quick Match.** `GameMode` has only `PASS_AND_PLAY` and `VS_AI`.
- **No levels, XP, or achievements.** Only the fun **win-streak** stat is kept.

These were intentionally omitted even though the original Figma design included them. Do not add
them without an explicit product decision to change course.
