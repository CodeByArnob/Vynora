package com.vynora.youtube

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeModelsTest {
    @Test
    fun `YouTubeItem formats official release vs upload date`() {
        val item = YouTubeItem(
            id = "abc123",
            title = "Kesariya 2026 Official Video",
            channelTitle = "T-Series",
            thumbnailUrl = "https://example.com/thumb.jpg",
            description = "Official Hindi release 2026",
            publishedAt = "2026-10-18T12:00:00Z",
            releaseYear = "2026",
            isOfficialChannel = true
        )
        assertFalse(item.isPlaylist)
        assertEquals("Official · Release 2026 · 2026-10-18", item.releaseVsUploadLabel)
    }

    @Test
    fun `YouTubeMusicFilter excludes Shorts, status videos, ringtones and template placeholders`() {
        assertFalse(YouTubeMusicFilter.isGenuineMusicResult("New Song #shorts", ""))
        assertFalse(YouTubeMusicFilter.isGenuineMusicResult("Romantic WhatsApp Status Video", "30 sec status"))
        assertFalse(YouTubeMusicFilter.isGenuineMusicResult("Teri Yaadon Mein | [Artist/Singer Name] | Latest Hindi Song", ""))
        assertTrue(YouTubeMusicFilter.isGenuineMusicResult("Arijit Singh - O Maahi (Official Music Video)", "Latest Hindi Release", "T-Series"))
        assertTrue(YouTubeMusicFilter.isOfficialMusicChannel("T-Series", "New Hindi Song"))
    }
}
