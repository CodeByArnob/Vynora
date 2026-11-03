# Architecture

## Current implementation

```
lib/
  main.dart
  features/discover/
    data/youtube_trending_service.dart
    domain/trending_video.dart
    presentation/discover_screen.dart
```

Riverpod owns asynchronous discovery state. API access and JSON parsing are separated from UI widgets.

## Planned production boundaries

- Presentation: Material 3 screens, reusable components, navigation and accessibility.
- Domain: entities, repository contracts and use cases.
- Data: local database, device media adapter, YouTube metadata adapter and cache.
- Playback: local audio engine, queue, media session, notification and audio focus.
- Platform: Android media permissions, foreground service and app links.

YouTube discovery metadata and local audio playback are separate products. Background playback is for authorized local files or properly licensed streams only.

## Remaining production gates

Device media indexing and permission flow; persistent playlists and favorites; local background playback and media controls; API cache, retry/backoff and offline states; region selector; attribution; privacy disclosures; complete tests; release signing; accessibility and real-device testing.
