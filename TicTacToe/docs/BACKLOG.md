# Backlog

Deferred work that is understood and scoped but deliberately not done yet. Each item says **why**
it was deferred and **what** it takes to finish, so picking it up needs no archaeology.

---

## 1. Sound effects — restore the Settings toggle and add audio playback

**Status:** deferred · **Deferred on:** 2026-08-29 · **Size:** small (~half a day + assets)

### Why it is deferred

`AppSettings.soundEnabled` has been persisted since the Compose rewrite, and Settings shipped a
"Sound effects" switch for it — but **nothing ever read the flag**. There is no audio playback code
anywhere in the app, and `app/src/main/res/raw/` is empty. The switch did nothing at all.

Implementing it properly needs short, correctly licensed audio files that do not exist in the repo,
and sourcing them was out of scope for the adaptive-UI bug-fix pass. A switch that visibly does
nothing is worse than no switch, so **the row was removed from the Settings UI** while everything
behind it was kept intact.

### What is still in place (nothing to re-add)

| Piece | Location | State |
| ----- | -------- | ----- |
| `soundEnabled` field | `data/settings/AppSettings.kt` | kept, defaults to `true` |
| `setSound(enabled)` | `data/settings/SettingsRepository.kt` | kept |
| `SettingsViewModel.setSound` | `ui/screens/settings/SettingsViewModel.kt` | kept |
| The `ToggleRow` | `ui/screens/settings/SettingsScreen.kt` | **removed** (a comment marks the spot) |

### What it takes to finish

1. **Add three short assets** to `app/src/main/res/raw/` — suggested: `tap.ogg` (tile placed,
   ~80 ms), `win.ogg` (round won, ~700 ms), `lose.ogg` (round lost/draw, ~500 ms). OGG keeps the
   APK small. Confirm the licence allows redistribution and record it in
   `Assets/play-store/_reference/`.
2. **Add `ui/audio/SoundEffects.kt`** — a small `SoundPool`-backed player created once in
   `di/AppContainer.kt`. Use `AudioAttributes.USAGE_GAME` / `CONTENT_TYPE_SONIFICATION`, preload
   the three clips, and expose `play(Sound)` that no-ops when the setting is off. Release the pool
   with the container.
3. **Consume the setting** the same way haptics already are: `GameViewModel` collects
   `settingsRepository.settings` into `GameUiState`, so add `soundEnabled` next to
   `hapticsEnabled` and gate playback on it in `GameScreen` (tile tap) and in the result overlay
   (win/lose).
4. **Restore the Settings row** — one line, at the marked spot in `SettingsScreen.kt`:
   `ToggleRow(Icons.AutoMirrored.Rounded.VolumeUp, "Sound effects", settings.soundEnabled, onSound)`
   plus a `Divider()`, re-add the `onSound` parameter, and re-wire `onSound = vm::setSound` in
   `ui/navigation/TicTacNavHost.kt`.
5. **Update the docs** — `FEATURES.md` (Settings screen), `CODEBASE.md` (new `ui/audio/` package),
   `ARCHITECTURE.md` (new singleton in `AppContainer`), per `CLAUDE.md` §2.

### Notes

- Respect the ringer/mute state; do not play through the alarm or notification stream.
- `shrinkResources` is on for release builds, so the raw files are only kept once something
  references `R.raw.*` — the same trap that currently strips `ic_stat_xoxo` (see `CODEBASE.md`).

---

## 2. Launch flash when the app theme disagrees with the system theme

**Status:** accepted, low priority · **Logged on:** 2026-08-29 · **Size:** small, but invasive

`res/values-night/themes.xml` swaps `android:windowBackground` on the **system** dark-mode setting,
while the app's own theme follows `AppSettings.themeMode`. If the user forces Dark while the system
is Light (or vice versa), every cold start and configuration change shows one light `#F4F4F8` frame
before Compose paints.

Fixing it properly means knowing the persisted theme before the first frame, which would require a
blocking DataStore read on the main thread at startup — a worse trade than a single-frame flash.
Options if it ever becomes worth doing: mirror the choice into a tiny `SharedPreferences` value
read synchronously in `MainActivity.onCreate` and set the window background from it, or keep a
splash background that is theme-neutral.

The three-way **Appearance** control (System / Light / Dark) added in the adaptive-UI pass makes the
mismatch rarer, since "System" is now reachable again and stays the default.
