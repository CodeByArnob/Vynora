package com.vynora.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * High-performance audio streaming cache using AndroidX Media3 SimpleCache.
 * Caches streaming chunks locally with an LRU eviction policy so that tracks switch
 * instantly without repeated network roundtrips.
 */
@OptIn(UnstableApi::class)
object VynoraAudioCache {
    private const val MAX_CACHE_SIZE_BYTES = 256L * 1024L * 1024L // 256 MB LRU cache
    private var simpleCache: SimpleCache? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null

    @Synchronized
    fun getCache(context: Context): SimpleCache {
        val existing = simpleCache
        if (existing != null) return existing

        val appContext = context.applicationContext
        val cacheFolder = File(appContext.cacheDir, "vynora_audio_cache").apply {
            if (!exists()) mkdirs()
        }
        val dbProvider = StandaloneDatabaseProvider(appContext).also { databaseProvider = it }
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
        val created = SimpleCache(cacheFolder, evictor, dbProvider)
        simpleCache = created
        return created
    }

    @Synchronized
    fun createDataSourceFactory(context: Context): DataSource.Factory {
        val appContext = context.applicationContext
        val cache = getCache(appContext)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Vynora/0.7.0 (Android; Mobile; Media3)")
            .setConnectTimeoutMs(8_000)
            .setReadTimeoutMs(15_000)
            .setAllowCrossProtocolRedirects(true)

        val upstreamFactory = DefaultDataSource.Factory(appContext, httpDataSourceFactory)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Synchronized
    fun release() {
        try {
            simpleCache?.release()
        } catch (_: Exception) {}
        simpleCache = null
        databaseProvider = null
    }
}
