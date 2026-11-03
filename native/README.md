# Vynora Native Android

This is the Kotlin + Jetpack Compose migration workspace. The original Flutter project remains at the repository root during the migration so the existing implementation is preserved.

## Stack
- Kotlin 2.2.20, Android Gradle Plugin 8.13.2, JDK 17
- Jetpack Compose + Material 3 (Compose BOM 2026.09.00)
- AndroidX Media3 1.11.1 (ExoPlayer + MediaSessionService)
- MediaStore for on-device audio indexing

## Current vertical slice
- Native Android application scaffold
- Runtime audio-library permission handling
- MediaStore audio scan with title, artist, album and duration
- Search across indexed track metadata
- Queue playback using Media3
- Background media session service and system media controls
- Compose library and now-playing surfaces

## Build
From this directory:

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

A checked-in Gradle wrapper will be added after the first reproducible CI bootstrap. Features not implemented yet (Room persistence, playlists, album artwork, lyrics, equalizer, advanced queue controls and release signing) are tracked in the root migration plan.
