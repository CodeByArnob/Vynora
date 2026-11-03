# Vynora

**Your music, beautifully organized.**

Vynora is a native Android offline-first music player built with Kotlin, Jetpack Compose, Material 3 and AndroidX Media3. It indexes device audio through Android MediaStore and plays local files through a MediaSession-backed background player.

## Native Android project

- Kotlin + Jetpack Compose + Material 3
- AndroidX Media3 ExoPlayer + MediaSessionService
- Room persistence for library metadata, favorites and playlists
- MediaStore audio indexing with granular runtime permission
- Queue playback, next/previous, seeking, shuffle and repeat
- Compose library, favorites, playlists and now-playing screens
- GitHub Actions build, lint, test and debug APK artifact
- Dependabot Gradle and GitHub Actions updates

## Build

Requirements: JDK 17, Android SDK Platform 36, Gradle 8.13, Android Gradle Plugin 8.13.2.

Run `gradle :app:assembleDebug`, `gradle :app:lintDebug` and `gradle :app:testDebugUnitTest`.

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Migration status

The native Kotlin app is the active Android implementation. The former Flutter source is retained under `lib/`, `test/` and `pubspec.yaml` as a migration reference and is not part of the native build.

Not yet release-complete: physical-device validation, full album/artist/folder browsing, artwork extraction/cache, advanced queue editing, lyrics, equalizer, sleep timer, Android Auto, signing and Play Console release preparation.

PixelPlayer is used only as a feature and UX reference. No proprietary source, trademarks or artwork are copied.

## License

MIT. See [LICENSE](LICENSE).
