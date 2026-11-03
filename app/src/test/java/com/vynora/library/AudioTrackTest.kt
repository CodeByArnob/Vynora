package com.vynora.library

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioTrackTest {
    @Test
    fun durationLabelFormatsMinutesAndSeconds() {
        val track = AudioTrack(
            uri = "content://media/external/audio/media/1", id = 1L, title = "Test",
            artist = "Artist", album = "Album", durationMs = 185_000L,
            albumId = 1L, artistId = 1L, dateAddedSeconds = 0L, relativePath = null
        )
        assertEquals("3:05", track.durationLabel)
    }

    @Test
    fun negativeDurationIsSafelyClamped() {
        val track = AudioTrack(
            uri = "content://media/external/audio/media/2", id = 2L, title = "Test",
            artist = "Artist", album = "Album", durationMs = -1L,
            albumId = 1L, artistId = 1L, dateAddedSeconds = 0L, relativePath = null
        )
        assertEquals("0:00", track.durationLabel)
    }
}
