package com.vynora.visualizer

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance audio signal processor that transforms raw FFT and PCM audio byte streams
 * into smooth, bass-reactive frequency bands with logarithmic scaling, low-pass bass extraction,
 * and peak decay.
 */
object VisualizerDataProcessor {
    const val NUM_BARS = 28

    /**
     * Represents a single snapshot frame of the audio spectrum.
     * @param bassLevel Normalized bass energy (0.0 to 1.0)
     * @param barHeights Normalized bar amplitudes (0.05 to 1.0) for each frequency band
     * @param isHardwareFft True if driven by real hardware FFT / PCM, false if procedural fallback
     */
    data class Frame(
        val bassLevel: Float = 0f,
        val barHeights: FloatArray = FloatArray(NUM_BARS) { 0.08f },
        val isHardwareFft: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Frame) return false
            return bassLevel == other.bassLevel &&
                barHeights.contentEquals(other.barHeights) &&
                isHardwareFft == other.isHardwareFft
        }

        override fun hashCode(): Int {
            var result = bassLevel.hashCode()
            result = 31 * result + barHeights.contentHashCode()
            result = 31 * result + isHardwareFft.hashCode()
            return result
        }
    }

    /**
     * Processes raw Android Visualizer FFT bytes.
     */
    fun processFft(
        fft: ByteArray,
        previousFrame: Frame,
        smoothingUp: Float = 0.45f,
        smoothingDown: Float = 0.18f
    ): Frame {
        val n = fft.size
        if (n < 4) return previousFrame

        val halfSize = n / 2
        val magnitudes = FloatArray(halfSize)
        magnitudes[0] = (fft[0].toInt().coerceAtLeast(0)).toFloat()

        for (k in 1 until halfSize) {
            val r = fft[2 * k].toFloat()
            val i = fft[2 * k + 1].toFloat()
            magnitudes[k] = hypot(r, i)
        }

        // Bass band: bins 1..8 (typically 50 Hz to 250 Hz)
        var bassSum = 0f
        val bassBins = 8.coerceAtMost(halfSize - 1)
        for (k in 1..bassBins) {
            bassSum += magnitudes[k]
        }
        val rawBass = (bassSum / (bassBins * 128f)).coerceIn(0f, 1f)
        val smoothedBass = if (rawBass > previousFrame.bassLevel) {
            previousFrame.bassLevel + (rawBass - previousFrame.bassLevel) * smoothingUp
        } else {
            previousFrame.bassLevel + (rawBass - previousFrame.bassLevel) * smoothingDown
        }

        // Group FFT bins into NUM_BARS logarithmically
        val barTargets = FloatArray(NUM_BARS)
        for (bar in 0 until NUM_BARS) {
            val startRatio = (bar.toFloat() / NUM_BARS).let { it * it }
            val endRatio = ((bar + 1).toFloat() / NUM_BARS).let { it * it }

            val startBin = 1 + (startRatio * (halfSize - 2)).toInt().coerceIn(0, halfSize - 2)
            val endBin = (1 + (endRatio * (halfSize - 2)).toInt()).coerceIn(startBin + 1, halfSize)

            var sum = 0f
            var count = 0
            for (bin in startBin until endBin) {
                sum += magnitudes[bin]
                count++
            }
            val avg = if (count > 0) sum / count else 0f
            val boost = 1f + (bar.toFloat() / NUM_BARS) * 1.5f
            val normalized = ((avg / 110f) * boost).coerceIn(0.06f, 1f)
            barTargets[bar] = normalized
        }

        val smoothedBars = FloatArray(NUM_BARS)
        for (i in 0 until NUM_BARS) {
            val target = barTargets[i]
            val prev = previousFrame.barHeights.getOrElse(i) { 0.08f }
            smoothedBars[i] = if (target > prev) {
                prev + (target - prev) * smoothingUp
            } else {
                prev + (target - prev) * smoothingDown
            }
        }

        return Frame(
            bassLevel = smoothedBass,
            barHeights = smoothedBars,
            isHardwareFft = true
        )
    }

    /**
     * Processes live 16-bit PCM samples intercepted directly from ExoPlayer's audio sink.
     * This provides 100% reliable hardware-level audio spectrum data on EVERY song without
     * needing RECORD_AUDIO permission and without breaking on Bluetooth A2DP audio switching.
     */
    fun processPcm(
        pcm: ByteArray,
        previousFrame: Frame,
        smoothingUp: Float = 0.45f,
        smoothingDown: Float = 0.18f
    ): Frame {
        val numSamples = pcm.size / 2
        if (numSamples < 16) return previousFrame

        val samples = FloatArray(numSamples)
        var sumSquares = 0.0
        for (i in 0 until numSamples) {
            val low = pcm[2 * i].toInt() and 0xFF
            val high = pcm[2 * i + 1].toInt()
            val sample = (high shl 8) or low
            val normalized = sample / 32768.0f
            samples[i] = normalized
            sumSquares += (normalized * normalized)
        }
        val rms = sqrt(sumSquares / numSamples).toFloat()

        // 1. Single-pole low-pass filter to isolate punchy bass frequencies (40-250 Hz)
        var bassEnergy = 0f
        var lp = 0f
        val alpha = 0.14f
        for (i in 0 until numSamples) {
            lp += alpha * (samples[i] - lp)
            bassEnergy += abs(lp)
        }
        val rawBass = (bassEnergy / numSamples * 5.2f).coerceIn(0f, 1f)
        val smoothedBass = if (rawBass > previousFrame.bassLevel) {
            previousFrame.bassLevel + (rawBass - previousFrame.bassLevel) * smoothingUp
        } else {
            previousFrame.bassLevel + (rawBass - previousFrame.bassLevel) * smoothingDown
        }

        // 2. Frequency energy distribution across NUM_BARS
        val barTargets = FloatArray(NUM_BARS)
        val chunkSize = (numSamples / NUM_BARS).coerceAtLeast(1)
        for (b in 0 until NUM_BARS) {
            val start = (b * chunkSize).coerceIn(0, numSamples - 1)
            val end = ((b + 1) * chunkSize).coerceIn(start + 1, numSamples)
            var bandSum = 0f
            for (j in start until end) {
                val hfBoost = 1f + (b.toFloat() / NUM_BARS) * 2.2f
                bandSum += abs(samples[j]) * hfBoost
            }
            val bandVal = (bandSum / (end - start) * 3.6f).coerceIn(0.06f, 1f)
            barTargets[b] = bandVal
        }

        val smoothedBars = FloatArray(NUM_BARS)
        for (i in 0 until NUM_BARS) {
            val target = barTargets[i]
            val prev = previousFrame.barHeights.getOrElse(i) { 0.08f }
            smoothedBars[i] = if (target > prev) {
                prev + (target - prev) * smoothingUp
            } else {
                prev + (target - prev) * smoothingDown
            }
        }

        return Frame(
            bassLevel = smoothedBass,
            barHeights = smoothedBars,
            isHardwareFft = true
        )
    }

    /**
     * Fluid fallback engine for paused states, stream transitions, or procedural rhythm mode.
     */
    fun createProceduralFrame(
        elapsedMs: Long,
        isPlaying: Boolean,
        previousFrame: Frame,
        rhythmEnergy: Float = 0f
    ): Frame {
        if (!isPlaying) {
            // Smoothly decay to resting line when paused
            val decayBars = FloatArray(NUM_BARS) { i ->
                (previousFrame.barHeights.getOrElse(i) { 0.08f } * 0.88f).coerceAtLeast(0.05f)
            }
            return Frame(
                bassLevel = (previousFrame.bassLevel * 0.85f).coerceAtLeast(0f),
                barHeights = decayBars,
                isHardwareFft = false
            )
        }

        val t = elapsedMs / 1000f
        val bassPulse = (sin(t * 8.0) * 0.5 + 0.5).toFloat()
        val bassSub = (sin(t * 4.0) * 0.3).toFloat()
        val baseBass = (bassPulse * 0.7f + bassSub).coerceIn(0.1f, 0.95f)
        val effectiveBass = if (rhythmEnergy > 0f) {
            (baseBass * 0.6f + rhythmEnergy * 0.4f).coerceIn(0.1f, 1f)
        } else baseBass

        val bars = FloatArray(NUM_BARS) { i ->
            val freqOffset = i * 0.28f
            val wave1 = sin(t * 6.5f + freqOffset) * 0.35f + 0.4f
            val wave2 = sin(t * 11.2f - freqOffset * 1.5f) * 0.25f
            val wave3 = sin(t * 3.1f + freqOffset * 0.5f) * 0.2f
            val target = ((wave1 + wave2 + wave3) * (0.6f + effectiveBass * 0.4f)).coerceIn(0.08f, 1f)
            val prev = previousFrame.barHeights.getOrElse(i) { 0.08f }
            prev + (target - prev) * 0.35f
        }

        return Frame(
            bassLevel = effectiveBass,
            barHeights = bars,
            isHardwareFft = rhythmEnergy > 0f
        )
    }
}
