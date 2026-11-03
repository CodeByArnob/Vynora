package com.vynora.playback

import android.content.ComponentName
import android.content.Context
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
    val durationMs: Long = 0L
)

@UnstableApi
class PlaybackController(context: Context) {
    var snapshot by mutableStateOf(PlaybackSnapshot())
        private set
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val appContext = context.applicationContext

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = refreshSnapshot()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = refreshSnapshot()
        override fun onPlaybackStateChanged(playbackState: Int) = refreshSnapshot()
    }

    init {
        val token = SessionToken(
            appContext,
            ComponentName(appContext, VynoraPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(appContext, token).buildAsync().also { future ->
            future.addListener({
                try {
                    controller = future.get().also { it.addListener(listener) }
                    refreshSnapshot()
                } catch (_: ExecutionException) {
                    snapshot = PlaybackSnapshot(title = "Player unavailable", artist = "Restart the app and try again")
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }, ContextCompat.getMainExecutor(appContext))
        }
    }

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int) {
        val player = controller ?: return
        if (tracks.isEmpty() || startIndex !in tracks.indices) return
        val items = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
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

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
    }

    private fun refreshSnapshot() {
        val player = controller ?: return
        val metadata = player.mediaMetadata
        snapshot = PlaybackSnapshot(
            title = metadata.title?.toString() ?: "Nothing playing",
            artist = metadata.artist?.toString() ?: "Unknown artist",
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0L } ?: 0L
        )
    }

    fun release() {
        controller?.removeListener(listener)
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
