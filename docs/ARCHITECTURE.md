# Vynora Architecture

## Active Android application

The production Android app is a native Kotlin project at the repository root.

```
app/src/main/
  java/com/vynora/
    MainActivity.kt
    library/
      AudioTrack.kt
      LibraryViewModel.kt
      MediaStoreMusicRepository.kt
      PlaylistEntity.kt
      TrackDao.kt
      VynoraDatabase.kt
    playback/
      PlaybackController.kt
      VynoraPlaybackService.kt
  res/
```

## Layers and responsibilities

- **Presentation:** Jetpack Compose + Material 3. Screens collect lifecycle-aware state from the library ViewModel and playback controller.
- **Library source:** `MediaStoreMusicRepository` queries device audio using Android MediaStore on `Dispatchers.IO`. Access is limited by Android's granular audio permission; no broad all-files permission is requested.
- **Persistence:** Room stores indexed tracks, favorite flags, playlists and playlist membership. Library synchronization preserves favorites and removes stale MediaStore entries transactionally.
- **Playback:** AndroidX Media3 ExoPlayer runs in `MediaSessionService`; the UI connects through `MediaController`. The service owns the player and the activity owns/releases its controller.
- **Automation:** GitHub Actions runs SDK verification, Gradle debug build, Android lint, unit tests and uploads a debug APK. Dependabot checks Gradle and GitHub Actions dependencies.

## Product boundaries

Vynora streams online music exclusively from Audius and plays local device audio exposed through MediaStore. The application does not rely on third-party video platforms.

PixelPlayer is a feature and UX reference only. Do not copy proprietary code, artwork or branding.

## Known gaps

- Album, artist and folder browse destinations and album-art cache.
- Editable/persisted queue, play-next, queue reordering and playback history.
- Sleep timer, lyrics, equalizer (only where supported by Android audio-effect APIs), widgets and Android Auto.
- More Room DAO and Compose UI tests.
- Physical-device validation for media permissions, audio focus, interruptions, notification controls, Bluetooth/headset routes and process recreation.
- Accessibility and large-font audit, privacy policy, signed AAB and Play Console release.
