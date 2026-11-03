package com.vynora.catalog

import com.vynora.library.AudioTrack

enum class AudioSourceProvider(val displayName: String, val badgeColorHex: Long) {
    AUDIUS("Audius Stream", 0xFFCC3399),
    LOCAL_DEVICE("Device Storage", 0xFF1DB954),
    ARCHIVE_ORG("Archive.org Open Audio", 0xFFE67E22),
    YOUTUBE_VIDEO("YouTube Video", 0xFFFF0000)
}

data class UnifiedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val streamUrl: String,
    val artworkUrl: String? = null,
    val provider: AudioSourceProvider = AudioSourceProvider.AUDIUS,
    val genre: String = "Music",
    val releaseYear: String? = null,
    val license: String = "Public / Streamable",
    val optionalYouTubeVideoId: String? = null
) {
    val durationLabel: String
        get() {
            val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000L)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    fun toAudioTrack(): AudioTrack = AudioTrack(
        id = id.hashCode().toLong(),
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        uri = streamUrl,
        albumId = 0L,
        artistId = 0L,
        dateAddedSeconds = System.currentTimeMillis() / 1000L,
        relativePath = provider.displayName,
        isFavorite = false
    ).apply {
        if (!artworkUrl.isNullOrBlank()) {
            customArtworkUri = artworkUrl
        }
    }
}

data class AlbumModel(
    val id: String,
    val title: String,
    val artist: String,
    val coverArtUri: String,
    val releaseYear: String? = null,
    val genre: String = "Various",
    val provider: AudioSourceProvider = AudioSourceProvider.AUDIUS,
    val description: String = "",
    val tracks: List<UnifiedTrack> = emptyList(),
    val curator: String = "Vynora Curated"
) {
    val trackCountLabel: String
        get() = "${tracks.size} tracks"
}

data class ArtistModel(
    val id: String,
    val name: String,
    val artworkUri: String? = null,
    val bio: String? = null,
    val albums: List<AlbumModel> = emptyList(),
    val topTracks: List<UnifiedTrack> = emptyList()
)
