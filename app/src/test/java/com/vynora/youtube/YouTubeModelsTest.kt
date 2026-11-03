package com.vynora.youtube

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeModelsTest {
    @Test
    fun videoAndPlaylistItemsAreDistinguished() {
        val video = YouTubeItem("abc123", "Song", "Artist", "", "", isPlaylist = false)
        val playlist = YouTubeItem("PL123", "Album", "Channel", "", "", isPlaylist = true)

        assertEquals("abc123", video.id)
        assertFalse(video.isPlaylist)
        assertEquals("PL123", playlist.id)
        assertTrue(playlist.isPlaylist)
    }
}
