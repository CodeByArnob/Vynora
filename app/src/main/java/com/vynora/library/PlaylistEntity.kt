package com.vynora.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackUri"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AudioTrack::class,
            parentColumns = ["uri"],
            childColumns = ["trackUri"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trackUri"]), Index(value = ["playlistId", "position"])]
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackUri: String,
    val position: Int
)
