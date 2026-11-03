package com.vynora.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    indices = [Index(value = ["title"]), Index(value = ["artist"]), Index(value = ["album"])]
)
data class AudioTrack(
    @PrimaryKey val uri: String,
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumId: Long,
    val artistId: Long,
    val dateAddedSeconds: Long,
    val relativePath: String?,
    val isFavorite: Boolean = false
) {
    val durationLabel: String
        get() {
            val seconds = durationMs.coerceAtLeast(0L) / 1_000
            return "%d:%02d".format(seconds / 60, seconds % 60)
        }
}
