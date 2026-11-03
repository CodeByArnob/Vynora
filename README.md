<div align="center">

<a href="https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-v0.6.0-debug.apk">
  <img src="docs/assets/vynora-hero-motion.svg" alt="Vynora v0.6.0 — Native Android Music & Video Player" width="100%" />
</a>

<br/>

[![Latest Release](https://img.shields.io/github/v/release/CodeByArnob/Vynora?style=for-the-badge&logo=github&label=LATEST%20RELEASE&color=1DB954)](https://github.com/CodeByArnob/Vynora/releases/latest)
[![Direct APK Download](https://img.shields.io/badge/1--CLICK_APK-DOWNLOAD_v0.6.0_(23.2_MB)-1ED760?style=for-the-badge&logo=android&logoColor=black)](https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-v0.6.0-debug.apk)
[![Native Android CI](https://img.shields.io/github/actions/workflow/status/CodeByArnob/Vynora/native-android-ci.yml?branch=main&style=for-the-badge&logo=githubactions&label=ANDROID%20CI)](https://github.com/CodeByArnob/Vynora/actions/workflows/native-android-ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Media3](https://img.shields.io/badge/AndroidX-Media3_ExoPlayer-38BDF8?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/media/media3)
[![License: MIT](https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge)](LICENSE)

<br/>

<a href="https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-v0.6.0-debug.apk">
  <img src="docs/assets/vynora-download-cta.svg" alt="1-Click Direct Download Vynora v0.6.0 APK" width="92%" />
</a>

<p align="center">
  <a href="https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-v0.6.0-debug.apk"><strong>⬇️ Direct 1-Click APK (`Vynora-v0.6.0-debug.apk`)</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/CodeByArnob/Vynora/releases/latest"><strong>📦 GitHub Releases Page (`v0.6.0`)</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/CodeByArnob/Vynora/releases/download/v0.6.0/SHA256SUMS.txt"><strong>🔐 SHA-256 Checksum</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/CodeByArnob/Vynora/actions/workflows/native-android-ci.yml"><strong>⚙️ CI Build Logs</strong></a>
</p>

</div>

---

## 📱 Real Application Interface & Motion Preview (`v0.6.0`)

Vynora combines a **Spotify-inspired Material 3 dark interface** with a **YouTube Music `Video | Song` mode switcher**, prioritizing **90-day official Indian music releases** (Hindi, Bollywood, Punjabi, Arijit Singh, Romantic, and Bangla) alongside **Audius V1 direct audio streaming** and **offline device MP3/FLAC playback** powered by **AndroidX Media3 ExoPlayer**.

<div align="center">
  <img src="docs/assets/vynora-app-screens.svg" alt="Vynora v0.6.0 Native Android Screens — Home Discovery, Video | Song Now Playing & Offline Media3 Library" width="100%" />
</div>

<br/>

### 🎨 Curated Indian Music Discovery Sections

| **Latest Hindi & Bollywood** | **Arijit Singh & Monsoon** | **Punjabi Pop & Drive** | **Bangla Indie & Folk** |
| :---: | :---: | :---: | :---: |
| <img src="docs/assets/hero_hindi_bollywood_banner_1793701775755.jpg" width="220" alt="Latest Hindi & Bollywood" /> | <img src="docs/assets/album_arijit_romantic_1793701776833.jpg" width="180" alt="Arijit Singh & Monsoon" /> | <img src="docs/assets/album_punjabi_beats_1793701777979.jpg" width="180" alt="Punjabi Pop & Drive" /> | <img src="docs/assets/album_bangla_indie_1793701779257.jpg" width="180" alt="Bangla Indie & Folk" /> |
| *T-Series · Sony Music · Zee Music* | *Soulful Vocals & Unplugged* | *Chandigarh Urban & Folk Fusion* | *Kolkata & Dhaka Acoustic* |

---

## 🧠 Hybrid Playback & Zero-Error Embed Architecture

<div align="center">
  <img src="docs/assets/vynora-architecture-motion.svg" alt="Vynora v0.6.0 Hybrid Architecture Pipeline" width="100%" />
</div>

### 🛠️ Root-Level Engineering Fixes in `v0.6.0`

| Subsystem | Root Cause Identified | Production Fix Implemented |
| :--- | :--- | :--- |
| **YouTube Embed `Error 152` & `Error 153`** | Loading the IFrame player with `https://appassets.androidplatform.net/` (Error 153) or spoofing first-party `https://www.youtube.com/` inside an Android `WebView` (Error 152) triggered YouTube client validation failures. | Configured `WebView.loadDataWithBaseURL("https://vynora.music.app/", ...)` with matching third-party HTTPS `origin`, `playsinline=1`, third-party cookies enabled, and WebView `; wv` token normalization. |
| **Publisher Embed Restrictions (`Error 101 / 150`)** | Certain music labels block external video embedding on specific uploads, previously stranding users on a static error card. | Added `VynoraBridge.onYouTubeError(code)` JavaScript-to-Kotlin bridge that automatically switches `YouTubeWatchScreen` to **Song** mode and invokes `YouTubeViewModel.resolveFallbackAudioQueue(...)` to stream audio via **AndroidX Media3**. |
| **`[Artist/Singer Name]` Spam Uploads** | Querying YouTube Data API v3 with raw `order=date` surfaced unverified minute-old template uploads (`[Artist/Singer Name]`, `#shorts`, WhatsApp status clips). | Switched to a **90-day `publishedAfter` window** + `order=relevance` + `videoCategoryId=10` (Music) + `regionCode=IN`, blocking template/status keywords and boosting verified channels (**T-Series**, **Sony Music India**, **Zee Music**, **Saregama**, **YRF**, **Coke Studio**, **Arijit Singh**). |
| **Launcher Icon Text Conflict** | `app/src/main/res/drawable-nodpi/vynora_icon.png` (which contained raster text) overrode `drawable/vynora_icon.xml` due to Android's `nodpi` resource qualifier precedence. | Removed `drawable-nodpi/vynora_icon.png` and split the adaptive icon into `@drawable/ic_launcher_background` and `@drawable/ic_launcher_foreground` featuring a text-free **Emerald Acoustic Wave & Vinyl** vector mark on dark slate (`#07090E`). |

---

## ⚡ 1-Click APK Download & Installation

The latest verified Android build is published directly under [**GitHub Releases (`v0.6.0`)**](https://github.com/CodeByArnob/Vynora/releases/latest):

| Asset | Direct 1-Click Link | Size | SHA-256 Digest |
| :--- | :--- | :--- | :--- |
| **`Vynora-v0.6.0-debug.apk`** | [**⬇️ Download APK (v0.6.0)**](https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-latest.apk) | `23.2 MB` | `33252a86591fd8b27ac1e4a9c48409b9add88f4b408d22700c90dddae9146b8e` |
| **`Vynora-latest.apk`** | [**⬇️ Download Latest APK**](https://github.com/CodeByArnob/Vynora/releases/latest/download/Vynora-latest.apk) | `23.2 MB` | `33252a86591fd8b27ac1e4a9c48409b9add88f4b408d22700c90dddae9146b8e` |
| **`SHA256SUMS.txt`** | [**🔐 View Checksums**](https://github.com/CodeByArnob/Vynora/releases/download/v0.6.0/SHA256SUMS.txt) | `174 B` | Verified |

---

## ✨ Key Capabilities

- **Spotify & YouTube Music Inspired Material 3 Dark UI**:
  - 2-column quick-launch category grid, featured Hindi spotlight hero card, horizontal square-artwork carousels, and clean release-year vs. upload-date badges.
  - Top **`Video | Song`** segmented pill in the Now Playing screen for instant switching between 16:9 embedded HD video and high-resolution square album art.
- **Official YouTube Embedded Player + Smart Audio Fallback**:
  - Plays eligible music videos directly inside Vynora without redirecting to external apps or browsers.
  - Automatically falls back to streamable audio when a publisher restricts video embedding.
- **Audius V1 Network & Local Device MP3/FLAC Engine**:
  - Built-in **Audius V1 API** client (`AudiusRepository.kt`) for independent & trending streams.
  - Full **Android MediaStore** scanner for offline MP3, FLAC, WAV, and M4A tracks with **Room** persistence for playlists and favorites.
  - Background playback, lockscreen transport controls, Bluetooth media button handling, audio-focus ducking, and configurable **Sleep Timer** (`15m`, `30m`, `45m`, `60m`) via `VynoraPlaybackService` (`MediaSessionService`).

---

## 🔧 Local Build & API Configuration

### 1. Configure API Keys (`local.properties`)
Add your YouTube Data API v3 key (and optional Audius public API key) to your root `local.properties` file (or GitHub Actions repository secrets):

```properties
YOUTUBE_API_KEY=your_youtube_data_api_v3_key
AUDIUS_API_KEY=your_optional_audius_public_key
```

### 2. Build, Lint & Run Unit Tests
Requirements: **JDK 17**, **Android SDK Platform 36**, and **Gradle 8.13**.

```bash
gradle :app:assembleDebug
gradle :app:lintDebug
gradle :app:testDebugUnitTest
```

Compiled APK output:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤝 Contributing & License

- See [CONTRIBUTING.md](CONTRIBUTING.md) for architecture guidelines and pull request checks.
- See [docs/RELEASE_CHECKLIST.md](docs/RELEASE_CHECKLIST.md) for pre-release verification steps.
- Distributed under the [MIT License](LICENSE).
