package com.vynora.visualizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualizerDataProcessorTest {

    @Test
    fun processFft_extractsBassAndNormalizedBars() {
        val fft = ByteArray(64)
        for (k in 1..4) {
            fft[2 * k] = 80.toByte()
            fft[2 * k + 1] = 60.toByte()
        }

        val initialFrame = VisualizerDataProcessor.Frame()
        val processed = VisualizerDataProcessor.processFft(fft, initialFrame)

        assertTrue("Processed frame should be hardware FFT", processed.isHardwareFft)
        assertTrue("Bass level should be greater than 0", processed.bassLevel > 0.05f)
        assertEquals(VisualizerDataProcessor.NUM_BARS, processed.barHeights.size)
        assertTrue("Bass bar height should be boosted", processed.barHeights[0] > 0.06f)
    }

    @Test
    fun processPcm_extractsRealPcmEnergyAndFrequencyBands() {
        // Generate simulated 16-bit PCM sinusoidal bass wave (100 Hz at 44.1 kHz, 256 samples = 512 bytes)
        val pcm = ByteArray(512)
        val amplitude = 25000 // Loud bass sample
        for (i in 0 until 256) {
            val angle = 2.0 * Math.PI * 100.0 * (i / 44100.0)
            val sampleVal = (Math.sin(angle) * amplitude).toInt().coerceIn(-32768, 32767)
            pcm[2 * i] = (sampleVal and 0xFF).toByte()
            pcm[2 * i + 1] = ((sampleVal shr 8) and 0xFF).toByte()
        }

        val initialFrame = VisualizerDataProcessor.Frame()
        val processed = VisualizerDataProcessor.processPcm(pcm, initialFrame)

        assertTrue("Processed PCM frame should report hardware/live audio", processed.isHardwareFft)
        assertTrue("Bass level should reflect 100Hz sine energy", processed.bassLevel > 0.05f)
        assertEquals(VisualizerDataProcessor.NUM_BARS, processed.barHeights.size)
    }

    @Test
    fun createProceduralFrame_generatesSmoothRhythmWhenPlaying() {
        val initialFrame = VisualizerDataProcessor.Frame()
        val frame1 = VisualizerDataProcessor.createProceduralFrame(
            elapsedMs = 1000L,
            isPlaying = true,
            previousFrame = initialFrame
        )

        assertFalse("Procedural frame should not be hardware FFT", frame1.isHardwareFft)
        assertTrue("Bass level should be positive during playback", frame1.bassLevel > 0f)
        assertEquals(VisualizerDataProcessor.NUM_BARS, frame1.barHeights.size)

        // Check decay when paused
        val pausedFrame = VisualizerDataProcessor.createProceduralFrame(
            elapsedMs = 2000L,
            isPlaying = false,
            previousFrame = frame1
        )
        assertTrue("Paused frame should decay bass", pausedFrame.bassLevel <= frame1.bassLevel)
    }
}
