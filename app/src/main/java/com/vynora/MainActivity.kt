package com.vynora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.PlayCircleOutline
import com.vynora.ui.SamplesChevronIcon
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.vynora.library.AudioTrack
import com.vynora.library.LibraryViewModel
import com.vynora.playback.PlaybackController
import com.vynora.playback.PlaybackSnapshot
import com.vynora.playback.PlaybackStatus
import com.vynora.ui.HomeDiscoveryScreen
import com.vynora.ui.NowPlayingScreen
import com.vynora.ui.PlaylistDetailScreen
import com.vynora.ui.PlaylistUiModel
import com.vynora.visualizer.AudioVisualizerManager
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

    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistUiModel?>(null) }
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

    // Run smooth visualizer animation tick loop during active playback (~30-60 FPS)
    LaunchedEffect(snapshot.isPlaying, snapshot.status, selectedYouTubeItem) {
        val isExoPlaying = snapshot.isPlaying || snapshot.status == PlaybackStatus.PLAYING
        val isAnyPlaying = isExoPlaying || (selectedYouTubeItem != null)
        if (isAnyPlaying) {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                visualizerManager.tick(now - startTime, isPlaying = true)
                delay(33L)
            }
        } else {
            visualizerManager.tick(0L, isPlaying = false)
        }
    }

    LaunchedEffect(Unit) { youTubeViewModel.loadHome() }
    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) libraryViewModel.refreshLibrary()
    }

    BackHandler(enabled = selectedYouTubeItem != null || selectedPlaylistForDetail != null || selectedTab != 0) {
        when {
            selectedYouTubeItem != null -> selectedYouTubeItem = null
            selectedPlaylistForDetail != null -> selectedPlaylistForDetail = null
            selectedTab != 0 -> selectedTab = 0
        }
    }

    val currentAudioTrackFavorite = remember(snapshot.queue, snapshot.currentIndex, favorites) {
        val curr = snapshot.queue.getOrNull(snapshot.currentIndex)
        curr != null && favorites.any { it.uri == curr.uri }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF1DB954),
            onPrimary = Color(0xFF000000),
            secondary = Color(0xFF38BDF8),
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF212121)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF000000),
            bottomBar = {
                // Only show docked MiniPlayer & Bottom Nav when not on full Now Playing or full Theatre screen
                if (selectedTab != 4 && selectedYouTubeItem == null) {
                    Column {
                        // 1. YouTube Music / Spotify Docked MiniPlayer (Matches Both Images!)
                        DockedMiniPlayer(
                            snapshot = snapshot,
                            controller = playbackController,
                            onOpen = { selectedTab = 4 },
                            onPlayDefault = {
                                if (tracks.isNotEmpty()) {
                                    playbackController.playQueue(tracks, 0)
                                } else {
                                    selectedTab = 4
                                }
                            }
                        )

                        // 2. Bottom Navigation Bar (5 tabs matching Image 1 & 2)
                        NavigationBar(
                            containerColor = Color(0xFF000000),
                            tonalElevation = 0.dp
                        ) {
                            val tabs = listOf(
                                Triple(0, "Home", Icons.Rounded.Home),
                                Triple(1, "Samples", null),
                                Triple(2, "Explore", Icons.Rounded.Explore),
                                Triple(3, "Library", Icons.Rounded.BookmarkBorder),
                                Triple(5, "Upgrade", Icons.Rounded.PlayCircleOutline)
                            )
                            tabs.forEach { (index, title, icon) ->
                                val isSelected = (selectedTab == index) || (selectedTab == 0 && selectedPlaylistForDetail != null && index == 0)
                                val itemTint = if (isSelected) Color.White else Color(0xFFAAAAAA)
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPlaylistForDetail = null
                                        selectedTab = index
                                    },
                                    icon = {
                                        if (index == 1) {
                                            SamplesChevronIcon(
                                                size = 22.dp,
                                                tint = itemTint
                                            )
                                        } else if (icon != null) {
                                            Icon(
                                                icon,
                                                contentDescription = title,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color(0xFFAAAAAA),
                                        unselectedTextColor = Color(0xFFAAAAAA),
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            if (selectedYouTubeItem != null) {
                YouTubeWatchScreen(
                    item = selectedYouTubeItem!!,
                    queue = youTubeState.videos,
                    snapshot = snapshot,
                    controller = playbackController,
                    visualizerFrame = visualizerFrame,
                    onBack = { selectedYouTubeItem = null },
                    onSelectQueueItem = { next ->
                        youTubeViewModel.recordPlayed(next)
                        selectedYouTubeItem = next
                    },
                    isFavorite = youTubeState.favoriteVideoIds.contains(selectedYouTubeItem!!.id),
                    onToggleFavorite = { youTubeViewModel.toggleFavoriteVideo(selectedYouTubeItem!!.id) },
                    modifier = Modifier.padding(padding)
                )
            } else if (selectedPlaylistForDetail != null && selectedTab == 0) {
                PlaylistDetailScreen(
                    playlist = selectedPlaylistForDetail!!,
                    currentTrackUri = snapshot.queue.getOrNull(snapshot.currentIndex)?.uri,
                    isPlaying = snapshot.isPlaying,
                    onBack = { selectedPlaylistForDetail = null },
                    onPlayAll = {
                        val pl = selectedPlaylistForDetail!!
                        if (pl.tracks.isNotEmpty()) {
                            playbackController.playQueue(pl.tracks, 0)
                        }
                    },
                    onTrackSelected = { trackIdx ->
                        val pl = selectedPlaylistForDetail!!
                        playbackController.playQueue(pl.tracks, trackIdx)
                    },
                    onToggleFavorite = { track ->
                        libraryViewModel.setFavorite(track, !track.isFavorite)
                    },
                    modifier = Modifier.padding(padding)
                )
            } else {
                when (selectedTab) {
                    0 -> HomeDiscoveryScreen(
                        state = youTubeState,
                        onPlaylistSelected = { playlist ->
                            selectedPlaylistForDetail = playlist
                        },
                        onTrackSelected = { track ->
                            playbackController.playQueue(listOf(track), 0)
                        },
                        onSearchVideos = youTubeViewModel::searchVideos,
                        onFilterLocalInstant = youTubeViewModel::filterLocalInstant,
                        onOpenSearch = { /* Toggle search */ },
                        modifier = Modifier.padding(padding)
                    )
                    1 -> {
                        // Samples Tab: Curated Video/Audio Discoveries
                        val trendingItem = youTubeState.videos.firstOrNull()
                        if (trendingItem != null) {
                            YouTubeWatchScreen(
                                item = trendingItem,
                                queue = youTubeState.videos,
                                snapshot = snapshot,
                                controller = playbackController,
                                visualizerFrame = visualizerFrame,
                                onBack = { selectedTab = 0 },
                                onSelectQueueItem = { next ->
                                    youTubeViewModel.recordPlayed(next)
                                    selectedYouTubeItem = next
                                },
                                isFavorite = youTubeState.favoriteVideoIds.contains(trendingItem.id),
                                onToggleFavorite = { youTubeViewModel.toggleFavoriteVideo(trendingItem.id) },
                                modifier = Modifier.padding(padding)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF1DB954))
                            }
                        }
                    }
                    2 -> {
                        // Explore Tab: Mood Categories & Audius Trending
                        ExploreScreen(
                            state = youTubeState,
                            onPlayAudius = { audiusTrack ->
                                val streamQueue = youTubeState.audiusTracks.map {
                                    it.toAudioTrack(youTubeViewModel.audiusStreamUrl(it.id))
                                }
                                val index = youTubeState.audiusTracks.indexOfFirst { it.id == audiusTrack.id }.coerceAtLeast(0)
                                playbackController.playQueue(streamQueue, index)
                                selectedTab = 4
                            },
                            onSearchVideos = youTubeViewModel::searchVideos,
                            modifier = Modifier.padding(padding)
                        )
                    }
                    3 -> {
                        // Library Tab: Offline MediaStore tracks & Favorites
                        LibraryScreen(
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
                    }
                    4 -> {
                        // Full-screen Now Playing Screen with Real-Time Bass-Reactive Visualizer!
                        NowPlayingScreen(
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
                    }
                    5 -> {
                        // Playlists / User Collections
                        PlaylistsScreen(
                            modifier = Modifier.padding(padding),
                            libraryViewModel = libraryViewModel,
                            playbackController = playbackController,
                            onPlayTrack = { selectedTab = 4 }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Docked MiniPlayer matching the exact layout in Image 1 and Image 2:
 * Square artwork thumbnail on left, title + artist (or "Nothing is playing"), Play/Pause on right.
 */
@Composable
private fun DockedMiniPlayer(
    snapshot: PlaybackSnapshot,
    controller: PlaybackController,
    onOpen: () -> Unit,
    onPlayDefault: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onOpen),
        color = Color(0xFF212121),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Square Artwork Thumbnail (Matches Images 1 & 2: plain dark gray #383838 when idle)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF383838)),
                contentAlignment = Alignment.Center
            ) {
                if (!snapshot.artworkUri.isNullOrBlank()) {
                    AsyncImage(
                        model = snapshot.artworkUri,
                        contentDescription = snapshot.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Title + Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val isIdle = snapshot.title == "Nothing playing" || snapshot.title.isBlank()
                Text(
                    text = if (isIdle) "Nothing is playing" else snapshot.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isIdle) {
                    Text(
                        text = snapshot.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFFAAAAAA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Play / Pause / Buffering Action
            IconButton(onClick = {
                val isIdle = snapshot.title == "Nothing playing" || snapshot.title.isBlank()
                if (isIdle) {
                    onPlayDefault()
                } else {
                    controller.togglePlayback()
                }
            }) {
                when (snapshot.status) {
                    PlaybackStatus.BUFFERING, PlaybackStatus.LOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Icon(
                            if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreScreen(
    state: com.vynora.youtube.YouTubeHomeState,
    onPlayAudius: (com.vynora.audius.AudiusTrack) -> Unit,
    onSearchVideos: (String) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                "Explore Music & Trends",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("New releases", "Charts", "Moods & genres").forEach { category ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clickable { onSearchVideos("$category official songs") },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF222222)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                category,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Trending Streams",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(state.audiusTracks) { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayAudius(track) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E1E1E))
                ) {
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(24.dp)
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
    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        Text("Your Library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            "${tracks.size} tracks saved on device",
            color = Color(0xFFAAAAAA)
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search songs, artists, albums...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White) }
        )
        Spacer(Modifier.height(14.dp))
        when {
            !hasPermission -> EmptyLibrary(
                "Audio permission required",
                "Allow Vynora to scan your device music library.",
                "Grant Permission",
                onGrantPermission
            )
            isScanning -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1DB954))
            }
            scanError != null -> EmptyLibrary("Scan failed", scanError, "Retry", onRescan)
            filtered.isEmpty() -> EmptyLibrary(
                "No audio files found",
                "Add music to your device or rescan.",
                "Rescan",
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
private fun TrackRow(
    track: AudioTrack,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    favoriteToggle: Boolean = true
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222)),
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
                Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFF666666))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(
                "${track.artist} • ${track.durationLabel}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFAAAAAA)
            )
        }
        IconButton(onClick = onFavorite) {
            Icon(
                if (favoriteToggle) {
                    if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
                } else {
                    Icons.Rounded.RemoveCircleOutline
                },
                contentDescription = null,
                tint = if (favoriteToggle && track.isFavorite) Color(0xFF1DB954) else Color(0xFFAAAAAA)
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
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAction) { Text(action) }
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

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    selectedPlaylist?.name ?: "Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    if (selectedPlaylist == null) "${playlists.size} collections" else "${selectedTracks.size} songs",
                    color = Color(0xFFAAAAAA)
                )
            }
            if (selectedPlaylist == null) {
                Button(onClick = { showCreateDialog = true }) { Text("New playlist") }
            } else {
                TextButton(onClick = { libraryViewModel.selectPlaylist(null) }) { Text("Back", color = Color.White) }
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            selectedPlaylist != null -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { libraryViewModel.addAllTracksToPlaylist(selectedPlaylist.id) }) {
                        Text("Add all songs")
                    }
                    TextButton(onClick = { libraryViewModel.deletePlaylist(selectedPlaylist.id) }) {
                        Text("Delete", color = Color(0xFFFF6B6B))
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (selectedTracks.isEmpty()) {
                    EmptyLibrary(
                        "This playlist is empty",
                        "Add songs from your library to start listening.",
                        "Add songs",
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
                    "No playlists yet",
                    "Create your first custom playlist collection.",
                    "Create playlist",
                    { showCreateDialog = true }
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(playlists, key = { it.id }) { playlist ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { libraryViewModel.selectPlaylist(playlist.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E1E1E)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = Color(0xFF1DB954)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    playlist.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("Open", color = Color(0xFFAAAAAA))
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
