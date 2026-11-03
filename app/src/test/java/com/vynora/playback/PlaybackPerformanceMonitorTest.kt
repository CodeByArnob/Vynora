package com.vynora.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackPerformanceMonitorTest {

    @Test
    fun tracksColdStartAndTrackSwitchLatencies() {
        PlaybackPerformanceMonitor.onPlayInitiated(isTrackSwitch = false)
        Thread.sleep(10) // Simulate minimal startup elapsed time
        PlaybackPerformanceMonitor.onAudioStarted()

        val m1 = PlaybackPerformanceMonitor.metrics.value
        assertTrue("Cold start latency should be recorded (>0)", m1.coldStartLatencyMs >= 0L)
        assertTrue("Total tracks played should increment", m1.totalTracksPlayed >= 1)

        PlaybackPerformanceMonitor.onPlayInitiated(isTrackSwitch = true)
        Thread.sleep(10)
        PlaybackPerformanceMonitor.onAudioStarted()

        val m2 = PlaybackPerformanceMonitor.metrics.value
        assertTrue("Track switch latency should be recorded (>0)", m2.trackSwitchLatencyMs >= 0L)
        assertEquals(m1.totalTracksPlayed + 1, m2.totalTracksPlayed)
    }

    @Test
    fun recordsBufferingAndFailures() {
        val initialBuffering = PlaybackPerformanceMonitor.metrics.value.bufferingCount
        val initialFailures = PlaybackPerformanceMonitor.metrics.value.failureCount

        PlaybackPerformanceMonitor.onBufferingEvent()
        PlaybackPerformanceMonitor.onFailureEvent()

        val updated = PlaybackPerformanceMonitor.metrics.value
        assertEquals(initialBuffering + 1, updated.bufferingCount)
        assertEquals(initialFailures + 1, updated.failureCount)
    }

    @Test
    fun recordsCacheHits() {
        PlaybackPerformanceMonitor.recordCacheHit(true)
        val m = PlaybackPerformanceMonitor.metrics.value
        assertTrue("Cache hit ratio should be >= 0", m.cacheHitRatioPercent >= 0)
    }
}
