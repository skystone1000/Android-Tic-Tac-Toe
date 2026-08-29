# XOXO — Play Store launch asset kit

Everything needed to publish `com.skystone1000.xoxo` v2.0. **Folders are numbered in the order
you upload them in the Play Console** — work top to bottom.

All text you have to type into the Console lives in one file:
**[`05-listing-text/PLAY-CONSOLE-TEXT.md`](05-listing-text/PLAY-CONSOLE-TEXT.md)**.

## Upload order

| # | Folder | Console location | What to do |
| - | ------ | ---------------- | ---------- |
| 1 | `01-app-icon/` | Store listing → App icon | Upload `play-icon-512.png` |
| 2 | `02-feature-graphic/` | Store listing → Feature graphic | Upload `feature-graphic-1024x500.jpg` |
| 3 | `03-phone-screenshots/` | Store listing → Phone screenshots | Upload all 8, in filename order |
| 4 | `04-promo-video/` | Store listing → Video | Paste a YouTube URL. Shoot it first — `PROMO-VIDEO.md` has the specs and a 30-second shot list. The thumbnails here are for YouTube, not for Play |
| 5 | `05-listing-text/` | Store listing text, Store settings, App content | Every field, in Console order. Includes the **required** privacy policy — host `PRIVACY-POLICY.md` and paste its URL |

`_reference/` is **not uploaded anywhere**: `brand-kit/` holds the logo lockups, splash icons and
the unused PNG icon layers; `source-screenshots/` holds the retouched raw captures the panels were
built from, kept so a panel can be re-rendered.

## Before you submit

`05-listing-text/PLAY-CONSOLE-TEXT.md` §8 asked whether to leave `android:allowBackup="true"`.
**Decided 2026-08-29: keep it `true`** so stats survive a device migration. It changes no answer on
any Console form.

Two fields cannot be filled in from this repo and are left as placeholders:

- the hosted **privacy policy URL** (required — the app cannot be submitted without it)
- the public **contact email** on the listing — pick an address you are willing to publish

The **promo video** YouTube URL is optional and also unfilled.

The full release process — build hardening, signing, pre-flight and Console upload — lives in
[`TicTacToe/docs/RELEASE-CHECKLIST.md`](../../TicTacToe/docs/RELEASE-CHECKLIST.md).

## Brand summary

| | |
| --- | --- |
| **Tagline** | Three in a row. Offline. |
| **Mark** | A squircle tile holding a teal X (upper-left) and an orange O ring (lower-right) — the corner of a board, not a board. Pure geometry, no text, legible at 48 px. |
| **Motif** | Thin outlined grid lines at 5–6% opacity over a soft indigo radial. On every asset. |
| **Display type** | Space Grotesk 700 |
| **Body type** | Hanken Grotesk 400/500 |
| **Primary** | `#4338CA` light · `#A5B4FC` dark |
| **Player X** | `#14B8A6` light · `#2DD4BF` dark · `#0F766E` when teal must read as text on light |
| **Player O** | `#F97316` light · `#FB923C` dark · `#C2410C` when orange must read as text on light |
| **Surfaces** | `#F4F4F8` / `#FFFFFF` light · `#0E0E14` / `#23232E` dark |

Emphasis words default to indigo. On dark panels that means `#A5B4FC`, never `#4338CA` — the deep
indigo is 2.43:1 on `#0E0E14`.

## Already in the codebase

The launcher icon and notification icon were installed into
`TicTacToe/app/src/main/res/` and removed from this folder, so there is only one copy of each:

| Destination | Files |
| ----------- | ----- |
| `res/drawable/` | `ic_launcher_foreground.xml`, `ic_launcher_background.xml`, `ic_launcher_monochrome.xml` (vectors — the app ships no bitmap game art, so vectors were preferred over the 432 px PNG layers, which remain in `_reference/brand-kit/` unused) |
| `res/mipmap-anydpi-v26/` | `ic_launcher.xml`, `ic_launcher_round.xml` — both declare `<monochrome>`, so themed icons work on Android 13+ |
| `res/mipmap-*dpi/` | `ic_launcher.png`, `ic_launcher_round.png`, 48→192 px |
| `res/drawable-*dpi/` | `ic_stat_xoxo.png`, 24 dp. **Currently unreferenced** — the app sends no notifications; shipped ahead of that feature |

Verified by a clean `:app:assembleDebug` and an install on device.

**Known geometry note.** The mark's bounding box is **37 dp** and its centre sits at **50.0** on
the 108 dp canvas rather than 54.0 — about 4 dp up and to the left. It is safely inside the 66 dp
visible circle (worst corner 31.8, limit 33.0) so it is never clipped on any launcher mask, but it
is not optically centred. Shifting every path `+3.98` in x and y would centre it.

**Splash.** The app does not use the Android 12+ `SplashScreen` API — it has a Compose splash
route. `res/values-night/colors.xml` already overrides `window_background` to `#0E0E14`, so there
is no light flash on a cold start in dark mode. The splash icons in `_reference/brand-kit/` are
unused; adopting the platform API would mean adding `core-splashscreen` and would overlap with the
existing Compose splash, which is a product decision.

## Verification

Every file was measured after the reorganisation — real pixel dimensions, real alpha channel, real
byte size — not assumed.

| Asset | Required | Actual | Alpha |
| ----- | -------- | ------ | ----- |
| `play-icon-512.png` | 512×512, 32-bit PNG, ≤ 1 MB | 512×512, 11.6 KB | present, 100% opaque |
| `feature-graphic-1024x500.jpg` | 1024×500, no alpha, ≤ 15 MB | 1024×500, 60 KB | none (JPEG) |
| `03-phone-screenshots/*.jpg` (8) | 1080×1920, no alpha, ≤ 8 MB each | all 1080×1920, 159–200 KB | none (JPEG) |
| `youtube-landscape-1280x720-UPLOAD.jpg` | 1280×720, ≤ 2 MB | 1280×720, 122 KB | none (JPEG) |
| `youtube-shorts-1080x1920.jpg` | 1080×1920 | 1080×1920, 187 KB | none (JPEG) |
| Icon layers (432 px, `_reference`) | 432×432 | 432×432 | fg/mono transparent, bg opaque |
| Splash icons (`_reference`) | 1152×1152 | 1152×1152 | transparent |
| Logo lockups (`_reference`) | — | 1200×360 / 720×640 | opaque, baked light/dark grounds |

Text limits, measured: app name **26**/30, short description **76**/80, full description
**1,918**/4,000, alt text **82–98**/140.
