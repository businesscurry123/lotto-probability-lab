# Lotto Probability Lab

Android v1 for `로또 누적확률 연구소 (AI)`.

## Included

- Latest and historical Lotto 6/45 draw display with winning and bonus balls.
- Official draw sync with Room caching and background history backfill.
- Local saved number sets with direct 1-45 number selection.
- Match rank checks, saved-set leaderboard, growth stats, near-miss score, and round report.
- Placeholder slots for mobile QR, today's draw number, favorites, and ads.

## Data

The app discovers the latest round from the official Donghaeng Lottery result page and reads
draw windows from the same `lt645` result flow. Cached draw results and user number sets stay
on the device. If the network refresh fails, cached draw results remain visible.

## Build

Android SDK path is read from the local `local.properties` file or the normal Android SDK
environment variables.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

The debug APK is emitted under `app\build\outputs\apk\debug\`.

## Desktop Preview

`C:\Users\wjdqk\OneDrive\바탕 화면\launch-lotto-preview.cmd` rebuilds the current debug
APK, starts the local Android emulator when needed, installs the APK, and opens the app.
This machine uses ADB port `5600` because the default ADB port range is reserved by Windows.

## Galaxy S25 Ultra Preview

The app is responsive, so it adapts to tall Android phone screens. For a Galaxy S25 Ultra
preview, run:

```powershell
.\launch-lotto-s25-ultra-preview.cmd
```

This applies a Galaxy S25 Ultra-like emulator display profile: `1440x3120` with density `560`.
The display override is only applied to emulators, not to a physical phone.

## Install On Your Phone

Connect a Galaxy phone with USB debugging enabled, then run:

```powershell
.\install-lotto-to-phone.cmd
```

If the phone is not detected, enable Android Developer options on the phone, turn on USB
debugging, reconnect the USB cable, and approve the debugging prompt on the phone.

## Weekly Draw Updates

The app checks the official Lotto 6/45 result flow every time it starts and when the refresh
button is tapped. After the Saturday draw result is published by the official source, opening
the app or tapping refresh updates the latest round and caches it on the device. If the official
result is not available yet, the app keeps showing the cached result and reports that refresh
failed instead of deleting existing data.
