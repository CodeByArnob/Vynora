package com.vynora.audius

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudiusRepositoryTest {
    @Test
    fun `streamUrl builds official Audius V1 stream URL with app_name and api_key`() {
        val repo = AudiusRepository(apiKey = "test_public_key")
        val url = repo.streamUrl("track_99")
        assertTrue(url.startsWith("https://api.audius.co/v1/tracks/track_99/stream?app_name=Vynora"))
        assertTrue(url.contains("api_key=test_public_key"))
    }

    @Test
    fun `AudiusTrack converts to Media3 AudioTrack with millisecond duration`() {
        val track = AudiusTrack(
            id = "ab12",
            title = "Mumbai Midnight Lofi",
            artist = "Vynora Collective",
            artworkUrl = "https://example.com/art.jpg",
            durationSeconds = 185
        )
        val audioTrack = track.toAudioTrack("https://api.audius.co/v1/tracks/ab12/stream?app_name=Vynora")
        assertEquals("Mumbai Midnight Lofi", audioTrack.title)
        assertEquals("Vynora Collective", audioTrack.artist)
        assertEquals(185_000L, audioTrack.durationMs)
    }
}
