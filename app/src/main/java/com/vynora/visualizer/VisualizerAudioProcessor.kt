package com.vynora.visualizer

import androidx.annotation.OptIn
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer

@OptIn(UnstableApi::class)
class VisualizerAudioProcessor(
    private val onPcmData: ((ByteArray) -> Unit)? = null
) : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val sampleSize = remaining.coerceAtMost(512)
        val pcmChunk = ByteArray(sampleSize)
        val pos = inputBuffer.position()
        inputBuffer.get(pcmChunk, 0, sampleSize)
        inputBuffer.position(pos)

        onPcmData?.invoke(pcmChunk)
        AudioDataBridge.onPcmData(pcmChunk)

        val buffer = replaceOutputBuffer(remaining)
        buffer.put(inputBuffer)
        buffer.flip()
    }
}
