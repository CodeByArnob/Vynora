package com.vynora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RemoveCircleOutline
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

@UnstableApi
class MainActivity : ComponentActivity() {
    private lateinit var playbackController: PlaybackController
    private val libraryViewModel: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackController = PlaybackController(applicationContext)
        setContent {
            VynoraApp(
                playbackController = playbackController,
                libraryViewModel = libraryViewModel
            )
        }
    }

    override fun onDestroy() {
        playbackController.release()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun VynoraApp(
    playbackController: PlaybackController,
    libraryViewModel: LibraryViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val favorites by libraryViewModel.favorites.collectAsStateWithLifecycle()
    val isScanning by libraryViewModel.isScanning.collectAsStateWithLifecycle()
    val scanError by libraryViewModel.scanError.collectAsStateWithLifecycle()
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
    var selectedTab by remember { mutableStateOf(0) }
    val snapshot = playbackController.snapshot
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) libraryViewModel.refreshLibrary()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) libraryViewModel.refreshLibrary()
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFC8B8FF),
        onPrimary = Color(0xFF24134D),
        secondary = Color(0xFFB8C7FF),
        background = Color(0xFF0D0B14),
        surface = Color(0xFF171421),
        surfaceVariant = Color(0xFF252133)
    )) {
        Scaffold(
            containerColor = Color(0xFF0D0B14),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("VYNORA", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                            Text("Your music, your space", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (hasPermission) libraryViewModel.refreshLibrary()
                            else permissionLauncher.launch(permission)
                        }) { Icon(Icons.Rounded.Refresh, contentDescription = "Rescan music") }
                    }
                )
            },
            bottomBar = {
                Column {
                    if (snapshot.title != "Nothing playing" && snapshot.title != "Player unavailable") {
                        MiniPlayer(snapshot, playbackController, onOpen = { selectedTab = 3 })
                    }
                    NavigationBar(containerColor = Color(0xFF171421)) {
                        val tabs = listOf(
                            "Library" to Icons.Rounded.LibraryMusic,
                            "Favorites" to Icons.Rounded.Favorite,
                            "Playlists" to Icons.Rounded.QueueMusic,
                            "Player" to Icons.Rounded.GraphicEq
                        )
                        tabs.forEachIndexed { index, pair ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(pair.second, contentDescription = null) },
                                label = { Text(pair.first) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> LibraryScreen(
                    modifier = Modifier.padding(padding),
                    tracks = tracks,
                    query = query,
                    onQueryChange = { query = it },
                    isScanning = isScanning,
                    hasPermission = hasPermission,
                    scanError = scanError,
                    onGrantPermission = { permissionLauncher.launch(permission) },
                    onPlay = { index -> playbackController.playQueue(tracks, index) },
                    onRescan = libraryViewModel::refreshLibrary,
                    onFavorite = libraryViewModel::setFavorite
                )
                1 -> TrackCollectionScreen(
                    modifier = Modifier.padding(padding),
                    title = "Your favorites",
                    subtitle = "${favorites.size} saved songs",
                    tracks = favorites,
                    onPlay = { index -> playbackController.playQueue(favorites, index) },
                    onFavorite = { track -> libraryViewModel.setFavorite(track, false) }
                )
                2 -> PlaylistsScreen(
                    modifier = Modifier.padding(padding),
                    libraryViewModel = libraryViewModel,
                    playbackController = playbackController
                )
                else -> NowPlayingScreen(
                    modifier = Modifier.padding(padding),
                    snapshot = snapshot,
                    controller = playbackController
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
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
        tracks.withIndex().filter { (_, track) ->
            "${track.title} ${track.artist} ${track.album}".contains(query, ignoreCase = true)
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(14.dp))
        Text("Your library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${tracks.size} songs on this device", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search songs, artists, albums") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )
        scanError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(16.dp))
        when {
            !hasPermission -> EmptyLibrary(
                title = "Let Vynora find your music",
                subtitle = "Audio access is used only to index music stored on this device.",
                action = "Allow audio access",
                onAction = onGrantPermission
            )
            isScanning -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            tracks.isEmpty() -> EmptyLibrary(
                title = "Your library is quiet",
                subtitle = "Add audio files to your device, then rescan your music library.",
                action = "Scan again",
                onAction = onRescan
            )
            filtered.isEmpty() -> EmptyLibrary(
                title = "No matching songs",
                subtitle = "Try another song, artist or album name.",
                action = "Clear search",
                onAction = { onQueryChange("") }
            )
            else -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("ALL SONGS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("Tap a song to play", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filtered, key = { it.value.uri }) { indexed ->
                        TrackRow(
                            track = indexed.value,
                            onClick = { onPlay(indexed.index) },
                            onFavorite = { onFavorite(indexed.value, !indexed.value.isFavorite) }
                        )
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TrackCollectionScreen(
    modifier: Modifier,
    title: String,
    subtitle: String,
    tracks: List<AudioTrack>,
    onPlay: (Int) -> Unit,
    onFavorite: (AudioTrack) -> Unit
) {
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
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

@androidx.compose.runtime.Composable
private fun TrackRow(
    track: AudioTrack,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    favoriteToggle: Boolean = true
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(
                Brush.linearGradient(listOf(Color(0xFF44346A), Color(0xFF24203A)))
            ),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFFD4C7FF)) }
        Column(Modifier.weight(1f).padding(start = 13.dp, end = 8.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(track.durationLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@androidx.compose.runtime.Composable
private fun EmptyLibrary(title: String, subtitle: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(92.dp).clip(CircleShape).background(Color(0xFF211B32)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.QueueMusic, contentDescription = null, modifier = Modifier.size(42.dp), tint = Color(0xFFC8B8FF)) }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAction) { Text(action) }
    }
}

@androidx.compose.runtime.Composable
private fun MiniPlayer(snapshot: PlaybackSnapshot, controller: PlaybackController, onOpen: () -> Unit) {
    Surface(color = Color(0xFF211B32), tonalElevation = 4.dp) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFFC8B8FF), modifier = Modifier.size(34.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(snapshot.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(snapshot.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = controller::togglePlayback) {
                Icon(if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = "Play or pause")
            }
        }
    }
}

@androidx.compose.runtime.Composable
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
            Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(34.dp)).background(
                Brush.linearGradient(listOf(Color(0xFF493A70), Color(0xFF171421), Color(0xFF29203E)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(104.dp), tint = Color(0xFFD4C7FF))
        }
        Spacer(Modifier.height(24.dp))
        Text(snapshot.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(snapshot.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Slider(
            value = snapshot.positionMs.toFloat().coerceIn(0f, snapshot.durationMs.coerceAtLeast(1L).toFloat()),
            onValueChange = { controller.seekTo(it.toLong()) },
            valueRange = 0f..snapshot.durationMs.coerceAtLeast(1L).toFloat(),
            enabled = snapshot.durationMs > 0L
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(snapshot.positionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatTime(snapshot.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = controller::toggleShuffle) { Icon(Icons.Rounded.Shuffle, contentDescription = "Toggle shuffle", tint = if (snapshot.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = controller::skipPrevious) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous track") }
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
            IconButton(onClick = controller::skipNext) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next track") }
            IconButton(onClick = controller::cycleRepeatMode) { Icon(Icons.Rounded.Repeat, contentDescription = "Cycle repeat mode", tint = if (snapshot.repeatMode != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(18.dp))
        Text("Background playback powered by Android Media3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1_000L
    return "%d:%02d".format(seconds / 60L, seconds % 60L)
}

@androidx.compose.runtime.Composable
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
                Text(selectedPlaylist?.name ?: "Playlists", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (selectedPlaylist == null) "${playlists.size} collections" else "${selectedTracks.size} songs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selectedPlaylist == null) {
                Button(onClick = { showCreateDialog = true }) { Text("New playlist") }
            } else {
                androidx.compose.material3.TextButton(onClick = { libraryViewModel.selectPlaylist(null) }) { Text("Back") }
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            selectedPlaylist != null -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { libraryViewModel.addAllTracksToPlaylist(selectedPlaylist.id) }) { Text("Add library songs") }
                    androidx.compose.material3.TextButton(onClick = { libraryViewModel.deletePlaylist(selectedPlaylist.id) }) { Text("Delete") }
                }
                Spacer(Modifier.height(12.dp))
                if (selectedTracks.isEmpty()) {
                    EmptyLibrary("This playlist is empty", "Add songs from your library to start listening.", "Add all songs", { libraryViewModel.addAllTracksToPlaylist(selectedPlaylist.id) })
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
                EmptyLibrary("Create your first playlist", "Collect songs for every mood and moment.", "Create playlist", { showCreateDialog = true })
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
                                Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(playlist.name, modifier = Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.SemiBold)
                                Text("Open", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCreateDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCreateDialog = false; name = "" },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Playlist name") }, singleLine = true)
            },
            confirmButton = {
                Button(enabled = name.isNotBlank(), onClick = {
                    libraryViewModel.createPlaylist(name)
                    showCreateDialog = false
                    name = ""
                }) { Text("Create") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCreateDialog = false; name = "" }) { Text("Cancel") }
            }
        )
    }
}
