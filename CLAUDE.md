# Project rules — XOXO (Android-Tic-Tac-Toe)

This repo maintains a small set of **context docs** that describe the whole app. They exist so an
assistant (or human) can understand the project without re-reading every source file each session,
and to keep token usage low.

## 1. Read the docs before reading the code

Before exploring source, opening files, or answering questions about how the app works, **read the
context documentation first** (in this order):

1. [`TicTacToe/docs/ARCHITECTURE.md`](TicTacToe/docs/ARCHITECTURE.md) — layers, data flow,
   navigation, design decisions.
2. [`TicTacToe/docs/CODEBASE.md`](TicTacToe/docs/CODEBASE.md) — file-by-file index (where things
   live).
3. [`TicTacToe/docs/FEATURES.md`](TicTacToe/docs/FEATURES.md) — what the app does, per screen.

These three files are the **source of truth for context**. Use them to locate the right file, then
open only that file. Do **not** re-scan the whole tree if the docs already answer the question.

## 2. Keep the docs in sync with the code

The docs and the code must not drift. **In the same change that alters the project, update the
affected doc(s):**

| If you change…                                              | Update…                          |
| ----------------------------------------------------------- | -------------------------------- |
| Layers, data flow, navigation, DI, a design decision, stack | `ARCHITECTURE.md`                |
| Add / move / rename / repurpose a file or package           | `CODEBASE.md`                    |
| User-facing behaviour, a screen, a mode, a setting          | `FEATURES.md`                    |
| Setup, build, or run steps                                  | `README.md`                      |

Treat a docs update as part of "done." A change that touches structure or behaviour without a
matching docs edit is incomplete. If you discover the docs are already stale, fix them as part of
your task and note it.

## 3. Firm product constraints (do not violate without explicit instruction)

- **Offline-only.** No online / multiplayer / Quick Match. `GameMode` is `PASS_AND_PLAY` and
  `VS_AI` only.
- **No progression system.** No levels, XP, or achievements — keep only the **win-streak** stat.

## 4. Conventions

- The Gradle root is **`TicTacToe/`**, not the repo root. Run `./gradlew` from there.
- Keep the **domain layer pure** (no Android imports) and unit-tested.
- MVVM: ViewModels expose `StateFlow<UiState>`; Composables stay stateless. New singletons are
  wired in `di/AppContainer.kt`.
- macOS build: if no JDK 17 is on `PATH`, use Android Studio's JBR as `JAVA_HOME`
  (`/Applications/Android Studio.app/Contents/jbr/Contents/Home`).
