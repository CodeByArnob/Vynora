package com.vynora.visualizer

import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Universal Audio Visualizer Manager:
 * Combines three audio analysis pipelines:
 * 1. ExoPlayer real-time PCM stream (zero permissions needed, 100% reliable across Bluetooth, local, & streaming).
 * 2. Native Android Visualizer (FFT capture on supported devices when RECORD_AUDIO is granted).
 * 3. Harmonic procedural wave fallback with smooth exponential decay on pause.
 */
class AudioVisualizerManager(private val context: Context) {
    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0
    private var lastRawFft: ByteArray? = null

    @Volatile
    private var latestPcmChunk: ByteArray? = null
    @Volatile
    private var latestPcmTime: Long = 0L

    @Volatile
    private var latestRhythmEnergy: Float = 0f
    @Volatile
    private var isRhythmPlaying: Boolean = false

    private val _currentFrame = MutableStateFlow(VisualizerDataProcessor.Frame())
    val currentFrame: StateFlow<VisualizerDataProcessor.Frame> = _currentFrame.asStateFlow()

    private val hasRecordPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    init {
        // Register listener for real-time PCM audio chunks from ExoPlayer
        AudioDataBridge.setPcmListener { pcm ->
            latestPcmChunk = pcm
            latestPcmTime = System.currentTimeMillis()
        }

        // Register listener for rhythm pulses
        AudioDataBridge.setRhythmListener { energy, isPlaying ->
            latestRhythmEnergy = energy
            isRhythmPlaying = isPlaying
        }
    }

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
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    false,
                    true
                )
                enabled = true
            }
            visualizer = v
        } catch (_: SecurityException) {
            detach()
        } catch (_: RuntimeException) {
            detach()
        }
    }

    /**
     * Called on each UI frame or timer tick (~30-60 FPS) to keep the visualizer fluid.
     */
    fun tick(elapsedMs: Long, isPlaying: Boolean) {
        val now = System.currentTimeMillis()
        val pcm = latestPcmChunk
        val pcmFresh = pcm != null && (now - latestPcmTime < 250L)
        val activePlaying = isPlaying || (isRhythmPlaying && (now - latestPcmTime < 1000L))

        if (!activePlaying) {
            // Smoothly decay to resting line when playback is paused or stopped
            _currentFrame.value = VisualizerDataProcessor.createProceduralFrame(
                elapsedMs = 0L,
                isPlaying = false,
                previousFrame = _currentFrame.value
            )
            return
        }

        // 1. Priority 1: Real PCM samples directly from ExoPlayer's audio sink
        if (pcm != null && pcmFresh) {
            _currentFrame.value = VisualizerDataProcessor.processPcm(
                pcm = pcm,
                previousFrame = _currentFrame.value
            )
            return
        }

        // 2. Priority 2: Native Android Visualizer hardware FFT
        val fft = lastRawFft
        if (visualizer != null && fft != null) {
            _currentFrame.value = VisualizerDataProcessor.processFft(
                fft = fft,
                previousFrame = _currentFrame.value
            )
            return
        }

        // 3. Priority 3: Streaming rhythm pulse or organic wave
        _currentFrame.value = VisualizerDataProcessor.createProceduralFrame(
            elapsedMs = elapsedMs,
            isPlaying = true,
            previousFrame = _currentFrame.value,
            rhythmEnergy = latestRhythmEnergy
        )
    }

    fun onTrackTransition() {
        latestPcmChunk = null
        lastRawFft = null
        latestRhythmEnergy = 0f
    }

    @Synchronized
    fun detach() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
        lastRawFft = null
        latestPcmChunk = null
        currentSessionId = 0
    }

    fun release() {
        detach()
        AudioDataBridge.setPcmListener(null)
        AudioDataBridge.setRhythmListener(null)
    }
}
