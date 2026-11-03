# Vynora Native Migration

## Target stack
- Kotlin + Jetpack Compose + Material 3
- Android Gradle Plugin 9.1.1, Gradle 9.3.1, JDK 17
- Compose BOM 2026.09.00 (API 37)
- Media3 1.11.1 (ExoPlayer + MediaSessionService)
- Room 2.8.5 with KSP

## Implemented foundation
- Dedicated native Android application under `native/`; Flutter code is preserved until native parity and release checks are complete.
- Runtime READ_MEDIA_AUDIO permission and MediaStore scanning on Dispatchers.IO.
- Room-backed song index, favorite state and playlist/playlist-track schema.
- Compose library/search/now-playing screens.
- Media3 ExoPlayer service, MediaSession, audio focus and noisy-route pause handling.
- Queue playback, next/previous, notification and lock-screen session integration.
- CI build, lint, unit tests, APK artifact upload and weekly dependency updates.

## Remaining delivery phases
1. **Library UX:** albums, artists, folders, favorites and persistent playlist screens; artwork cache.
2. **Player UX:** progress/seek, queue editor, shuffle/repeat, gapless, crossfade, sleep timer and playback history.
3. **Advanced:** lyrics provider with attribution/cache, equalizer where Android audio-effect APIs permit, widgets and Android Auto.
4. **Release:** accessibility and device test matrix, signed AAB, privacy disclosures and Play Console release.

## Guardrails
- Do not copy proprietary PixelPlayer source, trademarks or artwork.
- Do not extract or download YouTube audio; discovery metadata remains separate from local playback.
- Request only granular media permissions.
- No feature is complete without automated and device-level verification.
