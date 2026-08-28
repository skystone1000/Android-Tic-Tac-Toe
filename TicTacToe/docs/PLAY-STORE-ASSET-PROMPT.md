# Google Play Launch Asset Kit — XOXO

> **What this is.** A ready-to-run prompt for generating XOXO's complete Play Store asset kit.
> Every `[BRACKET]` from the generic template has already been filled in from the real codebase,
> so it can be pasted into a fresh chat as-is. Nothing here needs editing before use.
>
> **How to use it:** copy everything below the first horizontal rule into a new chat, attach the
> eight screenshots listed in *Attachments*, and run it. Pair with
> [FEATURES.md](FEATURES.md) (what the app does) and [ARCHITECTURE.md](ARCHITECTURE.md) (how it
> is built) if the generator asks for detail this brief doesn't cover.
>
> **Keep this in sync.** If a feature, colour, font, screen or product constraint changes, update
> the *Ground truth* section below in the same change — a stale brief here produces store assets
> that misrepresent the app, which is a Play policy problem, not just a docs problem.

---

## Role

You are a brand and store-listing designer. Build me the complete, policy-compliant asset kit
needed to publish an Android app on Google Play, plus YouTube launch thumbnails. Work from the
Google Play "Graphic assets, screenshots, and video" requirements and the Play Store listing
metadata policy. Export every file at its exact required pixel size.

## My app

- **App name:** XOXO
- **Package / bundle id:** `com.skystone1000.xoxo`
- **Version at launch:** 2.0 (versionCode 2)
- **One-line pitch:** A polished, fully offline tic-tac-toe game — pass and play with a friend on
  one phone, or take on a minimax AI with three difficulty levels.
- **Category:** Games › Board (casual, single- and two-player)
- **Primary audience:** Casual players of any age who want a quick, clean, no-account game that
  works on a plane, on the subway, in a waiting room — anywhere with no signal. Secondary: people
  who want a genuinely unbeatable opponent to practise against.
- **Key features to sell, in priority order:**
  1. **Vs AI with three difficulties** — Hard is optimal minimax and literally unbeatable; you can
     only draw or lose. Medium and Easy mix in random moves so they stay winnable.
  2. **Pass & Play** — two people, one device, alternating taps. No account, no pairing, no lobby.
  3. **Stats that persist** — win rate, current win streak, wins/losses/draws, a last-7-days
     activity chart, and a list of recent matches with mode and result.
  4. **Completely offline** — no network permission in the manifest at all. Nothing is uploaded,
     nothing is tracked, no sign-in.
  5. **Made to look at** — a custom design system: full light and dark themes, three board themes
     (Classic / Midnight / Aurora), variable-font typography, animated tiles, a highlighted
     winning line, and confetti on a win.
  6. **Small comforts** — haptics and sound toggles, a default-difficulty preference, an editable
     display name, and a round timer.
- **Tone:** Confident and calm. Understated, not shouty. This is a small game done carefully — the
  assets should read as "considered", not "arcade". No exclamation marks, no hype adjectives.
- **Platforms to cover:** Phone only. See *Tablet* below for why, and what would change that.
- **Minimum SDK:** 24 (Android 7.0) — themed icons target Android 13+, so the monochrome layer is
  additive, not a replacement.

## Attachments

Eight real device screenshots, captured on a 1080 × 2400 emulator (20:9), status bar already
cleaned via SysUI demo mode — fixed clock, full battery and wifi, no carrier name, no
notifications. Display name is set to a real-looking value and the stats are populated with a
plausible history (24 matches, 15W/6L/3D, 62% win rate, 7-win streak).

| File                    | Screen                                              | Theme |
| ----------------------- | --------------------------------------------------- | ----- |
| `01 Stats Dark.png`     | Stats — win rate, streak, W/L/D, 7-day chart, recent | Dark  |
| `02 Home Dark.png`      | Home — greeting, streak banner, both mode cards     | Dark  |
| `03 Profile Dark.png`   | Profile — avatar, name, Games / Wins / Win %        | Dark  |
| `04 Game Dark.png`      | Game — Pass & Play mid-round, turn pill, timer      | Dark  |
| `05 Win Dark.png`       | Game — win result overlay with confetti             | Dark  |
| `06 Settings Light.png` | Settings — toggles, difficulty, board themes        | Light |
| `07 Home Light.png`     | Home — same as 02 in the light palette              | Light |
| `08 Game Light.png`     | Game — Vs AI · Hard mid-round                       | Light |

