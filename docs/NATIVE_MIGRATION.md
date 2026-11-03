# Vynora Native Migration Plan

## Decision
The target application is native Android: Kotlin, Jetpack Compose and Material 3. The Flutter source is retained unchanged during migration and is not the long-term Android implementation.

## Architecture
- **Presentation:** Compose screens, Material 3 design system, lifecycle-aware state.
- **Domain:** track, album, artist, playlist and playback models; use-case boundaries.
- **Data:** MediaStore index, Room persistence, DataStore preferences and artwork/lyrics repositories.
- **Playback:** Media3 ExoPlayer hosted by MediaSessionService; MediaController exposed to UI.
- **Platform:** scoped media permissions, audio focus, noisy-route handling, notification and Android Auto integration.

## Delivery phases
1. **Foundation (in progress):** native Gradle project, Compose shell, MediaStore scan, search, Media3 background service and queue playback.
2. **Library:** Room database, album/artist/folder views, artwork extraction/cache, sorting, favorites, playlists and listening history.
3. **Player experience:** full queue editor, shuffle/repeat, gapless playback, crossfade, seek/progress, sleep timer and robust audio focus.
4. **Advanced:** synchronized lyrics (LRCLIB adapter with attribution and user controls), equalizer/audio effects where device APIs permit, tag editing and widgets.
5. **Release:** accessibility, performance, permission/lifecycle/device test matrix, signed AAB, privacy policy and Play Console readiness.

## Guardrails
- Do not copy PixelPlayer proprietary source, branding or assets. Treat it as a feature and UX reference only.
- No YouTube audio extraction or downloading. Discovery metadata remains separate from local playback.
- Never request broad storage access when MediaStore's granular audio permission is sufficient.
- No feature is marked complete until code, automated tests and device-level behavior are verified.
