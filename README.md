<div align="center">

# 🎧 Vynora — Native Android Music Player & Audius Streaming Engine

<p align="center">
  <strong>Native Android Music Discovery, Keyless Audius Decentralized Streaming, MediaStore Offline Player & Real-Time Bass Visualizer</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.1-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Audio-AndroidX%20Media3%20ExoPlayer-FF6F00?style=for-the-badge" alt="Media3" />
  <img src="https://img.shields.io/badge/Online%20Provider-Audius%20Decentralized-CC3399?style=for-the-badge" alt="Audius" />
  <img src="https://img.shields.io/badge/CI-GitHub%20Actions%20Passed-2EA44F?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI Status" />
</p>

<p align="center">
  <a href="https://github.com/CodeByArnob/Vynora/releases/latest"><strong>📦 GitHub Releases Page (`v0.8.0`)</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/CodeByArnob/Vynora/releases/latest/download/SHA256SUMS.txt"><strong>🔐 SHA-256 Checksum</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/CodeByArnob/Vynora/actions/workflows/native-android-ci.yml"><strong>⚙️ CI Build Logs</strong></a>
</p>

</div>

---

## 📱 Application Overview (`v0.8.0`)

Vynora combines a **Spotify-inspired Material 3 dark interface** with **Audius decentralized music streaming**, **offline device MP3/FLAC playback**, and **real-time bass-reactive audio visualization** powered by **AndroidX Media3 ExoPlayer**. Audius is the sole authorized online music streaming provider, operating in keyless mode without requiring Google Cloud or third-party video credentials.

---

## ⚡ 1-Click APK Download & Installation

The latest verified Android build is published under [**GitHub Releases (`v0.8.0`)**](https://github.com/CodeByArnob/Vynora/releases/latest):

| Asset | Direct 1-Click Link | Size | SHA-256 Digest |
| :--- | :--- | :--- | :--- |
| **`Vynora-v0.8.0-debug.apk`** | [**⬇️ Download APK (v0.8.0)**](https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-v0.8.0-debug.apk) | `26.9 MB` | Checksum Verified |
| **`Vynora-latest.apk`** | [**⬇️ Download Latest APK**](https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-latest.apk) | `26.9 MB` | Checksum Verified |
| **`SHA256SUMS.txt`** | [**🔐 View Checksums**](https://github.com/CodeByArnob/Vynora/releases/latest/download/SHA256SUMS.txt) | `175 B` | Verified |

---

## ✨ Key Capabilities

- **Audius-Powered Online Music Discovery**:
  - Direct keyless streaming via official decentralized Audius V1 endpoints (`api.audius.co`).
  - Search tracks, trending charts across genres (Electronic, Lo-Fi, Hip-Hop, Ambient, Rock, Indie).
  - Typo-tolerant phonetic search engine with instantaneous matching.
- **Hardware FFT Bass-Reactive Visualizer**:
  - Real-time frequency bars animated in Jetpack Compose Canvas with dynamic bass glow, peak decay, and organic fallback.
- **Low-Latency Streaming & 256MB LRU Cache**:
  - `VynoraAudioCache` caches stream chunks locally with `CacheDataSource` for zero-delay track switching and gapless continuity.
- **Local Device MP3/FLAC Engine**:
  - Full **Android MediaStore** scanner for offline MP3, FLAC, WAV, and M4A tracks with **Room** persistence for playlists and favorites.
  - Background playback, lockscreen transport controls, Bluetooth media button handling, audio-focus ducking, and configurable **Sleep Timer** via `VynoraPlaybackService` (`MediaSessionService`).

---

## 🔧 Local Build & API Configuration

### 1. Build Requirements
- **JDK 17**
- **Android SDK Platform 36 (Android 16)**
- **Gradle 8.13**

### 2. Build, Lint & Run Unit Tests

```bash
gradle :app:assembleDebug
gradle :app:lintDebug
gradle :app:testDebugUnitTest
```
