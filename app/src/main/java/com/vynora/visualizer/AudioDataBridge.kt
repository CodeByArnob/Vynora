package com.vynora.visualizer

/**
 * Thread-safe global audio data bridge connecting ExoPlayer's PCM stream
 * and audio streaming metrics directly to the visualizer subsystem.
 */
object AudioDataBridge {
    @Volatile
    private var pcmListener: ((ByteArray) -> Unit)? = null

    @Volatile
    private var rhythmListener: ((Float, Boolean) -> Unit)? = null

    fun setPcmListener(listener: ((ByteArray) -> Unit)?) {
        pcmListener = listener
    }

    fun setRhythmListener(listener: ((Float, Boolean) -> Unit)?) {
        rhythmListener = listener
    }

    fun onPcmData(bytes: ByteArray) {
        pcmListener?.invoke(bytes)
    }

    fun onRhythmTick(energy: Float, isPlaying: Boolean) {
        rhythmListener?.invoke(energy, isPlaying)
    }
}
