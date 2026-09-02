# Privacy Policy — XOXO

**Last updated: 29 August 2026**

This policy applies to the Android app **XOXO — Tic Tac Toe Offline**, package
`com.skystone1000.xoxo`, distributed on Google Play. It explains what the app does with your
information.

**Published by:** `Aditya Mahajan`
**Contact:** `adityaspmahajan@gmail.com`

## Short version

The app collects nothing and sends nothing. It has no internet permission, so it is technically
incapable of transmitting anything off your device. Everything it saves — your match history,
settings and display name — stays on your phone, and the developer never sees any of it.

## Information the app collects

**None.** The app does not collect, transmit, sell or share any personal or usage information.

There is no account system, no sign-in, no cloud sync and no server. The developer receives no
data about you or your use of the app, and has no means of doing so.

## Information stored on your device

The app saves the following locally, so the game works between sessions. This data stays in the
app's private storage, which Android makes readable only by the app itself:

- **Match history** — for each finished round: the game mode, the result (win, loss or draw), the
  AI difficulty if applicable, and the time it was played.
- **Settings** — sound and haptics toggles, theme preference, default AI difficulty, and board
  theme.
- **Display name** — a name you type yourself, shown only on your own device. You may enter
  anything; the app does not verify it, and it is never transmitted.
- **A flag recording that you have seen the intro screens**, so they are not shown again.

None of this is sent anywhere. It is stored using Android's standard on-device storage (a local
database and a preferences file).

## Permissions

The app requests **no permission that grants it access to your data, your device or the network.**
In particular it does **not** request the internet permission, which is why it cannot transmit
anything.

For completeness: the app's manifest does declare a single automatically-generated,
app-private permission, `com.skystone1000.xoxo.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. It is
added by Google's standard AndroidX libraries and exists to **restrict** access — it prevents other
apps on your device from reaching the app's internal message receivers. It grants the app nothing,
requires no approval from you, and gives access to no personal information.

## Third-party software

The app contains **no advertising, no in-app purchases, no analytics, no crash reporting, no
tracking and no advertising identifiers.** It does not use the Android Advertising ID.

The app is built with Google's standard AndroidX / Jetpack libraries (for example Jetpack Compose
for the interface, Room for local storage and DataStore for settings). These are code libraries
compiled into the app; they are not services, they do not collect or transmit information, and no
third party receives data from the app — the app sends data to no one.

## Device backup

If Android's system backup is enabled on your device, the operating system may include the app's
local data in your own device backup, stored in your personal Google Drive under your Google
account. This is performed by Android, not by the app; the developer has no access to it. You can
control this in your device's system backup settings.

## Google Play

Google Play distributes the app and may collect information about your download and installation
of it under **Google's own privacy policy**, independently of the app and outside the developer's
control. See <https://policies.google.com/privacy>.

## How long data is kept, and deleting it

Data stored on your device is kept until you remove it. Because nothing is collected or
transmitted, the developer holds no data about you and there is nothing to request deletion of.

To remove everything the app has stored: clear the app's storage in Android Settings → Apps →
XOXO → Storage, or uninstall the app. Uninstalling removes the local database and settings. If
system backup is enabled, you may also want to remove the app's backup from your Google account.

## Your rights (GDPR / UK GDPR)

If you are in the European Economic Area or the United Kingdom, data protection law gives you
rights over personal data that an organisation processes about you — including access,
rectification, erasure, restriction, objection and portability.

Because the app transmits nothing, **the developer does not collect, receive, store or process any
personal data about you**, and so is not a data controller of any personal data arising from your
use of the app. There is consequently no personal data held by the developer for these rights to
be exercised against, and no international transfer of personal data takes place. The data
described under *Information stored on your device* remains under your own control on your own
device, and you can erase it at any time as described above.

If you believe otherwise, you may contact the developer at the address above, and you have the
right to lodge a complaint with your national data protection authority.

## California privacy rights (CCPA / CPRA)

The app does not collect personal information as defined by the California Consumer Privacy Act.
The developer **does not sell or share** personal information, and has not sold or shared personal
information in the preceding twelve months. Because no personal information is collected, there
are no categories of personal information collected, disclosed or sold to describe, and no
financial incentives are offered. Californian residents are not discriminated against for
exercising any privacy right.

## Children

The app is suitable for all ages. It collects **no data from anyone, including children**, and
therefore does not knowingly collect personal information from children under 13 (or under 16 in
the EEA and UK). It contains no chat, no user-generated content, no advertising, no in-app
purchases and no way for users to communicate with one another. It is designed to comply with the
US Children's Online Privacy Protection Act (COPPA) and the Google Play Families policy by
collecting nothing at all.

## Security

Because no data leaves your device, there is no transmission to intercept and no server that could
be breached. On-device data is protected by Android's standard application sandbox, which prevents
other apps from reading it.

## Changes to this policy

If this policy changes, the updated version will be published at this URL and the date at the top
will be revised.

## Contact

Questions about this policy can be sent to `adityaspmahajan@gmail.com`, which is also the developer contact
address listed on the app's Google Play store listing.
