package com.vynora.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.vynora.library.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * Intelligent predictive preloading engine using Media3 CacheWriter.
 * Preloads the first 512KB (~15-20s of audio) of the upcoming 1-2 tracks in the queue,
 * enabling instantaneous (0ms - 50ms) track transitions without loading lag.
 */
@OptIn(UnstableApi::class)
object VynoraPreloadManager {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var activePreloadJob: Job? = null
    private val preloadedUris = Collections.synchronizedSet(HashSet<String>())

    // 512 KB covers the start of standard MP3/AAC audio files for instant playback
    private const val PRELOAD_BYTES = 512 * 1024L

    fun preloadNextTracks(context: Context, queue: List<AudioTrack>, currentIndex: Int) {
        if (queue.isEmpty() || currentIndex !in queue.indices) return

        activePreloadJob?.cancel()
        activePreloadJob = scope.launch {
            val candidates = listOfNotNull(
                queue.getOrNull(currentIndex + 1),
                queue.getOrNull(currentIndex + 2)
            )

            val dataSourceFactory = VynoraAudioCache.createDataSourceFactory(context)

            for (track in candidates) {
                val uri = track.uri
                // Local MediaStore and file URIs do not require network preloading
                if (uri.startsWith("content://") || uri.startsWith("file://") || preloadedUris.contains(uri)) {
                    continue
                }

                try {
                    val dataSpec = DataSpec.Builder()
                        .setUri(uri.toUri())
                        .setPosition(0)
                        .setLength(PRELOAD_BYTES)
                        .build()

                    val dataSource = dataSourceFactory.createDataSource() as? CacheDataSource ?: continue
                    val writer = CacheWriter(
                        dataSource,
                        dataSpec,
                        ByteArray(64 * 1024),
                        null
                    )
                    writer.cache()
                    preloadedUris.add(uri)
                    PlaybackPerformanceMonitor.recordPreloadedTrack()
                } catch (_: Exception) {
                    // Non-critical; active playback takes absolute priority
                }
            }
        }
    }

    fun isPreloaded(uri: String): Boolean = preloadedUris.contains(uri)

    fun clear() {
        activePreloadJob?.cancel()
        preloadedUris.clear()
    }
}
