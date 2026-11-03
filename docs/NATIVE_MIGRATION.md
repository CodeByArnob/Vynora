# Vynora Native Android Migration

The primary project is a root-level native Android Gradle application.

## Architecture
- Presentation: Jetpack Compose and Material 3.
- Persistence: Room database and Kotlin Flow.
- Device library: MediaStore queried on Dispatchers.IO.
- Playback: Media3 ExoPlayer in MediaSessionService with MediaController.
- Platform: granular media permission and Android media-session controls.

## Implemented foundation
- Room-backed track index and favorites.
- Persistent playlist creation, selection, adding library tracks, removal and deletion.
- MediaStore refresh with stale-record cleanup.
- Queue playback, pending queue until controller binds, next/previous, seek, shuffle and repeat.
- Compose library, favorites, playlists and now-playing surfaces.
- Native launcher icon, CI build/lint/test/APK artifact and Dependabot updates.

## Remaining production checklist
- Album/artist/folder browse screens and artwork cache.
- Playlist/queue reorder polish and playback history.
- Physical device/audio-route test matrix and permission/notification lifecycle validation.
- Lyrics, equalizer where Android audio-effect APIs permit, sleep timer, widgets and Android Auto.
- Accessibility, performance, signed AAB, privacy policy and Play Console readiness.

No feature is release-complete without automated and physical-device verification.