**Two things to fix in retouch, not to reproduce:**

1. A small circular screen-recording indicator sits immediately right of the clock in every
   screenshot. Mask it out — it is an emulator artifact, not app UI.
2. The device uses three-button navigation, so a back / home / recents row sits at the bottom of
   every capture. Crop it off; do not composite it into the panels.

**No logo exists.** The app currently ships the unmodified Android Studio default launcher icon —
the green droid on a `#3DDC84` grid. Treat that as a placeholder with no brand value and design a
new XOXO mark from scratch. Do not derive anything from it, and do not reuse green as a brand
colour.

---

# Ground truth — do not contradict any of this

These are verified facts about the shipped app. Assets that imply otherwise would misrepresent it
and put the listing at risk.

**The app does NOT have, and must never be depicted as having:**

- Online play, multiplayer over a network, matchmaking, "Quick Match", lobbies, invites, or
  friends lists. `GameMode` has exactly two values: `PASS_AND_PLAY` and `VS_AI`. This is a firm
  product decision, not a missing feature.
- Levels, XP, ranks, tiers, badges, achievements, unlockables, daily rewards, or a leaderboard.
  The **win streak is the only progression-flavoured number in the app**, and it is a plain count
  of consecutive wins.
- Accounts, sign-in, profiles-in-the-cloud, sync, or a social graph. The "Profile" screen is a
  local display name and three summary numbers.
- In-app purchases, ads, currency, energy, timers-that-block-play, or any monetisation.
- Chat, emoji reactions, avatars beyond a single-letter initial tile, or user-generated content.

**Exact brand values already in the code** — use these, do not re-derive or "improve" them:

| Role                 | Light       | Dark        |
| -------------------- | ----------- | ----------- |
| Primary (indigo)     | `#4338CA`   | `#A5B4FC`   |
| Primary container    | `#EEEDFB`   | `#2A2747`   |
| Player X (teal)      | `#14B8A6`   | `#2DD4BF`   |
| Player X soft fill   | `#E2F7F4`   | `#123833`   |
| Player O (orange)    | `#F97316`   | `#FB923C`   |
| Player O soft fill   | `#FEEEE2`   | `#3A2415`   |
| Background           | `#F4F4F8`   | `#0E0E14`   |
| Card surface         | `#FFFFFF`   | `#23232E`   |
| Ink (primary text)   | `#16162A`   | `#ECECF3`   |
| Ink muted            | `#75758A`   | `#9A9AAE`   |
| Outline              | `#EAEAF2`   | `#33333F`   |

**Measured contrast — use this to pick the emphasis colour, don't eyeball it.** Every ratio below
is computed against the real hex values, not estimated:

| Pair                                              | Ratio     | Verdict                          |
| ------------------------------------------------- | --------- | -------------------------------- |
| Indigo `#4338CA` on light bg `#F4F4F8`            | 7.20:1    | Safe for text of any size        |
| Indigo `#4338CA` on white card `#FFFFFF`          | 7.90:1    | Safe                             |
| Light indigo `#A5B4FC` on dark bg `#0E0E14`       | 9.65:1    | Safe — this is the dark-mode primary |
| Deep indigo `#4338CA` on dark bg `#0E0E14`        | **2.43:1**| **Fails. Never do this.**        |
| Teal `#14B8A6` on light bg `#F4F4F8`              | **2.27:1**| **Fails as text.**               |
| Orange `#F97316` on light bg `#F4F4F8`            | **2.56:1**| **Fails as text.**               |
| Teal `#2DD4BF` on dark bg `#0E0E14`               | 10.33:1   | Safe                             |
| Orange `#FB923C` on dark bg `#0E0E14`             | 8.50:1    | Safe                             |
| Ink `#16162A` on light / `#ECECF3` on dark        | 16.2:1 / 16.4:1 | Safe                       |

The trap: on a **light** panel, the teal and orange are decorative glyph colours only — they are
how the app paints large X and O marks on their own soft tints, not text colours. If a headline
word needs teal or orange emphasis on a light background, use the app's own readable variants —
`#0F766E` for teal (4.91:1) and `#C2410C` for orange (4.57:1). On **dark** panels the bright teal
and orange are both safe as text. Default the emphasis word to indigo unless the panel is
specifically about X or O.

- **Type pairing (already shipped, already avoids Inter/Roboto/Arial):** **Space Grotesk** for
  display, headlines, scores and numerals; **Hanken Grotesk** for body, labels and UI. Both are
  variable fonts and both are in the repo at `TicTacToe/app/src/main/res/font/`. Use them in the
  store assets so the panels and the app read as one product.
