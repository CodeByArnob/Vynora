# Architecture

## Current implementation

```
lib/
  main.dart
  app/vynora_shell.dart
  features/
    discover/
      data/youtube_trending_service.dart
      domain/trending_video.dart
      presentation/discover_screen.dart
    library/
      data/local_music_repository.dart
      data/local_music_providers.dart
      domain/local_track.dart
      presentation/library_screen.dart
    player/
      data/audio_player_provider.dart
```

- Riverpod owns asynchronous discovery and local-library state.
- YouTube metadata access and JSON parsing are separated from UI widgets.
- User-selected audio files are copied to app-private storage; Vynora does not request broad shared-storage access for this import flow.
- just_audio owns the local playback queue. just_audio_background exposes media notification, lock-screen and headset controls through the Android audio service.
- The app shell shares one player instance across Discover and Library and exposes a mini-player.

## Boundaries

YouTube discovery metadata and local audio playback are intentionally separate. The YouTube screen opens the official YouTube app/site and never extracts audio. The local player only plays files selected by the user.

## Remaining production work

- Device-wide MediaStore library indexing with granular permission UX (optional; current design uses system file picker import).
- Persistent playlists, favorites, playback history and user-editable metadata.
- Track metadata parsing and album-art thumbnails.
- Search, sorting and queue editing.
- Robust handling for missing, moved, unsupported and corrupted audio files.
- Verify Android foreground service, audio focus, interruptions, Bluetooth/headset controls and notification permission behavior on physical devices.
- Add integration/widget coverage for import, playback, deletion, navigation and lifecycle.
- API cache, quota monitoring, region selector, YouTube attribution and offline/error state audits.
- Crash reporting, accessibility and release performance testing.
- Protected upload-key signing, AAB build and Play Console release configuration.
