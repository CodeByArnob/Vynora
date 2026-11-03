# Vynora

<p align="center">
  <strong>A native Android music player, built for your local library.</strong><br/>
  Kotlin · Jetpack Compose · Material 3 · AndroidX Media3
</p>

<p align="center">
  <a href="https://github.com/CodeByArnob/Vynora/actions/workflows/native-android-ci.yml"><img alt="Native Android CI" src="https://github.com/CodeByArnob/Vynora/actions/workflows/native-android-ci.yml/badge.svg?branch=main"></a>
  <a href="https://github.com/CodeByArnob/Vynora/blob/main/LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Language" src="https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white">
</p>

Vynora is an offline-first Android music player focused on a clean native experience, fast local-library browsing and reliable background playback. The active application is implemented in **Kotlin and Jetpack Compose**; Flutter is no longer used to build the Android app.

## Product preview

> Real-device screenshots will be added here after capture and verification on a physical Android device. No concept renders or mockups are presented as application screenshots.

## Features

| Area | Current implementation |
| --- | --- |
| Local library | MediaStore audio indexing with Android media permissions |
| Browsing | Songs, albums, artists and folders |
| Personal library | Room-backed favorites and playlists |
| Playback | AndroidX Media3 ExoPlayer and MediaSessionService |
| Controls | Play/pause, next/previous, seek, shuffle and repeat |
| Sleep timer | 15, 30, 45 and 60 minute presets with cancellation |
| Interface | Native Jetpack Compose and Material 3 |
| Quality gates | Gradle build, Android lint and JVM unit tests in GitHub Actions |

Feature availability can vary by Android version and device. Physical-device validation is still required before a stable public release.

## Download

**GitHub Releases:** [Download the latest Vynora APK](https://github.com/CodeByArnob/Vynora/releases/latest)

If no release is published yet, use the [Actions page](https://github.com/CodeByArnob/Vynora/actions/workflows/native-android-ci.yml) to download the latest CI debug APK. CI artifacts are temporary; GitHub Releases are the permanent, versioned distribution channel.

### Build locally

Requirements: JDK 17, Android SDK Platform 36 and Gradle 8.13.

```bash
gradle :app:assembleDebug
gradle :app:lintDebug
gradle :app:testDebugUnitTest
```

Debug APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

- **UI:** Jetpack Compose + Material 3
- **Language:** Kotlin
- **Playback:** AndroidX Media3 / ExoPlayer
- **Background audio:** MediaSessionService
- **Local persistence:** Room
- **Library indexing:** Android MediaStore
- **Build:** Gradle Kotlin DSL

The old Flutter source is retained temporarily as migration history/reference and is not included in the native Android build. Its presence may still appear in GitHub's repository language statistics until that legacy source is archived or removed.

## Release status

A successful CI build verifies only the automated build, lint and unit-test gates. It does **not** certify physical-device compatibility, signed release readiness or Play Store compliance. Release APKs produced by the current automated pipeline are debug-signed preview builds, not production-signed Play Store packages.

Before a stable release, the project still needs broader device testing, audio-focus and Bluetooth validation, artwork handling, queue editing/persistence, accessibility review, a production signing configuration and Play Console preparation. See [the release checklist](docs/RELEASE_CHECKLIST.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the development workflow and contribution expectations.

## License

Vynora is distributed under the [MIT License](LICENSE).
