---
title: XOXO — Release Checklist
last_updated: 2026-08-29
scope: End-to-end checklist for shipping a release build (AAB) of XOXO to Google Play — build hardening, signing, pre-flight checks, build commands, and Play Console upload. Reusable for future releases.
---

# XOXO — Release Checklist

A repeatable playbook for cutting a Play Store release of `com.skystone1000.xoxo`. Work top to
bottom. The **one-time setup** (§1–§2) is done once per project/machine; the **per-release** steps
(§3 onward) run every time you ship.

> **The Gradle root is `TicTacToe/`**, not the repo root — run every Gradle command from there.
> Artifacts are written under `TicTacToe/app/build/outputs/`. Build files are **Groovy**
> (`build.gradle`), not Kotlin DSL. A JDK 17+ is required; Android Studio's bundled JBR works:
>
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
> ```
> ```bash
> export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
> ```

**Current state:** `versionCode 2`, `versionName "2.0"` — v2.0 has **not shipped yet**, so the first
pass through this document is a *first release*: every §6 policy task applies.

---

## 1. Release build hardening — configured ✅

Configured on 2026-08-29 and verified by a real R8 run. These live in
[`app/build.gradle`](../app/build.gradle) and [`app/proguard-rules.pro`](../app/proguard-rules.pro);
verify they are intact before shipping, but they do not need re-adding each release.

| Setting | Target value | Why |
|---|---|---|
| `minifyEnabled` | `true` | R8 code shrinking **+ obfuscation** — smaller, harder to reverse |
| `shrinkResources` | `true` | Strips unused resources (requires minify on) |
| `debuggable` | `false` (default) | No debug flag in release |
| `proguardFiles` | `proguard-android-optimize.txt` + `proguard-rules.pro` | Optimizing defaults + app keep rules |
| R8 full mode | on (AGP 9 default) | Maximum shrinking |
| ART baseline profile | auto (`compileReleaseArtProfile`) | Faster cold start (Compose ships profiles) |

**What is in `app/build.gradle`:**

```groovy
buildTypes {
    release {
        minifyEnabled = true
        shrinkResources = true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

**What is in `app/proguard-rules.pro`.** Compose, Coroutines, Lifecycle, Navigation, DataStore and
Room all ship their own consumer rules, so XOXO only needs crash-symbolication attributes plus its
own reflective surfaces:

```proguard
# Readable stack traces in Play Console (paired with an uploaded mapping.txt)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room entity + DAO — columns are resolved by name
-keep class com.skystone1000.xoxo.data.stats.** { *; }

# Kotlin enums (Player, GameMode, Difficulty, GameStatus): values()/valueOf() are reflective,
# and Difficulty/GameMode names are persisted in DataStore and passed as nav-route arguments
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```

**Verified after enabling** (`:app:assembleRelease`, R8 full mode):

- `minifyReleaseWithR8` + `convertShrunkResourcesToBinaryRelease` both ran; `mapping.txt` is
  produced and the app is genuinely obfuscated (`TicTacNavHostKt` methods collapse to `e`).
- The `data.stats.**` keep rule held — `MatchEntity`, `MatchDao_Impl`, `AppDatabase_Impl` and the
  rest of the package all map to themselves, so Room's name-based column resolution is safe.
- **Enum names survive.** `Difficulty`/`GameMode`/`Player`/`ThemeMode`/`BoardTheme` *classes* are
  renamed (`Difficulty -> ew`) and so are the constant *fields* (`MEDIUM -> f`) — that is expected
  and harmless. What matters is that the constant name **strings** (`EASY`, `MEDIUM`, `HARD`,
  `PASS_AND_PLAY`, `VS_AI`) are still present in `classes.dex`, so `.name` and `valueOf()` continue
  to round-trip DataStore values and Navigation route arguments.
- Fonts and every launcher-icon layer are reported reachable by the resource shrinker.
- Unsigned release APK: **1.78 MB**.

> Re-run this verification whenever you add a dependency: `./gradlew test`, then smoke-test an
> installed release build (§3). Obfuscation breaks things debug hides.

> XOXO stores `Difficulty` / `GameMode` **by name** — DataStore settings and Navigation route
> arguments — so those enum names must survive R8; hence the enum rule above. If you later add a
> library that uses reflection or name-based serialization (Gson/Moshi, `Class.forName`,
> `Resources.getIdentifier`), add matching `-keep` rules and re-test a minified build.

---

## 2. Signing — one-time setup 🔑

Release builds **must** be signed before upload. There is **no `signingConfigs` block today**, so
`bundleRelease` currently produces an **unsigned** bundle that Play will reject. Wire it once.

1. **Generate an upload keystore** (once; keep it and its passwords somewhere safe and **off**
   version control — losing it means you cannot update the app unless enrolled in Play App Signing):
   ```bash
   keytool -genkeypair -v -keystore xoxo-upload.jks -keyalg RSA -keysize 2048 \
           -validity 10000 -alias xoxo
   ```

2. **Create `TicTacToe/keystore.properties`** with real values (never committed):
   ```properties
   storeFile=C:/absolute/path/to/xoxo-upload.jks
   storePassword=…
   keyAlias=xoxo
   keyPassword=…
   ```
   Commit a `keystore.properties.template` with empty values so the next machine knows the shape.

3. **Wire it into `app/build.gradle`** — above the `android { }` block:
   ```groovy
   def keystorePropsFile = rootProject.file("keystore.properties")
   def keystoreProps = new Properties()
   if (keystorePropsFile.exists()) {
       keystoreProps.load(new FileInputStream(keystorePropsFile))
   }
   ```
   …and inside `android { }`:
   ```groovy
   signingConfigs {
       release {
           if (keystorePropsFile.exists()) {
               storeFile = file(keystoreProps['storeFile'])
               storePassword = keystoreProps['storePassword']
               keyAlias = keystoreProps['keyAlias']
               keyPassword = keystoreProps['keyPassword']
           }
       }
   }
   buildTypes {
       release {
           if (keystorePropsFile.exists()) { signingConfig = signingConfigs.release }
           // …plus the minify settings from §1
       }
   }
   ```
   Without the file the build still succeeds but is **unsigned** — the intended fallback, so CI and
   fresh clones do not break.

4. **`.gitignore` — already done ✅.** The repo-root `.gitignore` now carries `keystore.properties`,
   `*.jks` and `*.keystore`, so the keystore you generate in step 1 cannot be committed by accident.

5. **Enroll in Play App Signing** on the first upload (recommended) — Google holds the app signing
   key and you sign uploads with the upload key above. Protects you if the upload key is lost.

---

## 3. Pre-flight checklist (every release)

- [ ] **Bump the version** in [`app/build.gradle`](../app/build.gradle): increment `versionCode`
      (integer, must exceed anything previously uploaded) and set a human-readable `versionName`.
      Currently `versionCode = 2`, `versionName = "2.0"`.
- [ ] **Code is clean & committed** — on `main`, no stray debug code, no leftover `Log`/`println`,
      no test-only shortcuts (e.g. a fixed `Random` seed injected into `MinimaxAi`).
      `git status` clean.
- [ ] **Tests pass**: `./gradlew test` — covers `GameEngineTest`, `MinimaxAiTest`,
      `StatsRepositoryTest`.
- [ ] **Lint is clean** — release lint runs automatically as part of the release build via
      `lintVitalRelease`; to run it early: `./gradlew :app:lintVitalRelease`.
- [ ] **Manifest sanity** — check the **merged** release manifest, not just the source one, since
      dependencies inject entries:
      ```bash
      grep uses-permission app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml
      ```
      The only permission should be `com.skystone1000.xoxo.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`
      — an app-private signature permission AndroidX defines for its own runtime receivers. It asks
      the user for nothing and appears nowhere on the store listing. **There must be no `INTERNET`
      or other network permission**: the listing states the app "does not request the internet
      permission", so one pulled in by a new dependency would make the listing false *and* change
      the Data Safety answers. Also confirm `android:exported="true"` on `MainActivity` (launcher —
      correct), the XOXO `@mipmap/ic_launcher`, and `@string/app_name`.
- [x] **`allowBackup` decision made** — **decided 2026-08-29: keep `true`**, so stats and settings
      survive a device migration or reinstall. The streak is the app's main return hook, and the
      data is tiny and non-sensitive. It changes no Console answer — OS-level backup is not app
      data collection, so Data Safety stays "No data collected". Recorded in
      [`PLAY-CONSOLE-TEXT.md`](../../Assets/play-store/05-listing-text/PLAY-CONSOLE-TEXT.md) §8.
      Re-open only if the app ever stores something that should not leave the device.
- [ ] **DB migration present** for any schema change — `AppDatabase` is at `version = 1` with
      `exportSchema = false` and **no** `fallbackToDestructiveMigration`, so a changed
      `MatchEntity` shipped without a `Migration` **crashes on launch for existing users**. If you
      touch the schema: bump `version`, add the `Migration`, and switch `exportSchema = true` (plus
      the KSP `room.schemaLocation` argument) so future migrations are diffable.
- [ ] **Smoke-test a minified build on a device** — obfuscation surfaces issues debug hides:
      ```bash
      ./gradlew :app:installRelease
      ```
      **Clear app data first** so first-launch state is real, then exercise the risky paths:
      - fresh install → Splash → **Onboarding** (3 pages, Skip, Get started) → Home; relaunch and
        confirm onboarding does **not** reappear (`hasSeenOnboarding` in DataStore)
      - **Pass & Play**: a full round — win, draw, Restart (keeps session score), Quit
      - **Vs AI** at Easy / Medium / **Hard** — Hard must remain unbeatable (draw or lose only);
        pick **O** and confirm the AI opens automatically
      - Result overlay — confetti on a win, session score, Play Again, Home
      - Round timer ticks and resets per round; the winning line highlights
      - **Stats** — win rate, current streak, W/L/D counters and the last-7-days chart all update
        after a match (each finished round recorded exactly once)
      - **Settings** — name change reflects on Home, theme (light/dark/system), haptics toggle
      - **Persistence** — force-stop and relaunch: stats and settings survive
      - Rotation, and back-navigation out of every screen

---

## 4. Build the release bundle (AAB)

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
cd TicTacToe
.\gradlew.bat clean :app:bundleRelease
```

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
cd TicTacToe
./gradlew clean :app:bundleRelease
```

Outputs:
- **Bundle:** `app/build/outputs/bundle/release/app-release.aab` ← upload this to Play
- **Mapping:** `app/build/outputs/mapping/release/mapping.txt` ← upload for crash de-obfuscation
  (only produced once `minifyEnabled = true`, §1)
- The baseline profile is bundled automatically.

> Play requires an **AAB**, not an APK. Google generates per-device APKs from it, so users download
> far less than the full bundle. For a sideloadable APK: `./gradlew :app:assembleRelease`.

---

## 5. Post-build verification

- [ ] The bundle is **signed** (built with `keystore.properties` present) — an unsigned AAB is
      rejected by Play. The file is `app-release.aab`, not `-unsigned`. Verify:
      `jarsigner -verify -verbose app/build/outputs/bundle/release/app-release.aab`
- [ ] `mapping.txt` exists — if it is missing, `minifyEnabled` is still `false` (§1).
- [ ] Size is reasonable — XOXO is a single-module offline app that ships **no bitmap game art**
      (icons are vectors), so expect a few MB; the per-device download is smaller. A sudden jump
      means a dependency crept in.
- [ ] Optional: inspect device-specific APKs with **bundletool**
      (`bundletool build-apks --bundle=app-release.aab --output=out.apks`).

---

## 6. Play Console — upload & store listing

**Upload**
- [ ] Create/select the app in Play Console → **Internal testing** first, then promote to
      **Production**.
- [ ] Upload `app-release.aab`.
- [ ] Upload `mapping.txt` (App bundle explorer, or automatically with the bundle).
- [ ] **Expected warning — "contains native code, and you've not uploaded debug symbols."**
      Informational, not a blocker; publish through it. XOXO has no native code of its own. Two
      AndroidX dependencies ship prebuilt `.so` files: `libandroidx.graphics.path.so`
      (`androidx.graphics:graphics-path`, pulled in by `compose-ui-graphics`) and
      `libdatastore_shared_counter.so` (`androidx.datastore`). `release.ndk.debugSymbolLevel` is
      already set to `SYMBOL_TABLE` in `app/build.gradle`, but it only takes effect once an **NDK
      is installed** — AGP needs `objcopy`, and without it `extractReleaseNativeSymbolTables`
      emits nothing and `mergeReleaseNativeDebugMetadata` reports `NO-SOURCE`. Even with the NDK,
      `graphics-path` is fully stripped (no `.symtab`), so only the DataStore library can
      contribute symbols. Kotlin/Compose crash deobfuscation does not depend on any of this — the
      R8 mapping is already inside the bundle at
      `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`.
- [ ] Set the release name and paste the release notes — both are written out in
      [`PLAY-CONSOLE-TEXT.md`](../../Assets/play-store/05-listing-text/PLAY-CONSOLE-TEXT.md) §9.

**Store listing assets — already built.** Everything lives in
[`Assets/play-store/`](../../Assets/play-store/README.md), with folders numbered in Console upload
order. Do not regenerate them; upload what is there.

| # | Source | Console location | Spec |
|---|---|---|---|
| 1 | `01-app-icon/play-icon-512.png` | Store listing → App icon | 512×512, 32-bit PNG w/ alpha, ≤1 MB |
| 2 | `02-feature-graphic/feature-graphic-1024x500.jpg` | Store listing → Feature graphic | 1024×500, no alpha |
| 3 | `03-phone-screenshots/` — all, in filename order | Store listing → Phone screenshots | 2–8, 16:9–9:16, 320–3840 px/side |
| 3 | `03-tablet-screenshots/` — `01`–`08` in one slot, `09`–`11` in the other | Store listing → 7-inch / 10-inch tablet screenshots | max 8 per slot, ratio ≤ 2:1, 320–3840 px/side |
| 4 | `04-promo-video/` | Store listing → Video | YouTube URL — optional, must be shot first |
| 5 | `05-listing-text/PLAY-CONSOLE-TEXT.md` | Listing text, Store settings, App content | Every field, in Console order |

`_reference/` is never uploaded. **Tablet screenshots are produced** — 11 captures in
`03-tablet-screenshots/`, dark `01`–`08` and light `09`–`11`. Play caps each tablet slot at 8, so
split them across the 7" and 10" slots as the table says.

**Store listing text** — pre-written in `05-listing-text/PLAY-CONSOLE-TEXT.md` and already within
limits: app name ≤ 30 chars · short description ≤ 80 · full description ≤ 4000.

**Two values must be set before submitting:**
- [ ] **Privacy policy URL** — required; the app cannot be submitted without it.
      `05-listing-text/XOXO PRIVACY-POLICY.md` is complete (it names the publisher and a contact
      address, as the Play User Data policy and GDPR Art. 13 require). Host it — GitHub Pages
      works — and paste the URL. Note the filename contains a space, so a GitHub Pages URL will
      encode it as `%20`; rename the file if you want a cleaner URL.
- [ ] **Public contact email** — the listing field; use the same address the policy gives.
- [ ] **Promo video URL** — optional; leave blank if unshot.

**Play policy tasks** (first release, or whenever the answer changes):
- [ ] **Data Safety** — XOXO collects and transmits **nothing**: no permissions, no network, no
      account. Answer accordingly, and re-check it if a dependency ever adds a permission.
- [ ] **Content rating** questionnaire — trivial for an offline board game; expect Everyone / PEGI 3.
- [ ] **Target audience & content** — declare whether children are a target audience. Either
      answer is consistent with the privacy policy, which states the app collects nothing from
      anyone including children; if you do include children's age bands, the **Play Families
      policy** applies (no ads, no data collection, policy linked in the Families section — all
      already true).
- [ ] **Ads declaration** — no ads.
- [ ] **App access** — no login; all functionality is available without restrictions.
- [ ] **Government apps / financial features / health** — all "no".
- [ ] **App category** — Games → Board (or Puzzle). Set countries and pricing (Free).

---

## 7. Ship

- [ ] Roll out — Internal testing → Production promotion, or a **staged rollout** percentage.
- [ ] Tag the release in git: `git tag v2.0 && git push --tags`.
- [ ] Once live, install from Play and watch **Android vitals** — crash rate and ANRs — for the
      first few days. `minSdk 24` means the app runs on old devices you almost certainly have not
      tested on.
- [ ] Update the root `README.md` and this file's `last_updated` if the process itself changed.

---

## Quick reference

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
cd TicTacToe
.\gradlew.bat test                       # unit tests
.\gradlew.bat :app:lintVitalRelease      # release lint
.\gradlew.bat :app:installRelease        # signed release build, on a device
.\gradlew.bat clean :app:bundleRelease   # signed AAB -> app/build/outputs/bundle/release/
```

## First-release blockers, at a glance

| # | Blocker | Fix in | Status |
|---|---|---|---|
| 1 | `minifyEnabled` / `shrinkResources` off — no obfuscation, no `mapping.txt` | §1 | ✅ done |
| 2 | Keep rules unwritten — enums are persisted and routed **by name** | §1 | ✅ done |
| 3 | No `signingConfigs` → the AAB is unsigned and Play rejects it | §2 | ⬜ open |
| 4 | `keystore.properties` / `*.jks` not gitignored | §2 | ✅ done |
| 5 | Privacy policy — placeholders unfilled, not hosted, no URL | §6 | ⬜ open |
| 6 | Public contact email / developer name not chosen | §6 | ⬜ open |
| 7 | `allowBackup` decision | §3 | ✅ decided — keep `true` |

**Pre-flight run of 2026-08-29** (§3): `./gradlew test` passed (`GameEngineTest`, `MinimaxAiTest`,
`StatsRepositoryTest`); `:app:lintVitalRelease` passed; merged manifest carries no network
permission; no stray `Log`/`println`/`TODO` in `app/src/main`; no Room schema change, so no
migration needed; `allowBackup` decided (keep `true`). Still outstanding for a human: signing (§2)
and the on-device smoke test of a minified build (§3), which needs a signed build to install.
