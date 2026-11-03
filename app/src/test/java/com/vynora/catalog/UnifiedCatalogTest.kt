package com.vynora.catalog

import com.vynora.archive.ArchiveAudioRepository
import com.vynora.audius.AudiusTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedCatalogTest {

    @Test
    fun testCuratedBollywoodAlbumsHaveValidStreamsAndMetadata() {
        val repo = UnifiedCatalogRepository()
        val albums = repo.getCuratedBollywoodAlbums()

        assertTrue("Expected curated albums to be populated", albums.size >= 5)

        albums.forEach { album ->
            assertFalse("Album id cannot be blank", album.id.isBlank())
            assertFalse("Album title cannot be blank", album.title.isBlank())
            assertFalse("Album artist cannot be blank", album.artist.isBlank())
            assertFalse("Album coverUri cannot be blank", album.coverArtUri.isBlank())
            assertTrue("Album must contain at least 2 tracks", album.tracks.size >= 2)

            album.tracks.forEach { track ->
                assertFalse("Track id cannot be blank", track.id.isBlank())
                assertFalse("Track title cannot be blank", track.title.isBlank())
                assertFalse("Track artist cannot be blank", track.artist.isBlank())
                assertTrue("Stream URL must be valid HTTP/HTTPS: ${track.streamUrl}", track.streamUrl.startsWith("http://") || track.streamUrl.startsWith("https://"))
                assertFalse("Stream URL must NOT be a dummy uncut placeholder", track.streamUrl.contains("uncut_"))
                assertTrue("Duration must be positive", track.durationMs > 0L)
            }
        }
    }

    @Test
    fun testArchiveCuratedAlbumsHavePlayableStreams() {
        val archiveRepo = ArchiveAudioRepository()
        val albums = archiveRepo.getCuratedAlbums()

        assertTrue("Expected Archive.org curated albums", albums.isNotEmpty())
        albums.forEach { album ->
            assertEquals(AudioSourceProvider.ARCHIVE_ORG, album.provider)
            album.tracks.forEach { track ->
                assertTrue(track.streamUrl.startsWith("https://"))
                assertNotNull(track.artworkUrl)
                assertTrue("License must indicate public domain or open audio: ${track.license}", track.license.contains("Public Domain") || track.license.contains("Creative Commons"))
            }
        }
    }

    @Test
    fun testAudiusTrackConversion() {
        val track = AudiusTrack(
            id = "test_123",
            title = "Chill Sunset",
            artist = "Vynora Artist",
            artworkUrl = "https://example.com/art.jpg",
            durationSeconds = 180,
            genre = "Lo-Fi",
            releaseDate = "2026-10-01"
        )

        val streamUrl = "https://api.audius.co/v1/tracks/test_123/stream?app_name=Vynora"
        val unified = track.toUnifiedTrack(streamUrl)

        assertEquals("audius_test_123", unified.id)
        assertEquals("Chill Sunset", unified.title)
        assertEquals(streamUrl, unified.streamUrl)
        assertEquals(AudioSourceProvider.AUDIUS, unified.provider)
        assertEquals("3:00", unified.durationLabel)

        val audioTrack = unified.toAudioTrack()
        assertEquals(streamUrl, audioTrack.uri)
        assertEquals("https://example.com/art.jpg", audioTrack.artworkUri)
    }
}
