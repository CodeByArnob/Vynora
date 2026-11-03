package com.vynora.visualizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualizerDataProcessorTest {

    @Test
    fun processFft_extractsBassAndNormalizedBars() {
        val fft = ByteArray(64)
        // Set strong bass energy in lower bins (bins 1..4)
        for (k in 1..4) {
            fft[2 * k] = 80.toByte()     // Real part
            fft[2 * k + 1] = 60.toByte() // Imaginary part (hypot(80, 60) = 100)
        }

        val initialFrame = VisualizerDataProcessor.Frame()
        val processed = VisualizerDataProcessor.processFft(fft, initialFrame)

        assertTrue("Processed frame should be hardware FFT", processed.isHardwareFft)
        assertTrue("Bass level should be greater than 0", processed.bassLevel > 0.05f)
        assertEquals(VisualizerDataProcessor.NUM_BARS, processed.barHeights.size)

        // Lower frequency bars should have higher amplitudes than silence
        assertTrue("Bass bar height should be boosted", processed.barHeights[0] > 0.06f)
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
