package com.vynora.library

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioTrackTest {
    @Test
    fun durationLabelFormatsMinutesAndSeconds() {
        val track = AudioTrack(
            id = 1L,
            title = "Test",
            artist = "Artist",
            album = "Album",
            durationMs = 185_000L,
            uri = Uri.parse("content://media/external/audio/media/1")
        )
        assertEquals("3:05", track.durationLabel)
    }

    @Test
    fun negativeDurationIsSafelyClamped() {
        val track = AudioTrack(
            id = 2L,
            title = "Test",
            artist = "Artist",
            album = "Album",
            durationMs = -1L,
            uri = Uri.parse("content://media/external/audio/media/2")
        )
        assertEquals("0:00", track.durationLabel)
    }
}
