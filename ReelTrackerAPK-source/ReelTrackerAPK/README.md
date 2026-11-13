# Reel Tracker Android APK starter

This is an experimental Android-native starter project. It does not require Android Studio on your computer: GitHub Actions can compile it in the cloud.

## Build the APK
1. Create a GitHub repository and upload the contents of this folder.
2. Open the repository's Actions tab.
3. Select "Build Reel Tracker APK" and choose "Run workflow", or push to `main`.
4. When the workflow completes, open its run and download the `ReelTracker-debug-apk` artifact.
5. Extract the ZIP to get `app-debug.apk`, then install it on your Android phone.

## Enable monitoring
Open Reel Tracker, tap "Enable Reel monitoring", and enable its Accessibility service in Android Settings.

## Important limitations
- Instagram does not provide a public API for the number of Reels watched.
- This prototype uses an experimental Accessibility-event heuristic. It may miss Reels, count non-Reel scrolls, or stop working after Instagram UI changes.
- The overlay is best-effort; Android or Instagram changes may affect it.
- Test with a low daily limit first. Do not rely on this prototype as an exact or tamper-proof blocker.
- Accessibility permission is powerful. The service is designed to inspect the foreground Instagram UI locally and does not transmit screen contents.
