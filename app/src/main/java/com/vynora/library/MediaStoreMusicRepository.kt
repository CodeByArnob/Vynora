package com.vynora.library

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreMusicRepository(private val context: Context) {
    suspend fun scanAudio(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DATE_ADDED,
        ) + if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            arrayOf(MediaStore.Audio.Media.RELATIVE_PATH)
        } else emptyArray()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val tracks = mutableListOf<AudioTrack>()

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val relativePathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val duration = cursor.getLong(durationColumn)
                if (duration <= 0L) continue
                val uri = ContentUris.withAppendedId(collection, id).toString()
                tracks += AudioTrack(
                    uri = uri,
                    id = id,
                    title = cursor.getString(titleColumn)?.takeIf(String::isNotBlank) ?: "Unknown title",
                    artist = cursor.getString(artistColumn)?.takeIf(String::isNotBlank) ?: "Unknown artist",
                    album = cursor.getString(albumColumn)?.takeIf(String::isNotBlank) ?: "Unknown album",
                    durationMs = duration,
                    albumId = cursor.getLong(albumIdColumn),
                    artistId = cursor.getLong(artistIdColumn),
                    dateAddedSeconds = cursor.getLong(dateAddedColumn),
                    relativePath = relativePathColumn.takeIf { it >= 0 }?.let { cursor.getString(it) },
                    isFavorite = false
                )
            }
        }
        tracks
    }
}
