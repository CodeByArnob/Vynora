package com.vynora.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks real playback performance metrics:
 * 1. Cold-start latency (time to first sound)
 * 2. Next track switch latency
 * 3. Buffering event frequency
 * 4. Playback failure frequency
 * 5. Cache hit ratio
 */
data class PlaybackMetrics(
    val coldStartLatencyMs: Long = 0L,
    val trackSwitchLatencyMs: Long = 0L,
    val bufferingCount: Int = 0,
    val failureCount: Int = 0,
    val totalTracksPlayed: Int = 0,
    val cachedTracksCount: Int = 0,
    val preloadedTracksCount: Int = 0,
    val cacheHitRatioPercent: Int = 0
)

object PlaybackPerformanceMonitor {
    private val _metrics = MutableStateFlow(PlaybackMetrics())
    val metrics: StateFlow<PlaybackMetrics> = _metrics.asStateFlow()

    private var trackStartTime: Long = 0L
    private var isSwitchingTrack: Boolean = false

    fun onPlayInitiated(isTrackSwitch: Boolean = false) {
        trackStartTime = System.currentTimeMillis()
        isSwitchingTrack = isTrackSwitch
    }

    fun onAudioStarted() {
        if (trackStartTime > 0L) {
            val latency = (System.currentTimeMillis() - trackStartTime).coerceAtLeast(0L)
            trackStartTime = 0L
            val current = _metrics.value
            _metrics.value = if (isSwitchingTrack) {
                current.copy(
                    trackSwitchLatencyMs = latency,
                    totalTracksPlayed = current.totalTracksPlayed + 1
                )
            } else {
                current.copy(
                    coldStartLatencyMs = latency,
                    totalTracksPlayed = current.totalTracksPlayed + 1
                )
            }
        }
    }

    fun onBufferingEvent() {
        val current = _metrics.value
        _metrics.value = current.copy(bufferingCount = current.bufferingCount + 1)
    }

    fun onFailureEvent() {
        val current = _metrics.value
        _metrics.value = current.copy(failureCount = current.failureCount + 1)
    }

    fun recordPreloadedTrack() {
        val current = _metrics.value
        _metrics.value = current.copy(preloadedTracksCount = current.preloadedTracksCount + 1)
    }

    fun recordCacheHit(isHit: Boolean) {
        val current = _metrics.value
        val total = (current.totalTracksPlayed + 1).coerceAtLeast(1)
        val hits = if (isHit) current.cachedTracksCount + 1 else current.cachedTracksCount
        val ratio = ((hits.toDouble() / total) * 100).toInt().coerceIn(0, 100)
        _metrics.value = current.copy(
            cachedTracksCount = hits,
            cacheHitRatioPercent = ratio
        )
    }
}
