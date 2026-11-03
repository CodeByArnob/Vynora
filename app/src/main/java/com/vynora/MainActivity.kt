package com.vynora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.vynora.library.AudioTrack
import com.vynora.library.LibraryViewModel
import com.vynora.library.PlaylistEntity
import com.vynora.playback.PlaybackController
import com.vynora.playback.PlaybackSnapshot
import com.vynora.playback.PlaybackStatus
import com.vynora.ui.NowPlayingScreen
import com.vynora.visualizer.AudioVisualizerManager
import com.vynora.youtube.YouTubeHomeScreen
import com.vynora.youtube.YouTubeItem
import com.vynora.youtube.YouTubeViewModel
import com.vynora.youtube.YouTubeWatchScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@UnstableApi
class MainActivity : ComponentActivity() {
    private lateinit var playbackController: PlaybackController
    private lateinit var visualizerManager: AudioVisualizerManager
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val youTubeViewModel: YouTubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackController = PlaybackController(applicationContext)
        visualizerManager = AudioVisualizerManager(applicationContext)

        setContent {
            VynoraApp(
                playbackController = playbackController,
                visualizerManager = visualizerManager,
                libraryViewModel = libraryViewModel,
                youTubeViewModel = youTubeViewModel
            )
        }
    }

    override fun onDestroy() {
        visualizerManager.release()
        playbackController.release()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VynoraApp(
    playbackController: PlaybackController,
    visualizerManager: AudioVisualizerManager,
    libraryViewModel: LibraryViewModel,
    youTubeViewModel: YouTubeViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val favorites by libraryViewModel.favorites.collectAsStateWithLifecycle()
    val isScanning by libraryViewModel.isScanning.collectAsStateWithLifecycle()
    val scanError by libraryViewModel.scanError.collectAsStateWithLifecycle()
    val youTubeState by youTubeViewModel.state.collectAsStateWithLifecycle()
    val visualizerFrame by visualizerManager.currentFrame.collectAsStateWithLifecycle()

    var selectedYouTubeItem by remember { mutableStateOf<YouTubeItem?>(null) }
    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snapshot = playbackController.snapshot

    val storagePermission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStoragePermission = granted
        if (granted) libraryViewModel.refreshLibrary()
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (granted && snapshot.audioSessionId > 0) {
            visualizerManager.attachToSession(snapshot.audioSessionId)
        }
    }

    // Attach visualizer when audio session ID becomes available
    LaunchedEffect(snapshot.audioSessionId) {
        if (snapshot.audioSessionId > 0) {
            visualizerManager.attachToSession(snapshot.audioSessionId)
        }
    }

    // Run smooth visualizer animation tick loop during active playback
    LaunchedEffect(snapshot.isPlaying, snapshot.status) {
        if (snapshot.isPlaying || snapshot.status == PlaybackStatus.PLAYING) {
            var startTime = System.currentTimeMillis()
            while (isActive && snapshot.isPlaying) {
                val now = System.currentTimeMillis()
                visualizerManager.tick(now - startTime, isPlaying = true)
                delay(33L) // ~30 FPS for buttery smooth bars and low CPU usage
            }
        } else {
            visualizerManager.tick(0L, isPlaying = false)
        }
    }

    LaunchedEffect(Unit) { youTubeViewModel.loadHome() }
    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) libraryViewModel.refreshLibrary()
    }

    val startAudiusFallbackPlayback: () -> Unit = {
        playbackController.pause()
    }

    val currentAudioTrackFavorite = remember(snapshot.queue, snapshot.currentIndex, favorites) {
        val curr = snapshot.queue.getOrNull(snapshot.currentIndex)
        curr != null && favorites.any { it.uri == curr.uri }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF1DB954),
            onPrimary = Color(0xFF062010),
            secondary = Color(0xFF38BDF8),
            background = Color(0xFF07090E),
            surface = Color(0xFF111622),
            surfaceVariant = Color(0xFF182030)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF07090E),
            topBar = {
                if (selectedTab != 5 && selectedTab != 4) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF07090E)
                        ),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF13281E)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.GraphicEq,
                                            contentDescription = null,
                                            tint = Color(0xFF1DB954),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Vynora",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                youTubeViewModel.searchVideos(youTubeState.activeQuery)
                                youTubeViewModel.loadAudiusTrending()
                                if (hasStoragePermission) libraryViewModel.refreshLibrary()
                            }) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    contentDescription = "Refresh music",
                                    tint = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                Column {
                    if (selectedTab != 4 && selectedTab != 5 &&
                        snapshot.title != "Nothing playing" &&
                        snapshot.title != "Player unavailable"
                    ) {
                        MiniPlayer(
                            snapshot = snapshot,
                            controller = playbackController,
                            onOpen = { selectedTab = 4 }
                        )
                    }
                    NavigationBar(containerColor = Color(0xFF0D121C)) {
                        val tabs = listOf(
                            "Home" to Icons.Rounded.Home,
                            "Library" to Icons.Rounded.LibraryMusic,
                            "Favorites" to Icons.Rounded.Favorite,
                            "Playlists" to Icons.AutoMirrored.Rounded.QueueMusic,
                            "Playing" to Icons.Rounded.GraphicEq
                        )
                        tabs.forEachIndexed { index, pair ->
                            NavigationBarItem(
                                selected = selectedTab == index || (selectedTab == 5 && index == 4),
                                onClick = { selectedTab = index },
                                icon = { Icon(pair.second, contentDescription = null) },
                                label = { Text(pair.first, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> YouTubeHomeScreen(
                    state = youTubeState,
                    onSearchVideos = youTubeViewModel::searchVideos,
                    onSearchPlaylists = youTubeViewModel::searchPlaylists,
                    onSelectCategory = youTubeViewModel::selectCategory,
                    onFilterLocalInstant = youTubeViewModel::filterLocalInstant,
                    onOpen = { item ->
                        playbackController.pause()
                        youTubeViewModel.recordPlayed(item)
                        selectedYouTubeItem = item
                        selectedTab = 5
                    },
                    onPlayAudiusTrack = { audiusTrack ->
                        val streamQueue = youTubeState.audiusTracks.map {
                            it.toAudioTrack(youTubeViewModel.audiusStreamUrl(it.id))
                        }
                        val index = youTubeState.audiusTracks.indexOfFirst { it.id == audiusTrack.id }.coerceAtLeast(0)
                        selectedYouTubeItem = null
                        playbackController.playQueue(streamQueue, index)
                        selectedTab = 4
                    },
                    onSearchAudius = youTubeViewModel::searchAudius,
                    onToggleFavoriteVideo = youTubeViewModel::toggleFavoriteVideo,
                    modifier = Modifier.padding(padding)
                )
                1 -> LibraryScreen(
                    modifier = Modifier.padding(padding),
                    tracks = tracks,
                    query = query,
                    onQueryChange = { query = it },
                    isScanning = isScanning,
                    hasPermission = hasStoragePermission,
                    scanError = scanError,
                    onGrantPermission = { storagePermissionLauncher.launch(storagePermission) },
                    onPlay = { index ->
                        selectedYouTubeItem = null
                        playbackController.playQueue(tracks, index)
                        selectedTab = 4
                    },
                    onRescan = libraryViewModel::refreshLibrary,
                    onFavorite = libraryViewModel::setFavorite
                )
                2 -> TrackCollectionScreen(
                    modifier = Modifier.padding(padding),
                    title = "Favorites",
                    subtitle = "${favorites.size} local songs · ${youTubeState.favoriteVideoIds.size} saved videos",
                    tracks = favorites,
                    onPlay = { index ->
                        selectedYouTubeItem = null
                        playbackController.playQueue(favorites, index)
                        selectedTab = 4
                    },
                    onFavorite = { track -> libraryViewModel.setFavorite(track, false) }
                )
                3 -> PlaylistsScreen(
                    modifier = Modifier.padding(padding),
                    libraryViewModel = libraryViewModel,
                    playbackController = playbackController,
                    onPlayTrack = { selectedTab = 4 }
                )
                4 -> selectedYouTubeItem?.let { item ->
                    YouTubeWatchScreen(
                        item = item,
                        queue = youTubeState.videos,
                        snapshot = snapshot,
                        controller = playbackController,
                        onBack = { selectedTab = 0 },
                        onSelectQueueItem = { next ->
                            youTubeViewModel.recordPlayed(next)
                            selectedYouTubeItem = next
                        },
                        onSkipNext = {
                            val list = youTubeState.videos
                            if (list.isNotEmpty()) {
                                val nextIdx = (list.indexOfFirst { it.id == item.id } + 1) % list.size
                                val nextItem = list[nextIdx]
                                youTubeViewModel.recordPlayed(nextItem)
                                selectedYouTubeItem = nextItem
                            }
                        },
                        onSkipPrevious = {
                            val list = youTubeState.videos
                            if (list.isNotEmpty()) {
                                val prevIdx = (list.indexOfFirst { it.id == item.id } - 1 + list.size) % list.size
                                val prevItem = list[prevIdx]
                                youTubeViewModel.recordPlayed(prevItem)
                                selectedYouTubeItem = prevItem
                            }
                        },
                        onAutoFallbackToAudio = startAudiusFallbackPlayback,
                        isFavorite = youTubeState.favoriteVideoIds.contains(item.id),
                        onToggleFavorite = { youTubeViewModel.toggleFavoriteVideo(item.id) },
                        modifier = Modifier.padding(padding)
                    )
                } ?: NowPlayingScreen(
                    snapshot = snapshot,
                    controller = playbackController,
                    visualizerFrame = visualizerFrame,
                    onBack = { selectedTab = 0 },
                    onToggleFavorite = { track ->
                        libraryViewModel.setFavorite(track, !currentAudioTrackFavorite)
                    },
                    isCurrentFavorite = currentAudioTrackFavorite,
                    onRequestRecordPermission = {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.padding(padding)
                )
                else -> selectedYouTubeItem?.let { item ->
                    YouTubeWatchScreen(
                        item = item,
                        queue = youTubeState.videos,
                        snapshot = snapshot,
                        controller = playbackController,
                        onBack = { selectedTab = 0 },
                        onSelectQueueItem = { next ->
                            youTubeViewModel.recordPlayed(next)
                            selectedYouTubeItem = next
                        },
                        onSkipNext = {
                            val list = youTubeState.videos
                            if (list.isNotEmpty()) {
                                val nextIdx = (list.indexOfFirst { it.id == item.id } + 1) % list.size
                                val nextItem = list[nextIdx]
                                youTubeViewModel.recordPlayed(nextItem)
                                selectedYouTubeItem = nextItem
                            }
                        },
                        onSkipPrevious = {
                            val list = youTubeState.videos
                            if (list.isNotEmpty()) {
                                val prevIdx = (list.indexOfFirst { it.id == item.id } - 1 + list.size) % list.size
                                val prevItem = list[prevIdx]
                                youTubeViewModel.recordPlayed(prevItem)
                                selectedYouTubeItem = prevItem
                            }
                        },
                        onAutoFallbackToAudio = startAudiusFallbackPlayback,
                        isFavorite = youTubeState.favoriteVideoIds.contains(item.id),
                        onToggleFavorite = { youTubeViewModel.toggleFavoriteVideo(item.id) },
                        modifier = Modifier.padding(padding)
                    )
                } ?: YouTubeHomeScreen(
                    state = youTubeState,
                    onSearchVideos = youTubeViewModel::searchVideos,
                    onSearchPlaylists = youTubeViewModel::searchPlaylists,
                    onOpen = { item ->
                        youTubeViewModel.recordPlayed(item)
                        selectedYouTubeItem = item
                        selectedTab = 5
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    modifier: Modifier,
    tracks: List<AudioTrack>,
    query: String,
    onQueryChange: (String) -> Unit,
    isScanning: Boolean,
    hasPermission: Boolean,
    scanError: String?,
    onGrantPermission: () -> Unit,
    onPlay: (Int) -> Unit,
    onRescan: () -> Unit,
    onFavorite: (AudioTrack, Boolean) -> Unit
) {
    val filtered = remember(tracks, query) {
        if (query.isBlank()) tracks
        else tracks.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
        }
    }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Local Audio Library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "${tracks.size} offline MP3/FLAC tracks on device",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Filter local songs, artists, albums") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
        )
        Spacer(Modifier.height(16.dp))
        when {
            !hasPermission -> EmptyLibrary(
                "Grant audio permission",
                "Allow Vynora to scan offline MP3 and FLAC files stored on your device.",
                "Allow access",
                onGrantPermission
            )
            isScanning -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1DB954))
            }
            scanError != null -> EmptyLibrary("Scan failed", scanError, "Try again", onRescan)
            filtered.isEmpty() -> EmptyLibrary(
                "No local audio found",
                "Add MP3 or FLAC files to your device Music folder or rescan.",
                "Rescan library",
                onRescan
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.uri }) { track ->
                    TrackRow(
                        track,
                        onClick = { onPlay(tracks.indexOf(track)) },
                        onFavorite = { onFavorite(track, !track.isFavorite) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackCollectionScreen(
    modifier: Modifier,
    title: String,
    subtitle: String,
    tracks: List<AudioTrack>,
    onPlay: (Int) -> Unit,
    onFavorite: (AudioTrack) -> Unit
) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        if (tracks.isEmpty()) {
            EmptyLibrary("Nothing saved yet", "Tap the heart on a song to add it here.", "Browse library", {})
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(tracks, key = { it.uri }) { track ->
                    TrackRow(track, onClick = { onPlay(tracks.indexOf(track)) }, onFavorite = { onFavorite(track) })
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: AudioTrack,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    favoriteToggle: Boolean = true
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1DB954), Color(0xFF123524)))),
            contentAlignment = Alignment.Center
        ) {
            if (!track.artworkUri.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Rounded.Album, contentDescription = null, tint = Color.Black)
            }
        }
        Column(Modifier.weight(1f).padding(start = 13.dp, end = 8.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            track.durationLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClick = onFavorite) {
            Icon(
                if (favoriteToggle) {
                    if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
                } else {
                    Icons.Rounded.RemoveCircleOutline
                },
                contentDescription = if (favoriteToggle) {
                    if (track.isFavorite) "Remove from favorites" else "Add to favorites"
                } else {
                    "Remove from playlist"
                },
                tint = if (favoriteToggle && track.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyLibrary(title: String, subtitle: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(92.dp).clip(CircleShape).background(Color(0xFF14241B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = Color(0xFF1DB954)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun MiniPlayer(
    snapshot: PlaybackSnapshot,
    controller: PlaybackController,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151F2E),
        tonalElevation = 6.dp
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF132A1E)
            ) {
                if (!snapshot.artworkUri.isNullOrBlank()) {
                    AsyncImage(
                        model = snapshot.artworkUri,
                        contentDescription = snapshot.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    snapshot.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    snapshot.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }

            // Play / Pause / Buffering indicator
            IconButton(onClick = controller::togglePlayback) {
                when (snapshot.status) {
                    PlaybackStatus.BUFFERING, PlaybackStatus.LOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF1DB954),
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Icon(
                            if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play or pause",
                            tint = Color(0xFF1DB954)
                        )
                    }
                }
            }

            IconButton(onClick = controller::skipNext) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next track",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun PlaylistsScreen(
    modifier: Modifier,
    libraryViewModel: LibraryViewModel,
    playbackController: PlaybackController,
    onPlayTrack: () -> Unit
) {
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    val selectedId by libraryViewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val selectedTracks by libraryViewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val selectedPlaylist = playlists.firstOrNull { it.id == selectedId }

    Column(modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    selectedPlaylist?.name ?: "Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (selectedPlaylist == null) "${playlists.size} collections" else "${selectedTracks.size} songs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selectedPlaylist == null) {
                Button(onClick = { showCreateDialog = true }) { Text("New playlist") }
            } else {
                TextButton(onClick = { libraryViewModel.selectPlaylist(null) }) { Text("Back") }
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            selectedPlaylist != null -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { libraryViewModel.addAllTracksToPlaylist(selectedPlaylist.id) }) {
                        Text("Add library songs")
                    }
                    TextButton(onClick = { libraryViewModel.deletePlaylist(selectedPlaylist.id) }) {
                        Text("Delete")
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (selectedTracks.isEmpty()) {
                    EmptyLibrary(
                        "This playlist is empty",
                        "Add songs from your library to start listening.",
                        "Add all songs",
                        { libraryViewModel.addAllTracksToPlaylist(selectedPlaylist.id) }
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(selectedTracks, key = { it.uri }) { track ->
                            TrackRow(
                                track,
                                onClick = {
                                    playbackController.playQueue(selectedTracks, selectedTracks.indexOf(track))
                                    onPlayTrack()
                                },
                                onFavorite = { libraryViewModel.removeTrackFromPlaylist(selectedPlaylist.id, track) },
                                favoriteToggle = false
                            )
                        }
                    }
                }
            }
            playlists.isEmpty() -> {
                EmptyLibrary(
                    "Create your first playlist",
                    "Collect songs for every mood and moment.",
                    "Create playlist",
                    { showCreateDialog = true }
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(playlists, key = { it.id }) { playlist ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { libraryViewModel.selectPlaylist(playlist.id) },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    playlist.name,
                                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Open",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; name = "" },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank(),
                    onClick = {
                        libraryViewModel.createPlaylist(name)
                        showCreateDialog = false
                        name = ""
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; name = "" }) { Text("Cancel") }
            }
        )
    }
}
