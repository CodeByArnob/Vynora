package com.vynora.visualizer

import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages native Android [Visualizer] capture with zero memory leaks, safe permission checks,
 * and automatic fallback to procedural audio animation if permission is denied or audio session is 0.
 */
class AudioVisualizerManager(private val context: Context) {
    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0
    private var lastRawFft: ByteArray? = null

    private val _currentFrame = MutableStateFlow(VisualizerDataProcessor.Frame())
    val currentFrame: StateFlow<VisualizerDataProcessor.Frame> = _currentFrame.asStateFlow()

    private val hasRecordPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Attaches to an active audio session ID from ExoPlayer.
     */
    @Synchronized
    fun attachToSession(sessionId: Int) {
        if (sessionId <= 0) {
            detach()
            return
        }
        if (visualizer != null && currentSessionId == sessionId) return

        detach()
        currentSessionId = sessionId

        if (!hasRecordPermission) {
            // Will use procedural reactive wave until permission is granted
            return
        }

        try {
            val v = Visualizer(sessionId).apply {
                val ranges = Visualizer.getCaptureSizeRange()
                val targetSize = 256.coerceIn(ranges[0], ranges[1])
                captureSize = targetSize
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}
                        override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                            if (fft != null && fft.isNotEmpty()) {
                                lastRawFft = fft
                                _currentFrame.value = VisualizerDataProcessor.processFft(
                                    fft = fft,
                                    previousFrame = _currentFrame.value
                                )
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2, // 30-40 fps for battery and CPU efficiency
                    false, // waveform not needed for FFT
                    true   // FFT capture enabled
                )
                enabled = true
            }
            visualizer = v
        } catch (_: SecurityException) {
            detach()
        } catch (_: RuntimeException) {
            // Audio service or HAL not ready; will use procedural fallback
            detach()
        }
    }

    /**
     * Called on each UI frame or timer tick to keep the visualizer fluid.
     * If native FFT is active, processes the latest hardware FFT data.
     * Otherwise, advances the procedural reactive audio wave.
     */
    fun tick(elapsedMs: Long, isPlaying: Boolean) {
        val fft = lastRawFft
        if (visualizer != null && fft != null && isPlaying) {
            _currentFrame.value = VisualizerDataProcessor.processFft(
                fft = fft,
                previousFrame = _currentFrame.value
            )
        } else {
            _currentFrame.value = VisualizerDataProcessor.createProceduralFrame(
                elapsedMs = elapsedMs,
                isPlaying = isPlaying,
                previousFrame = _currentFrame.value
            )
        }
    }

    @Synchronized
    fun detach() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
        lastRawFft = null
        currentSessionId = 0
    }

    fun release() {
        detach()
    }
}
