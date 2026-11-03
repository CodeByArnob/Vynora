# Audius Online Music Streaming Integration in Vynora

## Overview
Vynora uses **Audius** as its **sole authorized online music streaming provider**, completely replacing all prior third-party video services and external API requirements.

## Core Features
1. **Keyless Operation**: Uses official read-only decentralized endpoints (`https://api.audius.co/v1`) without requiring mandatory user credentials or Google Cloud API setup.
2. **Direct Media3 ExoPlayer Playback**: Stream endpoints (`/tracks/{id}/stream?app_name=Vynora`) redirect via standard HTTP 302 to high-fidelity audio streams (`200 audio/mpeg`), which ExoPlayer buffers and plays with low latency (<500ms).
3. **Local LRU Cache Integration**: Audio chunks are cached in `VynoraAudioCache` (256MB LRU) for instant replay and offline playback.
4. **Real-Time Bass-Reactive Visualizer**: Audio data feeds directly into the 28-band FFT visualizer, creating dynamic waveforms synchronized with bass and rhythm.
5. **Local MediaStore Separation**: Device storage files remain 100% offline and isolated in Room/MediaStore repositories.
