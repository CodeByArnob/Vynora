package com.vynora.playback

import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSnapshotTest {

    @Test
    fun defaultSnapshot_hasExpectedInitialValues() {
        val snapshot = PlaybackSnapshot()

        assertEquals("Nothing playing", snapshot.title)
        assertEquals("Choose a song from your library", snapshot.artist)
        assertEquals(PlaybackStatus.IDLE, snapshot.status)
        assertFalse(snapshot.isPlaying)
        assertEquals(0L, snapshot.positionMs)
        assertEquals(0L, snapshot.durationMs)
        assertNull(snapshot.sleepTimerRemainingMs)
        assertEquals(Player.REPEAT_MODE_OFF, snapshot.repeatMode)
        assertFalse(snapshot.shuffleEnabled)
    }

    @Test
    fun snapshot_holdsQueueAndIndexAccurately() {
        val snapshot = PlaybackSnapshot(
            title = "Kesariya",
            artist = "Arijit Singh",
            status = PlaybackStatus.PLAYING,
            isPlaying = true,
            positionMs = 35_000L,
            durationMs = 210_000L,
            audioSessionId = 42,
            currentIndex = 2
        )

        assertEquals("Kesariya", snapshot.title)
        assertEquals(PlaybackStatus.PLAYING, snapshot.status)
        assertTrue(snapshot.isPlaying)
        assertEquals(42, snapshot.audioSessionId)
        assertEquals(2, snapshot.currentIndex)
    }
}
