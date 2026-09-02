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
difficulty, and symbol as route args. Selection is local screen state (no ViewModel) but is
**saved across rotation**. The symbol tiles are square at every width, and the screen scrolls.

### Game — `ui/screens/game/` (the centerpiece)
- **3×3 board** (`GameBoard` / `GameTile`) rendered purely from `GameState.board`. It is always
  the largest square that fits the space available (capped at 420dp on phones, 520dp on larger
  windows), and the X/O glyphs and corner radii scale with the tile — so it looks right from a
  small phone up to a tablet, and never overflows the screen.
- **Two layouts.** In portrait the board sits under the score panel with the controls pinned to the
  bottom. In landscape, and on expanded-width windows, the screen splits into two panes: chrome
  (back, scores, timer, turn pill, controls) on the left, board on the right.
- **Score panel** — per-symbol session scores plus a live **round timer** (mm:ss) that resets each
  new round, ticks while a round is in progress, and survives rotation.
- **Turn indicator** pill — whose turn it is (colored per symbol), "AI is thinking…", win, or draw.
- **Winning line** is highlighted when a player completes three in a row.
- **Haptics** — tapping a tile triggers haptic feedback when haptics are enabled in settings.
- **Controls** — Restart (new round, keeps session scores) and Quit (back to Home).
- **Board theme** — the Classic / Midnight / Aurora choice from Settings retints the board, the
  score cards and the turn pill together, live.
- **Result overlay** (`ResultDialog`) — appears on win/lose/draw with a contextual title, the
  session score, Play-Again and Home buttons, and **confetti on a win**. The card is width-capped
  so it stays a panel on a tablet rather than spanning the screen, and the back gesture leaves the
  round instead of doing nothing.
- **Accessibility** — each cell announces its position and contents ("row 2, column 3, empty").
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
- **Haptics** toggle.
- **Appearance** (segmented: System / Light / Dark) — "System" stays reachable, so following the
  OS theme is never a one-way door.
- **Default AI difficulty** (segmented) — pre-selects difficulty on Home/Setup.
- **Board theme** swatches (Classic / Midnight / Aurora) — these retint the game screen. Each
  swatch preview is generated from the palette it selects, so it always matches.

There is no **Sound effects** row: the setting is still persisted, but the app has no audio yet, so
the inert switch was removed. See [BACKLOG.md](BACKLOG.md) §1.
- **About** row showing the app version (v2.0).

### Profile — `ui/screens/profile/`
Gradient header with an initial-based avatar and the editable **display name** — tap the name row
in the header, or the "Edit display name" card, to open the rename dialog (persisted via DataStore;
the dialog and its draft survive rotation). Below it, summary cards (Games, Wins, Win %). The screen
scrolls and the header sizes itself to its content, so nothing clips in landscape or at large font
scale. No levels/XP/achievements by design.

---

## Cross-cutting features

| Feature                  | Where                                                                   |
| ------------------------ | ----------------------------------------------------------------------- |
| **Light / Dark theme**   | `ThemeMode` setting → `MainActivity` picks scheme → `TicTacTheme`. Defaults to follow system. |
| **Edge-to-edge UI**      | `MainActivity.enableEdgeToEdge()`; bar icon appearance set in `Theme.kt`; screens apply `safeDrawingPadding()` so the display cutout does not overlap content in landscape. |
| **Persistent settings**  | DataStore (`SettingsRepository`) — survives restarts.                   |
| **Persistent stats**     | Room (`StatsRepository` / `MatchEntity`) — survives restarts.           |
| **Custom typography**    | Variable fonts Space Grotesk (display/numerals) + Hanken Grotesk (UI).  |
| **Adaptive navigation**  | `MainScaffold` — Home / Stats / Profile / Settings as a bottom bar, or a left **navigation rail** on expanded-width (≥840dp) windows. State is saved/restored across tabs. |
| **Large-screen layouts** | `ui/layout/` — single-column content is capped at 560dp and centred; the game screen goes two-pane in landscape. Works in split-screen and on foldables because it reads the *window* size, not the device. |
| **Animations**           | Splash logo spring, onboarding pager, tile/result transitions, confetti, AI-thinking delay. |

---

## Explicit non-features (by product decision)

- **No online / multiplayer / Quick Match.** `GameMode` has only `PASS_AND_PLAY` and `VS_AI`.
- **No levels, XP, or achievements.** Only the fun **win-streak** stat is kept.

These were intentionally omitted even though the original Figma design included them. Do not add
them without an explicit product decision to change course.
