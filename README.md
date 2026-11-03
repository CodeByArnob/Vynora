# Vynora

**Your music, beautifully organized.**

Vynora is an open-source Flutter Material 3 music discovery and local music player project. The current app foundation provides a Bangladesh-focused YouTube trending-music metadata screen with thumbnail loading and explicit loading/error/empty states.

> **Playback boundary:** YouTube Data API provides video metadata, not downloadable audio. Vynora does not extract, download, proxy, or background-play YouTube audio. Selected YouTube videos open through the official YouTube app/site. A separate on-device audio player is still under development and is not yet part of the shipped feature set.

## Current release status

This repository is **not production-ready yet**. The launcher icon source is included. GitHub Actions bootstraps the missing Android platform during a verified main-branch build, generates Android launcher resources, runs formatting, static analysis and tests, builds a release-mode QA APK, and then persists the generated Android project and lockfile to `main`.

The QA APK uploaded by Actions is **not Play Store signed**. A protected upload keystore, signing configuration, store metadata, privacy disclosures, device testing and the remaining music-player features are still required before a public production release.

## Run locally

Requirements: Flutter 3.47.9 (or the version pinned in CI) and the matching Dart SDK.

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
