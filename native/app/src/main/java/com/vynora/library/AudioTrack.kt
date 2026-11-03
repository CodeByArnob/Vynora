package com.vynora.library

import android.net.Uri

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri
) {
    val durationLabel: String
        get() {
            val totalSeconds = durationMs.coerceAtLeast(0L) / 1_000
            return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
}
