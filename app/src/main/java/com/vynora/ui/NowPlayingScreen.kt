package com.vynora.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.vynora.library.AudioTrack
import com.vynora.playback.PlaybackController
import com.vynora.playback.PlaybackSnapshot
import com.vynora.playback.PlaybackStatus
import com.vynora.visualizer.BassReactiveVisualizer
import com.vynora.visualizer.VisualizerDataProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    snapshot: PlaybackSnapshot,
    controller: PlaybackController,
    visualizerFrame: VisualizerDataProcessor.Frame,
    onBack: () -> Unit,
    onToggleFavorite: ((AudioTrack) -> Unit)? = null,
    isCurrentFavorite: Boolean = false,
    onRequestRecordPermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var extractedColors by remember { mutableStateOf(ExtractedColors()) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Extract dynamic ambient background palette from artwork
    LaunchedEffect(snapshot.artworkUri) {
        extractedColors = PaletteExtractor.extractFromUri(context, snapshot.artworkUri)
    }

    val dynamicAccent = extractedColors.dominant
    val backgroundColors = extractedColors.backgroundGradient

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar: Collapse Button, Title Badge, Queue Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PLAYING FROM QUEUE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        snapshot.album.ifBlank { "Vynora Music" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { showQueueSheet = true }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Show Queue",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 2. High-Resolution Square Artwork Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), ambientColor = dynamicAccent)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF131A26)),
                contentAlignment = Alignment.Center
            ) {
                if (!snapshot.artworkUri.isNullOrBlank()) {
                    AsyncImage(
                        model = snapshot.artworkUri,
                        contentDescription = snapshot.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(dynamicAccent.copy(alpha = 0.6f), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Album,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3. Real-Time Bass-Reactive Visualizer
            BassReactiveVisualizer(
                frame = visualizerFrame,
                height = 72.dp,
                accentColor = dynamicAccent,
                secondaryColor = extractedColors.secondary,
                onRequestPermission = onRequestRecordPermission,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(10.dp))

            // 4. Song Metadata & Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        snapshot.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        snapshot.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val currentTrack = snapshot.queue.getOrNull(snapshot.currentIndex)
                IconButton(onClick = {
                    if (currentTrack != null) onToggleFavorite?.invoke(currentTrack)
                }) {
                    Icon(
                        if (isCurrentFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isCurrentFavorite) Color(0xFF1DB954) else Color(0xFF94A3B8),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 5. Error State Banner (if playback encountered an error)
            AnimatedVisibility(
                visible = snapshot.status == PlaybackStatus.ERROR && snapshot.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3B151E)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            snapshot.errorMessage ?: "Playback error",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF8080),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = controller::retry) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color(0xFFFF8080), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Retry", color = Color(0xFFFF8080), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. Interactive Seek Slider with Exact Time Indicators
            Column(modifier = Modifier.fillMaxWidth()) {
                val duration = snapshot.durationMs.coerceAtLeast(1L)
                val currentPos = snapshot.positionMs.coerceIn(0L, duration)

                Slider(
                    value = currentPos.toFloat(),
                    onValueChange = { controller.seekTo(it.toLong()) },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = dynamicAccent,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatTime(currentPos),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        formatTime(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 7. Tactical Controls Row (Shuffle, Prev, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = controller::toggleShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Toggle Shuffle",
                        tint = if (snapshot.shuffleEnabled) dynamicAccent else Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = controller::skipPrevious) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Play / Pause / Buffering FAB
                Surface(
                    shape = CircleShape,
                    color = dynamicAccent,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(
                        modifier = Modifier.clickable(onClick = controller::togglePlayback),
                        contentAlignment = Alignment.Center
                    ) {
                        when (snapshot.status) {
                            PlaybackStatus.BUFFERING, PlaybackStatus.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFF07090E),
                                    strokeWidth = 3.dp
                                )
                            }
                            else -> {
                                Icon(
                                    if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color(0xFF07090E),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = controller::skipNext) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(onClick = controller::cycleRepeatMode) {
                    Icon(
                        when (snapshot.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Cycle Repeat",
                        tint = if (snapshot.repeatMode != Player.REPEAT_MODE_OFF) dynamicAccent else Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 8. Sleep Timer Pill
            Surface(
                modifier = Modifier.clickable { showSleepTimer = true },
                shape = RoundedCornerShape(50),
                color = Color(0xFF131E2A)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = if (snapshot.sleepTimerRemainingMs != null) dynamicAccent else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        snapshot.sleepTimerRemainingMs?.let { "Sleep Timer: ${formatTime(it)}" }
                            ?: "Set Sleep Timer",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (snapshot.sleepTimerRemainingMs != null) dynamicAccent else Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Draggable Queue
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF0F1522),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Up Next (${snapshot.queue.size} songs)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showQueueSheet = false }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close queue", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (snapshot.queue.isEmpty()) {
                    Text(
                        "Queue is currently empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(snapshot.queue) { index, track ->
                            val isCurrent = index == snapshot.currentIndex
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        controller.playTrackAt(index)
                                        showQueueSheet = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) Color(0xFF162E20) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrent) {
                                        Icon(
                                            Icons.Rounded.GraphicEq,
                                            contentDescription = "Playing",
                                            tint = Color(0xFF1DB954),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                    } else {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.width(24.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            track.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isCurrent) Color(0xFF1DB954) else Color.White,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            track.artist,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        track.durationLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sleep Timer Selection Dialog
    if (showSleepTimer) {
        AlertDialog(
            onDismissRequest = { showSleepTimer = false },
            title = { Text("Sleep Timer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15L, 30L, 45L, 60L).forEach { minutes ->
                        TextButton(
                            onClick = {
                                controller.setSleepTimer(minutes * 60_000L)
                                showSleepTimer = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "$minutes Minutes",
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (snapshot.sleepTimerRemainingMs != null) {
                        TextButton(
                            onClick = {
                                controller.cancelSleepTimer()
                                showSleepTimer = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Turn Off Timer",
                                color = Color(0xFFFF6B6B),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimer = false }) { Text("Close") }
            }
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
