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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.vynora.library.AudioTrack
import com.vynora.library.LibraryViewModel
import com.vynora.playback.PlaybackController
import com.vynora.playback.PlaybackSnapshot
import com.vynora.youtube.YouTubeHomeScreen
import com.vynora.youtube.YouTubeItem
import com.vynora.youtube.YouTubeViewModel
import com.vynora.youtube.YouTubeWatchScreen

@UnstableApi
class MainActivity : ComponentActivity() {
    private lateinit var playbackController: PlaybackController
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val youTubeViewModel: YouTubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackController = PlaybackController(applicationContext)
        setContent {
            VynoraApp(
                playbackController = playbackController,
                libraryViewModel = libraryViewModel,
                youTubeViewModel = youTubeViewModel
            )
        }
    }

    override fun onDestroy() {
        playbackController.release()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VynoraApp(
    playbackController: PlaybackController,
    libraryViewModel: LibraryViewModel,
    youTubeViewModel: YouTubeViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val favorites by libraryViewModel.favorites.collectAsStateWithLifecycle()
    val isScanning by libraryViewModel.isScanning.collectAsStateWithLifecycle()
    val scanError by libraryViewModel.scanError.collectAsStateWithLifecycle()
    val youTubeState by youTubeViewModel.state.collectAsStateWithLifecycle()
    var selectedYouTubeItem by remember { mutableStateOf<YouTubeItem?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snapshot = playbackController.snapshot
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) libraryViewModel.refreshLibrary()
    }

    LaunchedEffect(Unit) { youTubeViewModel.loadHome() }
    LaunchedEffect(hasPermission) {
        if (hasPermission) libraryViewModel.refreshLibrary()
    }

    val startAudiusFallbackPlayback: () -> Unit = {
        // Real YouTube playback stays in-app via WebView theatre player; pause local Media3 to prevent audio overlap
        playbackController.pause()
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
                if (selectedTab != 5) {
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
                                if (hasPermission) libraryViewModel.refreshLibrary()
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
                        MiniPlayer(snapshot, playbackController, onOpen = { selectedTab = 4 })
                    }
                    NavigationBar(containerColor = Color(0xFF0D121C)) {
                        val tabs = listOf(
                            "Home" to Icons.Rounded.Home,
                            "Library" to Icons.Rounded.LibraryMusic,
                            "Favorites" to Icons.Rounded.Favorite,
                            "Playlists" to Icons.Rounded.QueueMusic,
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
                    hasPermission = hasPermission,
                    scanError = scanError,
                    onGrantPermission = { permissionLauncher.launch(permission) },
                    onPlay = { index -> selectedYouTubeItem = null; playbackController.playQueue(tracks, index) },
                    onRescan = libraryViewModel::refreshLibrary,
                    onFavorite = libraryViewModel::setFavorite
                )
                2 -> TrackCollectionScreen(
                    modifier = Modifier.padding(padding),
                    title = "Favorites",
                    subtitle = "${favorites.size} local songs · ${youTubeState.favoriteVideoIds.size} saved videos",
                    tracks = favorites,
                    onPlay = { index -> selectedYouTubeItem = null; playbackController.playQueue(favorites, index) },
                    onFavorite = { track -> libraryViewModel.setFavorite(track, false) }
                )
                3 -> PlaylistsScreen(
                    modifier = Modifier.padding(padding),
                    libraryViewModel = libraryViewModel,
                    playbackController = playbackController
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
                    modifier = Modifier.padding(padding),
                    snapshot = snapshot,
                    controller = playbackController
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
                CircularProgressIndicator()
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
            Icon(Icons.Rounded.Album, contentDescription = null, tint = Color.Black)
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
                Icons.Rounded.QueueMusic,
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
private fun MiniPlayer(snapshot: PlaybackSnapshot, controller: PlaybackController, onOpen: () -> Unit) {
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
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF132A1E)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(22.dp)
                    )
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
            IconButton(onClick = controller::togglePlayback) {
                Icon(
                    if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play or pause",
                    tint = Color(0xFF1DB954)
                )
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
private fun NowPlayingScreen(
    modifier: Modifier,
    snapshot: PlaybackSnapshot,
    controller: PlaybackController
) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF19452C), Color(0xFF0F172A), Color(0xFF1E293B)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.GraphicEq,
                contentDescription = null,
                modifier = Modifier.size(104.dp),
                tint = Color(0xFF1DB954)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            snapshot.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(snapshot.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Slider(
            value = snapshot.positionMs.toFloat().coerceIn(0f, snapshot.durationMs.coerceAtLeast(1L).toFloat()),
            onValueChange = { controller.seekTo(it.toLong()) },
            valueRange = 0f..snapshot.durationMs.coerceAtLeast(1L).toFloat(),
            enabled = snapshot.durationMs > 0L
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatTime(snapshot.positionMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatTime(snapshot.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = controller::toggleShuffle) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "Toggle shuffle",
                    tint = if (snapshot.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = controller::skipPrevious) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous track")
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                IconButton(onClick = controller::togglePlayback, modifier = Modifier.size(68.dp)) {
                    Icon(
                        if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play or pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            IconButton(onClick = controller::skipNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next track")
            }
            IconButton(onClick = controller::cycleRepeatMode) {
                Icon(
                    Icons.Rounded.Repeat,
                    contentDescription = "Cycle repeat mode",
                    tint = if (snapshot.repeatMode != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        var showSleepTimer by remember { mutableStateOf(false) }
        TextButton(onClick = { showSleepTimer = true }) {
            Text(snapshot.sleepTimerRemainingMs?.let { "Sleep timer · ${formatTime(it)}" } ?: "Sleep timer")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Background & Audius streaming powered by Android Media3",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showSleepTimer) {
            AlertDialog(
                onDismissRequest = { showSleepTimer = false },
                title = { Text("Sleep timer") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15L, 30L, 45L, 60L).forEach { minutes ->
                            TextButton(onClick = {
                                controller.setSleepTimer(minutes * 60_000L)
                                showSleepTimer = false
                            }) { Text("${minutes} minutes") }
                        }
                        if (snapshot.sleepTimerRemainingMs != null) {
                            TextButton(onClick = {
                                controller.cancelSleepTimer()
                                showSleepTimer = false
                            }) { Text("Cancel active timer") }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSleepTimer = false }) { Text("Close") }
                }
            )
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1_000L
    return "%d:%02d".format(seconds / 60L, seconds % 60L)
}

@Composable
private fun PlaylistsScreen(
    modifier: Modifier,
    libraryViewModel: LibraryViewModel,
    playbackController: PlaybackController
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
                                onClick = { playbackController.playQueue(selectedTracks, selectedTracks.indexOf(track)) },
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
                                    Icons.Rounded.QueueMusic,
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
