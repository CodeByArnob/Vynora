# Vynora

**Your music, beautifully organized.**

Vynora is an open-source Flutter Material 3 music discovery and local music player. The Discover screen shows Bangladesh-focused YouTube trending-music metadata and thumbnails. The Library lets users import supported audio files through Android's system file picker, stores a private app copy, and plays the imported tracks locally.

> **Playback boundary:** YouTube Data API provides video metadata, not downloadable audio. Vynora does not extract, download, proxy, or background-play YouTube audio. Selected YouTube videos open through the official YouTube app/site. Local playback is limited to audio files the user selects and is authorized to play.

## Current release status

Vynora now has a local import/playback foundation, background audio integration, an app icon source, and Android build automation. It is **not yet a finished production release**. Device-wide MediaStore indexing, persistent playlists/favorites, richer metadata and album artwork, complete error/permission testing, and real-device validation remain outstanding.

GitHub Actions bootstraps the Android platform when missing, generates launcher resources, runs formatting, static analysis and tests, builds a release-mode QA APK, and persists generated Android files and the dependency lockfile to `main`.

The QA APK uploaded by Actions is **not Play Store signed**. A protected upload keystore, verified signing configuration, store metadata, privacy disclosures and physical-device release validation are required before a public production release.

## Run locally

Requirements: Flutter 3.47.9 (or the version pinned in CI) and the matching Dart SDK.

```bash
flutter pub get
flutter run --dart-define=YOUTUBE_API_KEY=YOUR_RESTRICTED_API_KEY
```

Do not commit API keys. A key passed through `--dart-define` is still extractable from a distributed APK. Restrict it in Google Cloud Console to YouTube Data API and the Android app identity; use a backend proxy for stronger key isolation and quota management.

## Trending music source

The app calls YouTube Data API v3 `videos.list` with `chart=mostPopular`, `regionCode=BD` and Music category ID `10`. This is YouTube's regional Music-category chart metadata, not a guarantee that every new release appears first.

## Local music

Choose **Add music** in the Library and select one or more supported audio files. Vynora copies the selected files into its app-private storage so the imported library is available on later launches. Removing a track deletes only Vynora's private copy, not the original selected file. Supported formats are MP3, M4A, AAC, FLAC, WAV, OGG and OPUS, subject to Android decoder support.

Playback uses the local audio engine and Android media service for background notification and media controls. YouTube content is never used as a source for this player.

## Engineering

- Flutter + Material 3, system light/dark themes
- Riverpod state management
- YouTube metadata adapter isolated from presentation
- Local file import, app-private storage and audio playback
- Android background media service and notification controls
- Vynora launcher icon source and Android icon generation
- GitHub Actions: format, analyze, tests, Android QA build and platform bootstrap
- Dependabot: weekly pub and GitHub Actions dependency update PRs
- MIT License

See [architecture](docs/ARCHITECTURE.md), [YouTube integration rules](docs/YOUTUBE_INTEGRATION.md), [release checklist](docs/RELEASE_CHECKLIST.md), and [contributing](CONTRIBUTING.md).

## Local quality gates

```bash
dart format --set-exit-if-changed .
flutter analyze
flutter test
flutter build apk --release
```
