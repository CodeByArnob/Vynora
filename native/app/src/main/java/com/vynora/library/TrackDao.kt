package com.vynora.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<AudioTrack>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun observeFavorites(): Flow<List<AudioTrack>>

    @Upsert
    suspend fun upsertAll(tracks: List<AudioTrack>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Query("DELETE FROM tracks WHERE uri NOT IN (:activeUris)")
    suspend fun deleteMissing(activeUris: List<String>)

    @Query("UPDATE tracks SET isFavorite = :favorite WHERE uri = :uri")
    suspend fun setFavorite(uri: String, favorite: Boolean)

    @Query("SELECT uri FROM tracks WHERE isFavorite = 1")
    suspend fun getFavoriteUris(): List<String>

    @Query("SELECT COUNT(*) FROM tracks")
    fun observeCount(): Flow<Int>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAtEpochMs DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrack(item: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackUri = :trackUri")
    suspend fun removeTrack(playlistId: Long, trackUri: String)

    @Query("SELECT tracks.* FROM tracks INNER JOIN playlist_tracks ON tracks.uri = playlist_tracks.trackUri WHERE playlist_tracks.playlistId = :playlistId ORDER BY playlist_tracks.position ASC")
    fun observePlaylistTracks(playlistId: Long): Flow<List<AudioTrack>>

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)
}
