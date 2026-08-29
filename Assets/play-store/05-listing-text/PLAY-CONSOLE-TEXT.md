# Play Console — every text field for XOXO

One file, every value you have to type or choose in the Console. Copy the fenced blocks as-is;
character counts are measured against Play's limits. Ordered to match the Console's own flow.

- **Package:** `com.skystone1000.xoxo`
- **Version at launch:** 2.0 (versionCode 2)
- **App or game:** Game
- **Free or paid:** Free

> **Backup behaviour is decided — see [§8](#8-backup-behaviour--decided-).**

---

## 1. Store listing — main

### App name — 26 / 30

```
XOXO — Tic Tac Toe Offline
```

### Short description — 76 / 80

```
Tic tac toe with an unbeatable AI and pass-and-play. No account, no network.
```

### Full description — 1,918 / 4,000

```
XOXO is tic tac toe, made carefully.

Play it on a plane, on the subway, in a waiting room. The game runs entirely on your
phone — it does not request the internet permission, so it cannot connect even if it
wanted to. There is no account, no sign-in and nothing to set up. Open it and play.

TWO WAYS TO PLAY

Vs AI — three difficulties. Easy and Medium mix in random moves, so they stay winnable.
Hard plays optimally: it will never make a mistake, which means the best you can do is
force a draw. If you want an opponent that does not go easy on you, that is the one.

Pass & Play — two people, one device, alternating taps. No pairing, no lobby, no invites.
Hand the phone over and take your turn.

YOUR RECORD, KEPT ON YOUR PHONE

Every finished round is saved locally: win rate, current win streak, wins, losses and
draws, a chart of the last seven days, and a list of recent matches with the mode and
result. The streak is just a count of consecutive wins — there are no levels, no XP, no
ranks and no leaderboards to grind.

MADE TO LOOK AT

Full light and dark themes, both drawn properly rather than one dimmed version of the
other. Three board themes: Classic, Midnight and Aurora. Tiles animate as they land, the
winning line is highlighted when a round ends, and a win gets confetti.

SMALL COMFORTS

Sound and haptics toggles. A default difficulty you set once. An editable display name.
A round timer, so you can see how long a game actually took.

PRIVACY

Nothing is collected and nothing is shared. The app declares zero permissions. Your match
history, settings and display name are stored on the device using Room and DataStore. The
app never transmits them — with no internet permission, it has no way to. There are no
ads, no in-app purchases, no analytics and no third-party SDKs of any kind. To delete your
data, clear the app's storage or uninstall it.

Single device. Offline. Three in a row.
```

**Copy rules observed:** no emoji, no ALL-CAPS inside sentences (the section headers are label
lines), no "#1"/"best", no keyword stuffing, no competitor names, no download or rating claims,
no price or promotional text. The description states plainly, twice, that the game is offline and
single-device so nobody installs it expecting online play.

---

## 2. Store settings

| Field | Value |
| ----- | ----- |
| App type | Game |
| Category | **Board** |
| Tags (up to 5, chosen from Play's fixed list) | Board · Casual · Puzzle · Single player · Offline |
| Store listing contact — email | **Required and shown publicly on the listing.** Use an address you are willing to publish; a dedicated alias is safer than a personal inbox. |
| Store listing contact — phone | Optional. Leave blank. |
| Store listing contact — website | Optional. The GitHub repo works, or leave blank. |
| External marketing | Opt out unless you want Google to promote the app outside Play. |

Tags are picked from a fixed Console list, so the exact wording may differ slightly — choose the
nearest matches. Do not add tags for things the app does not do (no "Multiplayer", no "Online").

---

## 3. App content — privacy policy

**A privacy policy URL is required for every app on Play, including one that collects nothing.**
There is no exemption for zero-collection apps.

`PRIVACY-POLICY.md`, next to this file, is ready to publish. It must be reachable at a public,
non-expiring URL before you submit. The cheapest route: enable GitHub Pages on this repo and
paste the resulting URL into the Console.

```
Privacy policy URL:  [paste your hosted URL here before submitting]
```

---

## 4. App content — data safety

**Does your app collect or share any of the required user data types?** → **No.**

That one answer closes the form. Supporting facts, verified against the shipped code:

- The manifest declares **zero permissions**. With no `INTERNET` permission the app is technically
  incapable of network access — it cannot transmit anything.
- All persistence is on-device: **Room** (`tictac.db`, one `matches` table — mode, result,
  difficulty, timestamp) and **DataStore Preferences** (sound, haptics, theme, default difficulty,
  board theme, display name, onboarding flag).
- The display name is typed by the user, stored locally, never transmitted.
- No analytics SDK, no crash reporter, no ad network, no third-party SDK of any kind.
- No account system, no sign-in, no cloud sync, no server backup.

| Console question | Answer |
| ---------------- | ------ |
| Data collected | None |
| Data shared with third parties | None |
| Data encrypted in transit | N/A — nothing is transmitted |
| Users can request data deletion | N/A — nothing is collected. Local data goes when app storage is cleared or the app is uninstalled |
| Independent security review | Not claimed |

---

## 5. App content — remaining declarations

| Declaration | Answer |
| ----------- | ------ |
| App access | All functionality is available without restrictions. No login, no gated content. |
| Ads | **No**, the app does not contain ads. |
| Advertising ID | **No** — the app does not use the Advertising ID. (Must be declared; targetSdk is 37.) |
| Target audience | All ages. **Choosing an under-13 age group opts the app into the Families programme**, which adds its own policy requirements — pick that deliberately, not by accident. |
| News app | No |
| COVID-19 contact tracing / status | No |
| Government app | No |
| Financial features | None |
| Health apps | No |
| Data deletion request URL | Not applicable — no account, no collected data |
| Play Families Policy commitment | Only if you opt into a Families programme |

---

## 6. Content rating (IARC questionnaire)

Target rating: **Everyone** / PEGI 3.

| Question | Answer |
| -------- | ------ |
| Violence, blood, or realistic violence | None |
| Sexuality or nudity | None |
| Profanity or crude humour | None |
| Controlled substances (drugs, alcohol, tobacco) | None |
| Gambling — simulated or real | None. No currency, no wagering, no loot mechanics |
| In-app purchases | None |
| Ads | None |
| User-generated content or sharing | None. No chat, no reactions, no UGC |
| Users can interact or communicate | **No.** Pass & Play is two people on one physical device — there is no network interaction |
| Shares user location | No |
| Allows purchase of digital goods | No |
| Miscellaneous — provocative themes | None |

---

## 7. Graphics — alt text and upload order

Play shows the **first three** screenshots in search results, so 01–03 carry the strongest
features. Upload in filename order from `03-phone-screenshots/`.

| # | File | Alt text (≤ 140 chars) |
| - | ---- | ---------------------- |
| 1 | `01-hard-ai.jpg` | Vs AI on Hard mid-round: two O marks, two X marks on the board, round timer running, your turn. |
| 2 | `02-win.jpg` | End-of-round overlay reading "Player X wins!" with the round score and confetti over the board. |
| 3 | `03-stats.jpg` | Stats screen: 62% win rate, 7-win streak, wins/losses/draws, a last-7-days chart, recent matches. |
| 4 | `04-two-modes.jpg` | Home screen in the light theme with a win-streak banner and the Pass & Play and Vs AI mode cards. |
| 5 | `05-pass-and-play.jpg` | Pass & Play round in the dark theme: both player scores, whose-turn pill, round timer, live board. |
| 6 | `06-settings.jpg` | Settings: sound and haptics toggles, dark-mode switch, default difficulty, three board themes. |
| 7 | `07-dark-theme.jpg` | Home screen in the dark theme with the win-streak banner and both game-mode cards. |
| 8 | `08-no-account.jpg` | Profile screen with a locally stored display name and games played, wins and win-percentage tiles. |

Promo video is a **YouTube URL only** — Play does not host video files. See
`04-promo-video/PROMO-VIDEO.md` for the requirements and a 30-second shot list.

```
Promo video URL:  [paste the YouTube URL, or leave the field empty]
```

The tagline below is for graphics only. It is **not** a Console field.

```
Three in a row. Offline.
```

---

## 8. Backup behaviour — decided ✅

**`android:allowBackup` is currently `true`** — verified in
`TicTacToe/app/src/main/AndroidManifest.xml:6`. Android Auto Backup can therefore copy the match
database and preferences into the user's own Google Drive.

This does **not** change any answer above. OS-level backup is not app data collection, Play does
not require declaring it, and the Data safety answer stays "No data collected". The full
description in §1 has already been worded so it is accurate either way — it says the app never
transmits the data, which is true regardless.

**Decided 2026-08-29: ship v2.0 with `allowBackup="true"`, unchanged.** The streak is the app's
main reason to come back, so losing match history on a phone upgrade costs more than the OS-level
backup gains in purity. The data is tiny and non-sensitive, and no answer in this file changes.

| Option | Effect |
| ------ | ------ |
| **`allowBackup="true"`** — *chosen* | Stats and settings survive a device migration or reinstall-from-backup. Nothing to change; ship as-is. |
| `allowBackup="false"` — rejected | "Nothing ever leaves the device" would become literally true at the OS level too. Cost: users lose their match history and settings when they switch phones. |

If this is ever revisited, the middle path is to keep `true` but add an explicit
`dataExtractionRules` XML naming exactly what is backed up.

---

## Verified

Every character count in this file was measured, not estimated:

| Field | Limit | Actual |
| ----- | ----- | ------ |
| App name | 30 | **26** |
| Short description | 80 | **76** |
| Full description | 4,000 | **1,918** |
| Alt text, each | 140 | 82–98 |
