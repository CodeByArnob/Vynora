package com.vynora.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vynora.library.AudioTrack
import java.util.concurrent.ExecutionException

data class PlaybackSnapshot(
    val title: String = "Nothing playing",
    val artist: String = "Choose a song from your library",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val sleepTimerRemainingMs: Long? = null
)

@UnstableApi
class PlaybackController(context: Context) {
    var snapshot by mutableStateOf(PlaybackSnapshot())
        private set

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var pendingQueue: Pair<List<AudioTrack>, Int>? = null
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var sleepTimerEndAt: Long? = null
    private var sleepTimerRunnable: Runnable? = null

    private val progressTicker = object : Runnable {
        override fun run() {
            refreshSnapshot()
            mainHandler.postDelayed(this, 500L)
        }
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = refreshSnapshot()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = refreshSnapshot()
        override fun onPlaybackStateChanged(playbackState: Int) = refreshSnapshot()
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = refreshSnapshot()
        override fun onRepeatModeChanged(repeatMode: Int) = refreshSnapshot()
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, VynoraPlaybackService::class.java))
        controllerFuture = MediaController.Builder(appContext, token).buildAsync().also { future ->
            future.addListener({
                try {
                    controller = future.get().also { it.addListener(listener) }
                    refreshSnapshot()
                    pendingQueue?.let { (tracks, index) ->
                        pendingQueue = null
                        startQueue(tracks, index)
                    }
                } catch (_: ExecutionException) {
                    snapshot = PlaybackSnapshot(
                        title = "Player unavailable",
                        artist = "Restart the app and try again"
                    )
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }, ContextCompat.getMainExecutor(appContext))
        }
        mainHandler.post(progressTicker)
    }

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int) {
        if (tracks.isEmpty() || startIndex !in tracks.indices) return
        if (controller == null) {
            pendingQueue = tracks to startIndex
            return
        }
        startQueue(tracks, startIndex)
    }

    private fun startQueue(tracks: List<AudioTrack>, startIndex: Int) {
        val player = controller ?: return
        val items = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.uri)
                .setUri(track.uri.toUri())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .build()
                )
                .build()
        }
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun togglePlayback() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun skipNext() { controller?.seekToNextMediaItem() }
    fun skipPrevious() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs.coerceAtLeast(0L)) }

    fun setSleepTimer(durationMs: Long) {
        cancelSleepTimer()
        if (durationMs <= 0L) return
        sleepTimerEndAt = SystemClock.elapsedRealtime() + durationMs
        val task = Runnable {
            controller?.pause()
            sleepTimerEndAt = null
            sleepTimerRunnable = null
            refreshSnapshot()
        }
        sleepTimerRunnable = task
        mainHandler.postDelayed(task, durationMs)
        refreshSnapshot()
    }

    fun cancelSleepTimer() {
        sleepTimerRunnable?.let(mainHandler::removeCallbacks)
        sleepTimerRunnable = null
        sleepTimerEndAt = null
        refreshSnapshot()
    }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
        refreshSnapshot()
    }

    fun cycleRepeatMode() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
        refreshSnapshot()
    }

    private fun refreshSnapshot() {
        val player = controller ?: return
        val metadata = player.mediaMetadata
        snapshot = PlaybackSnapshot(
            title = metadata.title?.toString() ?: "Nothing playing",
            artist = metadata.artist?.toString() ?: "Unknown artist",
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0L } ?: 0L,
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            sleepTimerRemainingMs = sleepTimerEndAt?.let { (it - SystemClock.elapsedRealtime()).coerceAtLeast(0L) }
        )
    }

    fun release() {
        mainHandler.removeCallbacks(progressTicker)
        cancelSleepTimer()
        controller?.removeListener(listener)
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
