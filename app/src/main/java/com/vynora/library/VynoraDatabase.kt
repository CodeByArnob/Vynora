package com.vynora.library

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AudioTrack::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 1,
    exportSchema = true
)
abstract class VynoraDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var instance: VynoraDatabase? = null

        fun getInstance(context: Context): VynoraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VynoraDatabase::class.java,
                    "vynora.db"
                ).build().also { instance = it }
            }
    }
}
