package com.vynora.library

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryGroupingTest {
    private fun track(
        id: Long,
        title: String,
        artist: String,
        album: String,
        folder: String?
    ) = AudioTrack(
        uri = "content://audio/$id",
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 180_000L,
        albumId = id,
        artistId = id,
        dateAddedSeconds = id,
        relativePath = folder
    )

    @Test
    fun albumsAreSeparatedByAlbumAndArtist() {
        val tracks = listOf(
            track(1, "Song A", "Artist A", "Same Album", "Music/A"),
            track(2, "Song B", "Artist A", "Same Album", "Music/A"),
            track(3, "Song C", "Artist B", "Same Album", "Music/B")
        )
        val groups = LibraryGrouping.group(tracks.withIndex().toList(), "Albums")
        assertEquals(2, groups.size)
        assertEquals(2, groups.first { it.name.contains("Artist A") }.tracks.size)
    }

    @Test
    fun artistsGroupAcrossAlbums() {
        val tracks = listOf(
            track(1, "Song A", "Artist A", "Album A", "Music/A"),
            track(2, "Song B", "Artist A", "Album B", "Music/B"),
            track(3, "Song C", "Artist B", "Album C", "Music/C")
        )
        val groups = LibraryGrouping.group(tracks.withIndex().toList(), "Artists")
        assertEquals(2, groups.size)
        assertEquals(2, groups.first { it.name == "Artist A" }.tracks.size)
    }

    @Test
    fun foldersUseLastPathSegmentAndHandleMissingPath() {
        val tracks = listOf(
            track(1, "Song A", "Artist A", "Album A", "Music/Downloads/"),
            track(2, "Song B", "Artist A", "Album B", "Music/Downloads/"),
            track(3, "Song C", "Artist B", "Album C", null)
        )
        val groups = LibraryGrouping.group(tracks.withIndex().toList(), "Folders")
        assertEquals(2, groups.size)
        assertEquals(2, groups.first { it.name == "Downloads" }.tracks.size)
        assertEquals(1, groups.first { it.name == "Unknown folder" }.tracks.size)
    }

    @Test
    fun groupedTracksRetainOriginalQueueIndices() {
        val tracks = listOf(
            track(1, "Song A", "Artist A", "Album A", null),
            track(2, "Song B", "Artist B", "Album B", null),
            track(3, "Song C", "Artist A", "Album A", null)
        )
        val group = LibraryGrouping.group(tracks.withIndex().toList(), "Artists")
            .first { it.name == "Artist A" }
        assertEquals(listOf(0, 2), group.tracks.map { it.index })
    }
}
