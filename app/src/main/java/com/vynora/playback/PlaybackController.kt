package com.vynora.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vynora.library.AudioTrack
import java.util.concurrent.ExecutionException

enum class PlaybackStatus {
    IDLE,
    LOADING,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

@Stable
data class PlaybackSnapshot(
    val title: String = "Nothing playing",
    val artist: String = "Choose a song from your library",
    val album: String = "",
    val artworkUri: String? = null,
    val isPlaying: Boolean = false,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val sleepTimerRemainingMs: Long? = null,
    val audioSessionId: Int = 0,
    val errorMessage: String? = null,
    val queue: List<AudioTrack> = emptyList(),
    val currentIndex: Int = -1
)

@OptIn(UnstableApi::class)
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
    private var currentQueueTracks: List<AudioTrack> = emptyList()
    private var cachedAudioSessionId: Int = 0
    private var activeError: String? = null

    // Smooth UI progress update (every 400ms when playing)
    private val progressTicker = object : Runnable {
        override fun run() {
            val player = controller
            val active = (player?.isPlaying == true) || sleepTimerEndAt != null
            if (active) {
                refreshSnapshot()
                mainHandler.postDelayed(this, 400L)
            }
        }
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                PlaybackPerformanceMonitor.onAudioStarted()
                requestAudioSessionId()
            }
            refreshSnapshot()
            mainHandler.removeCallbacks(progressTicker)
            if (isPlaying || sleepTimerEndAt != null) {
                mainHandler.post(progressTicker)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    activeError = null
                    PlaybackPerformanceMonitor.onAudioStarted()
                    requestAudioSessionId()
                    controller?.let { c ->
                        VynoraPreloadManager.preloadNextTracks(appContext, currentQueueTracks, c.currentMediaItemIndex)
                    }
                }
                Player.STATE_BUFFERING -> {
                    PlaybackPerformanceMonitor.onBufferingEvent()
                }
            }
            refreshSnapshot()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            activeError = null
            requestAudioSessionId()
            controller?.let { c ->
                VynoraPreloadManager.preloadNextTracks(appContext, currentQueueTracks, c.currentMediaItemIndex)
            }
            refreshSnapshot()
        }

        override fun onPlayerError(error: PlaybackException) {
            PlaybackPerformanceMonitor.onFailureEvent()
            val message = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Network connection lost. Tap retry to reconnect."
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "Audio stream unavailable (HTTP ${error.errorCodeName})."
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                    "Audio format unsupported or corrupt."
                else -> error.localizedMessage ?: "Playback error encountered."
            }
            activeError = message
            refreshSnapshot()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = refreshSnapshot()
        override fun onRepeatModeChanged(repeatMode: Int) = refreshSnapshot()
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, VynoraPlaybackService::class.java))
        controllerFuture = MediaController.Builder(appContext, token).buildAsync().also { future ->
            future.addListener({
                try {
                    val ctrl = future.get()
                    controller = ctrl
                    ctrl.addListener(listener)
                    requestAudioSessionId()
                    refreshSnapshot()
                    pendingQueue?.let { (tracks, index) ->
                        pendingQueue = null
                        startQueue(tracks, index)
                    }
                } catch (_: ExecutionException) {
                    snapshot = PlaybackSnapshot(
                        title = "Player unavailable",
                        artist = "Restart the app and try again",
                        status = PlaybackStatus.ERROR,
                        errorMessage = "Failed to connect to playback service."
                    )
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }, ContextCompat.getMainExecutor(appContext))
        }
    }

    private fun requestAudioSessionId() {
        val player = controller ?: return
        val extras = player.sessionExtras
        val fromExtras = extras.getInt(VynoraPlaybackService.KEY_AUDIO_SESSION_ID, 0)
        if (fromExtras > 0) {
            cachedAudioSessionId = fromExtras
            return
        }

        val command = SessionCommand(VynoraPlaybackService.ACTION_GET_AUDIO_SESSION_ID, Bundle.EMPTY)
        val future = player.sendCustomCommand(command, Bundle.EMPTY)
        future.addListener({
            try {
                val result = future.get()
                val id = result.extras.getInt(VynoraPlaybackService.KEY_AUDIO_SESSION_ID, 0)
                if (id > 0) {
                    cachedAudioSessionId = id
                    refreshSnapshot()
                }
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int) {
        if (tracks.isEmpty() || startIndex !in tracks.indices) return
        activeError = null
        PlaybackPerformanceMonitor.onPlayInitiated(isTrackSwitch = false)
        PlaybackPerformanceMonitor.recordCacheHit(VynoraPreloadManager.isPreloaded(tracks[startIndex].uri))
        if (controller == null) {
            pendingQueue = tracks to startIndex
            currentQueueTracks = tracks
            snapshot = snapshot.copy(
                status = PlaybackStatus.LOADING,
                queue = tracks,
                currentIndex = startIndex
            )
            return
        }
        startQueue(tracks, startIndex)
    }

    private fun startQueue(tracks: List<AudioTrack>, startIndex: Int) {
        val player = controller ?: return
        currentQueueTracks = tracks

        // Optimization: If the current playlist is identical, seek directly without recreating queue items
        val sameQueue = player.mediaItemCount == tracks.size &&
            player.mediaItemCount > 0 &&
            (0 until player.mediaItemCount).all { i ->
                player.getMediaItemAt(i).mediaId == tracks[i].uri
            }

        if (sameQueue) {
            player.seekToDefaultPosition(startIndex)
            player.play()
            refreshSnapshot()
            return
        }

        val items = tracks.map { track ->
            val builder = MediaItem.Builder()
                .setMediaId(track.uri)
                .setUri(track.uri.toUri())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .apply {
                            val art = track.artworkUri ?: track.albumArtUri
                            if (art.isNotBlank()) {
                                setArtworkUri(art.toUri())
                            }
                        }
                        .build()
                )
            builder.build()
        }

        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
        requestAudioSessionId()
        refreshSnapshot()
    }

    fun playTrackAt(index: Int) {
        val player = controller ?: return
        PlaybackPerformanceMonitor.onPlayInitiated(isTrackSwitch = true)
        if (index in currentQueueTracks.indices) {
            PlaybackPerformanceMonitor.recordCacheHit(VynoraPreloadManager.isPreloaded(currentQueueTracks[index].uri))
        }
        if (index in 0 until player.mediaItemCount) {
            player.seekToDefaultPosition(index)
            player.play()
            refreshSnapshot()
        } else if (index in currentQueueTracks.indices) {
            startQueue(currentQueueTracks, index)
        }
    }

    fun retry() {
        val player = controller ?: return
        activeError = null
        player.prepare()
        player.play()
        refreshSnapshot()
    }

    fun pause() {
        controller?.pause()
    }

    fun play() {
        activeError = null
        controller?.play()
    }

    fun togglePlayback() {
        val player = controller ?: return
        activeError = null
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipNext() {
        val player = controller ?: return
        activeError = null
        PlaybackPerformanceMonitor.onPlayInitiated(isTrackSwitch = true)
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (player.mediaItemCount > 0) {
            player.seekToDefaultPosition(0)
        }
    }

    fun skipPrevious() {
        val player = controller ?: return
        activeError = null
        PlaybackPerformanceMonitor.onPlayInitiated(isTrackSwitch = true)
        if (player.currentPosition > 3_000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else if (player.mediaItemCount > 0) {
            player.seekToDefaultPosition(player.mediaItemCount - 1)
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        refreshSnapshot()
    }

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
        mainHandler.removeCallbacks(progressTicker)
        mainHandler.post(progressTicker)
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
        val player = controller
        if (player == null) {
            if (pendingQueue != null) {
                snapshot = snapshot.copy(status = PlaybackStatus.LOADING)
            }
            return
        }

        val metadata = player.mediaMetadata
        val playbackState = player.playbackState
        val isPlaying = player.isPlaying

        val status = when {
            activeError != null -> PlaybackStatus.ERROR
            playbackState == Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
            playbackState == Player.STATE_IDLE && player.mediaItemCount == 0 -> PlaybackStatus.IDLE
            playbackState == Player.STATE_IDLE && activeError != null -> PlaybackStatus.ERROR
            isPlaying -> PlaybackStatus.PLAYING
            playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
            playbackState == Player.STATE_ENDED -> PlaybackStatus.IDLE
            else -> PlaybackStatus.IDLE
        }

        val currentIdx = player.currentMediaItemIndex

        val artwork = metadata.artworkUri?.toString()
            ?: currentQueueTracks.getOrNull(currentIdx)?.artworkUri
            ?: currentQueueTracks.getOrNull(currentIdx)?.albumArtUri

        val nextSnapshot = PlaybackSnapshot(
            title = metadata.title?.toString()
                ?: currentQueueTracks.getOrNull(currentIdx)?.title
                ?: "Nothing playing",
            artist = metadata.artist?.toString()
                ?: currentQueueTracks.getOrNull(currentIdx)?.artist
                ?: "Choose a song from your library",
            album = metadata.albumTitle?.toString()
                ?: currentQueueTracks.getOrNull(currentIdx)?.album
                ?: "",
            artworkUri = artwork,
            isPlaying = isPlaying,
            status = status,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0L }
                ?: currentQueueTracks.getOrNull(currentIdx)?.durationMs
                ?: 0L,
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            sleepTimerRemainingMs = sleepTimerEndAt?.let { (it - SystemClock.elapsedRealtime()).coerceAtLeast(0L) },
            audioSessionId = cachedAudioSessionId,
            errorMessage = activeError,
            queue = currentQueueTracks,
            currentIndex = currentIdx
        )

        if (nextSnapshot != snapshot) {
            snapshot = nextSnapshot
        }
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