- **The letter marks:** X is always teal, O is always orange. That pairing is the strongest visual
  asset the app has — build the icon and the motif from it.
- **Board themes:** Classic, Midnight, Aurora (names are user-facing; don't rename them).
- **Difficulty names:** Easy, Medium, Hard (user-facing; don't rename or add a fourth).

**Data safety facts** — state these, don't research or invent them:

- The manifest declares **zero permissions**. There is no `INTERNET` permission, so the app is
  incapable of network access.
- All persistence is on-device: **Room** (`tictac.db`, one `matches` table — mode, result,
  difficulty, timestamp) and **DataStore Preferences** (sound, haptics, theme, default difficulty,
  board theme, display name, onboarding flag).
- No analytics SDK, no crash reporter, no ad network, no third-party SDK of any kind.
- The display name is typed by the user, stored locally, and never transmitted.
- Nothing is collected, nothing is shared, nothing leaves the device. Data deletion = uninstall,
  or clearing app storage.
- Content rating: suitable for **Everyone** — no violence, no gambling mechanics, no chat, no
  UGC, no purchases.

---

## Brand direction

The in-app palette and type pairing above are **locked** — inherit them exactly. What does not yet
exist, and what you must design and commit to:

- **A logo / wordmark for XOXO.** There is none. The obvious well is the X/O pairing and the 3×3
  grid; find something better than a literal screenshot of a board. It has to survive being
  rendered at 48 px.
- **One repeatable background motif** for the asset family — a dot grid, a thin-outlined grid, a
  soft radial, offset X and O glyphs at low opacity. Pick one and use it across every asset so the
  kit reads as a family.
- **A tagline, ≤ 5 words**, no hype, no call to action, and **it must not imply online play or
  progression**. "Play with friends anywhere" is wrong — it reads as online. Something in the
  register of "Three in a row. Offline." is right.

Check the indigo against both surfaces before you commit: `#4338CA` clears 4.5:1 on the light
background, and the dark theme deliberately switches to the lighter `#A5B4FC` because the deep
indigo does not clear on `#0E0E14`. Respect that inversion in every dark asset — do not paint
`#4338CA` text on near-black.

State the locked palette, type scale, motif and tagline in a few lines before building anything.
Don't ask me to approve — commit, and I'll redirect if it's wrong.

---

# Deliverables

## 1. App icon — for the codebase

Adaptive icon, built as separate layers on a **108 × 108 dp** canvas where only the centre
**66 dp** is guaranteed visible (outer 18 dp is masked by the launcher shape) and the safe zone
for the logo is a **~60 dp centred circle**. Keep the mark inside that circle: a shape that fills
the frame will be clipped on circular, squircle and rounded-square launchers.

Export:

- `ic_launcher_foreground.png` — logo only, transparent background, 432 × 432 px
- `ic_launcher_background.png` — flat brand colour or subtle motif, 432 × 432 px, opaque
- `ic_launcher_monochrome.png` — single-colour silhouette on transparent, 432 × 432 px
  (Android 13+ themed icons)
- Legacy square/round PNGs at every density, opaque, no alpha padding:
  mdpi 48, hdpi 72, xhdpi 96, xxhdpi 144, xxxhdpi 192
- **Play Store icon:** 512 × 512 px, 32-bit PNG **with alpha**, ≤ 1 MB, no rounded corners or drop
  shadow baked in — Play applies the mask and shadow itself

**Integration notes specific to this repo** — the res root is
`TicTacToe/app/src/main/res/`, and the current state is:

- `drawable/ic_launcher_foreground.xml` and `drawable/ic_launcher_background.xml` are the stock
  Android Studio **vector** droid and green grid. Both must be replaced. If your mark is
  geometric enough to stay vector, prefer replacing them with new `<vector>` XML over shipping
  PNGs — the rest of the app draws everything in Compose and carries no bitmap assets, so a vector
  icon keeps that property.
- `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` currently declare only
  `<background>` and `<foreground>`. **Add the `<monochrome>` line** — themed icons are missing
  today.
- `mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.png` and `ic_launcher_round.png` exist and must be
  regenerated at the sizes above.
- The manifest already points at `@mipmap/ic_launcher` / `@mipmap/ic_launcher_round`, so no
  manifest change is needed.

Give me the finished `ic_launcher.xml` / `ic_launcher_round.xml`, any new vector drawables, and
the folder tree showing exactly which existing files are overwritten.

Rules: no text in the icon beyond a single letter mark, no screenshots inside the icon, no
"new"/"free"/"sale" badges, no Google Play iconography, no other platform's badges.

## 2. Feature graphic

- **1024 × 500 px**, PNG or JPEG, **no alpha**, ≤ 15 MB
- Assume it may be cropped for different surfaces and may have a **play button overlaid dead
  centre** when a promo video is attached — keep the centre clear of text and logo
- Keep all essential content inside a centred safe area of roughly **924 × 400 px**
- Must be legible as a thumbnail: logo + tagline only, ≤ 6 words of text
- No device frames, no screenshots pasted in small, no call to action, no store badges, no price,
  no ranking or award claims

## 3. Phone screenshots — styled panels

Built from the eight attached captures:

- **1080 × 1920 px** (9:16), JPEG or 24-bit PNG **without alpha**, each ≤ 8 MB
- Deliver **8 panels**; Play shows the first 3 in search, so the first three must carry the
  strongest features
- Structure of each panel:
  - a **top title band** of fixed height ≈ **318 px (≤ 20% of the image)** containing one headline
    (≤ 6 words, one brand-colour word for emphasis) and one supporting line (≤ 10 words), both
    centred
  - below it, the **app UI at large scale** — corner-rounded, soft shadow, no phone bezel and no
    hand holding a device
  - the shared background: brand-tinted gradient plus the motif at low opacity, consistent across
    all 8
- **Aspect note:** the sources are 1080 × 2400 (20:9) but the panels are 9:16, and ~318 px goes to
  the title band. The UI will not fit at 1:1 — scale it down and let it bleed off the bottom edge
  of the panel rather than squashing it or letterboxing it. Never distort the aspect ratio of the
  app UI.
- Alternate light and dark panels for rhythm; lead with whichever best sells the feature.

Suggested order and angle — refine the copy, keep the priority:

| # | Source                  | Sells                          | Headline direction              |
| - | ----------------------- | ------------------------------ | ------------------------------- |
| 1 | `08 Game Light.png`     | Vs AI, Hard difficulty         | The unbeatable opponent         |
| 2 | `05 Win Dark.png`       | The payoff — win state         | Every round resolves clearly    |
| 3 | `01 Stats Dark.png`     | Persistent stats               | Your record, kept               |
| 4 | `07 Home Light.png`     | Two modes, zero setup          | Pick a mode and go              |
| 5 | `04 Game Dark.png`      | Pass & Play                    | One device, two players         |
| 6 | `06 Settings Light.png` | Themes, haptics, difficulty    | Tune it to taste                |
| 7 | `02 Home Dark.png`      | Dark theme + streak            | A real dark theme               |
| 8 | `03 Profile Dark.png`   | Local profile, no account      | No account, no sign-in          |

Content rules: no call to action, no store badges, no ranking / award / testimonial claims, no
prices or promo wording, no time-bound or seasonal content, and nothing that misrepresents the app
(re-read *Ground truth* before writing a single headline).

Also produce **`ALT-TEXT.md`** — one alt line per file, ≤ 140 characters, describing what the
screen actually shows, plus the recommended upload order.

## 4. Tablet screenshots — out of scope for this release

Skip these. XOXO has no tablet-specific layout: the Compose UI is a single phone-width column, so
tablet captures would be the phone layout stretched, which sells the app badly and invites
"designed for phone" quality flags. Ship phone-only.

If a tablet layout is added later, re-enable this section with: 7" at **1024 × 600 px** minimum
(16:9 or 16:10), 10" at **1920 × 1200 px** (16:10), minimum 4 screenshots each, every dimension
between 320 px and 3840 px, long side no more than twice the short side — re-laid out for
landscape, never letterboxed phone panels.

## 5. YouTube thumbnails

- **Landscape:** 1280 × 720 px (author at **1920 × 1080** and downscale), 16:9, ≤ 2 MB, JPEG or
  PNG. Keep text out of the bottom-right corner where the duration chip sits, and inside a centred
  safe area — it will be read at 320 px wide.
- **Shorts / vertical:** 1080 × 1920 px, 9:16. Keep the top ~180 px and bottom ~420 px clear of
  anything essential.
- Both: ≤ 5 words of headline at very large weight, one focal subject, brand colour as the loudest
  thing in frame, high contrast at thumbnail size. No fake play buttons, no fake UI chrome, no
  clickbait arrows or red circles, no faces.

## 6. Promo video (specs only, no render)

Requirements plus a 30-second shot list I can screen-record: YouTube URL only (public or unlisted,
not age-restricted), landscape 16:9, no ads on the video, the first 10 seconds must show the app in
use, and it must stand alone without sound.

Build the shot list from what the app actually does — a round against Hard that ends in a draw
because Hard cannot be beaten, the winning-line highlight and confetti on a Pass & Play win, the
stats screen filling in, a board-theme switch. No invented screens.

## 7. Everything else required

- **Splash screen.** Note the current state before designing: XOXO does **not** use the Android 12+
  `SplashScreen` API. It has a Compose splash route (`ui/screens/splash/`) with a spring-animated
  logo, and `Theme.TicTacToe` sets only `android:windowBackground` to `@color/window_background`
  (`#F4F4F8`) to avoid a launch flash. Deliver both halves: (a) the icon asset per the platform
  API — 288 × 288 dp with the logo inside the centred **192 dp** circle, or 160 dp if the icon has
  a background — plus the `windowSplashScreen*` theme values. Say clearly which files change.
  Note that `values-night/colors.xml` **already** overrides `window_background` to `#0E0E14`, so
  cold-start flash in dark mode is already handled — don't "fix" it.
- **Notification icon**: white-on-transparent silhouette, 24 × 24 dp, exported mdpi→xxxhdpi.
  (The app sends no notifications today — deliver it as a forward-looking asset and label it as
  unused, don't imply the app notifies.)
- **Monochrome / themed-icon** variant (covered in §1).
- **Logo lockups**: horizontal and stacked, on light and on dark, plus a clear-space rule.
- **Store listing copy**: app name ≤ 30 chars, short description ≤ 80 chars, full description
  ≤ 4000 chars — no emoji, no ALL CAPS, no "#1"/"best", no keyword stuffing, no competitor names,
  no store performance claims, no price or promo text. The full description must state plainly
  that the game is offline and single-device, so nobody installs it expecting online play.
- **Data safety + content rating notes**: write these from the *Ground truth* facts above — a "no
  data collected" declaration, no third-party sharing, no data-deletion endpoint required, and an
  Everyone rating. Flag anything in the console form that the facts above don't cover rather than
  guessing an answer.

---

# Output format

1. First, in chat: state the locked palette, type pairing, tagline and background motif in a few
   lines. Commit; don't ask for approval.
2. Build one browsable design file containing every asset at true pixel size, grouped by section
   with a label above each.
3. Export each asset as a real file at its exact required dimensions into `Assets/play-store/`:

```
Assets/play-store/
  icon/            ic_launcher_*.png, play-icon-512.png, *.xml
  splash/
  notification/
  logo/
  play/
    feature-graphic-1024x500.jpg
    screenshots/      01..08 .jpg + ALT-TEXT.md
  youtube/
    landscape-1920x1080.jpg
    shorts-1080x1920.jpg
  STORE-COPY.md
  DATA-SAFETY.md
  README.md          what each file is, where it goes, upload order
```

> Note the path: the repo already has a top-level `Assets/` holding GIMP sources
> (`x.xcf`, `o.xcf`, `Grid.xcf`, `null.xcf`). macOS filesystems are case-insensitive by default, so
> a lowercase `assets/` would collide with it — nest under `Assets/play-store/` instead.
> The `.xcf` files are legacy art from the pre-Compose version; ignore them, they are not brand.

4. Verify every export's real pixel dimensions and alpha channel against the spec above and tell me
   any that failed, rather than assuming they're right.
5. Zip `Assets/play-store/` and hand it to me as one download.

# Hard rules

- Exact pixel sizes, never "close enough", never an upscale of a smaller render
- No alpha on the feature graphic or screenshots; alpha only on the 512 icon, the icon
  foreground/monochrome layers and the notification icon
- Never invent testimonials, ratings, download counts, awards or press quotes
- Never use Google Play's or another store's branding, badges or iconography
- No emoji anywhere — the app's UI uses none
- No AI-slop styling: no heavy multi-hue gradients, no rounded box with a coloured left border, no
  meaningless stat chips, no decorative icon soup
- **Never depict or imply online play, multiplayer, matchmaking, friends, accounts or sync**
- **Never depict or imply levels, XP, ranks, achievements, unlockables or leaderboards** — the win
  streak is the only progression number that exists
- Keep X teal and O orange everywhere; never recolour the marks for composition's sake
- If a required input is genuinely missing, use a clearly-marked placeholder and list it — don't
  fabricate a screen the app doesn't have
