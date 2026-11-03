package com.vynora.library

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaStoreMusicRepository(private val context: Context) {
    fun scanAudio(): List<AudioTrack> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
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

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)?.takeIf(String::isNotBlank) ?: "Unknown title"
                val artist = cursor.getString(artistColumn)?.takeIf(String::isNotBlank) ?: "Unknown artist"
                val album = cursor.getString(albumColumn)?.takeIf(String::isNotBlank) ?: "Unknown album"
                val duration = cursor.getLong(durationColumn)
                if (duration <= 0L) continue

                tracks += AudioTrack(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = duration,
                    uri = ContentUris.withAppendedId(collection, id)
                )
            }
        }
        return tracks
    }
}
