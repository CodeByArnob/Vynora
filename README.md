# Vynora

**Your music, beautifully organized.**

Vynora is an open-source Flutter Material 3 music discovery and local music player. The initial app foundation includes a region-aware YouTube Music chart screen, loading/error/empty states, and a separate domain model and API service.

> **Playback boundary:** YouTube Data API provides video metadata, not downloadable audio. Vynora does not extract, download, proxy, or background-play YouTube audio. Selected YouTube videos open through the official YouTube app/site. Local audio playback is a separate feature for files the user owns or is authorized to play.

## Current status

The Flutter foundation and first YouTube trending discovery implementation are committed. This is **not yet a finished production release**. Local media scanning, persistent playlists, local background playback, region selection, full test coverage, platform configuration and real-device release validation remain outstanding.

## Run

Requirements: Flutter stable and Dart compatible with Flutter 3.7+.

```bash
flutter pub get
flutter run --dart-define=YOUTUBE_API_KEY=YOUR_RESTRICTED_API_KEY
```

Do not commit API keys. A key passed through `--dart-define` is still extractable from a distributed APK. Restrict it in Google Cloud Console to YouTube Data API and the Android app identity; use a backend proxy for stronger key isolation and quota management.

## Trending music source

The app calls YouTube Data API v3 `videos.list` with `chart=mostPopular`, `regionCode=BD` and Music category ID `10`. Since July 21, 2025, YouTube's mostPopular chart reflects Trending Music, Movies and Gaming charts; filtering the Music category is used here. This is a regional trending chart, not a guarantee that every newly released song appears first.

## Engineering

- Flutter + Material 3, system light/dark themes
- Riverpod async state management
- YouTube metadata adapter isolated from presentation
- GitHub Actions workflow for formatting, analysis and tests
- MIT License

See [architecture](docs/ARCHITECTURE.md), [YouTube integration rules](docs/YOUTUBE_INTEGRATION.md), [release checklist](docs/RELEASE_CHECKLIST.md), and [contributing](CONTRIBUTING.md).

## Quality gates

```bash
dart format --set-exit-if-changed .
flutter analyze
flutter test
flutter build apk --release
```
