# Vynora

**Your music, beautifully organized.**

Vynora is an offline-first native Android music player built with Kotlin, Jetpack Compose, Material 3 and AndroidX Media3.

## Current capabilities

- MediaStore indexing of device audio with granular runtime permission.
- Room-backed library metadata, favorites and playlists.
- Library, Favorites, Playlists and Now Playing Compose screens.
- Media3 ExoPlayer with background MediaSessionService.
- Queue playback, play/pause, next/previous, seeking, shuffle and repeat.
- Native launcher icon.
- GitHub Actions debug build, Android lint, unit tests and APK artifact.
- Dependabot updates for Gradle and GitHub Actions.

## Build and verify

Requirements: JDK 17, Android SDK Platform 36, Gradle 8.13 and Android Gradle Plugin 8.13.2.

```bash
gradle :app:assembleDebug
gradle :app:lintDebug
gradle :app:testDebugUnitTest
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Migration

The root-level Kotlin/Compose project is the active Android app. Previous Flutter source under `lib/`, `test/` and `pubspec.yaml` is retained as a migration reference and is not part of the Android build.

## Not yet production-complete

Album/artist/folder browsing, artwork cache, editable queue, sleep timer, lyrics, equalizer, widgets, Android Auto, full UI/device test coverage, physical-device validation, signed AAB and Play Console preparation remain outstanding. See [docs/RELEASE_CHECKLIST.md](docs/RELEASE_CHECKLIST.md).

A green CI run confirms only the configured automated gates; it is not a claim of device or store readiness.

PixelPlayer is used only as a UX/feature reference. No proprietary source, trademarks or artwork are copied.

## License

MIT. See [LICENSE](LICENSE).
